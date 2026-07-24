package com.example.eventonboarding.infrastructure.messaging

import com.example.eventonboarding.account.AccountInformationDeletedEvent
import com.example.eventonboarding.account.AccountInformationUpdatedEvent
import com.example.eventonboarding.domain.OnboardingStep
import com.example.eventonboarding.scoring.CreditScoringCalculatedEvent
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.amqp.AmqpConnectException
import org.springframework.amqp.rabbit.core.RabbitTemplate
import java.net.ConnectException

class RabbitDomainEventPublisherTest {
    private val rabbitTemplate = mock(RabbitTemplate::class.java)
    private val payload = byteArrayOf(1, 2, 3, 4)

    private fun publisher(serializer: EventSerializer) = RabbitDomainEventPublisher(
        rabbitTemplate = rabbitTemplate,
        eventSerializer = serializer,
        exchange = "event-onboarding.domain-events",
        creditScoringRoutingKey = "onboarding.credit-scoring.calculated",
        accountUpdatedRoutingKey = "account.information.updated",
        accountDeletedRoutingKey = "account.information.deleted",
    )

    @Test
    fun `serializes the event and sends the bytes to the configured exchange and routing key`() {
        publisher { subject, _ ->
            assert(subject == "onboarding.credit-scoring.calculated")
            payload
        }.publish(creditScoringCalculatedEvent())

        verify(rabbitTemplate).convertAndSend(
            "event-onboarding.domain-events",
            "onboarding.credit-scoring.calculated",
            payload,
        )
    }

    @Test
    fun `does not fail caller when RabbitMQ publish fails`() {
        doThrow(AmqpConnectException(ConnectException("connection refused")))
            .`when`(rabbitTemplate)
            .convertAndSend("event-onboarding.domain-events", "onboarding.credit-scoring.calculated", payload)

        // Should not throw.
        publisher { _, _ -> payload }.publish(creditScoringCalculatedEvent())
    }

    @Test
    fun `does not fail caller when serialization fails`() {
        // A registry outage surfaces as a RuntimeException from the serializer.
        publisher { _, _ -> throw RuntimeException("registry unavailable") }
            .publish(creditScoringCalculatedEvent())
    }

    @Test
    fun `routes account update events`() {
        publisher { subject, _ ->
            assert(subject == "account.information.updated")
            payload
        }.publish(
            AccountInformationUpdatedEvent("app-1", "user@example.com", "Ada", "+15550100", 150000, 9),
        )

        verify(rabbitTemplate).convertAndSend(
            "event-onboarding.domain-events",
            "account.information.updated",
            payload,
        )
    }

    @Test
    fun `routes account deletion events`() {
        publisher { subject, _ ->
            assert(subject == "account.information.deleted")
            payload
        }.publish(AccountInformationDeletedEvent("app-1", "user@example.com"))

        verify(rabbitTemplate).convertAndSend(
            "event-onboarding.domain-events",
            "account.information.deleted",
            payload,
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
