package com.example.eventonboarding.adapter.outbound.token

import com.example.eventonboarding.application.port.outbound.TokenGenerator
import org.springframework.stereotype.Component
import kotlin.random.Random

/** Outbound adapter generating a random 6-digit verification token. */
@Component
class RandomTokenGenerator : TokenGenerator {
    override fun generate(): String = "%06d".format(Random.nextInt(0, 1_000_000))
}
