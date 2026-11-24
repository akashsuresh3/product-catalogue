package com.example.catalogue.domain.model

/** Client-side validation, mirrored on the backend. Pure functions, easy to unit test. */
object EnquiryValidator {
    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private val phoneChars = Regex("^[+\\d\\s()-]+$")
    const val MESSAGE_MAX = 1000

    fun validateName(v: String): String? =
        if (v.isBlank()) "Please enter your name" else null

    fun validateEmail(v: String): String? = when {
        v.isBlank() -> "Please enter your email"
        !emailRegex.matches(v.trim()) -> "Enter a valid email address"
        else -> null
    }

    fun validatePhone(v: String): String? =
        if (v.isBlank() || (phoneChars.matches(v.trim()) && v.count { it.isDigit() } in 8..15)) null
        else "Enter a valid phone number"

    fun validateMessage(v: String): String? = when {
        v.isBlank() -> "Please tell us what you need"
        v.length > MESSAGE_MAX -> "Message is too long"
        else -> null
    }
}
