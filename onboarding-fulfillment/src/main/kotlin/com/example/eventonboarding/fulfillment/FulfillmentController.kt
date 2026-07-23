package com.example.eventonboarding.fulfillment

import com.example.eventonboarding.domain.OnboardingStep
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/onboarding")
class FulfillmentController(private val fulfillment: FulfillmentUseCase) {
    @PostMapping("/{id}/fulfillment")
    fun fulfill(
        @PathVariable id: String,
        @Valid @RequestBody request: FulfillmentRequest,
    ): StepResponse {
        val application = fulfillment.fulfill(
            id, request.name, request.email, request.phone, request.salary, request.yearsOfExperience,
        )
        return StepResponse(application.step)
    }
}

data class FulfillmentRequest(
    @field:NotBlank val name: String,
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank
    @field:Pattern(regexp = "^\\+?[0-9 ()-]{7,20}$", message = "must be a valid phone number")
    val phone: String,
    @field:Min(0) val salary: Int,
    @field:Min(0) val yearsOfExperience: Int,
)

data class StepResponse(val step: OnboardingStep)
