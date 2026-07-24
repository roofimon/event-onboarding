package com.example.eventonboarding.infrastructure.scoring

import com.example.eventonboarding.scoring.CreditScorer
import com.example.eventonboarding.domain.OnboardingApplication
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.math.roundToInt

/**
 * Outbound adapter computing a deterministic credit score (0–100) from the
 * applicant data captured at fulfillment.
 *
 * The score is a weighted sum of three components whose weights add up to 100:
 *  - salary (up to [SALARY_WEIGHT] pts), linear up to [SALARY_CAP] then saturating;
 *  - years of experience (up to [EXPERIENCE_WEIGHT] pts), linear up to [EXPERIENCE_CAP];
 *  - an efficiency bonus (up to [EFFICIENCY_WEIGHT] pts) rewarding salary earned per
 *    year of tenure, up to [RATIO_CAP].
 *
 * The result always lands in 0..100 and is monotonic in salary. It is *not* strictly
 * monotonic in experience: the efficiency bonus rewards salary earned per year, so at a
 * fixed salary a very long tenure slightly lowers that bonus. Experience is still the
 * dominant lever within its cap; the efficiency term only re-weights it at the extremes.
 */
@Component
class WeightedCreditScorer : CreditScorer {
    private val logger = LoggerFactory.getLogger(WeightedCreditScorer::class.java)

    override fun score(application: OnboardingApplication): Int {
        val salary = (application.salary ?: 0).coerceAtLeast(0)
        val years = (application.yearsOfExperience ?: 0).coerceAtLeast(0)

        val salaryPts = fraction(salary.toDouble(), SALARY_CAP) * SALARY_WEIGHT
        val experiencePts = fraction(years.toDouble(), EXPERIENCE_CAP) * EXPERIENCE_WEIGHT

        val salaryPerYear = salary.toDouble() / maxOf(years, 1)
        val efficiencyPts = fraction(salaryPerYear, RATIO_CAP) * EFFICIENCY_WEIGHT

        val score = (salaryPts + experiencePts + efficiencyPts).roundToInt().coerceIn(0, 100)
        logger.info("Credit score for {} : {}", application.email, score)
        return score
    }

    /** Linear ramp of [value] against [cap], clamped to 0.0..1.0. */
    private fun fraction(value: Double, cap: Double): Double =
        (value / cap).coerceIn(0.0, 1.0)

    private companion object {
        const val SALARY_WEIGHT = 60.0
        const val EXPERIENCE_WEIGHT = 30.0
        const val EFFICIENCY_WEIGHT = 10.0

        const val SALARY_CAP = 200_000.0
        const val EXPERIENCE_CAP = 20.0
        const val RATIO_CAP = 20_000.0
    }
}
