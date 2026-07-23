package com.example.eventonboarding.account

import com.example.eventonboarding.domain.InvalidCredentialsException
import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.domain.OnboardingStep
import com.example.eventonboarding.ports.outbound.ApplicationRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AccountServiceTest {
    private val application = OnboardingApplication("app-1", "user@example.com", "token")
    private val repository = SingleRepository(application)
    private var delivered: Pair<String, String>? = null
    private val service = AccountService(repository, { "generated-password" }, TestHasher(), { user, password ->
        delivered = user to password
    })

    @Test
    fun `provision hashes and delivers credentials`() {
        service.provision(application)
        assertEquals("hashed:generated-password", application.passwordHash)
        assertEquals("user@example.com" to "generated-password", delivered)
    }

    @Test
    fun `approved applicant can log in`() {
        application.step = OnboardingStep.COMPLETED
        application.passwordHash = "hashed:secret"
        assertEquals(application, service.login(" user@example.com ", "secret"))
    }

    @Test
    fun `unknown declined and mismatched credentials are rejected`() {
        assertFailsWith<InvalidCredentialsException> { service.login("missing@example.com", "secret") }
        assertFailsWith<InvalidCredentialsException> { service.login("user@example.com", "secret") }
        application.step = OnboardingStep.COMPLETED
        assertNull(application.passwordHash)
        assertFailsWith<InvalidCredentialsException> { service.login("user@example.com", "secret") }
        application.passwordHash = "hashed:secret"
        assertFailsWith<InvalidCredentialsException> { service.login("user@example.com", "wrong") }
    }

    @Test
    fun `profile update reauthenticates and preserves approval`() {
        application.step = OnboardingStep.COMPLETED
        application.passwordHash = "hashed:secret"
        application.score = 80
        val result = service.updateProfile("user@example.com", "secret", " Ada ", " +15550100 ", 150000, 9)
        assertEquals("Ada", result.name)
        assertEquals("+15550100", result.phone)
        assertEquals(80, result.score)
        assertEquals(OnboardingStep.COMPLETED, result.step)
    }
}

private class TestHasher : PasswordHasher {
    override fun hash(raw: String) = "hashed:$raw"
    override fun matches(raw: String, hash: String) = hash == "hashed:$raw"
}

private class SingleRepository(private val application: OnboardingApplication) : ApplicationRepository {
    override fun save(application: OnboardingApplication) = application
    override fun findById(id: String) = application.takeIf { it.id == id }
    override fun findByEmail(email: String) = application.takeIf { it.email == email }
}
