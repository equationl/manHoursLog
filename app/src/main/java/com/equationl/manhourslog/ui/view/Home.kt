@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.equationl.manhourslog.ui.view

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.equationl.manhourslog.constants.Route
import com.equationl.manhourslog.ui.view.home.screen.HomeScreen
import com.equationl.manhourslog.ui.view.list.screen.StatisticsScreen
import com.equationl.manhourslog.ui.view.sync.screen.SyncClientScreen
import com.equationl.manhourslog.ui.view.sync.screen.SyncHomeScreen
import com.equationl.manhourslog.ui.view.sync.screen.SyncServerScreen

val LocalNavController = staticCompositionLocalOf<NavHostController> { error("No NavController provided") }
val LocalSharedTransitionScope =
    staticCompositionLocalOf<SharedTransitionScope> { error("No SharedTransitionScope provided") }
val LocalShareAnimatedContentScope =
    staticCompositionLocalOf<AnimatedContentScope> { error("No AnimatedContentScope provided") }

@Composable
fun HomeNavHost(
    startDestination: String = Route.HOME
) {
    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalNavController provides rememberNavController(),
            LocalSharedTransitionScope provides this@SharedTransitionLayout
        ) {
            NavHost(navController = LocalNavController.current, startDestination) {
                composable(Route.HOME) {
                    HomeScreen()
                }
                composable(
                    Route.STATISTIC,
                    enterTransition = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(600))
                    },
                    exitTransition = null,
                    popExitTransition = {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(600))
                    },
                    popEnterTransition = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(600))
                    }
                ) {
                    StatisticsScreen()
                }
                composable(
                    Route.SYNC_HOME,
                    enterTransition = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(600))
                    },
                    exitTransition = null,
                    popExitTransition = {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(600))
                    },
                    popEnterTransition = null
                ) {
                    CompositionLocalProvider(LocalShareAnimatedContentScope provides this@composable) {
                        SyncHomeScreen()
                    }
                }
                composable(Route.SYNC_CLIENT) {
                    CompositionLocalProvider(LocalShareAnimatedContentScope provides this@composable) {
                        SyncClientScreen()
                    }
                }
                composable(Route.SYNC_SERVER) {
                    CompositionLocalProvider(LocalShareAnimatedContentScope provides this@composable) {
                        SyncServerScreen()
                    }
                }
            }
        }
    }
}