package com.example.eventonboarding.infrastructure.scoring

import com.example.eventonboarding.domain.APPROVAL_THRESHOLD
import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.domain.OnboardingStep
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WeightedCreditScorerTest {
    private val scorer = WeightedCreditScorer()

    private fun applicationOf(salary: Int, years: Int) = OnboardingApplication(
        id = "app-1",
        email = "user@example.com",
        token = "000000",
        tokenVerified = true,
        salary = salary,
        yearsOfExperience = years,
        step = OnboardingStep.SCORING,
    )

    private fun score(salary: Int, years: Int) = scorer.score(applicationOf(salary, years))

    @Test
    fun `high salary and experience score at or near the maximum and never exceed it`() {
        val score = score(salary = 500_000, years = 30)
        assertTrue(score in 95..100, "expected near-max score, got $score")

        // Values well beyond every cap still clamp to exactly 100.
        assertEquals(100, score(salary = 10_000_000, years = 100))
    }

    @Test
    fun `zero salary and zero experience score zero`() {
        assertEquals(0, score(salary = 0, years = 0))
    }

    @Test
    fun `score is monotonic in salary`() {
        var previous = -1
        for (salary in listOf(0, 25_000, 50_000, 100_000, 200_000, 400_000)) {
            val current = score(salary = salary, years = 5)
            assertTrue(current >= previous, "score dropped when salary rose to $salary")
            previous = current
        }
    }

    @Test
    fun `experience raises the score within its normal range`() {
        // At a salary high enough that the efficiency bonus stays saturated, added
        // experience only ever helps (up to the experience cap).
        var previous = -1
        for (years in listOf(0, 3, 5, 10, 20)) {
            val current = score(salary = 200_000, years = years)
            assertTrue(current >= previous, "score dropped when experience rose to $years")
            previous = current
        }
    }

    @Test
    fun `values above the caps stay within range`() {
        val score = score(salary = Int.MAX_VALUE, years = Int.MAX_VALUE)
        assertTrue(score in 0..100, "score out of range: $score")
    }

    @Test
    fun `zero experience does not divide by zero and yields a valid score`() {
        val score = score(salary = 150_000, years = 0)
        assertTrue(score in 0..100, "score out of range: $score")
    }

    @Test
    fun `null inputs are treated as zero`() {
        val application = OnboardingApplication(
            id = "app-2",
            email = "user@example.com",
            token = "000000",
            step = OnboardingStep.SCORING,
        )
        assertEquals(0, scorer.score(application))
    }

    @Test
    fun `mid-range cases straddle the approval threshold`() {
        // 60k over 3 years -> below the 40 threshold (declines).
        val declining = score(salary = 60_000, years = 3)
        assertTrue(declining <= APPROVAL_THRESHOLD, "expected decline, got $declining")

        // 100k over 5 years -> above the 40 threshold (approves).
        val approving = score(salary = 100_000, years = 5)
        assertTrue(approving > APPROVAL_THRESHOLD, "expected approval, got $approving")
    }
}
