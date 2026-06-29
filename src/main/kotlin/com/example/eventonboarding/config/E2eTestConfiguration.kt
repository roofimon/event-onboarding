package com.example.eventonboarding.config

import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.ports.outbound.CreditScorer
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
import java.util.concurrent.ConcurrentHashMap

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

    @Bean
    @Primary
    fun e2eCreditScorer(state: E2eScenarioState): CreditScorer = CreditScorer { application ->
        state.scoreFor(application)
    }
}

class E2eScenarioState {
    @Volatile
    var token: String = "123456"
        private set

    private val scoresByEmail = ConcurrentHashMap<String, Int>()

    fun configure(request: E2eScenarioRequest) {
        token = request.token
        scoresByEmail[request.email.trim()] = request.score
    }

    fun scoreFor(application: OnboardingApplication): Int =
        scoresByEmail[application.email] ?: DEFAULT_SCORE

    companion object {
        private const val DEFAULT_SCORE = 80
    }
}

data class E2eScenarioRequest(
    val email: String,
    val token: String = "123456",
    val score: Int,
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
