package com.example.eventonboarding.application.port.outbound

import com.example.eventonboarding.domain.OnboardingApplication

/** Driven (outbound) port that computes a credit score for an application. */
fun interface CreditScorer {
    fun score(application: OnboardingApplication): Int
}
