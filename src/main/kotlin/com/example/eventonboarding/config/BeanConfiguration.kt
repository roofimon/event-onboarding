package com.example.eventonboarding.config

import com.example.eventonboarding.ports.inbound.OnboardingUseCase
import com.example.eventonboarding.ports.outbound.ApplicationRepository
import com.example.eventonboarding.ports.outbound.CreditScorer
import com.example.eventonboarding.ports.outbound.DomainEventPublisher
import com.example.eventonboarding.ports.outbound.TokenGenerator
import com.example.eventonboarding.ports.outbound.VerificationNotifier
import com.example.eventonboarding.application.service.OnboardingService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Composition root — wires the framework-free [OnboardingService] to the adapter
 * beans that implement its outbound ports. This is the only place the application
 * core meets Spring.
 */
@Configuration
class BeanConfiguration {
    @Bean
    fun onboardingUseCase(
        repository: ApplicationRepository,
        tokenGenerator: TokenGenerator,
        notifier: VerificationNotifier,
        creditScorer: CreditScorer,
        domainEventPublisher: DomainEventPublisher,
    ): OnboardingUseCase = OnboardingService(repository, tokenGenerator, notifier, creditScorer, domainEventPublisher)
}
