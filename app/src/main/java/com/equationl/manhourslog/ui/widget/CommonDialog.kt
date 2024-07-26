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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    initValue: StatisticsShowRange,
    onFilterDate: (range: StatisticsShowRange) -> Unit,
    onDismissRequest: () -> Unit
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initValue.start,
        initialSelectedEndDateMillis = initValue.end,
    )

    state.setSelection(initValue.start, initValue.end)

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(text = "Cancel")
            }

            TextButton(
                onClick = {
                    state.setSelection(0L, System.currentTimeMillis())
                }
            ) {
                Text(text = "All")
            }

            TextButton(
                onClick = {
                    onDismissRequest()
                    onFilterDate(StatisticsShowRange(state.selectedStartDateMillis ?: 0L, state.selectedEndDateMillis ?: ((state.selectedStartDateMillis ?: 0L) + 1)))
                }
            ) {
                Text(text = "Confirm")
            }
        }
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

@Composable
fun CommonConfirmDialog(
    title: String,
    content: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Sure")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Cancel")
            }
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = content)
        }
    )
}