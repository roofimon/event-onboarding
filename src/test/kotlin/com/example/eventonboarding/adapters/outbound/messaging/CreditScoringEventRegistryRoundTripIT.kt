package com.example.eventonboarding.adapters.outbound.messaging

import com.example.eventonboarding.domain.OnboardingStep
import com.example.eventonboarding.domain.event.CreditScoringCalculatedEvent
import io.apicurio.registry.serde.SerdeConfig
import io.apicurio.registry.serde.avro.AvroKafkaDeserializer
import org.apache.avro.generic.GenericRecord
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.time.Instant

/**
 * End-to-end proof: publish a domain event as Avro binary through RabbitMQ with the
 * schema managed by a real Apicurio Registry, then consume the raw bytes and decode
 * them by resolving the schema from the registry.
 *
 * Skipped automatically when Docker is unavailable.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreditScoringEventRegistryRoundTripIT {

    private val exchange = "event-onboarding.domain-events"
    private val routingKey = "onboarding.credit-scoring.calculated"
    private val queue = "event-onboarding.credit-scoring-calculated"

    private var apicurio: GenericContainer<*>? = null
    private var rabbit: RabbitMQContainer? = null

    @BeforeAll
    fun startContainers() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is not available")
        apicurio = GenericContainer(DockerImageName.parse("apicurio/apicurio-registry-mem:2.5.11.Final"))
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/apis/registry/v2/system/info").forPort(8080).forStatusCode(200))
            .also { it.start() }
        rabbit = RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"))
            .also { it.start() }
    }

    @AfterAll
    fun stopContainers() {
        apicurio?.stop()
        rabbit?.stop()
    }

    @Test
    fun `event travels as avro binary and is decoded via the registry`() {
        val registry = apicurio!!
        val broker = rabbit!!
        val registryUrl = "http://${registry.host}:${registry.getMappedPort(8080)}/apis/registry/v2"

        val connectionFactory = CachingConnectionFactory(broker.host, broker.amqpPort).apply {
            username = broker.adminUsername
            setPassword(broker.adminPassword)
        }
        try {
            val admin = RabbitAdmin(connectionFactory)
            val topic = TopicExchange(exchange, true, false)
            val q = Queue(queue, true)
            admin.declareExchange(topic)
            admin.declareQueue(q)
            admin.declareBinding(BindingBuilder.bind(q).to(topic).with(routingKey))

            val template = RabbitTemplate(connectionFactory)
            val publisher = RabbitDomainEventPublisher(
                rabbitTemplate = template,
                eventSerializer = AvroEventSerializer(registryUrl),
                exchange = exchange,
                creditScoringRoutingKey = routingKey,
            )

            val event = CreditScoringCalculatedEvent(
                applicationId = "app-1",
                email = "user@example.com",
                score = 80,
                approved = true,
                step = OnboardingStep.COMPLETED,
                occurredAt = Instant.parse("2026-07-03T10:15:30.123Z"),
            )
            publisher.publish(event)

            val message = template.receive(queue, 5_000)
                ?: error("no message received from $queue")

            // Compact binary on the wire — not self-describing JSON.
            assertEquals(MessageProperties.CONTENT_TYPE_BYTES, message.messageProperties.contentType)

            val deserializer = AvroKafkaDeserializer<GenericRecord>().apply {
                configure(HashMap<String, Any>().apply { put(SerdeConfig.REGISTRY_URL, registryUrl) }, false)
            }
            val decoded = deserializer.deserialize(routingKey, message.body)

            assertEquals("app-1", decoded.get("applicationId").toString())
            assertEquals("user@example.com", decoded.get("email").toString())
            assertEquals(80, decoded.get("score"))
            assertEquals(true, decoded.get("approved"))
            assertEquals("COMPLETED", decoded.get("step").toString())
            assertEquals(event.occurredAt.toEpochMilli(), decoded.get("occurredAt"))
        } finally {
            connectionFactory.destroy()
        }
    }
}
