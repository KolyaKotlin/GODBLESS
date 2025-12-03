package com.example.godbless.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.godbless.R
import java.text.SimpleDateFormat
import java.util.*

enum class DateInputMode {
    DAYS_COUNT,      // Просто количество дней
    DATE_RANGE       // Дата выработки + дата окончания
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInputSection(
    onDaysCalculated: (Int) -> Unit,
    onProductionDateChanged: (Date?) -> Unit = {},
    onExpiryDateChanged: (Date?) -> Unit = {}
) {
    var inputMode by remember { mutableStateOf(DateInputMode.DAYS_COUNT) }
    var daysCount by remember { mutableStateOf("7") }
    var productionDate by remember { mutableStateOf<Date?>(null) }
    var expiryDate by remember { mutableStateOf<Date?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    // Календарь состояния
    var showProductionDatePicker by remember { mutableStateOf(false) }
    var showExpiryDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Переключатель режимов
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = inputMode == DateInputMode.DAYS_COUNT,
                onClick = { inputMode = DateInputMode.DAYS_COUNT },
                label = { Text(stringResource(R.string.num_days)) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = inputMode == DateInputMode.DATE_RANGE,
                onClick = { inputMode = DateInputMode.DATE_RANGE },
                label = { Text(stringResource(R.string.dates)) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (inputMode) {
            DateInputMode.DAYS_COUNT -> {
                // Ввод количества дней с улучшенной маской
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = daysCount,
                        onValueChange = {
                            // Ограничиваем ввод только цифрами и максимум 4 символа
                            if ((it.all { char -> char.isDigit() } && it.length <= 4) || it.isEmpty()) {
                                daysCount = it
                                val days = it.toIntOrNull() ?: 0
                                onDaysCalculated(days)
                            }
                        },
                        label = { Text(stringResource(R.string.expiry_days)) },
                        placeholder = { Text("7") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        supportingText = {
                            val days = daysCount.toIntOrNull() ?: 0
                            val expiryDate = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, days)
                            }
                            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            if (days > 0) {
                                Text(
                                    stringResource(R.string.expiry_date_calculated, dateFormat.format(expiryDate.time)),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        singleLine = true
                    )
                }
            }

            DateInputMode.DATE_RANGE -> {
                // Выбор диапазона дат
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Дата выработки
                    OutlinedCard(
                        onClick = { showProductionDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    stringResource(R.string.production_date_short),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    productionDate?.let { dateFormat.format(it) } ?: stringResource(R.string.not_selected),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Дата окончания срока годности
                    OutlinedCard(
                        onClick = { showExpiryDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    stringResource(R.string.expiry_until),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    expiryDate?.let { dateFormat.format(it) } ?: stringResource(R.string.not_selected),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Расчет дней
                    if (productionDate != null && expiryDate != null) {
                        val days = ((expiryDate!!.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()
                        LaunchedEffect(productionDate, expiryDate) {
                            onDaysCalculated(days)
                            onProductionDateChanged(productionDate)
                            onExpiryDateChanged(expiryDate)
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                stringResource(R.string.days_remaining, days),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }

    // Material3 Date Picker диалоги с календарем
    if (showProductionDatePicker) {
        MaterialDatePickerDialog(
            title = stringResource(R.string.production_date_short),
            onDismiss = { showProductionDatePicker = false },
            onDateSelected = { date ->
                productionDate = date
                showProductionDatePicker = false
            }
        )
    }

    if (showExpiryDatePicker) {
        MaterialDatePickerDialog(
            title = stringResource(R.string.expiry_until),
            onDismiss = { showExpiryDatePicker = false },
            onDateSelected = { date ->
                expiryDate = date
                showExpiryDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDatePickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onDateSelected: (Date) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(Date(millis))
                    }
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = title,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                )
            }
        )
    }
}
