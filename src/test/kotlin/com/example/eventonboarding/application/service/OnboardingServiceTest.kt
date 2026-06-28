package com.example.eventonboarding.application.service

import com.example.eventonboarding.application.port.outbound.ApplicationRepository
import com.example.eventonboarding.domain.ApplicationNotFoundException
import com.example.eventonboarding.domain.InvalidStepException
import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.domain.OnboardingStep
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** In-memory test double for the persistence port. */
private class FakeRepository : ApplicationRepository {
    private val store = mutableMapOf<String, OnboardingApplication>()
    override fun save(application: OnboardingApplication) = application.also { store[it.id] = it }
    override fun findById(id: String) = store[id]
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

class OnboardingServiceTest {

    private val notifier = RecordingNotifier()

    /** Service with the token and score outcomes pinned via port doubles. */
    private fun service(token: String = "000007", scoreValue: Int = 50) = OnboardingService(
        repository = FakeRepository(),
        tokenGenerator = { token },
        notifier = notifier::send,
        creditScorer = { scoreValue },
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
            svc.fulfill(app.id, "Ada", "user@example.com", "+1 555 0100")
        }
    }

    @Test
    fun `score above threshold completes the application`() {
        val svc = service(token = "000001", scoreValue = 41)
        val app = svc.start("user@example.com")
        svc.verifyToken(app.id, "000001")
        svc.fulfill(app.id, "Ada", "user@example.com", "+1 555 0100")

        val scored = svc.score(app.id)
        assertEquals(41, scored.score)
        assertEquals(OnboardingStep.COMPLETED, scored.step)
    }

    @Test
    fun `score at or below threshold declines the application`() {
        val svc = service(token = "000001", scoreValue = 40)
        val app = svc.start("user@example.com")
        svc.verifyToken(app.id, "000001")
        svc.fulfill(app.id, "Ada", "user@example.com", "+1 555 0100")

        val scored = svc.score(app.id)
        assertEquals(40, scored.score)
        assertEquals(OnboardingStep.DECLINED, scored.step)
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
}
