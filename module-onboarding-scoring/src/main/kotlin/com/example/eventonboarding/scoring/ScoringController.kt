package com.example.eventonboarding.scoring

import com.example.eventonboarding.domain.OnboardingStep
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/onboarding")
class ScoringController(private val scoring: ScoringUseCase) {
    @PostMapping("/{id}/score")
    fun score(@PathVariable id: String): ScoreResponse {
        val application = scoring.score(id)
        return ScoreResponse(
            application.score!!,
            application.step == OnboardingStep.COMPLETED,
            application.step,
        )
    }
}

data class ScoreResponse(val score: Int, val approved: Boolean, val step: OnboardingStep)
