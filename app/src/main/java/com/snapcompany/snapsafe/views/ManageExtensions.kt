package com.snapcompany.snapsafe.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.snapcompany.snapsafe.R
import com.snapcompany.snapsafe.models.ControlModel
import com.snapcompany.snapsafe.navigation.Views
import com.snapcompany.snapsafe.utilities.GateUser
import com.snapcompany.snapsafe.utilities.ExtensionStatusTypes
import com.snapcompany.snapsafe.utilities.GateData
import com.snapcompany.snapsafe.views.common.BackgroundText
import com.snapcompany.snapsafe.views.common.CustomColors
import com.snapcompany.snapsafe.views.common.OptionCard


@Composable
fun ManageExtensions(
    controlModel: ControlModel,
    navController: NavController
) {
    val controlUiState by controlModel.uiState.collectAsState()

    ManageExtensionsPreview(
        onBack = { navController.navigateUp() },
        extensions = controlUiState.extensions,
        onNavigate = { view -> navController.navigate(view) },
        onChangeExtensionEnable = { extensionEmail, newState ->
            controlModel.firestore.changeEnableStatus(
                gateId = controlModel.currentGateData.gateId,
                extensionEmail = extensionEmail,
                newStatus = newState,
                callback = { result ->

                }
            )
        },
        currentGateData = controlModel.currentGateData,
        firestoreLoading = controlUiState.firestoreLoading
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun ManageExtensionsPreview(
    onBack: () -> Unit = {},
    onNavigate: (view: String) -> Unit = {},
    onChangeExtensionEnable: (extensionEmail: String, enabled: Boolean) -> Unit = { _, _ -> },
    extensions: List<GateUser> = listOf(
        GateUser(name = "Extension 1"),
        GateUser(name = "Extension 2"),
        GateUser(name = "Extension 3")
    ),
    currentGateData: GateData = GateData(),
    firestoreLoading: Boolean = false
) {

    val extensionsFilter = extensions.filter { currentGateData.gateId == it.gateId }
    val extensionStatusTypes = ExtensionStatusTypes()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Ad. Extensiones")
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
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { val views = Views(); onNavigate(views.newExtension) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.person_add),
                    contentDescription = "Add"
                )
                Spacer(modifier = Modifier.padding(5.dp))
                Text("Extender control")
            }
        }

    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            if(firestoreLoading) BackgroundText(text = "Cargando...",)

            else if(extensionsFilter.isEmpty()){
                BackgroundText(text = "No hay extensiones",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp))
            }
            else {
                OptionCard(
                    header = "USUARIOS DEPENDIENTES",
                    footer = "Usuarios a los que puedes controlar el acceso. Dichos usuarios siempre requerirán de conexión a internet para acceder.",
                    options = {
                        extensionsFilter.filter { extension ->
                            extension.boundExtension && extension.status == extensionStatusTypes.accepted
                        }
                            .forEach() {
                                ExtensionRow(extensionData = it, onChangeExtensionEnable = onChangeExtensionEnable)
                                if (it != extensionsFilter.last()) HorizontalDivider(
                                    modifier = Modifier.padding(
                                        start = 16.dp
                                    )
                                )
                            }
                    }
                )
                OptionCard(
                    header = "USUARIOS INDEPENDIENTES",
                    footer = "Para eliminar un usuario independiente, debes realizar un cambio de clave",
                    options = {
                        extensionsFilter.filter { extension -> !extension.boundExtension }.forEach() {
                            ExtensionRow(extensionData = it)
                            if (it != extensionsFilter.last()) HorizontalDivider(
                                modifier = Modifier.padding(
                                    start = 16.dp
                                )
                            )
                        }
                    }
                )
                OptionCard(
                    header = "USUARIOS PENDIENTES",
                    footer = "Extensión a la espera de se aceptada o rechazada. Pulsa para cancelar la invitación.",
                    options = {
                        extensionsFilter.filter { extension -> extension.status == ExtensionStatusTypes().pending }.forEach {
                            ExtensionRow(extensionData = it)
                            if (it != extensionsFilter.last()) HorizontalDivider(
                                modifier = Modifier.padding(
                                    start = 16.dp
                                )
                            )
                        }
                    }
                )

                OptionCard(
                    header = "USUARIOS RECHAZADOS",
                    footer = "El usuario ha decidio rechzar el control. Has clic para eliminarla.",
                    options = {
                        extensionsFilter.filter { extension -> extension.status == ExtensionStatusTypes().rejected }.forEach {
                            ExtensionRow(extensionData = it)
                        }
                    }
                )

                OptionCard(
                    header = "USUARIOS CON ACCESO",
                    footer = "Aquí se muestran los usuarios invitados por ti o por otro usuario con control completo."
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ExtensionRow(
    onChangeExtensionEnable: (extensionEmail:String, enabled: Boolean) -> Unit = {_,_ ->},
    onRowClick: () -> Unit = {},
    extensionData: GateUser = GateUser(
        name = "Extension 1",
    )
) {

    val customColors = CustomColors()

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onRowClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.info),
            contentDescription = "Extension",
            modifier = Modifier.padding(end = 8.dp),
            tint = customColors.dodgerBlue
        )
        Text(
            text = extensionData.name,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        ExtensionStateButton(onChangeEnable = { newState ->
            onChangeExtensionEnable(extensionData.email, newState)
        }, extensionData = extensionData)

    }
}

@Composable
@Preview(showBackground = true)
fun ExtensionStateButton(
    onChangeEnable: (enabled: Boolean) -> Unit = {},
    extensionData: GateUser = GateUser(
        enabled = false,
        status = ExtensionStatusTypes().accepted,
        boundExtension = false
    )
) {
    val customColors = CustomColors()
    var color = if (extensionData.enabled) Color.Green else Color.Red
    var text = if (extensionData.enabled) "Habilitado" else "Deshabilitado"
    val extensionInvitationStatus = ExtensionStatusTypes()


    when (extensionData.status) {
        extensionInvitationStatus.accepted -> {
            if (extensionData.enabled) {
                if (extensionData.boundExtension) {
                    color = customColors.dodgerBlue
                    text = "Habilitado"
                } else {
                    color = customColors.darkGreen
                    text = "Habilitado"
                }
            } else {
                color = customColors.crimson
                text = "Deshabilitado"
            }
        }

            extensionInvitationStatus.pending -> {
                text = "Pendiente"
                color = Color.Gray
            }

            extensionInvitationStatus.rejected -> {
                text = "Rechazada"
                color = Color.Gray
            }


    }

    Button(
        onClick = { onChangeEnable(extensionData.enabled) },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonColors(
            containerColor = color,
            contentColor = Color.White,
            disabledContainerColor = color,
            disabledContentColor = Color.White
        ),
        enabled = extensionData.enabled,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),

    ){
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 12.sp,
            )
    }

}