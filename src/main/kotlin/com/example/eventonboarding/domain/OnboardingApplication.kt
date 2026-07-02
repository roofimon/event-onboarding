package com.example.eventonboarding.domain

/** A credit score strictly above this threshold approves the application. */
const val APPROVAL_THRESHOLD = 40

/**
 * Core domain entity holding the state of a single onboarding application.
 *
 * Framework-free and progressively filled in as the user moves through the four
 * steps. The [token] is part of domain state but must never be exposed outside
 * the application — outbound adapters decide how it is delivered.
 */
data class OnboardingApplication(
    val id: String,
    var email: String,
    var token: String,
    var tokenVerified: Boolean = false,
    var name: String? = null,
    var phone: String? = null,
    var salary: Int? = null,
    var yearsOfExperience: Int? = null,
    var score: Int? = null,
    var step: OnboardingStep = OnboardingStep.TOKEN_VERIFY,
) {
    /** Record a credit score and resolve the application against the threshold. */
    fun applyScore(value: Int) {
        score = value
        step = if (value > APPROVAL_THRESHOLD) OnboardingStep.COMPLETED else OnboardingStep.DECLINED
    }
}
