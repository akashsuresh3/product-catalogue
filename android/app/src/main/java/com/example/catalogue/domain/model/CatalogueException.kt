package com.example.catalogue.domain.model

/** Typed failures so the UI never has to know about HTTP or IO details. */
sealed class CatalogueException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause) {
    class NetworkUnavailable(cause: Throwable? = null) : CatalogueException("Network unavailable", cause)
    class NotFound : CatalogueException("Not found")
    class Server(val code: Int) : CatalogueException("Server error $code")
    /** [fieldErrors] maps a request field name (e.g. "email") to a message. */
    class Validation(val fieldErrors: Map<String, String>) : CatalogueException("Validation failed")
    class Unknown(cause: Throwable? = null) : CatalogueException("Unexpected error", cause)
}
