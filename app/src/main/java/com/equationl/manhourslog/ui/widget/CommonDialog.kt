package com.equationl.manhourslog.ui.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NoteAdd
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            button("All") {
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

@Composable
fun ShowNoteDialog(
    showState: MaterialDialogState,
    initValue: String,
    onChangeValue: (value: String) -> Unit
) {
    var isEdit by remember(initValue) { mutableStateOf(false) }
    var showValue by remember(initValue) { mutableStateOf(initValue) }

    MaterialDialog(
        dialogState = showState,
        buttons = {
            positiveButton(if (isEdit) "Save" else "Edit") {
                if (isEdit) {
                    onChangeValue(showValue)
                }
                isEdit = !isEdit
            }
            negativeButton("OK") {
                showState.hide()
            }
        },
        backgroundColor = MaterialTheme.colorScheme.background,
        autoDismiss = false
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .heightIn(min = 180.dp)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (isEdit) {
                OutlinedTextField(
                    value = showValue,
                    label = {
                        Text(text = "Edit note...")
                    },
                    singleLine = true,
                    onValueChange = {
                        showValue = it
                    },
                )
            }
            else {
                if (showValue.isBlank()) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.NoteAdd, contentDescription = "Empty")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "No note append now")
                }
                else {
                    Text(text = showValue)
                }
            }
        }
    }
}