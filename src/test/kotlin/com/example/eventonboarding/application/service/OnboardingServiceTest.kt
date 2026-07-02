package com.example.eventonboarding.application.service

import com.example.eventonboarding.ports.outbound.ApplicationRepository
import com.example.eventonboarding.ports.outbound.PasswordHasher
import com.example.eventonboarding.domain.ApplicationNotFoundException
import com.example.eventonboarding.domain.InvalidCredentialsException
import com.example.eventonboarding.domain.InvalidStepException
import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.domain.OnboardingStep
import com.example.eventonboarding.domain.event.CreditScoringCalculatedEvent
import com.example.eventonboarding.domain.event.DomainEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** In-memory test double for the persistence port. */
private class FakeRepository : ApplicationRepository {
    private val store = mutableMapOf<String, OnboardingApplication>()
    override fun save(application: OnboardingApplication) = application.also { store[it.id] = it }
    override fun findById(id: String) = store[id]
    override fun findByEmail(email: String) = store.values.firstOrNull { it.email == email }
}

/** Deterministic hasher double — reversible marker instead of real BCrypt. */
private class FakeHasher : PasswordHasher {
    override fun hash(raw: String) = "hashed:$raw"
    override fun matches(raw: String, hash: String) = hash == "hashed:$raw"
}

/** Captures the last token delivered, so tests can assert on it. */
private class RecordingNotifier {
    var lastEmail: String? = null
    var lastToken: String? = null
    fun send(email: String, token: String) {
        lastEmail = email
        lastToken = token
    }
}

/** Captures generated credentials without printing them during tests. */
private class RecordingCredentialNotifier {
    var lastUsername: String? = null
    var lastPassword: String? = null
    fun send(username: String, password: String) {
        lastUsername = username
        lastPassword = password
    }
}

/** Captures emitted domain events without involving adapters. */
private class RecordingDomainEventPublisher {
    val events = mutableListOf<DomainEvent>()
    fun publish(event: DomainEvent) {
        events += event
    }
}

class OnboardingServiceTest {

    private val notifier = RecordingNotifier()
    private val credentialNotifier = RecordingCredentialNotifier()
    private val domainEventPublisher = RecordingDomainEventPublisher()

    @BeforeEach
    fun resetDoubles() {
        credentialNotifier.lastUsername = null
        credentialNotifier.lastPassword = null
        domainEventPublisher.events.clear()
    }

    /** Service with the token and score outcomes pinned via port doubles. */
    private fun service(token: String = "000007", scoreValue: Int = 50, password: String = "passw0rd1234") = OnboardingService(
        repository = FakeRepository(),
        tokenGenerator = { token },
        notifier = notifier::send,
        creditScorer = { scoreValue },
        passwordGenerator = { password },
        passwordHasher = FakeHasher(),
        credentialNotifier = credentialNotifier::send,
        domainEventPublisher = domainEventPublisher::publish,
    )

    @Test
    fun `start issues a token via the notifier and moves to token verification`() {
        val app = service(token = "424242").start("user@example.com")

        assertEquals(OnboardingStep.TOKEN_VERIFY, app.step)
        assertFalse(app.tokenVerified)
        assertEquals("user@example.com", notifier.lastEmail)
        assertEquals("424242", notifier.lastToken)
    }

    @Test
    fun `verifyToken rejects a wrong token`() {
        val svc = service(token = "000007")
        val app = svc.start("user@example.com")

        assertThrows(InvalidStepException::class.java) { svc.verifyToken(app.id, "999999") }
        assertFalse(svc.get(app.id).tokenVerified)
    }

    @Test
    fun `verifyToken accepts the right token and advances`() {
        val svc = service(token = "000007")
        val app = svc.start("user@example.com")

        val verified = svc.verifyToken(app.id, "000007")
        assertTrue(verified.tokenVerified)
        assertEquals(OnboardingStep.FULFILLMENT, verified.step)
    }

    @Test
    fun `fulfillment is blocked until the token is verified`() {
        val svc = service()
        val app = svc.start("user@example.com")

        assertThrows(InvalidStepException::class.java) {
            svc.fulfill(app.id, "Ada", "user@example.com", "+1 555 0100", 120000, 7)
        }
    }

    @Test
    fun `fulfillment stores salary and years of experience`() {
        val svc = service(token = "000001")
        val app = svc.start("user@example.com")
        svc.verifyToken(app.id, "000001")

        val fulfilled = svc.fulfill(app.id, "Ada", "user@example.com", "+1 555 0100", 120000, 7)

        assertEquals(120000, fulfilled.salary)
        assertEquals(7, fulfilled.yearsOfExperience)
        assertEquals(OnboardingStep.SCORING, fulfilled.step)
    }

    @Test
    fun `score above threshold completes the application`() {
        val svc = service(token = "000001", scoreValue = 41, password = "abc123ABC456")
        val app = svc.start("user@example.com")
        svc.verifyToken(app.id, "000001")
        svc.fulfill(app.id, "Ada", "user@example.com", "+1 555 0100", 120000, 7)

        val scored = svc.score(app.id)
        assertEquals(41, scored.score)
        assertEquals(OnboardingStep.COMPLETED, scored.step)
        val event = domainEventPublisher.events.single() as CreditScoringCalculatedEvent
        assertEquals(app.id, event.applicationId)
        assertEquals("user@example.com", event.email)
        assertEquals(41, event.score)
        assertTrue(event.approved)
        assertEquals(OnboardingStep.COMPLETED, event.step)
        assertEquals("user@example.com", credentialNotifier.lastUsername)
        assertEquals("abc123ABC456", credentialNotifier.lastPassword)
    }

    @Test
    fun `score at or below threshold declines the application`() {
        val svc = service(token = "000001", scoreValue = 40)
        val app = svc.start("user@example.com")
        svc.verifyToken(app.id, "000001")
        svc.fulfill(app.id, "Ada", "user@example.com", "+1 555 0100", 120000, 7)

        val scored = svc.score(app.id)
        assertEquals(40, scored.score)
        assertEquals(OnboardingStep.DECLINED, scored.step)
        val event = domainEventPublisher.events.single() as CreditScoringCalculatedEvent
        assertEquals(app.id, event.applicationId)
        assertEquals(40, event.score)
        assertFalse(event.approved)
        assertEquals(OnboardingStep.DECLINED, event.step)
    }

    @Test
    fun `scoring before fulfillment is rejected`() {
        val svc = service(token = "000001")
        val app = svc.start("user@example.com")
        svc.verifyToken(app.id, "000001")

        assertThrows(InvalidStepException::class.java) { svc.score(app.id) }
    }

    @Test
    fun `unknown id throws not found`() {
        assertThrows(ApplicationNotFoundException::class.java) { service().get("nope") }
    }

    @Test
    fun `login returns the profile for valid credentials of an approved applicant`() {
        val svc = service(token = "000001", scoreValue = 80, password = "secretPass99")
        val app = svc.start("user@example.com")
        svc.verifyToken(app.id, "000001")
        svc.fulfill(app.id, "Ada", "user@example.com", "+1 555 0100", 120000, 7)
        svc.score(app.id)

        val loggedIn = svc.login("user@example.com", "secretPass99")
        assertEquals(app.id, loggedIn.id)
        assertEquals("Ada", loggedIn.name)
    }

    @Test
    fun `login rejects a wrong password`() {
        val svc = service(token = "000001", scoreValue = 80, password = "secretPass99")
        val app = svc.start("user@example.com")
        svc.verifyToken(app.id, "000001")
        svc.fulfill(app.id, "Ada", "user@example.com", "+1 555 0100", 120000, 7)
        svc.score(app.id)

        assertThrows(InvalidCredentialsException::class.java) {
            svc.login("user@example.com", "wrong-password")
        }
    }

    @Test
    fun `login rejects an unknown email`() {
        assertThrows(InvalidCredentialsException::class.java) {
            service().login("nobody@example.com", "whatever")
        }
    }

    @Test
    fun `declined applicants have no credentials and cannot log in`() {
        val svc = service(token = "000001", scoreValue = 40, password = "secretPass99")
        val app = svc.start("user@example.com")
        svc.verifyToken(app.id, "000001")
        svc.fulfill(app.id, "Ada", "user@example.com", "+1 555 0100", 120000, 7)
        val scored = svc.score(app.id)

        assertEquals(OnboardingStep.DECLINED, scored.step)
        assertEquals(null, scored.passwordHash)
        assertEquals(null, credentialNotifier.lastPassword)
        assertThrows(InvalidCredentialsException::class.java) {
            svc.login("user@example.com", "secretPass99")
        }
    }
}
