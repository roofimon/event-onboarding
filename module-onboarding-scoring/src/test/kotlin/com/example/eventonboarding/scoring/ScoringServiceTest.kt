package com.example.eventonboarding.scoring

import com.example.eventonboarding.domain.event.DomainEvent
import com.example.eventonboarding.domain.event.DomainEventPublisher
import com.example.eventonboarding.account.AccountProvisioning
import com.example.eventonboarding.domain.ApplicationNotFoundException
import com.example.eventonboarding.domain.InvalidStepException
import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.domain.OnboardingStep
import com.example.eventonboarding.ports.outbound.ApplicationRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScoringServiceTest {
    @Test
    fun `approved score provisions account and publishes result`() {
        val fixture = fixture(score = 41)
        val result = fixture.service.score("app-1")
        assertEquals(OnboardingStep.COMPLETED, result.step)
        assertEquals(41, result.score)
        assertTrue(fixture.provisioned)
        val event = fixture.events.single() as CreditScoringCalculatedEvent
        assertTrue(event.approved)
        assertEquals(41, event.score)
    }

    @Test
    fun `declined score does not provision account`() {
        val fixture = fixture(score = 40)
        val result = fixture.service.score("app-1")
        assertEquals(OnboardingStep.DECLINED, result.step)
        assertFalse(fixture.provisioned)
        assertFalse((fixture.events.single() as CreditScoringCalculatedEvent).approved)
    }

    @Test
    fun `scoring before fulfillment is rejected`() {
        val fixture = fixture(score = 80, step = OnboardingStep.FULFILLMENT)
        assertFailsWith<InvalidStepException> { fixture.service.score("app-1") }
    }

    @Test
    fun `unknown application is rejected`() {
        val fixture = fixture(score = 80)
        assertFailsWith<ApplicationNotFoundException> { fixture.service.score("missing") }
    }

    private fun fixture(score: Int, step: OnboardingStep = OnboardingStep.SCORING): Fixture {
        val application = OnboardingApplication("app-1", "user@example.com", "token", step = step)
        return Fixture(application, score)
    }
}

private class Fixture(application: OnboardingApplication, score: Int) {
    var provisioned = false
    val events = mutableListOf<DomainEvent>()
    val service = ScoringService(
        SingleRepository(application),
        { score },
        AccountProvisioning { provisioned = true },
        DomainEventPublisher { events += it },
    )
}

private class SingleRepository(private val application: OnboardingApplication) : ApplicationRepository {
    override fun save(application: OnboardingApplication) = application
    override fun findById(id: String) = application.takeIf { it.id == id }
    override fun findByEmail(email: String) = application.takeIf { it.email == email }
}
