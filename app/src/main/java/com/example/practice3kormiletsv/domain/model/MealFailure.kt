package com.example.practice3kormiletsv.domain.model

enum class FailureReason { NETWORK, TIMEOUT, SERVER, INVALID_DATA, NOT_FOUND, UNKNOWN }

class MealFailure(val reason: FailureReason, cause: Throwable? = null) : Exception(cause)
