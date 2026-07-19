package com.example.eventonboarding.adapters.outbound.messaging

import com.example.eventonboarding.domain.event.CreditScoringCalculatedEvent
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord

/**
 * Maps the framework-free domain event to an Avro [GenericRecord], keeping Avro out
 * of the domain. The schema is the single source of truth, loaded from the classpath.
 */
object CreditScoringAvroMapper {
    /** Parsed once from `src/main/resources/avro/CreditScoringCalculated.avsc`. */
    val schema: Schema = Schema.Parser().parse(
        requireNotNull(javaClass.getResourceAsStream("/avro/CreditScoringCalculated.avsc")) {
            "Avro schema /avro/CreditScoringCalculated.avsc not found on the classpath"
        },
    )

    fun toRecord(event: CreditScoringCalculatedEvent): GenericRecord =
        GenericData.Record(schema).apply {
            put("applicationId", event.applicationId)
            put("email", event.email)
            put("score", event.score)
            put("approved", event.approved)
            put("step", event.step.name)
            // timestamp-millis is stored as the underlying epoch-millis long.
            put("occurredAt", event.occurredAt.toEpochMilli())
        }
}
