package com.example.eventonboarding.email

import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.domain.OnboardingStep
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/onboarding")
class EmailOnboardingController(private val onboarding: EmailOnboardingUseCase) {
    @PostMapping("/start")
    fun start(@Valid @RequestBody request: StartRequest): StartResponse {
        val application = onboarding.start(request.email)
        return StartResponse(application.id, application.step)
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ApplicationView = ApplicationView.from(onboarding.get(id))
}

data class StartRequest(@field:NotBlank @field:Email val email: String)
data class StartResponse(val applicationId: String, val step: OnboardingStep)

data class ApplicationView(
    val id: String,
    val email: String,
    val tokenVerified: Boolean,
    val name: String?,
    val phone: String?,
    val salary: Int?,
    val yearsOfExperience: Int?,
    val score: Int?,
    val step: OnboardingStep,
) {
    companion object {
        fun from(application: OnboardingApplication) = ApplicationView(
            application.id,
            application.email,
            application.tokenVerified,
            application.name,
            application.phone,
            application.salary,
            application.yearsOfExperience,
            application.score,
            application.step,
        )
    }
}
