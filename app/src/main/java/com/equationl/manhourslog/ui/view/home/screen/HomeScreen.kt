package com.equationl.manhourslog.ui.view.home.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equationl.manhourslog.ui.view.home.state.HomeState
import com.equationl.manhourslog.ui.view.home.viewmodel.HomeViewModel
import com.equationl.manhourslog.ui.widget.LoadingContent
import com.equationl.manhourslog.util.Utils.formatTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                LoadingContent()
            }
            else {
                HomeContent(
                    state = state,
                    onToggleStart = viewModel::toggleStart
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
    TopAppBar(
        title = {
            Text(text = "Man Hour Log")
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Outlined.Menu, contentDescription = "menu")
            }
        }
    )
}

@Composable
private fun HomeContent(
    state: HomeState,
    onToggleStart: () -> Unit,
) {
    var currentTime by remember(key1 = state.logState) { mutableStateOf(-1L) }

    LaunchedEffect(key1 = state.logState) {
        withContext(Dispatchers.IO) {
            while (state.logState.startTime != null) {
                val current = System.currentTimeMillis()
                val startTime = state.logState.startTime

                currentTime = current - startTime

                delay(1000)
            }
        }
    }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(text = "今日已工作 ${(state.totalManHours + currentTime.coerceAtLeast(0L)).formatTime()}")
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            onClick = onToggleStart,
            shape = CircleShape,
            modifier = Modifier.size(200.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(text = if (state.logState.isStart) "停止" else "开始")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (currentTime >= 0L) {
            Text(text = "本次启动已工作 ${currentTime.formatTime()}")
        }
    }
}