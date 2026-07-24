package com.example.eventonboarding.domain.event

import java.time.Instant

interface DomainEvent {
    val occurredAt: Instant
}

fun interface DomainEventPublisher {
    fun publish(event: DomainEvent)
}
