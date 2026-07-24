package com.example.eventonboarding.account

import com.example.eventonboarding.domain.InvalidCredentialsException
import com.example.eventonboarding.domain.OnboardingApplication
import com.example.eventonboarding.domain.OnboardingStep
import com.example.eventonboarding.domain.event.DomainEvent
import com.example.eventonboarding.domain.event.DomainEventPublisher
import com.example.eventonboarding.ports.outbound.ApplicationRepository
import java.time.Instant

interface AccountUseCase {
    fun login(email: String, password: String): OnboardingApplication
    fun updateProfile(
        email: String,
        password: String,
        name: String,
        phone: String,
        salary: Int,
        yearsOfExperience: Int,
    ): OnboardingApplication
    fun deleteProfile(email: String, password: String)
}

data class AccountInformationUpdatedEvent(
    val applicationId: String,
    val email: String,
    val name: String,
    val phone: String,
    val salary: Int,
    val yearsOfExperience: Int,
    override val occurredAt: Instant = Instant.now(),
) : DomainEvent

data class AccountInformationDeletedEvent(
    val applicationId: String,
    val email: String,
    override val occurredAt: Instant = Instant.now(),
) : DomainEvent

fun interface AccountProvisioning {
    fun provision(application: OnboardingApplication)
}

fun interface PasswordGenerator {
    fun generate(): String
}

interface PasswordHasher {
    fun hash(raw: String): String
    fun matches(raw: String, hash: String): Boolean
}

fun interface CredentialNotifier {
    fun send(username: String, password: String)
}

class AccountService(
    private val repository: ApplicationRepository,
    private val passwordGenerator: PasswordGenerator,
    private val passwordHasher: PasswordHasher,
    private val credentialNotifier: CredentialNotifier,
    private val domainEventPublisher: DomainEventPublisher,
) : AccountUseCase, AccountProvisioning {
    override fun provision(application: OnboardingApplication) {
        val password = passwordGenerator.generate()
        application.passwordHash = passwordHasher.hash(password)
        credentialNotifier.send(application.email, password)
    }

    override fun login(email: String, password: String): OnboardingApplication {
        val application = repository.findByEmail(email.trim())
        val hash = application?.passwordHash
        if (application == null || hash == null || application.step != OnboardingStep.COMPLETED ||
            !passwordHasher.matches(password, hash)
        ) {
            throw InvalidCredentialsException()
        }
        return application
    }

    override fun updateProfile(
        email: String,
        password: String,
        name: String,
        phone: String,
        salary: Int,
        yearsOfExperience: Int,
    ): OnboardingApplication {
        val application = login(email, password)
        application.name = name.trim()
        application.phone = phone.trim()
        application.salary = salary
        application.yearsOfExperience = yearsOfExperience
        val saved = repository.save(application)
        domainEventPublisher.publish(
            AccountInformationUpdatedEvent(
                saved.id,
                saved.email,
                saved.name!!,
                saved.phone!!,
                saved.salary!!,
                saved.yearsOfExperience!!,
            ),
        )
        return saved
    }

    override fun deleteProfile(email: String, password: String) {
        val application = login(email, password)
        if (repository.deleteById(application.id)) {
            domainEventPublisher.publish(AccountInformationDeletedEvent(application.id, application.email))
        }
    }
}
