package com.equationl.manhourslog.ui.view.list.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.PieChartOutline
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equationl.manhourslog.model.StaticsScreenModel
import com.equationl.manhourslog.ui.view.LocalNavController
import com.equationl.manhourslog.ui.view.list.state.StatisticsShowScale
import com.equationl.manhourslog.ui.view.list.state.StatisticsShowType
import com.equationl.manhourslog.ui.view.list.state.StatisticsState
import com.equationl.manhourslog.ui.view.list.state.getNext
import com.equationl.manhourslog.ui.view.list.viewmodel.StatisticsViewModel
import com.equationl.manhourslog.ui.widget.ListEmptyContent
import com.equationl.manhourslog.ui.widget.LoadingContent
import com.equationl.manhourslog.util.DateTimeUtil.formatDateTime
import com.equationl.manhourslog.util.DateTimeUtil.formatTime

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                showType = state.showType
            )
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
                    state,
                    onChangeScale = {
                        viewModel.changeShowScale(it)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    showType: StatisticsShowType
) {
    val navController = LocalNavController.current
    TopAppBar(
        title = {
            Text(text = "Statistics")
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
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Outlined.DateRange, contentDescription = "date filter")
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    if (showType == StatisticsShowType.List) Icons.Outlined.PieChartOutline else Icons.AutoMirrored.Outlined.List,
                    contentDescription = "chart"
                )
            }
        }
    )
}

@Composable
private fun HomeContent(
    state: StatisticsState,
    onChangeScale: (newScale: StatisticsShowScale) -> Unit,
) {
    when (state.showType) {
        StatisticsShowType.List ->
            ListContent(
                state,
                onChangeScale = onChangeScale
            )
        StatisticsShowType.Chart -> ChartContent()
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ListContent(
    state: StatisticsState,
    onChangeScale: (newScale: StatisticsShowScale) -> Unit,
) {

    val dataList = state.dataList

    if (dataList.isEmpty()) {
        ListEmptyContent("Data is Empty")
    }
    else {
        Column(
            Modifier
                .fillMaxSize()
        ) {
            LazyColumn {
                item(key = "headerFilter") {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        SingleChoiceSegmentedButtonRow {
                            StatisticsShowScale.entries.forEachIndexed { index, statisticsShowScale ->
                                SegmentedButton(selected = statisticsShowScale == state.showScale, onClick = { onChangeScale(statisticsShowScale) }, shape = SegmentedButtonDefaults.itemShape(index = index, count = StatisticsShowScale.entries.size),) {
                                    Text(text = statisticsShowScale.name)
                                }
                            }
                        }

                    }
                }


                var lastTitle = ""

                dataList.forEach { item ->
                    if (item.headerTitle != lastTitle) {
                        stickyHeader {
                            TodoListGroupHeader(leftText = item.headerTitle, rightText = item.headerTotalTime.formatTime())
                        }
                        lastTitle = item.headerTitle
                    }
                    item(key = item.id) {
                        ListItem(
                            item,
                            currentScale = state.showScale,
                            onClickCard = {
                                if (state.showScale != StatisticsShowScale.Day) {
                                    onChangeScale(state.showScale.getNext())
                                }
                            }
                        )
                    }

                }
            }
        }
    }
}

@Composable
private fun ChartContent() {
    // TODO
}

@Composable
private fun ListItem(
    item: StaticsScreenModel,
    currentScale: StatisticsShowScale,
    onClickCard: () -> Unit
) {
    Card(
        onClick = onClickCard,
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                if (currentScale == StatisticsShowScale.Day) {
                    Text(text = "Start Time: ${item.startTime.formatDateTime()}")
                    Text(text = "End Time: ${item.endTime.formatDateTime()}")
                }
                else {
                    Text(text = "Date: ${item.startTime.formatDateTime(format = if (currentScale == StatisticsShowScale.Month) "yyyy-MM-dd" else "yyyy-MM")}")
                }
            }

            Text(text = item.totalTime.formatTime(), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun TodoListGroupHeader(leftText: String, rightText: String = "") {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp)
    ) {
        Text(text = leftText)
        Text(text = rightText)
    }
}