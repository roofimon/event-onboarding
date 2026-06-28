package com.example.eventonboarding.adapter.outbound.persistence

import com.example.eventonboarding.application.port.outbound.ApplicationRepository
import com.example.eventonboarding.domain.OnboardingApplication
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/** Outbound adapter storing applications in an in-memory map (lost on restart). */
@Repository
class InMemoryApplicationRepository : ApplicationRepository {
    private val store = ConcurrentHashMap<String, OnboardingApplication>()

    override fun save(application: OnboardingApplication): OnboardingApplication =
        application.also { store[it.id] = it }

    override fun findById(id: String): OnboardingApplication? = store[id]
}
