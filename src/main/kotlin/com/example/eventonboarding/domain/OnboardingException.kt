package com.example.eventonboarding.domain

/** No application exists for the supplied id. */
class ApplicationNotFoundException(id: String) :
    RuntimeException("No onboarding application found for id '$id'")

/** A step was attempted out of order, or with the wrong token. */
class InvalidStepException(message: String) : RuntimeException(message)
