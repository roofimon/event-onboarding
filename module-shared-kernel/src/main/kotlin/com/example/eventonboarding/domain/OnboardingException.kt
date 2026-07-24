package com.example.eventonboarding.domain

/** No application exists for the supplied id. */
class ApplicationNotFoundException(id: String) :
    RuntimeException("No onboarding application found for id '$id'")

/** A step was attempted out of order, or with the wrong token. */
class InvalidStepException(message: String) : RuntimeException(message)

/** Login was attempted with an unknown email or a password that did not match. */
class InvalidCredentialsException : RuntimeException("Invalid email or password")
