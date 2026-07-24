package com.example.eventonboarding.infrastructure.messaging

import com.example.eventonboarding.scoring.CreditScoringCalculatedEvent
import com.example.eventonboarding.account.AccountInformationDeletedEvent
import com.example.eventonboarding.account.AccountInformationUpdatedEvent
import com.example.eventonboarding.domain.event.DomainEvent
import io.apicurio.registry.serde.SerdeConfig
import io.apicurio.registry.serde.avro.AvroKafkaSerializer
import org.apache.avro.generic.GenericRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Serializes domain events to compact Avro binary using Apicurio's Avro SerDe.
 *
 * The bytes carry only a schema-id prefix plus the Avro body — no field names — and the
 * schema itself lives in the registry. The Apicurio serializer implements the Kafka
 * `Serializer` interface but is used here standalone (no Kafka broker); the `subject`
 * we pass stands in for the Kafka "topic" that drives artifact resolution.
 */
@Component
class AvroEventSerializer(
    @Value("\${apicurio.registry.url}") registryUrl: String,
) : EventSerializer {

    private val serializer: AvroKafkaSerializer<GenericRecord> = AvroKafkaSerializer<GenericRecord>().apply {
        configure(
            // Auto-register the schema on first publish (dev convenience).
            HashMap<String, Any>().apply {
                put(SerdeConfig.REGISTRY_URL, registryUrl)
                put(SerdeConfig.AUTO_REGISTER_ARTIFACT, "true")
            },
            false,
        )
    }

    override fun serialize(subject: String, event: DomainEvent): ByteArray {
        val record = when (event) {
            is CreditScoringCalculatedEvent -> CreditScoringAvroMapper.toRecord(event)
            is AccountInformationUpdatedEvent -> AccountInformationAvroMapper.toUpdatedRecord(event)
            is AccountInformationDeletedEvent -> AccountInformationAvroMapper.toDeletedRecord(event)
            else -> error("Unsupported domain event: ${event::class.qualifiedName}")
        }
        return serializer.serialize(subject, record)
    }
}
