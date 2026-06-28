package com.example.eventonboarding.adapter.outbound.scoring

import com.example.eventonboarding.application.port.outbound.CreditScorer
import com.example.eventonboarding.domain.OnboardingApplication
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.random.Random

/** Outbound adapter computing a credit score from a random algorithm (0–100). */
@Component
class RandomCreditScorer : CreditScorer {
    private val logger = LoggerFactory.getLogger(RandomCreditScorer::class.java)

    override fun score(application: OnboardingApplication): Int {
        val score = Random.nextInt(0, 101)
        logger.info("Credit score for {} : {}", application.email, score)
        return score
    }
}
