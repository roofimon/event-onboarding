package com.example.eventonboarding.ports.outbound

/** Driven (outbound) port that produces initial account passwords. */
fun interface PasswordGenerator {
    fun generate(): String
}
