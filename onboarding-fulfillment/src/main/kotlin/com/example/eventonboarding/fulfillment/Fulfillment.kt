package com.example.eventonboarding.fulfillment

import com.example.eventonboarding.domain.ApplicationNotFoundException
import com.example.eventonboarding.domain.InvalidStepException
import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.domain.OnboardingStep
import com.example.eventonboarding.ports.outbound.ApplicationRepository

interface FulfillmentUseCase {
    fun fulfill(
        id: String,
        name: String,
        email: String,
        phone: String,
        salary: Int,
        yearsOfExperience: Int,
    ): OnboardingApplication
}

class FulfillmentService(private val repository: ApplicationRepository) : FulfillmentUseCase {
    override fun fulfill(
        id: String,
        name: String,
        email: String,
        phone: String,
        salary: Int,
        yearsOfExperience: Int,
    ): OnboardingApplication {
        val application = repository.findById(id) ?: throw ApplicationNotFoundException(id)
        if (!application.tokenVerified) throw InvalidStepException("Token must be verified before fulfillment")
        application.name = name.trim()
        application.email = email.trim()
        application.phone = phone.trim()
        application.salary = salary
        application.yearsOfExperience = yearsOfExperience
        application.step = OnboardingStep.SCORING
        return repository.save(application)
    }
}
