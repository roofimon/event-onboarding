package com.example.eventonboarding.tokenverification

import com.example.eventonboarding.domain.ApplicationNotFoundException
import com.example.eventonboarding.domain.InvalidStepException
import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.domain.OnboardingStep
import com.example.eventonboarding.ports.outbound.ApplicationRepository

fun interface TokenVerificationUseCase {
    fun verifyToken(id: String, token: String): OnboardingApplication
}

class TokenVerificationService(private val repository: ApplicationRepository) : TokenVerificationUseCase {
    override fun verifyToken(id: String, token: String): OnboardingApplication {
        val application = repository.findById(id) ?: throw ApplicationNotFoundException(id)
        if (application.tokenVerified) return application
        if (token.trim() != application.token) throw InvalidStepException("Invalid verification token")
        application.tokenVerified = true
        application.step = OnboardingStep.FULFILLMENT
        return repository.save(application)
    }
}
