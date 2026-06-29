package com.example.eventonboarding.ports.outbound

/** Driven (outbound) port that produces verification tokens. */
fun interface TokenGenerator {
    fun generate(): String
}
