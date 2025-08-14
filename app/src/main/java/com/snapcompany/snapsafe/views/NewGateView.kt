package com.snapcompany.snapsafe.views

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.snapcompany.snapsafe.R
import com.snapcompany.snapsafe.models.ControlModel
import com.snapcompany.snapsafe.navigation.Views
import com.snapcompany.snapsafe.utilities.ExtensionStatusTypes
import com.snapcompany.snapsafe.utilities.SharedExtensionAccess
import com.snapcompany.snapsafe.views.common.LoadingView
import com.snapcompany.snapsafe.views.common.ResultView
import com.snapcompany.snapsafe.views.common.TextInputDialog
import kotlinx.coroutines.delay


@Composable
fun NewGateView(
    navController: NavController,
    controlModel: ControlModel,
){
    val controlUiState by controlModel.uiState.collectAsState()
    val views = Views()
    var showNewGateDialog by remember { mutableStateOf(false) }
    var showResultView by remember { mutableStateOf(false) }
    var resultValue by remember { mutableStateOf(false) }

    var showLoadingView by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = controlUiState.showNewGateDialog){
        if(controlUiState.showNewGateDialog){
            delay(500)
            showNewGateDialog = true
        }
    }



    NewGatePreview(
        onBack = {navController.navigateUp()},
        onQrScanner = { navController.navigate(views.qrView) },
        sharedExtensions = controlUiState.sharedExtensions,
        onAcceptSharedExtension = { sharedExtensionAccess ->
            showLoadingView = true
            controlModel.onAcceptSharedExtension(sharedExtensionAccess) { result ->
                controlModel.loadGatesFromAppDb(true)
                resultValue = result
                showLoadingView = false
                showResultView = true

            }
        },
        onRejectSharedExtension = { sharedExtensionAccess ->
            showLoadingView = true
            controlModel.onRejectSharedExtension(sharedExtensionAccess) { result ->
                resultValue = result
                showLoadingView = false
                showResultView = true
            }
        }
    )

    AnimatedVisibility(visible = showLoadingView, enter = fadeIn(), exit = fadeOut()) {
        LoadingView()
    }

    AnimatedVisibility(visible = showResultView, enter = fadeIn(), exit = fadeOut()) {
        ResultView(
            result = resultValue,
            onCompleteDelay = { showResultView = false }
        )
    }

    if(controlUiState.globalAlertShow){
        AlertDialog(
            onDismissRequest = {
                controlModel.hideGlobalAlert()
            },
            title = { Text(text = controlUiState.globalAlertTitle) },
            text = { Text(text = controlUiState.globalAlertMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        controlModel.hideGlobalAlert()
                    }
                ) {
                    Text(
                        text = "OK",
                        color = Color.White
                    )
                }
            }
        )
    }

    if(showNewGateDialog){
        TextInputDialog(
            defaultValue = "Agregar nombre",
            title = "Nombre del acceso",
            onDismiss = {
                showNewGateDialog = false
                controlModel.updateShowNewGateDialog(false)
                        },
            onConfirm = { gateName ->
                showNewGateDialog = false
                controlModel.updateShowNewGateDialog(false)

                controlModel.saveNewGate(gateName = gateName){
                    resultValue = it
                    showResultView = true
                    Log.d("Dao","gate saved...")
                }

            }
        )
    }


}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun NewGatePreview(
    onBack: () -> Unit = {},
    onQrScanner: () -> Unit = {},
    sharedExtensions: List<SharedExtensionAccess> = emptyList(),
    onAcceptSharedExtension: (sharedExtensionAccess: SharedExtensionAccess) -> Unit = {},
    onRejectSharedExtension: (sharedExtensionAccess: SharedExtensionAccess) -> Unit = {}
){

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.new_gate_view)) },
            navigationIcon = {
                IconButton(onClick = { onBack() }) {
                    Icon(
                        painterResource(id = R.drawable.arrow_back),
                        contentDescription = "back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            })}
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)){
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {

                NewGateCard(
                    title = "Mediante QR",
                    message = "Controla un portón mediante el escaneo de su código QR",
                    iconId = R.drawable.qr_code_scanner,
                    textButton = "Abrir escáner",
                    onQrScanner = onQrScanner
                )

                SharedExtensionAccessList(
                    header = "Mediante invitaciones",
                    footer = "",
                    sharedExtensions = sharedExtensions,
                    onAcceptSharedExtension = onAcceptSharedExtension,
                    onRejectSharedExtension = onRejectSharedExtension
                )
            }

            
        }

    }
}





@Composable
fun SharedExtensionAccessList(
    header: String = "Mediante invitaciones",
    footer: String = "",
    onRejectSharedExtension: (sharedExtensionAccess: SharedExtensionAccess) -> Unit = {},
    onAcceptSharedExtension: (sharedExtensionAccess: SharedExtensionAccess) -> Unit = {},
    sharedExtensions: List<SharedExtensionAccess> = listOf(
        SharedExtensionAccess(
            name = "Andres Trotti",
            accessFrom = "james.buchanan@examplepetstore.com",
            boundExtension = true,
            daysRestricted = "XLS",
            message = "Hola, por favor acepta.",
            privateRestrictions = false,
            deadline = System.currentTimeMillis().toString(),
            status = "pending",
            gateType = "Portón",
            entryTime = "12:00 a.m.",
            departureTime = "5:00 p.m.",
            restrictions = true,
            gateName = "Porton 1",
            shareable = true,
            allowHistory = true,
            allowGateConfig = true,
            allowManageAccess = true,
        )
    )
){
    val extensionStatusTypes = ExtensionStatusTypes()
    val sharedExtensionsFilter = sharedExtensions.filter { it.status == extensionStatusTypes.pending }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {


            Text(
                modifier = Modifier

                    .padding(start = 16.dp, end = 4.dp)
                    .alpha(0.7f),
                text = header,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,

                )

            this@Row.AnimatedVisibility(
                sharedExtensionsFilter.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Badge {
                    Text(
                        text = sharedExtensionsFilter.count().toString(),
                        modifier =
                        Modifier.semantics {
                            contentDescription =
                                "$ new notifications"
                        }
                    )
                }
            }


        }

        AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {


            if (sharedExtensionsFilter.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .alpha(0.7f),
                        text = "No tienes invitaciones pendientes",
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                sharedExtensionsFilter.forEach { sharedExtension ->
                        SharedExtensionCard(
                            sharedExtension = sharedExtension,
                            onRejectSharedExtension = { onRejectSharedExtension(sharedExtension) },
                            onAcceptSharedExtension = { onAcceptSharedExtension(sharedExtension) }
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                    }
            }
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                .alpha(0.7f),
            text = footer,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,

            )
    }
}


@Composable
@Preview
fun SharedExtensionCard(
    sharedExtension: SharedExtensionAccess = SharedExtensionAccess(
        name = "Andres Trotti",
        accessFrom = "james.buchanan@examplepetstore.com",
        boundExtension = true,
        daysRestricted = "XLS",
        message = "Hola, por favor acepta.",
        privateRestrictions = false,
        deadline = System.currentTimeMillis().toString(),
        status = "pending",
        gateType = "Portón",
        entryTime = "12:00 a.m.",
        departureTime = "5:00 p.m.",
        restrictions = true,
        gateName = "Porton 1",
        shareable = true,
        allowHistory = true,
        allowGateConfig = true,
        allowManageAccess = true,
    ),
    onRejectSharedExtension: (sharedExtensionAccess: SharedExtensionAccess) -> Unit = {},
    onAcceptSharedExtension: (sharedExtensionAccess: SharedExtensionAccess) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row {
                Column {
                    Text(
                        text = "De: " + sharedExtension.name + "\n" + sharedExtension.accessFrom,
                        fontSize = 16.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.size(8.dp))

                    Text(text = "Nota: " + sharedExtension.message, fontSize = 14.sp)

                    Spacer(modifier = Modifier.size(4.dp))

                    Text(text = stringResource(id = R.string.access_to) + ": " + sharedExtension.gateName, fontSize = 14.sp)

                    Spacer(modifier = Modifier.size(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Permisos:")
                        if(sharedExtension.shareable) {
                            Spacer(modifier = Modifier.size(8.dp))
                            Icon(painterResource(R.drawable.person_add), contentDescription = "add")
                        }
                        if(sharedExtension.allowHistory) {
                            Spacer(modifier = Modifier.size(8.dp))
                            Icon(painterResource(R.drawable.history), contentDescription = "history")
                        }
                        if(sharedExtension.allowGateConfig) {
                            Spacer(modifier = Modifier.size(8.dp))
                            Icon(painterResource(R.drawable.settings), contentDescription = "config")
                        }
                        if(sharedExtension.allowManageAccess) {
                            Spacer(modifier = Modifier.size(8.dp))
                            Icon(painterResource(R.drawable.manage_accounts), contentDescription = "remove")
                        }

                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            shape = RoundedCornerShape(8.dp),
                            onClick = { onRejectSharedExtension(sharedExtension) }) {
                            Text(text = "Rechazar")
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Button(
                            shape = RoundedCornerShape(8.dp),
                            onClick = { onAcceptSharedExtension(sharedExtension) }) {
                            Text(text = "Aceptar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewGateCard(
    message: String,
    title: String,
    footer: String = "",
    textButton: String = "action",
    iconId: Int,
    onQrScanner: () -> Unit = {}
){

    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    ) {

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                .alpha(0.7f),
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,

        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 0.dp, max = 150.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = message,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Button(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier,
                        onClick = { onQrScanner() }) {
                        Text(text = textButton)
                    }
                    Icon(
                        modifier = Modifier
                            .size(54.dp)
                            .alpha(0.7F),
                        painter = painterResource(id = iconId),
                        contentDescription = "Qr scanner"
                    )
                }

            }

        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                .alpha(0.7f),
            text = footer,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,

            )
    }
}
