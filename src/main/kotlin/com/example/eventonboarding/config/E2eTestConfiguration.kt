package com.example.eventonboarding.config

import com.example.eventonboarding.ports.outbound.PasswordGenerator
import com.example.eventonboarding.ports.outbound.TokenGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * E2E profile wiring. Pins the otherwise-random verification token to a known value so
 * Playwright can drive the flow deterministically. The credit score is intentionally
 * *not* overridden here — the real [com.example.eventonboarding.adapters.outbound.scoring.WeightedCreditScorer]
 * runs, so e2e tests exercise the production scoring algorithm from the salary/experience
 * they enter in the form.
 */
@Configuration
@Profile("e2e")
class E2eTestConfiguration {
    @Bean
    fun e2eScenarioState(): E2eScenarioState = E2eScenarioState()

    @Bean
    @Primary
    fun e2eTokenGenerator(state: E2eScenarioState): TokenGenerator = TokenGenerator {
        state.token
    }

    /** Fixed password so e2e tests can log in as an approved applicant. */
    @Bean
    @Primary
    fun e2ePasswordGenerator(): PasswordGenerator = PasswordGenerator { E2E_PASSWORD }

    companion object {
        const val E2E_PASSWORD = "e2e-password-123"
    }
}

class E2eScenarioState {
    @Volatile
    var token: String = "123456"
        private set

    fun configure(request: E2eScenarioRequest) {
        token = request.token
    }
}

data class E2eScenarioRequest(
    val token: String = "123456",
)

data class E2eHealthResponse(val status: String)

@RestController
@Profile("e2e")
@RequestMapping("/api/e2e")
class E2eScenarioController(
    private val state: E2eScenarioState,
) {
    @GetMapping("/health")
    fun health(): E2eHealthResponse = E2eHealthResponse("ok")

    @PostMapping("/scenario")
    fun configure(@RequestBody request: E2eScenarioRequest): E2eScenarioRequest {
        state.configure(request)
        return request
    }
}
