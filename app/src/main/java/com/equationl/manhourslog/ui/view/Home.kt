package com.equationl.manhourslog.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.equationl.manhourslog.constants.Route
import com.equationl.manhourslog.ui.view.home.screen.HomeScreen

val LocalNavController = staticCompositionLocalOf<NavHostController> { error("No NavController provided") }

@Composable
fun HomeNavHost() {
    CompositionLocalProvider(
        LocalNavController provides rememberNavController(),
    ) {
        NavHost(navController = LocalNavController.current, Route.HOME) {
            composable(Route.HOME) {
                HomeScreen()
            }
        }
    }
}