package com.example.eventonboarding.ports.outbound

import com.example.eventonboarding.domain.event.DomainEvent

/** Driven (outbound) port for publishing domain events outside the core. */
fun interface DomainEventPublisher {
    fun publish(event: DomainEvent)
}
