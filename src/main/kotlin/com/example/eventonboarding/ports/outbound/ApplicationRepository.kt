package com.example.eventonboarding.ports.outbound

import com.example.eventonboarding.domain.OnboardingApplication

/** Driven (outbound) port for persisting onboarding applications. */
interface ApplicationRepository {
    fun save(application: OnboardingApplication): OnboardingApplication
    fun findById(id: String): OnboardingApplication?
}
