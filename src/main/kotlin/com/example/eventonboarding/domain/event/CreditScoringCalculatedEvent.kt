package com.example.eventonboarding.domain.event

import com.example.eventonboarding.domain.OnboardingStep
import java.time.Instant

data class CreditScoringCalculatedEvent(
    val applicationId: String,
    val email: String,
    val score: Int,
    val approved: Boolean,
    val step: OnboardingStep,
    override val occurredAt: Instant = Instant.now(),
) : DomainEvent
