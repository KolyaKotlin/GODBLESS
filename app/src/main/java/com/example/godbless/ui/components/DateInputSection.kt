package com.example.godbless.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale("ru")) }

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
                label = { Text("Кол-во дней") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = inputMode == DateInputMode.DATE_RANGE,
                onClick = { inputMode = DateInputMode.DATE_RANGE },
                label = { Text("Даты") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (inputMode) {
            DateInputMode.DAYS_COUNT -> {
                // Ввод количества дней
                OutlinedTextField(
                    value = daysCount,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                            daysCount = it
                            val days = it.toIntOrNull() ?: 0
                            onDaysCalculated(days)
                        }
                    },
                    label = { Text("Срок годности (дней)") },
                    placeholder = { Text("7") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
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
                                    "Дата выработки",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    productionDate?.let { dateFormat.format(it) } ?: "Не выбрана",
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
                                    "Срок годности до",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    expiryDate?.let { dateFormat.format(it) } ?: "Не выбрана",
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
                                "Осталось: $days дней",
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

    // Date Picker диалоги (упрощенная версия - используем просто AlertDialog с текстовым вводом)
    // В реальной версии лучше использовать материал DatePicker, но это требует API 26+
    if (showProductionDatePicker) {
        DatePickerDialog(
            title = "Дата выработки",
            onDismiss = { showProductionDatePicker = false },
            onDateSelected = { date ->
                productionDate = date
                showProductionDatePicker = false
            }
        )
    }

    if (showExpiryDatePicker) {
        DatePickerDialog(
            title = "Срок годности до",
            onDismiss = { showExpiryDatePicker = false },
            onDateSelected = { date ->
                expiryDate = date
                showExpiryDatePicker = false
            }
        )
    }
}

@Composable
fun DatePickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onDateSelected: (Date) -> Unit
) {
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = day,
                    onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) day = it },
                    label = { Text("День") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = month,
                    onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) month = it },
                    label = { Text("Месяц") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) year = it },
                    label = { Text("Год") },
                    modifier = Modifier.weight(1.2f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val calendar = Calendar.getInstance()
                        calendar.set(
                            year.toInt(),
                            month.toInt() - 1,
                            day.toInt()
                        )
                        onDateSelected(calendar.time)
                    } catch (e: Exception) {
                        // Handle invalid date
                    }
                },
                enabled = day.isNotBlank() && month.isNotBlank() && year.isNotBlank()
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
