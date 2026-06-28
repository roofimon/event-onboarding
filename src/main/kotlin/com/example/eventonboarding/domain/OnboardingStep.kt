package com.example.eventonboarding.domain

/**
 * The stage an onboarding application is currently at.
 *
 * The first four values map to the four onboarding steps; the last two are
 * terminal outcomes reached after credit scoring.
 */
enum class OnboardingStep {
    EMAIL,
    TOKEN_VERIFY,
    FULFILLMENT,
    SCORING,
    COMPLETED,
    DECLINED,
}
