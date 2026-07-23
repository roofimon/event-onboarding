package com.example.eventonboarding.infrastructure.messaging

import com.example.eventonboarding.scoring.CreditScoringCalculatedEvent
import com.example.eventonboarding.scoring.DomainEvent
import com.example.eventonboarding.scoring.DomainEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Component
class RabbitDomainEventPublisher(
    private val rabbitTemplate: RabbitTemplate,
    private val eventSerializer: EventSerializer,
    @param:Value("\${onboarding.events.exchange}") private val exchange: String,
    @param:Value("\${onboarding.events.credit-scoring.routing-key}") private val creditScoringRoutingKey: String,
) : DomainEventPublisher {
    private val logger = LoggerFactory.getLogger(RabbitDomainEventPublisher::class.java)

    override fun publish(event: DomainEvent) {
        val routingKey = routingKeyFor(event)
        try {
            // Serialize to Avro binary (schema in the registry), then send the raw bytes.
            val payload = eventSerializer.serialize(routingKey, event)
            rabbitTemplate.convertAndSend(exchange, routingKey, payload)
        } catch (ex: RuntimeException) {
            // Fire-and-forget: a broker or registry outage must not break onboarding.
            logger.warn("Failed to publish domain event {}", event::class.simpleName, ex)
        }
    }

    private fun routingKeyFor(event: DomainEvent): String = when (event) {
        is CreditScoringCalculatedEvent -> creditScoringRoutingKey
    }
}

@Configuration
class RabbitDomainEventConfiguration {
    @Bean
    fun onboardingEventsExchange(
        @Value("\${onboarding.events.exchange}") exchange: String,
    ): TopicExchange = TopicExchange(exchange, true, false)

    @Bean
    fun creditScoringCalculatedQueue(
        @Value("\${onboarding.events.credit-scoring.queue}") queue: String,
    ): Queue = Queue(queue, true)

    @Bean
    fun creditScoringCalculatedBinding(
        onboardingEventsExchange: TopicExchange,
        creditScoringCalculatedQueue: Queue,
        @Value("\${onboarding.events.credit-scoring.routing-key}") routingKey: String,
    ): Binding = BindingBuilder
        .bind(creditScoringCalculatedQueue)
        .to(onboardingEventsExchange)
        .with(routingKey)
}
