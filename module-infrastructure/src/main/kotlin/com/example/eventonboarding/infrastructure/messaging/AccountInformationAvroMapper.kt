package com.example.eventonboarding.infrastructure.messaging

import com.example.eventonboarding.account.AccountInformationDeletedEvent
import com.example.eventonboarding.account.AccountInformationUpdatedEvent
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord

object AccountInformationAvroMapper {
    private fun schema(resource: String): Schema = Schema.Parser().parse(
        requireNotNull(javaClass.getResourceAsStream(resource)) { "Avro schema $resource not found" },
    )

    val updatedSchema: Schema = schema("/avro/AccountInformationUpdated.avsc")
    val deletedSchema: Schema = schema("/avro/AccountInformationDeleted.avsc")

    fun toUpdatedRecord(event: AccountInformationUpdatedEvent): GenericRecord =
        GenericData.Record(updatedSchema).apply {
            put("applicationId", event.applicationId)
            put("email", event.email)
            put("name", event.name)
            put("phone", event.phone)
            put("salary", event.salary)
            put("yearsOfExperience", event.yearsOfExperience)
            put("occurredAt", event.occurredAt.toEpochMilli())
        }

    fun toDeletedRecord(event: AccountInformationDeletedEvent): GenericRecord =
        GenericData.Record(deletedSchema).apply {
            put("applicationId", event.applicationId)
            put("email", event.email)
            put("occurredAt", event.occurredAt.toEpochMilli())
        }
}
