package com.example.eventonboarding.account

import com.example.eventonboarding.domain.OnboardingApplication
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AccountController(private val account: AccountUseCase) {
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ProfileResponse =
        ProfileResponse.from(account.login(request.email, request.password))

    @PutMapping("/profile")
    fun updateProfile(@Valid @RequestBody request: UpdateProfileRequest): ProfileResponse =
        ProfileResponse.from(
            account.updateProfile(
                request.email,
                request.password,
                request.name,
                request.phone,
                request.salary,
                request.yearsOfExperience,
            ),
        )
}

data class LoginRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val password: String,
)

data class UpdateProfileRequest(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val password: String,
    @field:NotBlank val name: String,
    @field:NotBlank
    @field:Pattern(regexp = "^\\+?[0-9 ()-]{7,20}$", message = "must be a valid phone number")
    val phone: String,
    @field:Min(0) val salary: Int,
    @field:Min(0) val yearsOfExperience: Int,
)

data class ProfileResponse(
    val name: String,
    val email: String,
    val phone: String,
    val salary: Int,
    val yearsOfExperience: Int,
) {
    companion object {
        fun from(application: OnboardingApplication) = ProfileResponse(
            application.name!!,
            application.email,
            application.phone!!,
            application.salary!!,
            application.yearsOfExperience!!,
        )
    }
}
