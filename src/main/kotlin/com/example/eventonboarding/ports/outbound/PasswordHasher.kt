package com.example.eventonboarding.ports.outbound

/** Driven (outbound) port for hashing and verifying account passwords. */
interface PasswordHasher {
    /** Hash a raw password for storage. */
    fun hash(raw: String): String

    /** Verify a raw password against a previously stored [hash]. */
    fun matches(raw: String, hash: String): Boolean
}
