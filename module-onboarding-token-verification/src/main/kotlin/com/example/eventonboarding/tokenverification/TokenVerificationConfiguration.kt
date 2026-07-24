package com.example.eventonboarding.tokenverification

import com.example.eventonboarding.ports.outbound.ApplicationRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TokenVerificationConfiguration {
    @Bean
    fun tokenVerificationUseCase(repository: ApplicationRepository): TokenVerificationUseCase =
        TokenVerificationService(repository)
}
