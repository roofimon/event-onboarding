package com.example.eventonboarding.scoring

import com.example.eventonboarding.account.AccountProvisioning
import com.example.eventonboarding.ports.outbound.ApplicationRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ScoringConfiguration {
    @Bean
    fun scoringUseCase(
        repository: ApplicationRepository,
        creditScorer: CreditScorer,
        accountProvisioning: AccountProvisioning,
        domainEventPublisher: DomainEventPublisher,
    ): ScoringUseCase = ScoringService(repository, creditScorer, accountProvisioning, domainEventPublisher)
}
