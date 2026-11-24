package com.example.catalogue.ui.mvi

import com.example.catalogue.domain.model.CatalogueException

fun Throwable.toErrorKind(): ErrorKind = when (this) {
    is CatalogueException.NetworkUnavailable -> ErrorKind.Network
    is CatalogueException.NotFound -> ErrorKind.NotFound
    is CatalogueException.Server -> ErrorKind.Server
    else -> ErrorKind.Unknown
}
