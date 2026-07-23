package com.example.eventonboarding.tokenverification

import com.example.eventonboarding.domain.OnboardingStep
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/onboarding")
class TokenVerificationController(private val verification: TokenVerificationUseCase) {
    @PostMapping("/{id}/verify-token")
    fun verifyToken(
        @PathVariable id: String,
        @Valid @RequestBody request: VerifyTokenRequest,
    ): VerifyTokenResponse {
        val application = verification.verifyToken(id, request.token)
        return VerifyTokenResponse(application.tokenVerified, application.step)
    }
}

data class VerifyTokenRequest(@field:NotBlank val token: String)
data class VerifyTokenResponse(val verified: Boolean, val step: OnboardingStep)
