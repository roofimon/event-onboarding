package com.example.eventonboarding.adapters.outbound.notification

import com.example.eventonboarding.ports.outbound.CredentialNotifier
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/** Outbound adapter printing generated account credentials to the console. */
@Component
class ConsoleCredentialNotifier : CredentialNotifier {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun send(username: String, password: String) {
        logger.info("Generated account credentials: username={}, password={}", username, password)
    }
}
