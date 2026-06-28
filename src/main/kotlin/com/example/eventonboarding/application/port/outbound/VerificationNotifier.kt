package com.example.eventonboarding.application.port.outbound

/**
 * Driven (outbound) port that delivers a verification token to the user.
 * The production adapter prints it to the console; a real one might email/SMS it.
 */
fun interface VerificationNotifier {
    fun send(email: String, token: String)
}
