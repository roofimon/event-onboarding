package com.example.eventonboarding.adapters.inbound.web

import com.example.eventonboarding.domain.ApplicationNotFoundException
import com.example.eventonboarding.domain.InvalidStepException
import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.domain.OnboardingStep
import com.example.eventonboarding.ports.inbound.OnboardingUseCase
import jakarta.validation.Validation
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.SpringValidatorAdapter

class OnboardingControllerUnitTest {
    private val onboarding = mock(OnboardingUseCase::class.java)
    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(OnboardingController(onboarding))
        .setControllerAdvice(OnboardingExceptionHandler())
        .setValidator(SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().validator))
        .build()

    @AfterEach
    fun verifyMocks() {
        verifyNoMoreInteractions(onboarding)
    }

    @Test
    fun `start returns application id and current step`() {
        `when`(onboarding.start("user@example.com")).thenReturn(
            application(id = "app-1", email = "user@example.com"),
        )

        mockMvc.perform(
            post("/api/onboarding/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"user@example.com"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.applicationId").value("app-1"))
            .andExpect(jsonPath("$.step").value("TOKEN_VERIFY"))

        verify(onboarding).start("user@example.com")
    }

    @Test
    fun `verify token returns verified state and next step`() {
        `when`(onboarding.verifyToken("app-1", "123456")).thenReturn(
            application(id = "app-1", tokenVerified = true, step = OnboardingStep.FULFILLMENT),
        )

        mockMvc.perform(
            post("/api/onboarding/app-1/verify-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"token":"123456"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.verified").value(true))
            .andExpect(jsonPath("$.step").value("FULFILLMENT"))

        verify(onboarding).verifyToken("app-1", "123456")
    }

    @Test
    fun `fulfillment returns scoring step`() {
        `when`(onboarding.fulfill("app-1", "Ada", "ada@example.com", "+1 555 0100", 120000, 7)).thenReturn(
            application(
                id = "app-1",
                email = "ada@example.com",
                tokenVerified = true,
                name = "Ada",
                phone = "+1 555 0100",
                salary = 120000,
                yearsOfExperience = 7,
                step = OnboardingStep.SCORING,
            ),
        )

        mockMvc.perform(
            post("/api/onboarding/app-1/fulfillment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """{"name":"Ada","email":"ada@example.com","phone":"+1 555 0100","salary":120000,"yearsOfExperience":7}""",
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.step").value("SCORING"))

        verify(onboarding).fulfill("app-1", "Ada", "ada@example.com", "+1 555 0100", 120000, 7)
    }

    @Test
    fun `score returns declined result`() {
        `when`(onboarding.score("app-1")).thenReturn(
            application(id = "app-1", score = 40, step = OnboardingStep.DECLINED),
        )

        mockMvc.perform(post("/api/onboarding/app-1/score"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.score").value(40))
            .andExpect(jsonPath("$.approved").value(false))
            .andExpect(jsonPath("$.step").value("DECLINED"))

        verify(onboarding).score("app-1")
    }

    @Test
    fun `get returns public application state without token`() {
        `when`(onboarding.get("app-1")).thenReturn(
            application(
                id = "app-1",
                email = "ada@example.com",
                tokenVerified = true,
                name = "Ada",
                phone = "+1 555 0100",
                salary = 120000,
                yearsOfExperience = 7,
                score = 80,
                step = OnboardingStep.COMPLETED,
            ),
        )

        mockMvc.perform(get("/api/onboarding/app-1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("app-1"))
            .andExpect(jsonPath("$.email").value("ada@example.com"))
            .andExpect(jsonPath("$.token").doesNotExist())
            .andExpect(jsonPath("$.tokenVerified").value(true))
            .andExpect(jsonPath("$.name").value("Ada"))
            .andExpect(jsonPath("$.phone").value("+1 555 0100"))
            .andExpect(jsonPath("$.salary").value(120000))
            .andExpect(jsonPath("$.yearsOfExperience").value(7))
            .andExpect(jsonPath("$.score").value(80))
            .andExpect(jsonPath("$.step").value("COMPLETED"))

        verify(onboarding).get("app-1")
    }

    @Test
    fun `invalid token maps to conflict response`() {
        `when`(onboarding.verifyToken("app-1", "000000")).thenThrow(
            InvalidStepException("Invalid verification token"),
        )

        mockMvc.perform(
            post("/api/onboarding/app-1/verify-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"token":"000000"}"""),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value("Invalid verification token"))

        verify(onboarding).verifyToken("app-1", "000000")
    }

    @Test
    fun `unknown application maps to not found response`() {
        `when`(onboarding.get("missing")).thenThrow(ApplicationNotFoundException("missing"))

        mockMvc.perform(get("/api/onboarding/missing"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("No onboarding application found for id 'missing'"))

        verify(onboarding).get("missing")
    }

    @Test
    fun `invalid fulfillment request returns bad request before use case is called`() {
        mockMvc.perform(
            post("/api/onboarding/app-1/fulfillment")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"","email":"not-an-email","phone":"x","salary":-1,"yearsOfExperience":-1}"""),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())

        verifyNoInteractions(onboarding)
    }

    private fun application(
        id: String = "app-1",
        email: String = "user@example.com",
        token: String = "123456",
        tokenVerified: Boolean = false,
        name: String? = null,
        phone: String? = null,
        salary: Int? = null,
        yearsOfExperience: Int? = null,
        score: Int? = null,
        step: OnboardingStep = OnboardingStep.TOKEN_VERIFY,
    ) = OnboardingApplication(
        id = id,
        email = email,
        token = token,
        tokenVerified = tokenVerified,
        name = name,
        phone = phone,
        salary = salary,
        yearsOfExperience = yearsOfExperience,
        score = score,
        step = step,
    )
}
