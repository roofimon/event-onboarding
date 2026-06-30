package com.example.eventonboarding.domain.event

import java.time.Instant

sealed interface DomainEvent {
    val occurredAt: Instant
}
