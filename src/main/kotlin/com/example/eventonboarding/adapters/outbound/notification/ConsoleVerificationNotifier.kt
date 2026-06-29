package com.example.eventonboarding.adapters.outbound.notification

import com.example.eventonboarding.ports.outbound.VerificationNotifier
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/** Outbound adapter that delivers the verification token by printing it to the console. */
@Component
class ConsoleVerificationNotifier : VerificationNotifier {
    private val logger = LoggerFactory.getLogger(ConsoleVerificationNotifier::class.java)

    override fun send(email: String, token: String) {
        logger.info("Verification token for {} : {}", email, token)
    }
}
