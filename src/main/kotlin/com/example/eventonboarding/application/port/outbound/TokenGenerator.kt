package com.example.eventonboarding.application.port.outbound

/** Driven (outbound) port that produces verification tokens. */
fun interface TokenGenerator {
    fun generate(): String
}
