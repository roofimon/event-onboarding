package com.example.eventonboarding.adapters.inbound.web.dto

import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.domain.OnboardingStep
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

// ----- Requests -----

data class StartRequest(
    @field:NotBlank @field:Email val email: String,
)

data class VerifyTokenRequest(
    @field:NotBlank val token: String,
)

data class FulfillmentRequest(
    @field:NotBlank val name: String,
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank
    @field:Pattern(regexp = "^\\+?[0-9 ()-]{7,20}$", message = "must be a valid phone number")
    val phone: String,
    @field:Min(0) val salary: Int,
    @field:Min(0) val yearsOfExperience: Int,
)

// ----- Responses -----

data class StartResponse(
    val applicationId: String,
    val step: OnboardingStep,
)

data class VerifyTokenResponse(
    val verified: Boolean,
    val step: OnboardingStep,
)

data class StepResponse(
    val step: OnboardingStep,
)

data class ScoreResponse(
    val score: Int,
    val approved: Boolean,
    val step: OnboardingStep,
)

/** Safe public view of an application — never includes the verification token. */
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
        fun from(app: OnboardingApplication) = ApplicationView(
            id = app.id,
            email = app.email,
            tokenVerified = app.tokenVerified,
            name = app.name,
            phone = app.phone,
            salary = app.salary,
            yearsOfExperience = app.yearsOfExperience,
            score = app.score,
            step = app.step,
        )
    }
}

data class ErrorResponse(val error: String)
