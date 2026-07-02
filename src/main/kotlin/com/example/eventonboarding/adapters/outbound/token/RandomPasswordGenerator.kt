package com.example.eventonboarding.adapters.outbound.token

import com.example.eventonboarding.ports.outbound.PasswordGenerator
import org.springframework.stereotype.Component
import java.security.SecureRandom

/** Outbound adapter generating an initial account password. */
@Component
class RandomPasswordGenerator : PasswordGenerator {
    private val random = SecureRandom()

    override fun generate(): String =
        (1..PASSWORD_LENGTH)
            .map { ALPHANUMERIC[random.nextInt(ALPHANUMERIC.length)] }
            .joinToString("")

    private companion object {
        const val PASSWORD_LENGTH = 12
        const val ALPHANUMERIC = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789"
    }
}
