package com.equationl.manhourslog.ui.view.sync.screen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.TextButton
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
import com.equationl.manhourslog.ui.view.LocalNavController
import com.equationl.manhourslog.ui.view.sync.state.SyncDeviceType
import com.equationl.manhourslog.ui.view.sync.viewmodel.SyncViewModel
import com.equationl.manhourslog.ui.widget.PulsatingCircles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    viewModel: SyncViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Sync Data")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.stop()
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "back")
                    }
                },
                actions = {
                    if (state.syncDeviceType != SyncDeviceType.Wait) {
                        TextButton(onClick = viewModel::stop) {
                            Text(text = "Stop")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.syncDeviceType == SyncDeviceType.Send && !state.isClientConnected) {
                ClientConnectContent(
                    onClickConnect = viewModel::clientConnect
                )
            }

            SyncContent(
                syncDeviceType = state.syncDeviceType,
                bottomTip = state.bottomTip,
                currentTitle = state.currentTitle,
                onClickSend = viewModel::onClickSend,
                onCliReceive = viewModel::onClickReceive
            )
        }
    }
}

@Composable
private fun SyncContent(
    syncDeviceType: SyncDeviceType,
    bottomTip: String? = null,
    currentTitle: String? = null,
    onClickSend: () -> Unit,
    onCliReceive: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(visible = syncDeviceType != SyncDeviceType.Receive) {
            Card(
                onClick = onClickSend,
                shape = CircleShape,
                modifier = Modifier.size(200.dp),
                colors = CardDefaults.cardColors().copy(containerColor = Color.Transparent)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    PulsatingCircles(
                        text = currentTitle ?: "Send",
                        isAnimation = syncDeviceType == SyncDeviceType.Send,
                        onClick = onClickSend,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(visible = syncDeviceType != SyncDeviceType.Send) {
            Card(
                onClick = onCliReceive,
                shape = CircleShape,
                modifier = Modifier.size(200.dp),
                colors = CardDefaults.cardColors().copy(containerColor = Color.Transparent)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    PulsatingCircles(
                        text = currentTitle ?: "Receive",
                        isAnimation = syncDeviceType == SyncDeviceType.Receive,
                        onClick = onCliReceive,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        if (bottomTip != null) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = bottomTip)
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