package com.example.eventonboarding.ports.outbound

/**
 * Driven (outbound) port that delivers generated account credentials.
 *
 * The username is the applicant email address. Production currently prints the
 * credentials to the console.
 */
fun interface CredentialNotifier {
    fun send(username: String, password: String)
}
