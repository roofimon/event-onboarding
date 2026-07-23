package com.example.eventonboarding.fulfillment

import com.example.eventonboarding.ports.outbound.ApplicationRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FulfillmentConfiguration {
    @Bean
    fun fulfillmentUseCase(repository: ApplicationRepository): FulfillmentUseCase = FulfillmentService(repository)
}
