package com.example.eventonboarding.infrastructure.persistence

import com.example.eventonboarding.ports.outbound.ApplicationRepository
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

    override fun findByEmail(email: String): OnboardingApplication? =
        store.values.firstOrNull { it.email == email }

    override fun deleteById(id: String): Boolean = store.remove(id) != null
}
