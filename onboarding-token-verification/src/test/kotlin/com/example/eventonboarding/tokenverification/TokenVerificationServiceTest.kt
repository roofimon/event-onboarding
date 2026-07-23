package com.example.eventonboarding.tokenverification

import com.example.eventonboarding.domain.ApplicationNotFoundException
import com.example.eventonboarding.domain.InvalidStepException
import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.domain.OnboardingStep
import com.example.eventonboarding.ports.outbound.ApplicationRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TokenVerificationServiceTest {
    private val application = OnboardingApplication("app-1", "user@example.com", "123456")
    private val service = TokenVerificationService(SingleRepository(application))

    @Test
    fun `correct token verifies and advances the application`() {
        val result = service.verifyToken("app-1", " 123456 ")
        assertTrue(result.tokenVerified)
        assertEquals(OnboardingStep.FULFILLMENT, result.step)
    }

    @Test
    fun `wrong token is rejected without mutation`() {
        assertFailsWith<InvalidStepException> { service.verifyToken("app-1", "wrong") }
        assertEquals(OnboardingStep.TOKEN_VERIFY, application.step)
    }

    @Test
    fun `verified application is idempotent and unknown id is rejected`() {
        application.tokenVerified = true
        assertEquals(application, service.verifyToken("app-1", "anything"))
        assertFailsWith<ApplicationNotFoundException> { service.verifyToken("missing", "123456") }
    }
}

private class SingleRepository(private val application: OnboardingApplication) : ApplicationRepository {
    override fun save(application: OnboardingApplication) = application
    override fun findById(id: String) = application.takeIf { it.id == id }
    override fun findByEmail(email: String) = application.takeIf { it.email == email }
}
