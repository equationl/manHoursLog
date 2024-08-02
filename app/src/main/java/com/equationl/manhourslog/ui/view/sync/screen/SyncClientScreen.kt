package com.equationl.manhourslog.ui.view.sync.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equationl.manhourslog.constants.ShareElementKey
import com.equationl.manhourslog.ui.view.LocalNavController
import com.equationl.manhourslog.ui.view.LocalShareAnimatedContentScope
import com.equationl.manhourslog.ui.view.LocalSharedTransitionScope
import com.equationl.manhourslog.ui.view.sync.viewmodel.SyncClientViewModel
import com.equationl.manhourslog.ui.widget.CommonConfirmDialog
import com.equationl.manhourslog.ui.widget.PulsatingCircles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncClientScreen(
    viewModel: SyncClientViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var isShowExitConfirmDialog by remember { mutableStateOf(false) }

    BackHandler(!isShowExitConfirmDialog && state.isClientConnected) {
        isShowExitConfirmDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Sync Data (Client)")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.isClientConnected) {
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
//                    TextButton(
//                        onClick = {
//                            isShowExitConfirmDialog = true
//                        }
//                    ) {
//                        Text(text = "Stop")
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
            if (!state.isClientConnected) {
                ClientConnectContent(
                    onClickConnect = viewModel::clientConnect
                )
            }

            SyncClientContent(
                isConnect = state.isClientConnected,
                bottomTip = state.bottomTip,
                currentTitle = state.currentTitle,
                onClickSend = viewModel::onClickSend,
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
private fun SyncClientContent(
    isConnect: Boolean,
    bottomTip: String? = null,
    currentTitle: String? = null,
    onClickSend: () -> Unit,
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
                        text = currentTitle ?: "Send",
                        isAnimation = isConnect,
                        onClick = onClickSend,
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

@Composable
private fun ClientConnectContent(
    onClickConnect: (address: String) -> Unit
) {
    var value by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (it.replace(".", "").isDigitsOnly() && it.length <= 15) {
                    value = it
                }
            },
            label = {
                Text(text = "Input Receive Device Ip Address")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                onClickConnect(value)
            }
        ) {
            Text(text = "Connect")
        }
    }
}