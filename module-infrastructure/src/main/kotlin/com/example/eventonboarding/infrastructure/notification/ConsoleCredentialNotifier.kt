package com.example.eventonboarding.infrastructure.notification

import com.example.eventonboarding.account.CredentialNotifier
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
