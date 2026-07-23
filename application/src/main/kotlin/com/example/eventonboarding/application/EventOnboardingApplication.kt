package com.example.eventonboarding.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.example.eventonboarding"])
class EventOnboardingApplication

fun main(args: Array<String>) {
    runApplication<EventOnboardingApplication>(*args)
}
