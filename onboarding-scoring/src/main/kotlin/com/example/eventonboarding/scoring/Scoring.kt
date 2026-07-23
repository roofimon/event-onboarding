package com.example.eventonboarding.scoring

import com.example.eventonboarding.account.AccountProvisioning
import com.example.eventonboarding.domain.ApplicationNotFoundException
import com.example.eventonboarding.domain.InvalidStepException
import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.domain.OnboardingStep
import com.example.eventonboarding.ports.outbound.ApplicationRepository
import java.time.Instant

fun interface ScoringUseCase {
    fun score(id: String): OnboardingApplication
}

fun interface CreditScorer {
    fun score(application: OnboardingApplication): Int
}

fun interface DomainEventPublisher {
    fun publish(event: DomainEvent)
}

sealed interface DomainEvent {
    val occurredAt: Instant
}

data class CreditScoringCalculatedEvent(
    val applicationId: String,
    val email: String,
    val score: Int,
    val approved: Boolean,
    val step: OnboardingStep,
    override val occurredAt: Instant = Instant.now(),
) : DomainEvent

class ScoringService(
    private val repository: ApplicationRepository,
    private val creditScorer: CreditScorer,
    private val accountProvisioning: AccountProvisioning,
    private val domainEventPublisher: DomainEventPublisher,
) : ScoringUseCase {
    override fun score(id: String): OnboardingApplication {
        val application = repository.findById(id) ?: throw ApplicationNotFoundException(id)
        if (application.step != OnboardingStep.SCORING &&
            application.step != OnboardingStep.COMPLETED &&
            application.step != OnboardingStep.DECLINED
        ) {
            throw InvalidStepException("Fulfillment must be completed before scoring")
        }
        application.applyScore(creditScorer.score(application))
        if (application.step == OnboardingStep.COMPLETED) accountProvisioning.provision(application)
        val saved = repository.save(application)
        domainEventPublisher.publish(
            CreditScoringCalculatedEvent(
                saved.id,
                saved.email,
                saved.score!!,
                saved.step == OnboardingStep.COMPLETED,
                saved.step,
            ),
        )
        return saved
    }
}
