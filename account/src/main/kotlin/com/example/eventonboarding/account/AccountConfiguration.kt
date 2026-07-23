package com.example.eventonboarding.account

import com.example.eventonboarding.ports.outbound.ApplicationRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AccountConfiguration {
    @Bean
    fun accountService(
        repository: ApplicationRepository,
        passwordGenerator: PasswordGenerator,
        passwordHasher: PasswordHasher,
        credentialNotifier: CredentialNotifier,
    ): AccountService = AccountService(repository, passwordGenerator, passwordHasher, credentialNotifier)
}
