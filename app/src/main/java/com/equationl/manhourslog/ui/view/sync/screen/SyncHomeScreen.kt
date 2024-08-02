package com.equationl.manhourslog.ui.view.sync.screen

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.equationl.manhourslog.constants.Route
import com.equationl.manhourslog.constants.ShareElementKey
import com.equationl.manhourslog.ui.view.LocalNavController
import com.equationl.manhourslog.ui.view.LocalShareAnimatedContentScope
import com.equationl.manhourslog.ui.view.LocalSharedTransitionScope
import com.equationl.manhourslog.ui.widget.PulsatingCircles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncHomeScreen() {
    val navController = LocalNavController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Sync Data")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "back")
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SyncContent(
                onClickSend = {
                    navController.navigate(Route.SYNC_CLIENT)
                },
                onCliReceive = {
                    navController.navigate(Route.SYNC_SERVER)
                }
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SyncContent(
    onClickSend: () -> Unit,
    onCliReceive: () -> Unit
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedContentScope = LocalShareAnimatedContentScope.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        with(sharedTransitionScope) {
            Card(
                onClick = onClickSend,
                shape = CircleShape,
                colors = CardDefaults.cardColors().copy(containerColor = Color.Transparent),
                modifier = Modifier
                    .size(200.dp)
                    .sharedElement(
                        sharedTransitionScope.rememberSharedContentState(key = ShareElementKey.SYNC_CLIENT),
                        animatedVisibilityScope = animatedContentScope
                    )
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    PulsatingCircles(
                        text = "Send",
                        isAnimation = false,
                        onClick = onClickSend,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }


            Spacer(modifier = Modifier.height(32.dp))

            Card(
                onClick = onCliReceive,
                shape = CircleShape,
                colors = CardDefaults.cardColors().copy(containerColor = Color.Transparent),
                modifier = Modifier
                    .size(200.dp)
                    .sharedElement(
                        sharedTransitionScope.rememberSharedContentState(key = ShareElementKey.SYNC_SERVER),
                        animatedVisibilityScope = animatedContentScope
                    )
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    PulsatingCircles(
                        text = "Receive",
                        isAnimation = false,
                        onClick = onCliReceive,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}