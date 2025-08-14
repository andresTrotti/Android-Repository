package com.snapcompany.snapsafe.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.snapcompany.snapsafe.R
import com.snapcompany.snapsafe.models.ControlModel
import com.snapcompany.snapsafe.models.Orders
import com.snapcompany.snapsafe.navigation.Views
import com.snapcompany.snapsafe.utilities.GateData
import com.snapcompany.snapsafe.views.common.OptionCard
import com.snapcompany.snapsafe.views.common.ResultView
import kotlinx.coroutines.launch

@Composable
fun GateSettings(
    navController: NavController,
    controlModel: ControlModel,
){
    val controlUiState by controlModel.uiState.collectAsState()

    var showResult by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf(false)}
    var navigateUp by remember { mutableStateOf(false) }


    GateSettingsPreview(
        isConnectedUi = controlModel.isConnected,
        onBack = {navController.navigateUp()},
        gateData = controlModel.currentGateData,
        infoMessage = controlUiState.infoMessage,
        gateDataEncrypted = controlModel.encryptQrData(),
        opens = controlUiState.opens.toString(),
        closes = controlUiState.closes.toString(),
        stops = controlUiState.stops.toString(),
        position = controlUiState.position.toString(),
        infraredState = controlUiState.infraredState,
        stopOnOpenState = controlUiState.stopOnOpenState,
        closeOnPassState = controlUiState.closeOnPassState,
        timeToPass = controlUiState.timeToPass,
        brakeLevel = controlUiState.brakeLevel,

        onNavigation = {
            navController.navigate(it)
        },
        onDelete = {
            controlModel.viewModelScope.launch {
                controlModel.endScan() // if is scanning while you delete the gate the app will crash
                controlModel.appDao.deleteGate(controlModel.currentGateData.gateId)
                controlModel.loadGatesFromAppDb(true)
                result = true
                navigateUp = true
                showResult = true
            }
        },
        onInfraredChange = {
            if(it) controlModel.onBleOrder(order = Orders.ACTIVATE_INFRARED)
            else controlModel.onBleOrder(order = Orders.DEACTIVATE_INFRARED)
            controlModel.updateInfraredState(it)
        },
        onStopOnOpen = {
            if(it) controlModel.onBleOrder(order = Orders.ACTIVATE_STOP_ON_OPEN)
            else controlModel.onBleOrder(order = Orders.DEACTIVATE_STOP_ON_OPEN)
            controlModel.updateStopOnOpenState(it)
        },
        onCloseOnPass = {
            if(it) controlModel.onBleOrder(order = Orders.ACTIVATE_CLOSE_ON_PASS)
            else controlModel.onBleOrder(order = Orders.DEACTIVATE_CLOSE_ON_PASS)
            controlModel.updateCloseOnPassState(it)
        },
        onUpdateTime = {
            controlModel.updateTimeToPass(it)
        },
        onUpdateBrake = {
            controlModel.updateBrakeLevel(it)
        },
        onSetTime = {
            controlModel.onBleOrder(order = Orders.SET_TIME_TO_PASS)
        },
        onSetBrake = {
            controlModel.onBleOrder(order = Orders.BRAKE_LEVEL,
                newBrakeLevel = controlUiState.brakeLevel
            )
        },
        onReportRequest = {
            controlModel.onBleOrder(order = Orders.REPORT)
        },
        onSaveNewPassword = { newPassword ->
            controlModel.onBleOrder(
                order = Orders.CHANGE_PASSWORD,
                newPassword = newPassword
            )
        }
    )


    AnimatedVisibility(visible = showResult, enter = fadeIn(), exit = fadeOut()){
        ResultView(
            result = result,
            onCompleteDelay = {
                showResult = false
                if(navigateUp) navController.navigateUp()
            }
        )
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun GateSettingsPreview(
    gateData: GateData = GateData(),
    gateDataEncrypted: String? = null,
    isConnectedUi: Boolean = false,
    onBack: () -> Unit = {},
    onDelete: () -> Unit = {},
    infoMessage: Int = R.string.report,
    opens: String = "",
    closes: String = "",
    stops: String = "",
    position: String = "",
    infraredState: Boolean = false,
    stopOnOpenState: Boolean = false,
    closeOnPassState: Boolean = false,
    timeToPass: Int = 5,
    brakeLevel: Int = 50,
    showAlert: Boolean = false,
    newMessageAlert: String = "",
    onNavigation: (String) -> Unit = {},
    onInfraredChange: (value: Boolean) -> Unit = {},
    onStopOnOpen: (value: Boolean) -> Unit = {},
    onCloseOnPass: (value: Boolean) -> Unit = {},
    onSetTime: () -> Unit = {},
    onSetBrake: () -> Unit = {},
    onUpdateTime: (value: Int) -> Unit = {},
    onUpdateBrake: (value: Int) -> Unit = {},
    onReportRequest: () -> Unit = {},
    onSaveNewPassword: (newPassword: String) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val sheetHeight = (screenHeight * 0.85).toInt()
    var showReportSheet by remember { mutableStateOf(false) }
    val reportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showQrSheet by remember { mutableStateOf(false) }
    val qrSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showChangePasswordSheet by remember { mutableStateOf(false) }
    val changePasswordSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showAlertDialog by remember { mutableStateOf(showAlert) }
    var messageAlert by remember { mutableStateOf(newMessageAlert) }
    var titleAlert by remember { mutableStateOf("") }
    var onAction: () -> Unit = {}




    val views = Views()


    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = gateData.gateName) },
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
        Box(modifier = Modifier.padding(innerPadding)) {

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OptionCard(
                    header = "Acerca del dispositivo",
                    footer = "Personalizar",
                    options = {
                        OptionRow(text = "Ajustes y reporte de funcionamiento", enabled = gateData.allowGateConfig) {
                            showReportSheet = true
                        }
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                        OptionRow("Cambiar nombre o imagen") {
                            onNavigation(views.editGate)
                        }
                    }
                )

                OptionCard(
                    header = "Administrar usuarios",
                    footer = "Dar o revocar acceso a tu dispositivo",
                    options = {
                        OptionRow("Administrar accesos", enabled = gateData.allowManageAccess) {
                            onNavigation(views.manageExtensions)
                        }
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                        OptionRow("Mostrar código QR", enabled = gateData.shareable) {
                            showQrSheet = true
                        }
                    }
                )

                OptionCard(
                    header = "Registros",
                    options = {
                        OptionRow(text = "Historial de accesos", enabled = gateData.allowHistory) {
                            onNavigation(views.gateHistory)
                        }
                    }
                )

                OptionCard(
                    header = "Manos libres",
                    options = {
                        Row(modifier = Modifier.padding(horizontal = 16.dp)){
                            ToggleRow(
                                rowText = "Abrir al conectarse"
                            )
                        }

                    }
                )


                OptionCard(
                    options = {
                        OptionRow("Eliminar control") {
                            onAction = onDelete
                            messageAlert = "¿Seguro que deseas eliminar este control?"
                            titleAlert = "Eliminar control"
                            showAlertDialog = true
                        }
                    }
                )

            }
            if (showAlertDialog) {

                AlertDialog(
                    onDismissRequest = {
                        showAlertDialog = false
                    },
                    title = { Text(text = titleAlert) },
                    text = { Text(text = messageAlert) },
                    dismissButton = {
                        Button(onClick = {showAlertDialog = false}) {
                            Text(
                                text = "Cancelar",
                                color = Color.White
                            )
                        }
                    },

                    confirmButton = {
                        Button(
                            onClick = {
                                showAlertDialog = false
                                when(onAction){
                                    onDelete ->{
                                        onDelete()
                                    }
                                }

                            }
                        ) {
                            Text(
                                text = "Eliminar",
                                color = Color.White
                            )
                        }
                    }
                )

            }



            if(showQrSheet){
                ModalBottomSheet(
                    onDismissRequest = {
                        showQrSheet = false
                    },
                    sheetState = qrSheetState
                ) {

                    ShareQrView(
                        sheetHeight = sheetHeight,
                        encryptedText = gateDataEncrypted,
                        gateName = gateData.gateName

                    )
                }
            }

            if (showReportSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showReportSheet = false

                    },
                    sheetState = reportSheetState
                ) {
                    LaunchedEffect(Unit) {
                        onReportRequest()
                    }

                    OperationReportPreview(
                        sheetHeight = sheetHeight,
                        infoMessage = infoMessage,
                        opens = opens,
                        closes = closes,
                        stops = stops,
                        position = position,
                        infraredState = infraredState,
                        stopOnOpenState = stopOnOpenState,
                        closeOnPassState = closeOnPassState,
                        timeToPass = timeToPass,
                        brakeLevel = brakeLevel,
                        onInfraredChange = { onInfraredChange(it) },
                        onStopOnOpen = { onStopOnOpen(it) },
                        onCloseOnPass = { onCloseOnPass(it) },
                        onSetTime = { onSetTime() },
                        onSetBrake = onSetBrake,
                        onUpdateTime = { onUpdateTime(it) },
                        onUpdateBrake = { onUpdateBrake(it) }
                    )

                }
            }
        }
    }
}

@Composable
fun OptionRow(text: String = "Option", enabled: Boolean = true, action: () -> Unit = {}){
    Row(
        modifier = Modifier.clickable { if(enabled) action() }.alpha(if(enabled) 1f else 0.5f)
    ){
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}