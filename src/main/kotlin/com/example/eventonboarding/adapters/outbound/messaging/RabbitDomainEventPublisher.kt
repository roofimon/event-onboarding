package com.example.eventonboarding.adapters.outbound.messaging

import com.example.eventonboarding.domain.event.CreditScoringCalculatedEvent
import com.example.eventonboarding.domain.event.DomainEvent
import com.example.eventonboarding.ports.outbound.DomainEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpException
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Component
class RabbitDomainEventPublisher(
    private val rabbitTemplate: RabbitTemplate,
    @param:Value("\${onboarding.events.exchange}") private val exchange: String,
    @param:Value("\${onboarding.events.credit-scoring.routing-key}") private val creditScoringRoutingKey: String,
) : DomainEventPublisher {
    private val logger = LoggerFactory.getLogger(RabbitDomainEventPublisher::class.java)

    override fun publish(event: DomainEvent) {
        val routingKey = routingKeyFor(event)
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event)
        } catch (ex: AmqpException) {
            logger.warn("Failed to publish domain event {} to RabbitMQ", event::class.simpleName, ex)
        }
    }

    private fun routingKeyFor(event: DomainEvent): String = when (event) {
        is CreditScoringCalculatedEvent -> creditScoringRoutingKey
    }
}

@Configuration
class RabbitDomainEventConfiguration {
    @Bean
    fun rabbitJsonMessageConverter(): MessageConverter = Jackson2JsonMessageConverter()

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
