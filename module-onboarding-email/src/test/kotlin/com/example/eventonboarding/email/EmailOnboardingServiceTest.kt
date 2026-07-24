package com.example.eventonboarding.email

import com.example.eventonboarding.domain.ApplicationNotFoundException
import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.domain.OnboardingStep
import com.example.eventonboarding.ports.outbound.ApplicationRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class EmailOnboardingServiceTest {
    private val repository = TestRepository()
    private var notified: Pair<String, String>? = null
    private val service = EmailOnboardingService(repository, { "123456" }, { email, token -> notified = email to token })

    @Test
    fun `start creates an application and sends its token`() {
        val result = service.start("  user@example.com ")
        assertEquals("user@example.com", result.email)
        assertEquals(OnboardingStep.TOKEN_VERIFY, result.step)
        assertFalse(result.tokenVerified)
        assertEquals("user@example.com" to "123456", notified)
        assertEquals(result, service.get(result.id))
    }

    @Test
    fun `get rejects an unknown id`() {
        assertFailsWith<ApplicationNotFoundException> { service.get("missing") }
    }
}

private class TestRepository : ApplicationRepository {
    private val values = mutableMapOf<String, OnboardingApplication>()
    override fun save(application: OnboardingApplication) = application.also { values[it.id] = it }
    override fun findById(id: String) = values[id]
    override fun findByEmail(email: String) = values.values.firstOrNull { it.email == email }
}
