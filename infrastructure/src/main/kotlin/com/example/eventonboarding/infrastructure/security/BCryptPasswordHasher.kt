package com.example.eventonboarding.infrastructure.security

import com.example.eventonboarding.account.PasswordHasher
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

/** Outbound adapter hashing passwords with BCrypt (via spring-security-crypto). */
@Component
class BCryptPasswordHasher : PasswordHasher {
    private val encoder = BCryptPasswordEncoder()

    override fun hash(raw: String): String = encoder.encode(raw)

    override fun matches(raw: String, hash: String): Boolean = encoder.matches(raw, hash)
}
