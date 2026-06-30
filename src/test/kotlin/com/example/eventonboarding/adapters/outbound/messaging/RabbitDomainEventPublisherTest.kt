package com.example.eventonboarding.adapters.outbound.messaging

import com.example.eventonboarding.domain.OnboardingStep
import com.example.eventonboarding.domain.event.CreditScoringCalculatedEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.amqp.AmqpConnectException
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.net.ConnectException

class RabbitDomainEventPublisherTest {
    private val rabbitTemplate = mock(RabbitTemplate::class.java)
    private val publisher = RabbitDomainEventPublisher(
        rabbitTemplate = rabbitTemplate,
        exchange = "event-onboarding.domain-events",
        creditScoringRoutingKey = "onboarding.credit-scoring.calculated",
    )

    @Test
    fun `publishes credit scoring event to configured exchange and routing key`() {
        val event = creditScoringCalculatedEvent()

        publisher.publish(event)

        verify(rabbitTemplate).convertAndSend(
            "event-onboarding.domain-events",
            "onboarding.credit-scoring.calculated",
            event,
        )
    }

    @Test
    fun `does not fail caller when RabbitMQ publish fails`() {
        val event = creditScoringCalculatedEvent()
        doThrow(AmqpConnectException(ConnectException("connection refused")))
            .`when`(rabbitTemplate)
            .convertAndSend("event-onboarding.domain-events", "onboarding.credit-scoring.calculated", event)

        publisher.publish(event)

        verify(rabbitTemplate).convertAndSend(
            "event-onboarding.domain-events",
            "onboarding.credit-scoring.calculated",
            event,
        )
    }

    private fun creditScoringCalculatedEvent() = CreditScoringCalculatedEvent(
        applicationId = "app-1",
        email = "user@example.com",
        score = 80,
        approved = true,
        step = OnboardingStep.COMPLETED,
    )
}
