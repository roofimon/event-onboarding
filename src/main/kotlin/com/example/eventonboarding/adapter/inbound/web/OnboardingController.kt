package com.example.eventonboarding.adapter.inbound.web

import com.example.eventonboarding.adapter.inbound.web.dto.ApplicationView
import com.example.eventonboarding.adapter.inbound.web.dto.FulfillmentRequest
import com.example.eventonboarding.adapter.inbound.web.dto.ScoreResponse
import com.example.eventonboarding.adapter.inbound.web.dto.StartRequest
import com.example.eventonboarding.adapter.inbound.web.dto.StartResponse
import com.example.eventonboarding.adapter.inbound.web.dto.StepResponse
import com.example.eventonboarding.adapter.inbound.web.dto.VerifyTokenRequest
import com.example.eventonboarding.adapter.inbound.web.dto.VerifyTokenResponse
import com.example.eventonboarding.application.port.inbound.OnboardingUseCase
import com.example.eventonboarding.domain.OnboardingStep
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** Inbound (driving) adapter exposing the onboarding use case over HTTP. */
@RestController
@RequestMapping("/api/onboarding")
class OnboardingController(
    private val onboarding: OnboardingUseCase,
) {
    /** Step 1 — submit email, receive an application id (token goes to the console). */
    @PostMapping("/start")
    fun start(@Valid @RequestBody request: StartRequest): StartResponse {
        val app = onboarding.start(request.email)
        return StartResponse(applicationId = app.id, step = app.step)
    }

    /** Step 2 — verify the console token. */
    @PostMapping("/{id}/verify-token")
    fun verifyToken(
        @PathVariable id: String,
        @Valid @RequestBody request: VerifyTokenRequest,
    ): VerifyTokenResponse {
        val app = onboarding.verifyToken(id, request.token)
        return VerifyTokenResponse(verified = app.tokenVerified, step = app.step)
    }

    /** Step 3 — submit name, email and phone. */
    @PostMapping("/{id}/fulfillment")
    fun fulfillment(
        @PathVariable id: String,
        @Valid @RequestBody request: FulfillmentRequest,
    ): StepResponse {
        val app = onboarding.fulfill(id, request.name, request.email, request.phone)
        return StepResponse(step = app.step)
    }

    /** Step 4 — run credit scoring. */
    @PostMapping("/{id}/score")
    fun score(@PathVariable id: String): ScoreResponse {
        val app = onboarding.score(id)
        return ScoreResponse(
            score = app.score!!,
            approved = app.step == OnboardingStep.COMPLETED,
            step = app.step,
        )
    }

    /** Current application state (never includes the token). */
    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ApplicationView = ApplicationView.from(onboarding.get(id))
}
