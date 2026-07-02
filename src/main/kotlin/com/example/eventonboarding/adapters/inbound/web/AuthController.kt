package com.example.eventonboarding.adapters.inbound.web

import com.example.eventonboarding.adapters.inbound.web.dto.LoginRequest
import com.example.eventonboarding.adapters.inbound.web.dto.ProfileResponse
import com.example.eventonboarding.adapters.inbound.web.dto.UpdateProfileRequest
import com.example.eventonboarding.ports.inbound.OnboardingUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** Inbound (driving) adapter exposing login for approved applicants. */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val onboarding: OnboardingUseCase,
) {
    /** Authenticate with the credentials issued on approval; returns the profile. */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ProfileResponse =
        ProfileResponse.from(onboarding.login(request.email, request.password))

    /** Update the caller's profile after re-verifying their password; returns it. */
    @PutMapping("/profile")
    fun updateProfile(@Valid @RequestBody request: UpdateProfileRequest): ProfileResponse =
        ProfileResponse.from(
            onboarding.updateProfile(
                request.email,
                request.password,
                request.name,
                request.phone,
                request.salary,
                request.yearsOfExperience,
            ),
        )
}
