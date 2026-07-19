package com.example.eventonboarding.adapters.outbound.messaging

import com.example.eventonboarding.domain.OnboardingStep
import com.example.eventonboarding.domain.event.CreditScoringCalculatedEvent
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.EncoderFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.time.Instant

class CreditScoringAvroMapperTest {

    private val event = CreditScoringCalculatedEvent(
        applicationId = "app-1",
        email = "user@example.com",
        score = 80,
        approved = true,
        step = OnboardingStep.COMPLETED,
        occurredAt = Instant.parse("2026-07-03T10:15:30.123Z"),
    )

    @Test
    fun `maps every domain field onto the Avro record`() {
        val record = CreditScoringAvroMapper.toRecord(event)

        assertEquals("app-1", record.get("applicationId").toString())
        assertEquals("user@example.com", record.get("email").toString())
        assertEquals(80, record.get("score"))
        assertEquals(true, record.get("approved"))
        assertEquals("COMPLETED", record.get("step").toString())
        // timestamp-millis is stored as epoch millis (sub-millis precision is dropped).
        assertEquals(event.occurredAt.toEpochMilli(), record.get("occurredAt"))
    }

    @Test
    fun `record round-trips through Avro binary encoding`() {
        val record = CreditScoringAvroMapper.toRecord(event)

        val bytes = ByteArrayOutputStream().use { out ->
            val encoder = EncoderFactory.get().binaryEncoder(out, null)
            GenericDatumWriter<GenericRecord>(CreditScoringAvroMapper.schema).write(record, encoder)
            encoder.flush()
            out.toByteArray()
        }

        val decoder = DecoderFactory.get().binaryDecoder(bytes, null)
        val decoded = GenericDatumReader<GenericRecord>(CreditScoringAvroMapper.schema).read(null, decoder)

        assertEquals("app-1", decoded.get("applicationId").toString())
        assertEquals("user@example.com", decoded.get("email").toString())
        assertEquals(80, decoded.get("score"))
        assertEquals(true, decoded.get("approved"))
        assertEquals("COMPLETED", decoded.get("step").toString())
        assertEquals(event.occurredAt.toEpochMilli(), decoded.get("occurredAt"))
    }
}
