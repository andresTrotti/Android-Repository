package com.snapcompany.snapsafe.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.snapcompany.snapsafe.R
import com.snapcompany.snapsafe.models.ControlModel
import com.snapcompany.snapsafe.utilities.GoogleSignInClient
import com.snapcompany.snapsafe.utilities.UserData
import com.snapcompany.snapsafe.views.common.OptionCard
import com.snapcompany.snapsafe.views.common.ResultView
import kotlinx.coroutines.launch

@Composable
fun AccountView(
    navController: NavController,
    controlModel: ControlModel
) {
    val controlUiState by controlModel.uiState.collectAsState()
    val googleSignInClient = GoogleSignInClient(LocalContext.current)
    val coroutineScope = rememberCoroutineScope()

    AccountPreview(
        email = controlUiState.userEmail,
        phone = controlUiState.phone,
        subscription = controlUiState.subscription,
        name = controlUiState.name,

        onBack = { navController.navigateUp() },
        onDeleteAll = {
            controlUiState.gateList.clear()
            controlModel.deleteAllGatesInAppDatabase()
        },
        onLogout = {
            coroutineScope.launch {
                googleSignInClient.signOut()
                controlModel.updateUserIsLogged(false)
                controlModel.appDao.deleteKey("userEmailLogged")
                navController.navigateUp()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun AccountPreview(
    email: String = "",
    phone: String = "",
    subscription: Boolean = false,
    name: String = "",
    onBack: () -> Unit = {},
    onDeleteAll: () -> Unit = {},
    onLogout: () -> Unit = {},

) {

    var showAlertDialog by remember { mutableStateOf(false) }
    var messageAlert by remember { mutableStateOf("") }
    var titleAlert by remember { mutableStateOf("") }
    var onAction: () -> Unit = {}
    var showResult by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = name) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            painterResource(id = R.drawable.arrow_back),
                            contentDescription = "back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                })
        }
    ) { innerPadding ->


        if (showAlertDialog) {

            AlertDialog(
                onDismissRequest = {
                    showAlertDialog = false
                },
                title = { Text(text = titleAlert) },
                text = { Text(text = messageAlert) },
                dismissButton = {
                    Button(
                        onClick = {
                            showAlertDialog = false
                        }
                    ) {
                        Text(
                            text = "Cancelar",
                            color = Color.White
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            when(onAction){
                                onDeleteAll -> {
                                    onDeleteAll()
                                    result = true
                                    showResult = true
                                }
                                onLogout ->{
                                    onLogout()
                                    onBack()
                                }
                            }
                            showAlertDialog = false
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

        Box(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    OptionCard("CUENTA") {
                        Text(text = "Correo: $email" , modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onBackground)
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                        Text(text = "Suscripción: " + if(subscription) "Premiun" else "gratuita", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onBackground)
                    }

                    Spacer(modifier = Modifier.size(16.dp))
                    OptionCard("DISPOSITIVOS") {
                        OptionRow("Eliminar todos los controles") {
                            titleAlert = "Eliminar todos los controles"
                            messageAlert = "¿Estás seguro de que quieres eliminar todos los controles?"
                            onAction = onDeleteAll
                            showAlertDialog = true
                        }
                    }
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp),
                    shape = RoundedCornerShape(10.dp),
                    onClick = {
                        titleAlert = "Cerrar sesión"
                        messageAlert = "¿Estás seguro de que quieres cerrar sesión?"
                        onAction = onLogout
                        showAlertDialog = true

                    },

                ){
                    Text(
                        text = "Cerrar sesión",
                        maxLines = 3
                    )
                }
            }
        }

        AnimatedVisibility(visible = showResult, enter = fadeIn(), exit = fadeOut()){
            ResultView(
                result = result,
                onCompleteDelay = {
                    showResult = false
                }
            )
        }
    }
}
