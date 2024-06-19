package com.equationl.manhourslog.ui.widget

import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import com.equationl.manhourslog.ui.view.list.state.StatisticsShowRange
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeRangePickerDialog(
    showState: MaterialDialogState,
    initValue: StatisticsShowRange,
    onFilterDate: (range: StatisticsShowRange) -> Unit
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initValue.start,
        initialSelectedEndDateMillis = initValue.end,
    )

    state.setSelection(initValue.start, initValue.end)

    MaterialDialog(
        dialogState = showState,
        buttons = {
            positiveButton("Confirm") {
                onFilterDate(StatisticsShowRange(state.selectedStartDateMillis ?: 0L, state.selectedEndDateMillis ?: 0L))
            }
            negativeButton("Cancel")
            button("Reset") {
                // state.setSelection(DateTimeUtil.getWeeOfCurrentMonth(), DateTimeUtil.getCurrentMonthEnd())
                state.setSelection(0L, System.currentTimeMillis())
            }
        },
        backgroundColor = MaterialTheme.colorScheme.background
    ) {
        DateRangePicker(
            state = state,
            headline = null,
            title = null
        )
    }
}