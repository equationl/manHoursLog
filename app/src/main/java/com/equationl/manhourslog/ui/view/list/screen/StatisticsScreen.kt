package com.equationl.manhourslog.ui.view.list.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.BackupTable
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.ImportContacts
import androidx.compose.material.icons.outlined.InsertChartOutlined
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equationl.manhourslog.model.StaticsScreenModel
import com.equationl.manhourslog.ui.view.LocalNavController
import com.equationl.manhourslog.ui.view.list.state.StatisticsShowRange
import com.equationl.manhourslog.ui.view.list.state.StatisticsShowScale
import com.equationl.manhourslog.ui.view.list.state.StatisticsShowType
import com.equationl.manhourslog.ui.view.list.state.StatisticsState
import com.equationl.manhourslog.ui.view.list.state.getNext
import com.equationl.manhourslog.ui.view.list.state.getNextRange
import com.equationl.manhourslog.ui.view.list.viewmodel.StatisticsViewModel
import com.equationl.manhourslog.ui.widget.DateTimeRangePickerDialog
import com.equationl.manhourslog.ui.widget.ListEmptyContent
import com.equationl.manhourslog.ui.widget.LoadingContent
import com.equationl.manhourslog.util.DateTimeUtil.formatDateTime
import com.equationl.manhourslog.util.DateTimeUtil.formatTime
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.launch
import me.bytebeats.views.charts.bar.BarChart
import me.bytebeats.views.charts.bar.BarChartData
import me.bytebeats.views.charts.bar.render.bar.SimpleBarDrawer
import me.bytebeats.views.charts.bar.render.label.SimpleLabelDrawer
import me.bytebeats.views.charts.bar.render.xaxis.SimpleXAxisDrawer
import me.bytebeats.views.charts.bar.render.yaxis.SimpleYAxisDrawer
import me.bytebeats.views.charts.simpleChartAnimation

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onExport(result, context)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onImport(result, context)
    }

    val isListScroll by remember{
        derivedStateOf {
            state.listState.firstVisibleItemIndex > 0
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                showType = state.showType,
                iniDateRangeValue = state.showRange,
                onFilterDateRange = viewModel::onFilterShowRange,
                onChangeShowType = {
                    viewModel.onChangeShowType(context)
                },
                onExport = {
                    val intent = viewModel.createNewDocumentIntent()
                    exportLauncher.launch(intent)
                },
                onImport = {
                    val intent = viewModel.createReadDocumentIntent()
                    importLauncher.launch(intent)
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = state.showType == StatisticsShowType.List && isListScroll,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            state.listState.animateScrollToItem(0)
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Outlined.ArrowUpward, contentDescription = "Back to top")
                }
            }
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
                    onChangeScale = viewModel::changeShowScale,
                    onClickDeleteItem = viewModel::onClickDeleteItem
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    showType: StatisticsShowType,
    iniDateRangeValue: StatisticsShowRange,
    onFilterDateRange: (value: StatisticsShowRange) -> Unit,
    onChangeShowType: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    val navController = LocalNavController.current
    val dialogState = rememberMaterialDialogState()
    var isExpandMenu by remember { mutableStateOf(false) }

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
            IconButton(
                onClick = {
                    dialogState.show()
                }
            ) {
                Icon(Icons.Outlined.DateRange, contentDescription = "date filter")
            }
            IconButton(onClick = onChangeShowType) {
                Icon(
                    if (showType == StatisticsShowType.List) Icons.Outlined.InsertChartOutlined else Icons.AutoMirrored.Outlined.List,
                    contentDescription = "show type"
                )
            }
            IconButton(
                onClick = {
                    isExpandMenu = true
                }
            ) {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = "more function"
                )
            }

            TopBarMoreFunction(
                expanded = isExpandMenu,
                onDismissRequest = { isExpandMenu = !isExpandMenu },
                onClickExport = {
                    isExpandMenu = false
                    onExport()
                },
                onClickImport = {
                    isExpandMenu = false
                    onImport()
                }
            )
        }
    )

    DateTimeRangePickerDialog(showState = dialogState, initValue = iniDateRangeValue, onFilterDate = onFilterDateRange)
}

@Composable
private fun TopBarMoreFunction(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onClickExport: () -> Unit,
    onClickImport: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
        DropdownMenuItem(
            text = {
                Text(text = "Export Data To .csv")
            },
            onClick = onClickExport,
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.BackupTable, contentDescription = "export")
            }
        )
        DropdownMenuItem(
            text = {
                Text(text = "Import Data From .csv")
            },
            onClick = onClickImport,
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.ImportContacts, contentDescription = "import")
            }
        )
    }

}

@Composable
private fun HomeContent(
    state: StatisticsState,
    onChangeScale: (newScale: StatisticsShowScale, newRange: StatisticsShowRange?) -> Unit,
    onClickDeleteItem: (id: Int) -> Unit
) {
    when (state.showType) {
        StatisticsShowType.List ->
            ListContent(
                state,
                onChangeScale = onChangeScale,
                onClickDeleteItem = onClickDeleteItem
            )
        StatisticsShowType.Chart -> ChartContent(
            state,
            onChangeScale = onChangeScale
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListContent(
    state: StatisticsState,
    onChangeScale: (newScale: StatisticsShowScale, newRange: StatisticsShowRange?) -> Unit,
    onClickDeleteItem: (id: Int) -> Unit
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
            LazyColumn(
                state = state.listState
            ) {
                item(key = "headerFilter") {
                    HeaderFilter(state, onChangeScale)
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
                        if (state.showScale == StatisticsShowScale.Day) {
                            SwipeAbleListItem(
                                item,
                                currentScale = state.showScale,
                                onClickDeleteItem = {
                                    onClickDeleteItem(item.id)
                                }
                            )
                        }
                        else {
                            ListItem(
                                item,
                                currentScale = state.showScale,
                                onClickCard = {
                                        onChangeScale(state.showScale.getNext(), state.showScale.getNextRange(item.startTime))
                                }
                            )
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun ChartContent(
    state: StatisticsState,
    onChangeScale: (newScale: StatisticsShowScale, newRange: StatisticsShowRange?) -> Unit
) {
    val dataList = state.dataList

    if (dataList.isEmpty()) {
        ListEmptyContent("Data is Empty")
    }
    else {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BarChartView(data = state.barChartData)
            }

//            Column(
//                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.Top,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                HeaderFilter(state = state, onChangeScale = onChangeScale)
//            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeaderFilter(
    state: StatisticsState,
    onChangeScale: (newScale: StatisticsShowScale, newRange: StatisticsShowRange?) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        SingleChoiceSegmentedButtonRow {
            StatisticsShowScale.entries.forEachIndexed { index, statisticsShowScale ->
                SegmentedButton(selected = statisticsShowScale == state.showScale, onClick = { onChangeScale(statisticsShowScale, null) }, shape = SegmentedButtonDefaults.itemShape(index = index, count = StatisticsShowScale.entries.size),) {
                    Text(text = statisticsShowScale.name)
                }
            }
        }

    }

    Spacer(modifier = Modifier.height(16.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeAbleListItem(
    item: StaticsScreenModel,
    currentScale: StatisticsShowScale,
    onClickDeleteItem: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onClickDeleteItem()
                true
            }
            else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Row(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.error),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Swipe To Delete", color = MaterialTheme.colorScheme.background)
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.background)
            }
        },
        content = {
            ListItem(item, currentScale, null)
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
    )
}

@Composable
private fun ListItem(
    item: StaticsScreenModel,
    currentScale: StatisticsShowScale,
    onClickCard: (() -> Unit)?
) {
    if (onClickCard == null) {
        Card(
            modifier = Modifier.padding(16.dp)
        ) {
            ListCardContent(item = item, currentScale = currentScale)
        }
    }
    else {
        Card(
            onClick = onClickCard,
            modifier = Modifier.padding(16.dp)
        ) {
            ListCardContent(item = item, currentScale = currentScale)
        }
    }
}

@Composable
private fun ListCardContent(
    item: StaticsScreenModel,
    currentScale: StatisticsShowScale,
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

@Composable
private fun BarChartView(data: List<BarChartData.Bar>) {
    if (data.isEmpty()) {
        ListEmptyContent(msg = "NO data")
    }
    else {
        val barChartData = BarChartData(bars = data)
        BarChart(
            barChartData = barChartData,
            modifier = Modifier.fillMaxSize(),
            animation = simpleChartAnimation(),
            barDrawer = SimpleBarDrawer(),
            xAxisDrawer = SimpleXAxisDrawer(),
            yAxisDrawer = SimpleYAxisDrawer(),
            labelDrawer = SimpleLabelDrawer(
                drawLocation = SimpleLabelDrawer.DrawLocation.Outside
            )
        )
    }
}