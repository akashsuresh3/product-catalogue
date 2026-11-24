package com.example.catalogue.ui.navigation

import kotlinx.serialization.Serializable

@Serializable data object HomeRoute
@Serializable data object SearchRoute
@Serializable data class CategoryRoute(val name: String)
@Serializable data class ProductRoute(val id: Int)
@Serializable data class EnquiryRoute(val productId: Int)

/**
 * SavedStateHandle keys. With type-safe navigation the keys are the route property names above,
 * so these MUST stay in sync with them. Reading plain keys (rather than toRoute<T>()) keeps the
 * ViewModels unit-testable on the JVM without an Android Bundle.
 */
object NavArgs {
    const val CATEGORY_NAME = "name"
    const val PRODUCT_ID = "id"
    const val ENQUIRY_PRODUCT_ID = "productId"
}
