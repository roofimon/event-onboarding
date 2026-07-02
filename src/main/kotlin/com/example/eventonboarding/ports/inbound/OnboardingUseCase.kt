package com.example.eventonboarding.ports.inbound

import com.example.eventonboarding.domain.OnboardingApplication

/**
 * Driving (inbound) port — the application's API for the four onboarding steps.
 * Inbound adapters (e.g. the REST controller) depend on this, never on the
 * concrete service.
 */
interface OnboardingUseCase {
    /** Step 1 — start an application from an email; a token is issued out of band. */
    fun start(email: String): OnboardingApplication

    /** Step 2 — verify the token delivered to the user. */
    fun verifyToken(id: String, token: String): OnboardingApplication

    /** Step 3 — capture the applicant's details. */
    fun fulfill(
        id: String,
        name: String,
        email: String,
        phone: String,
        salary: Int,
        yearsOfExperience: Int,
    ): OnboardingApplication

    /** Step 4 — run credit scoring and resolve the application. */
    fun score(id: String): OnboardingApplication

    /** Read the current state of an application. */
    fun get(id: String): OnboardingApplication

    /**
     * Authenticate an approved applicant with the credentials issued on approval.
     * @throws com.example.eventonboarding.domain.InvalidCredentialsException on any mismatch.
     */
    fun login(email: String, password: String): OnboardingApplication

    /**
     * Update an approved applicant's profile details after re-verifying their password.
     * Does not re-score or change the approval outcome.
     * @throws com.example.eventonboarding.domain.InvalidCredentialsException on any mismatch.
     */
    fun updateProfile(
        email: String,
        password: String,
        name: String,
        phone: String,
        salary: Int,
        yearsOfExperience: Int,
    ): OnboardingApplication
}
