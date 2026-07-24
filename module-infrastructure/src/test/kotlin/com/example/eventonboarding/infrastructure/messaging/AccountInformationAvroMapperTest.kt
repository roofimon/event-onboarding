package com.example.eventonboarding.infrastructure.messaging

import com.example.eventonboarding.account.AccountInformationDeletedEvent
import com.example.eventonboarding.account.AccountInformationUpdatedEvent
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class AccountInformationAvroMapperTest {
    private val occurredAt = Instant.parse("2026-07-23T10:15:30Z")

    @Test
    fun `maps account update event to Avro`() {
        val record = AccountInformationAvroMapper.toUpdatedRecord(
            AccountInformationUpdatedEvent(
                "app-1",
                "user@example.com",
                "Ada",
                "+15550100",
                150000,
                9,
                occurredAt,
            ),
        )

        assertEquals("app-1", record["applicationId"].toString())
        assertEquals("Ada", record["name"].toString())
        assertEquals(150000, record["salary"])
        assertEquals(occurredAt.toEpochMilli(), record["occurredAt"])
    }

    @Test
    fun `maps account deletion event to Avro`() {
        val record = AccountInformationAvroMapper.toDeletedRecord(
            AccountInformationDeletedEvent("app-1", "user@example.com", occurredAt),
        )

        assertEquals("app-1", record["applicationId"].toString())
        assertEquals("user@example.com", record["email"].toString())
        assertEquals(occurredAt.toEpochMilli(), record["occurredAt"])
    }
}
