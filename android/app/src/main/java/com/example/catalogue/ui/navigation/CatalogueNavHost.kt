package com.example.catalogue.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.catalogue.ui.category.CategoryScreen
import com.example.catalogue.ui.detail.DetailScreen
import com.example.catalogue.ui.enquiry.EnquiryScreen
import com.example.catalogue.ui.home.HomeScreen
import com.example.catalogue.ui.search.SearchScreen

/** The only place that knows how screens connect. Screens just call the lambdas they are given. */
@Composable
fun CatalogueNavHost(navController: NavHostController = rememberNavController()) {
    val openProduct = { id: Int -> navController.navigate(ProductRoute(id)) }
    val back = { navController.popBackStack(); Unit }

    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                onOpenProduct = openProduct,
                onOpenCategory = { navController.navigate(CategoryRoute(it)) },
                onOpenSearch = { navController.navigate(SearchRoute) },
            )
        }
        composable<SearchRoute> {
            SearchScreen(onBack = back, onOpenProduct = openProduct)
        }
        composable<CategoryRoute> {
            CategoryScreen(onBack = back, onOpenProduct = openProduct)
        }
        composable<ProductRoute> {
            DetailScreen(onBack = back, onEnquire = { navController.navigate(EnquiryRoute(it)) })
        }
        composable<EnquiryRoute> {
            EnquiryScreen(onClose = back)
        }
    }
}
