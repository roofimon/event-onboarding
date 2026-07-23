package com.example.eventonboarding.email

import com.example.eventonboarding.domain.ApplicationNotFoundException
import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.ports.outbound.ApplicationRepository
import java.util.UUID

interface EmailOnboardingUseCase {
    fun start(email: String): OnboardingApplication
    fun get(id: String): OnboardingApplication
}

fun interface TokenGenerator {
    fun generate(): String
}

fun interface VerificationNotifier {
    fun send(email: String, token: String)
}

class EmailOnboardingService(
    private val repository: ApplicationRepository,
    private val tokenGenerator: TokenGenerator,
    private val notifier: VerificationNotifier,
) : EmailOnboardingUseCase {
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

    override fun get(id: String): OnboardingApplication =
        repository.findById(id) ?: throw ApplicationNotFoundException(id)
}
