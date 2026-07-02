package com.example.eventonboarding.adapters.inbound.web

import com.example.eventonboarding.ports.outbound.CreditScorer
import com.example.eventonboarding.ports.outbound.TokenGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class OnboardingControllerTest {

    /** Pin the outbound randomness so the flow is deterministic end to end. */
    @TestConfiguration
    class FixedAdapters {
        @Bean
        @Primary
        fun tokenGenerator(): TokenGenerator = TokenGenerator { "123456" }

        @Bean
        @Primary
        fun creditScorer(): CreditScorer = CreditScorer { 80 }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `walks through all four steps to approval`() {
        // Step 1
        val startResult = mockMvc.perform(
            post("/api/onboarding/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"user@example.com"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.step").value("TOKEN_VERIFY"))
            .andReturn()

        val id = objectMapper.readTree(startResult.response.contentAsString)["applicationId"].asText()

        // Step 2
        mockMvc.perform(
            post("/api/onboarding/$id/verify-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"token":"123456"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.verified").value(true))
            .andExpect(jsonPath("$.step").value("FULFILLMENT"))

        // Step 3
        mockMvc.perform(
            post("/api/onboarding/$id/fulfillment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"name":"Ada","email":"user@example.com","phone":"+1 555 0100","salary":120000,"yearsOfExperience":7}""",
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.step").value("SCORING"))

        // Step 4
        mockMvc.perform(post("/api/onboarding/$id/score"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.score").value(80))
            .andExpect(jsonPath("$.approved").value(true))
            .andExpect(jsonPath("$.step").value("COMPLETED"))

        // State view never leaks the token
        mockMvc.perform(get("/api/onboarding/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").doesNotExist())
            .andExpect(jsonPath("$.salary").value(120000))
            .andExpect(jsonPath("$.yearsOfExperience").value(7))
            .andExpect(jsonPath("$.score").value(80))
    }

    @Test
    fun `rejects an invalid email with 400`() {
        mockMvc.perform(
            post("/api/onboarding/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"not-an-email"}"""),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `unknown application id returns 404`() {
        mockMvc.perform(get("/api/onboarding/does-not-exist"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").exists())
    }
}
