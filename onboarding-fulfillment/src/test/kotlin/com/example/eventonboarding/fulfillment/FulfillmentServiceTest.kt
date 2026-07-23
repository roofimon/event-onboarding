package com.example.eventonboarding.fulfillment

import com.example.eventonboarding.domain.ApplicationNotFoundException
import com.example.eventonboarding.domain.InvalidStepException
import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.domain.OnboardingStep
import com.example.eventonboarding.ports.outbound.ApplicationRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FulfillmentServiceTest {
    @Test
    fun `verified application captures details and advances to scoring`() {
        val application = OnboardingApplication("app-1", "old@example.com", "token", tokenVerified = true)
        val service = FulfillmentService(SingleRepository(application))
        val result = service.fulfill("app-1", " Ada ", " ada@example.com ", " +1 555 0100 ", 120000, 7)
        assertEquals("Ada", result.name)
        assertEquals("ada@example.com", result.email)
        assertEquals(120000, result.salary)
        assertEquals(7, result.yearsOfExperience)
        assertEquals(OnboardingStep.SCORING, result.step)
    }

    @Test
    fun `unverified and missing applications are rejected`() {
        val application = OnboardingApplication("app-1", "user@example.com", "token")
        val service = FulfillmentService(SingleRepository(application))
        assertFailsWith<InvalidStepException> { service.fulfill("app-1", "Ada", "a@b.com", "+15550100", 1, 1) }
        assertFailsWith<ApplicationNotFoundException> { service.fulfill("missing", "Ada", "a@b.com", "+15550100", 1, 1) }
    }
}

private class SingleRepository(private val application: OnboardingApplication) : ApplicationRepository {
    override fun save(application: OnboardingApplication) = application
    override fun findById(id: String) = application.takeIf { it.id == id }
    override fun findByEmail(email: String) = application.takeIf { it.email == email }
}
