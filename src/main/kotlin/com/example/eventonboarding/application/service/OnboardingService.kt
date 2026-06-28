package com.example.eventonboarding.application.service

import com.example.eventonboarding.application.port.inbound.OnboardingUseCase
import com.example.eventonboarding.application.port.outbound.ApplicationRepository
import com.example.eventonboarding.application.port.outbound.CreditScorer
import com.example.eventonboarding.application.port.outbound.TokenGenerator
import com.example.eventonboarding.application.port.outbound.VerificationNotifier
import com.example.eventonboarding.domain.ApplicationNotFoundException
import com.example.eventonboarding.domain.InvalidStepException
import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.domain.OnboardingStep
import java.util.UUID

/**
 * Application service orchestrating the four onboarding steps.
 *
 * Framework-free: it depends only on ports and the domain, and is wired to its
 * adapters by the composition root. All randomness, persistence and delivery of
 * the token live behind the outbound ports.
 */
class OnboardingService(
    private val repository: ApplicationRepository,
    private val tokenGenerator: TokenGenerator,
    private val notifier: VerificationNotifier,
    private val creditScorer: CreditScorer,
) : OnboardingUseCase {

    override fun start(email: String): OnboardingApplication {
        val token = tokenGenerator.generate()
        val application = OnboardingApplication(
            id = UUID.randomUUID().toString(),
            email = email.trim(),
            token = token,
        )
        repository.save(application)
        notifier.send(application.email, token)
        return application
    }

    override fun verifyToken(id: String, token: String): OnboardingApplication {
        val application = get(id)
        if (application.tokenVerified) return application
        if (token.trim() != application.token) {
            throw InvalidStepException("Invalid verification token")
        }
        application.tokenVerified = true
        application.step = OnboardingStep.FULFILLMENT
        return repository.save(application)
    }

    override fun fulfill(id: String, name: String, email: String, phone: String): OnboardingApplication {
        val application = get(id)
        if (!application.tokenVerified) {
            throw InvalidStepException("Token must be verified before fulfillment")
        }
        application.name = name.trim()
        application.email = email.trim()
        application.phone = phone.trim()
        application.step = OnboardingStep.SCORING
        return repository.save(application)
    }

    override fun score(id: String): OnboardingApplication {
        val application = get(id)
        if (application.step != OnboardingStep.SCORING &&
            application.step != OnboardingStep.COMPLETED &&
            application.step != OnboardingStep.DECLINED
        ) {
            throw InvalidStepException("Fulfillment must be completed before scoring")
        }
        application.applyScore(creditScorer.score(application))
        return repository.save(application)
    }

    override fun get(id: String): OnboardingApplication =
        repository.findById(id) ?: throw ApplicationNotFoundException(id)
}
