package com.example.eventonboarding.email

import com.example.eventonboarding.ports.outbound.ApplicationRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EmailOnboardingConfiguration {
    @Bean
    fun emailOnboardingUseCase(
        repository: ApplicationRepository,
        tokenGenerator: TokenGenerator,
        verificationNotifier: VerificationNotifier,
    ): EmailOnboardingUseCase = EmailOnboardingService(repository, tokenGenerator, verificationNotifier)
}
