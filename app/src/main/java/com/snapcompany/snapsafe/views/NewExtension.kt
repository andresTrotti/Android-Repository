package com.snapcompany.snapsafe.views

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.snapcompany.snapsafe.R
import com.snapcompany.snapsafe.models.ControlModel
import com.snapcompany.snapsafe.utilities.DateUtilities
import com.snapcompany.snapsafe.utilities.GateUser
import com.snapcompany.snapsafe.utilities.ExtensionStatusTypes
import com.snapcompany.snapsafe.views.common.CustomColors
import com.snapcompany.snapsafe.views.common.OptionCard

@Composable
fun NewExtension(
    navController: NavController,
    controlModel: ControlModel
) {
    val controlUiState by controlModel.uiState.collectAsState()
    val dateUtilities = DateUtilities()
    var showAlert by remember { mutableStateOf(false) }
    var titleAlert by remember { mutableStateOf("") }
    var messageAlert by remember { mutableStateOf("") }
    var onSendResult by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val date = dateUtilities.millisToDate(System.currentTimeMillis(), true)
        val year = date[0]
        val month = date[1]
        val deadlineSelected = "${date[2]} ${dateUtilities.monthNumberToName(date[1])} ${date[0]}"
        controlModel.updateDeadlineSelected(deadlineSelected)
    }

    if(showAlert) {
        AlertDialog(
            onDismissRequest = { // 4
                showAlert = false
            },
            // 5
            title = { Text(text = titleAlert) },
            text = { Text(text = messageAlert) },
            confirmButton = { // 6
                Button(
                    onClick = {
                        if(onSendResult) navController.navigateUp()
                        else showAlert = false
                    }
                ) {
                    Text(
                        text = "Aceptar",
                        color = Color.White
                    )
                }
            }
        )
    }



    NewExtensionPreview(
        currentGateId = controlModel.currentGateData.gateId,
        onBack = { navController.navigateUp() },
        emailExists = controlUiState.emailExists,
        checkingEmail = controlUiState.isCheckingEmail,
        deadlineSelected = controlUiState.deadlineSelected,
        onSend = { extensionData, message ->
            onSendResult = false
            controlModel.firestore.sendInvitationExtension(extensionData, controlModel.currentGateData, message){ result, message ->
                onSendResult = result
                if(result){
                    titleAlert = "Listo"
                    messageAlert = message
                    showAlert = true
                }
                else{
                    titleAlert = "Error"
                    messageAlert = message
                    showAlert = true
                }
            }
        },
        onCheckEmail = { email ->
            if (!controlUiState.isCheckingEmail) {
                controlModel.updateIsCheckingEmail(true)
                controlModel.firestore.checkEmailExists(email) { result ->
                    controlModel.updateEmailExists(result)
                    controlModel.updateIsCheckingEmail(false)
                }
            }
        },
        onNewEmail = {
            controlModel.updateEmailExists(false)
        },
        onDateSelected = { dateMillis ->
            val date = dateUtilities.millisToDate(dateMillis, true)
            val year = date[0]
            val month = date[1]
            val deadlineSelected =
                "${date[2]} ${dateUtilities.monthNumberToName(date[1])} ${date[0]}"
            controlModel.updateDeadlineSelected(deadlineSelected)

        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun NewExtensionPreview(
    currentGateId: String = "Gate id",
    onBack: () -> Unit = {},
    emailExists: Boolean = false,
    checkingEmail: Boolean = false,
    deadlineSelected: String = "",
    onCheckEmail: (email: String) -> Unit = {},
    onNewEmail: () -> Unit = {},
    onSend: (extensionData: GateUser, message: String) -> Unit = {_,_ ->},
    onDateSelected: (Long) -> Unit = {}
) {
    val customColors = CustomColors()
    var extensionNameValue by remember { mutableStateOf("") }
    var extensionNameFocused by remember { mutableStateOf(false) }
    var extensionEmailValue by remember { mutableStateOf("") }
    var extensionEmailFocused by remember { mutableStateOf(false) }
    var extensionMessageValue by remember { mutableStateOf("") }
    var extensionMessageFocused by remember { mutableStateOf(false) }

    var shareableToggle by remember { mutableStateOf(false) }
    var allowHistoryAccessToggle by remember { mutableStateOf(false) }
    var allowGateConfigToggle by remember { mutableStateOf(false) }
    var allowManageAccessToggle by remember { mutableStateOf(false) }
    var boundExtensionToggle by remember { mutableStateOf(true) }
    var restrictionsToggle by remember { mutableStateOf(true) }
    var privateRestrictionsToggle by remember { mutableStateOf(false) }
    var deadlineToggle by remember { mutableStateOf(false) }
    var daysRestrictedToggle by remember { mutableStateOf(false) }
    var hoursRestrictedToggle by remember { mutableStateOf(false) }


    var messageAlert by remember { mutableStateOf("") }
    var titleAlert by remember { mutableStateOf("") }
    var showAlert by remember { mutableStateOf(false) }

    var daysRestricted by remember { mutableStateOf("") }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showEntryTimePickerDialog by remember { mutableStateOf(false) }
    var entryTimeButtonText by remember { mutableStateOf("11:59 p.m.") }
    var showExitTimePickerDialog by remember { mutableStateOf(false) }
    var exitTimeButtonText by remember { mutableStateOf("12:00 a.m.") }

    var extensionData: GateUser = GateUser()
    var message by remember { mutableStateOf("") }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Nueva extensión")
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onBack() }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if(emailExists && !checkingEmail) {

                                extensionData.name = extensionNameValue.trim()
                                extensionData.email = extensionEmailValue
                                extensionData.shareable = shareableToggle
                                extensionData.allowHistory = allowHistoryAccessToggle
                                extensionData.allowManageAccess = allowManageAccessToggle
                                extensionData.allowGateConfig = allowGateConfigToggle
                                extensionData.boundExtension = boundExtensionToggle
                                extensionData.restrictions = if(boundExtensionToggle) restrictionsToggle else false
                                extensionData.privateRestrictions = if(restrictionsToggle) privateRestrictionsToggle else false
                                extensionData.enabled = true
                                extensionData.independent = boundExtensionToggle
                                extensionData.daysRestricted = if(daysRestrictedToggle) daysRestricted else ""
                                extensionData.status = ExtensionStatusTypes().pending
                                extensionData.entryTime = if(hoursRestrictedToggle) entryTimeButtonText else ""
                                extensionData.departureTime = if(hoursRestrictedToggle) exitTimeButtonText else ""
                                extensionData.deadline = if(deadlineToggle) deadlineSelected else ""
                                extensionData.gateId = currentGateId


                                checkExtension(extensionData, daysRestrictedToggle) { result, message ->
                                    if (result) {
                                        onSend(extensionData, message)
                                    }
                                    else {
                                        titleAlert = "Error"
                                        messageAlert = message
                                        showAlert = true
                                    }
                                }
                            }
                            else{
                                titleAlert = "Correo no encontrado"
                                messageAlert = "El correo ingresado no se encuentra registrado."
                                showAlert = true
                            }
                            Log.d("Extension", extensionData.toString())

                        }

                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.send),
                            contentDescription = "Save"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        if(showEntryTimePickerDialog){
            DialWithDialog(
                onConfirm = {
                    showEntryTimePickerDialog = false
                    var hour = if(it.hour < 10) "0${it.hour}" else it.hour.toString()
                    val minute = if(it.minute < 10) "0${it.minute}" else it.minute.toString()
                    if(it.isAfternoon) {
                        hour = (it.hour - 12).toString()
                        if(hour.toInt() < 10) hour = "0$hour"

                    }
                    if(hour == "00") hour = "12"
                    entryTimeButtonText = hour + ":" + minute + if(it.isAfternoon) " p.m." else " a.m."
                },
                onDismiss = {
                    showEntryTimePickerDialog = false
                }
            )
        }

        if(showExitTimePickerDialog){
            DialWithDialog(
                onConfirm = {
                    showExitTimePickerDialog = false
                    var hour = if(it.hour < 10) "0${it.hour}" else it.hour.toString()
                    val minute = if(it.minute < 10) "0${it.minute}" else it.minute.toString()
                    if(it.isAfternoon) {
                        hour = (it.hour - 12).toString()
                        if(hour.toInt() < 10) hour = "0$hour"

                    }
                    if(hour == "00") hour = "12"
                    exitTimeButtonText = hour + ":" + minute + if(it.isAfternoon) " p.m." else " a.m."
                },
                onDismiss = {
                    showExitTimePickerDialog = false
                }
            )
        }

        if(showAlert) {
            AlertDialog(
                onDismissRequest = { // 4
                    showAlert = false
                },
                // 5
                title = { Text(text = titleAlert) },
                text = { Text(text = messageAlert) },
                confirmButton = { // 6
                    Button(
                        onClick = {
                            showAlert = false
                        }
                    ) {
                        Text(
                            text = "Aceptar",
                            color = Color.White
                        )
                    }
                }
            )
        }

        if (showDatePickerDialog) {
            DatePickerModal(
                onDateSelected = { date ->
                    showDatePickerDialog = false
                    if (date != null) {
                        extensionData.deadline = date.toString()
                        onDateSelected(date)

                    }
                },
                onDismiss = {
                    showDatePickerDialog = false
                }
            )
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            OptionCard(
                header = "EXTENDER CONTROL A:",
                options = {
                    Row {
                        BasicTextField(
                            value = extensionNameValue,
                            onValueChange = { extensionNameValue = it; extensionData = extensionData.copy(name = it) },
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .weight(1f)
                                .onFocusEvent {
                                    extensionNameFocused = it.isFocused
                                },
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (extensionNameValue.isEmpty()) {
                                        Text(
                                            text = "Nombre",
                                            color = Color.Gray,
                                            fontSize = 14.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                    innerTextField()  // This is where the actual text input is displayed
                                }
                            }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = extensionEmailValue,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                            textStyle = TextStyle(color = if (!emailExists && !extensionEmailFocused && !checkingEmail) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground),
                            onValueChange = { extensionEmailValue = it.trim().lowercase() ; onNewEmail() },
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .weight(1f)
                                .onFocusChanged {
                                    extensionEmailFocused = it.isFocused
                                    if (!it.isFocused && extensionEmailValue.isNotBlank() && !checkingEmail) {
                                        onCheckEmail(extensionEmailValue)
                                    }
                                },
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (extensionEmailValue.isEmpty()) {
                                        Text(
                                            text = "Correo",
                                            color = Color.Gray,
                                            fontSize = 14.sp,
                                            lineHeight = 14.sp,
                                        )
                                    }
                                    innerTextField()  // This is where the actual text input is displayed
                                }
                            }
                        )
                        AnimatedVisibility(visible = !emailExists && extensionEmailValue.isNotBlank() && !extensionEmailFocused && !checkingEmail) {
                            IconButton(
                                onClick = {
                                    titleAlert = "Correo no encontrado"
                                    messageAlert = "El correo ingresado no se encuentra registrado."
                                    showAlert = true
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.error),
                                    contentDescription = "Info",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        AnimatedVisibility(visible = checkingEmail) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(20.dp)
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                    Row {
                        BasicTextField(
                            value = message,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            onValueChange = { message = it },
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .weight(1f)
                                .onFocusEvent {
                                    extensionMessageFocused = it.isFocused
                                },
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (message.isEmpty()) {
                                        Text(
                                            text = "Mensaje o nota",
                                            color = Color.Gray,
                                            fontSize = 14.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                    innerTextField()  // This is where the actual text input is displayed
                                }
                            }
                        )
                    }
                }
            )

            OptionCard() {
                ToggleRow(
                    rowText = "Permitir dar acceso a otros",
                    checked = shareableToggle,
                    modifier = Modifier
                        .padding(start = 4.dp, end = 16.dp)
                        .align(Alignment.CenterHorizontally),
                    iconButton = R.drawable.info,
                    iconTint = customColors.dodgerBlue,
                    onIconButton = { showAlert = true ; titleAlert = "Información"; messageAlert = "Permitir que este usuario otorgue acceso a otros mediante el código QR o extensiones."},
                    onCheckedChange = { shareableToggle = !shareableToggle }
                )
            }

            OptionCard() {
                ToggleRow(
                    rowText = "Dar acceso al historial",
                    checked = allowHistoryAccessToggle,
                    modifier = Modifier
                        .padding(start = 4.dp, end = 16.dp)
                        .align(Alignment.CenterHorizontally),
                    iconButton = R.drawable.info,
                    iconTint = customColors.dodgerBlue,
                    onIconButton = { showAlert = true ; titleAlert = "Información"; messageAlert = "Otorgar acceso al historial de órdenes recibidas por el dispositivo."},
                    onCheckedChange = { allowHistoryAccessToggle = !allowHistoryAccessToggle }
                )
            }

            OptionCard() {
                ToggleRow(
                    rowText = "Permirtir control de extensiones",
                    checked = allowManageAccessToggle,
                    modifier = Modifier
                        .padding(start = 4.dp, end = 16.dp)
                        .align(Alignment.CenterHorizontally),
                    iconButton = R.drawable.info,
                    iconTint = customColors.dodgerBlue,
                    onIconButton = { showAlert = true ; titleAlert = "Información"; messageAlert = "Permitir que este usuario habilite o inhabilite usuarios"},
                    onCheckedChange = { allowManageAccessToggle = !allowManageAccessToggle }
                )
            }

            OptionCard() {
                ToggleRow(
                    rowText = "Permirtir ajustes al motor",
                    checked = allowGateConfigToggle,
                    modifier = Modifier
                        .padding(start = 4.dp, end = 16.dp)
                        .align(Alignment.CenterHorizontally),
                    iconButton = R.drawable.info,
                    iconTint = customColors.dodgerBlue,
                    onIconButton = { showAlert = true ; titleAlert = "Información"; messageAlert = "Permitir que este usuario haga cambios en el funcionamiento del motor"},
                    onCheckedChange = { allowGateConfigToggle = !allowGateConfigToggle }
                )
            }

            OptionCard() {
                ToggleRow(
                    rowText = "Dependiente",
                    checked = boundExtensionToggle,
                    modifier = Modifier
                        .padding(start = 4.dp, end = 16.dp)
                        .align(Alignment.CenterHorizontally),
                    iconButton = R.drawable.info,
                    iconTint = customColors.dodgerBlue,
                    onIconButton = { showAlert = true ; titleAlert = "Información"; messageAlert = "Dependiente: Permite revocar el control al otro usuario en cualquier momento y a distancia.\n\n" +
                            "Independiente: una vez otorgado solo podrás revocarle el acceso mediante un cambio de clave."},
                    onCheckedChange = { boundExtensionToggle = !boundExtensionToggle }
                )
            }

            AnimatedVisibility(visible = boundExtensionToggle) {
                OptionCard() {
                    ToggleRow(
                        rowText = "Agregar restricciones",
                        checked = restrictionsToggle,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        onCheckedChange = { restrictionsToggle = !restrictionsToggle }
                    )
                }
            }

            AnimatedVisibility(visible = restrictionsToggle && boundExtensionToggle) {
                Column {
                    OptionCard(
                        footer = "Esta opción permite ocultar el detalle de las restricciones. El usuario sabrá que está restringido pero, no en que días o en que horario."
                    ) {
                        ToggleRow(
                            rowText = "Restricciones privadas",
                            checked = privateRestrictionsToggle,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            onCheckedChange = {
                                privateRestrictionsToggle = !privateRestrictionsToggle
                            }
                        )
                    }

                    OptionCard(
                        header = "FECHA LÍMITE DE ACCESO",
                        footer = "El acceso será revocado en el momento indicado.",
                        options = {
                            ToggleRow(
                                rowText = "Límitar acceso",
                                checked = deadlineToggle,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                onCheckedChange = { deadlineToggle = !deadlineToggle }
                            )
                            AnimatedVisibility(visible = deadlineToggle) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .fillMaxWidth()
                                ){
                                    Text(text = "Fecha límite de acceso:", maxLines = 2, modifier = Modifier.weight(1f))
                                    Button(

                                        shape = RoundedCornerShape(8.dp),
                                        onClick = {
                                            showDatePickerDialog = true
                                        }) {
                                        Text(text = deadlineSelected, maxLines = 1)
                                    }
                                }
                            }
                        }
                    )

                    OptionCard(
                        footer = "Revocar el acceso en los días seleccionados. Debes permitir al menos uno si activas esta opción",
                        options = {
                            ToggleRow(
                                rowText = "Restringir de días",
                                checked = daysRestrictedToggle,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                onCheckedChange = { daysRestrictedToggle = !daysRestrictedToggle }
                            )
                            if(daysRestrictedToggle) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                ) {
                                    DayButton(
                                        day = "D",
                                        selected = daysRestricted.contains("D"),
                                        onClick = { daysRestricted = onDayRestrictedSelected("D", daysRestricted) },
                                        selectedColor = customColors.darkGreen,
                                        unselectedColor = customColors.red
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    DayButton(
                                        day = "L",
                                        selected = daysRestricted.contains("L"),
                                        onClick = { daysRestricted = onDayRestrictedSelected("L", daysRestricted) },
                                        selectedColor = customColors.darkGreen,
                                        unselectedColor = customColors.red
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    DayButton(
                                        day = "M",
                                        selected = daysRestricted.contains("M"),
                                        onClick = { daysRestricted = onDayRestrictedSelected("M", daysRestricted) },
                                        selectedColor = customColors.darkGreen,
                                        unselectedColor = customColors.red
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    DayButton(
                                        day = "X",
                                        selected = daysRestricted.contains("X"),
                                        onClick = { daysRestricted = onDayRestrictedSelected("X", daysRestricted) },
                                        selectedColor = customColors.darkGreen,
                                        unselectedColor = customColors.red
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    DayButton(
                                        day = "J",
                                        selected = daysRestricted.contains("J"),
                                        onClick = { daysRestricted = onDayRestrictedSelected("J", daysRestricted) },
                                        selectedColor = customColors.darkGreen,
                                        unselectedColor = customColors.red
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    DayButton(
                                        day = "V",
                                        selected = daysRestricted.contains("V"),
                                        onClick = { daysRestricted = onDayRestrictedSelected("V", daysRestricted) },
                                        selectedColor = customColors.darkGreen,
                                        unselectedColor = customColors.red
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    DayButton(
                                        day = "S",
                                        selected = daysRestricted.contains("S"),
                                        onClick = { daysRestricted = onDayRestrictedSelected("S", daysRestricted) },
                                        selectedColor = customColors.darkGreen,
                                        unselectedColor = customColors.red
                                    )
                                }
                            }
                        }
                    )

                    OptionCard(
                        options = {
                            ToggleRow(
                                rowText = "Restringir horario",
                                checked = hoursRestrictedToggle,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                onCheckedChange = { hoursRestrictedToggle = !hoursRestrictedToggle }
                            )
                            AnimatedVisibility(visible = hoursRestrictedToggle) {
                                HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .fillMaxWidth(),
                                    ) {
                                        Text(text = "Hora de entrada:")
                                        Button(
                                            shape = RoundedCornerShape(8.dp),
                                            onClick = { showEntryTimePickerDialog = true }) {
                                            Text(text = entryTimeButtonText)
                                        }
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .fillMaxWidth(),
                                    ) {
                                        Text(text = "Hora de salida:")
                                        Button(
                                            shape = RoundedCornerShape(8.dp),
                                            onClick = { showExitTimePickerDialog = true }) {
                                            Text(text = exitTimeButtonText)
                                        }
                                    }
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.size(50.dp))
                }

            }
        }
    }
}

fun checkExtension(
    extensionData: GateUser,
    daysRestrictedToggle: Boolean,
    onCheck: (result: Boolean, message: String) -> Unit
) {

    if (extensionData.name.isBlank() || extensionData.name.length < 3) {
        onCheck(false, "El nombre no puede estar vacío o ser muy corto")
    }
    else if(extensionData.email.isBlank()) {
        onCheck(false, "El correo no puede estar vacío")
    }
    else if ( extensionData.daysRestricted.isBlank() && daysRestrictedToggle){
        onCheck(false, "Debes seleccionar al menos un día de la semana")
    }
    else {
        onCheck(true, "")
    }

}

@Composable
@Preview
fun DayButton(
    day: String = "D",
    selected: Boolean = true,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit = {}
){
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(
                if (selected) selectedColor else unselectedColor,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)

    ) {
        Text(text = day, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

fun onDayRestrictedSelected(day: String, currentDaysRestricted: String): String {
    return if (currentDaysRestricted.contains(day)) {
        currentDaysRestricted.replace(day, "")
    } else {
        currentDaysRestricted + day
    }
}