package com.snapcompany.snapsafe.views

import android.icu.util.Calendar
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.snapcompany.snapsafe.R
import com.snapcompany.snapsafe.models.Orders
import com.snapcompany.snapsafe.utilities.DateUtilities
import com.snapcompany.snapsafe.utilities.Firestore
import com.snapcompany.snapsafe.utilities.GateData
import com.snapcompany.snapsafe.utilities.HistoryData

@Composable
fun GateHistory(
    gateData: GateData = GateData(),
    navController: NavController,
    firestore: Firestore
) {


    val dateUtilities = DateUtilities()

    val dateArray = dateUtilities.millisToDate(System.currentTimeMillis(), false)

    var fromTimeButtonText by remember { mutableStateOf("12:00 a.m.") }
    var toTimeButtonText by remember { mutableStateOf("11:59 p.m.") }
    var dateButtonText by remember {
        mutableStateOf(
            "${dateArray[2]} ${
                dateUtilities.monthNumberToName(
                    dateArray[1]
                )
            } ${dateArray[0]}"
        )
    }
    var lastYearMonthSelected by remember { mutableStateOf("") }

    val historyDateList = remember { mutableStateListOf<HistoryData>() }
    var loadingHistory by remember { mutableStateOf(false) }

    fun fillHistoryList(historyResult: List<HistoryData>) {
        historyResult.forEach {
            historyDateList.add(
                HistoryData(
                    user = it.user,
                    order = it.order,
                    time = dateUtilities.millisToLocalTime(it.time.toLong()),
                    year = it.year,
                    month = it.month,
                    day = it.day,
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        loadingHistory = true
        firestore.getHistoryYearMonth(
            deviceId = gateData.gateId,
            year = dateArray[0],
            month = dateArray[1]
        ) { historyResult ->

            lastYearMonthSelected = "${dateArray[0]}-${dateArray[1]}"
            historyDateList.clear()
            fillHistoryList(historyResult)
            loadingHistory = false

        }
    }

    GateHistoryPreview(
        gateData = gateData,
        loadingHistory = loadingHistory,
        fromTimeButtonText = fromTimeButtonText,
        toTimeButtonText = toTimeButtonText,
        dateButtonText = dateButtonText,
        historyResult = historyDateList,
        onFromTimeSelected = {
            fromTimeButtonText = it
        },
        onToTimeSelected = {
            toTimeButtonText = it
        },
        onDateSelected = { dateMillis ->
            val date = dateUtilities.millisToDate(dateMillis, true)
            val year = date[0]
            val month = date[1]
            dateButtonText = "${date[2]} ${dateUtilities.monthNumberToName(date[1])} ${date[0]}"

            if(lastYearMonthSelected != "$year-$month") {
                loadingHistory = true
                firestore.getHistoryYearMonth(
                    deviceId = gateData.gateId,
                    year = year,
                    month = month
                ) { historyResult ->
                    lastYearMonthSelected = "$year-$month"
                    historyDateList.clear()
                    fillHistoryList(historyResult)
                    loadingHistory = false
                }
            }
        },
        onBack = {
            navController.navigateUp()
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun GateHistoryPreview(
    gateData: GateData = GateData(),
    loadingHistory: Boolean = false,
    fromTimeButtonText: String = "12:00 a.m.",
    toTimeButtonText: String = "11:59 a.m.",
    dateButtonText: String = "15 nov. 2024",
    historyResult: List<HistoryData> = listOf(),
    onFromTimeSelected: (String) -> Unit = {},
    onToTimeSelected: (String) -> Unit = {},
    onDateSelected: (Long) -> Unit = { _ -> },
    onBack: () -> Unit = {}
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showFromTimePickerDialog by remember { mutableStateOf(false) }
    var showToTimePickerDialog by remember { mutableStateOf(false) }
    val dateUtilities = DateUtilities()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Historial") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            painterResource(id = R.drawable.arrow_back),
                            contentDescription = "back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                if (historyResult.none { it.day == dateButtonText.split(" ")[0]}) {
                    Text(text = "No hay datos en este día",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth())
                }
                else {
                    AnimatedVisibility(loadingHistory) {
                        Text(text = "Cargando datos del día",
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth())
                    }
                    AnimatedVisibility(visible = !loadingHistory, enter = fadeIn()) {
                        Card {
                            historyResult.filter { it.day == dateButtonText.split(" ")[0] }
                                .filter {
                                    val fromTime =  dateUtilities.convert12to24Hour(fromTimeButtonText)
                                    val toTime = dateUtilities.convert12to24Hour(toTimeButtonText)
                                    val time = dateUtilities.convert12to24Hour(it.time)
                                    val result =  time in fromTime..toTime
                                    result
                                }
                                .sortedBy { it.time }
                                .forEach {
                                HistoryRow(
                                    user = it.user,
                                    action = it.order,
                                    time = it.time
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }

                        }
                    }
                }
            }

            Column(
                Modifier.padding(vertical = 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(text = "Fecha:")
                    Button(
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            showDatePickerDialog = true
                        }) {
                        Text(text = dateButtonText)
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(text = "De:")
                    Button(
                        shape = RoundedCornerShape(8.dp),
                        onClick = { showFromTimePickerDialog = true }) {
                        Text(text = fromTimeButtonText)
                    }
                    Text(text = "a:")
                    Button(
                        shape = RoundedCornerShape(8.dp),
                        onClick = { showToTimePickerDialog = true }) {
                        Text(text = toTimeButtonText)
                    }

                }
            }

        }

        if(showFromTimePickerDialog){
            DialWithDialog(
                onConfirm = {
                    showFromTimePickerDialog = false
                    var hour = if(it.hour < 10) "0${it.hour}" else it.hour.toString()
                    val minute = if(it.minute < 10) "0${it.minute}" else it.minute.toString()
                    if(it.isAfternoon) {
                        hour = (it.hour - 12).toString()
                        if(hour.toInt() < 10) hour = "0$hour"

                    }
                    if(hour == "00") hour = "12"
                    onFromTimeSelected(hour + ":" + minute + if(it.isAfternoon) " p.m." else " a.m."  )
                },
                onDismiss = {
                    showFromTimePickerDialog = false
                }
            )
        }

        if(showToTimePickerDialog){
            DialWithDialog(
                onConfirm = {
                    showToTimePickerDialog = false
                    var hour = if(it.hour < 10) "0${it.hour}" else it.hour.toString()
                    val minute = if(it.minute < 10) "0${it.minute}" else it.minute.toString()
                    if(it.isAfternoon) {
                        hour = (it.hour - 12).toString()
                        if(hour.toInt() < 10) hour = "0$hour"
                    }
                    if(hour == "00") hour = "12"
                    onToTimeSelected(hour + ":" + minute + if(it.isAfternoon) " p.m." else " a.m."  )

                },
                onDismiss = {
                    showToTimePickerDialog = false
                }
            )
        }


        if (showDatePickerDialog) {
            DatePickerModal(
                onDateSelected = { date ->
                    showDatePickerDialog = false
                    if (date != null) onDateSelected(date)
                    else Log.e("DatePicker", "Date is null")
                },
                onDismiss = {
                    showDatePickerDialog = false
                }
            )
        }

    }
}

@Composable
@Preview
fun HistoryRow(
    user: String = "Usuario",
    action: String = Orders.STOP.name,
    time: String = "12:30p.m."
) {


    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = user,
                    fontSize = 14.sp
                )
                Text(text = time)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HistoryActionBox(action)
        }

    }

}

@Composable
@Preview()
fun HistoryActionBox(
    order: String = Orders.STOP.name,
) {
    var text = ""
    var color = Color.Black

    when (order) {

        Orders.OPEN.name -> {
            text = "Abrir"
            color = Color.Green
        }

        Orders.CLOSE.name -> {
            text = "Cerrar"
            color = Color.Blue
        }

        Orders.STOP.name -> {
            text = "Detener"
            color = Color.Red
        }

        else -> {

        }
    }

    Box(
        modifier = Modifier
            .background(color = color, shape = RoundedCornerShape(3.dp))
    ) {

        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialWithDialog(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = false,
    )

    TimePickerDialog(
        onDismiss = { onDismiss() },
        onConfirm = { onConfirm(timePickerState) }
    ) {
        TimePicker(
            state = timePickerState,
        )
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text("Seleccionar")
            }
        },
        text = { content() }
    )
}

