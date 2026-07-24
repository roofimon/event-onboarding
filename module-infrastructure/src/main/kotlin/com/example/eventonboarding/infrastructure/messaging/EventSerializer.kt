package com.example.eventonboarding.infrastructure.messaging

import com.example.eventonboarding.domain.event.DomainEvent

/**
 * Serializes a domain event to the bytes placed on the wire. Kept as an interface so
 * the publisher can be unit-tested without a running schema registry.
 *
 * @param subject logical name driving schema resolution/registration in the registry.
 */
fun interface EventSerializer {
    fun serialize(subject: String, event: DomainEvent): ByteArray
}
