package com.equationl.manhourslog.ui.view.sync.screen

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equationl.manhourslog.constants.ShareElementKey
import com.equationl.manhourslog.ui.view.LocalNavController
import com.equationl.manhourslog.ui.view.LocalShareAnimatedContentScope
import com.equationl.manhourslog.ui.view.LocalSharedTransitionScope
import com.equationl.manhourslog.ui.view.sync.viewmodel.SyncServerViewModel
import com.equationl.manhourslog.ui.widget.CommonConfirmDialog
import com.equationl.manhourslog.ui.widget.PulsatingCircles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncServerScreen(
    viewModel: SyncServerViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var isShowExitConfirmDialog by remember { mutableStateOf(false) }

    BackHandler(!isShowExitConfirmDialog && state.isServerConnected) {
        isShowExitConfirmDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Sync Data (Server)")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.isServerConnected) {
                                isShowExitConfirmDialog = true
                            }
                            else {
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "back")
                    }
                },
//                actions = {
//                    if (state.syncDeviceType != SyncDeviceType.Wait) {
//                        TextButton(
//                            onClick = {
//                                isShowExitConfirmDialog = true
//                            }
//                        ) {
//                            Text(text = "Stop")
//                        }
//                    }
//                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SyncServerContent(
                isConnect = state.isServerConnected,
                bottomTip = state.bottomTip,
                currentTitle = state.currentTitle,
                onCliReceive = viewModel::onClickReceive
            )
        }
    }

    if (isShowExitConfirmDialog) {
        CommonConfirmDialog(
            title = "Tip",
            content = "Are you sure to stop? If sync is in progress, Data will be destroyed",
            onConfirm = {
                viewModel.stop()
                isShowExitConfirmDialog = false
                navController.popBackStack()
            },
            onDismissRequest = {
                isShowExitConfirmDialog = false
            }
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SyncServerContent(
    isConnect: Boolean,
    bottomTip: String? = null,
    currentTitle: String? = null,
    onCliReceive: () -> Unit
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedContentScope = LocalShareAnimatedContentScope.current

    with(sharedTransitionScope) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                        text = currentTitle ?: "Receive",
                        isAnimation = isConnect,
                        onClick = onCliReceive,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            if (bottomTip != null) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = bottomTip, modifier = Modifier.padding(8.dp))
            }
        }
    }
}