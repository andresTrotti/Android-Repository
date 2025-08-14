package com.snapcompany.snapsafe.views

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.snapcompany.snapsafe.R
import com.snapcompany.snapsafe.models.ControlModel
import com.snapcompany.snapsafe.navigation.Views
import com.snapcompany.snapsafe.utilities.GoogleSignInClient
import com.snapcompany.snapsafe.utilities.RoomKey
import com.snapcompany.snapsafe.utilities.UserData
import com.snapcompany.snapsafe.utilities.UserDataAppDb
import com.snapcompany.snapsafe.views.common.CustomColors
import com.snapcompany.snapsafe.views.common.OptionCard
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun MainView(
    navController: NavController,
    controlModel: ControlModel
) {
    val controlUiState = controlModel.uiState.collectAsState()
    val googleSignInClient = GoogleSignInClient(LocalContext.current)
    val coroutineScope = rememberCoroutineScope()
    val views = Views()
    val context = LocalContext.current

    var loading by remember { mutableStateOf(false) }

    var showAlert by remember { mutableStateOf(false) }
    var titleAlert by remember { mutableStateOf("") }
    var messageAlert by remember { mutableStateOf("") }

    MainViewPreview(
        loading = loading,
        onNav = { view ->
            navController.navigate(view)
        },
        onRegisterButtonClick = {
            navController.navigate(views.signIn)
        },
        onSignIn = { email, password, ok, errorMessage ->

            if(!ok){
                messageAlert = errorMessage
                titleAlert = "Ha ocurrido un error"
                showAlert = true
            }
            else{
                loading = true
                controlModel.firestore.signIn(email, password) { success, userData, errorMessage2 ->

                    loading = false
                    if(success && userData != null) {
                        controlModel.updateUserData(userData = userData)

                        coroutineScope.launch {

                            controlModel.appDao.insertUser(
                                userDataAppDb = UserDataAppDb(
                                    name = userData.name,
                                    email = userData.email,
                                    profileImageUrl = userData.profileImageUrl,
                                    phone = userData.phone,
                                    subscription = userData.subscription
                                )
                            )

                            controlModel.appDao.insertKey(
                                RoomKey(
                                    roomKey = "userEmailLogged",
                                    value = userData.email
                                )
                            )
                            controlModel.updateUserIsLogged(true)
                        }

                    }
                    else{
                        messageAlert = errorMessage2 ?: ""
                        titleAlert = "Ha ocurrido un error"
                        showAlert = true
                    }

                }
            }
        },
        onGoogleAuthButtonClick = {
            loading = true
            coroutineScope.launch {
                if (googleSignInClient.isSignedIn()) {

                    val deferred = async { controlModel.appDao.getKey("userEmailLogged") }
                    val userEmail = deferred.await()?.value
                    val deferred2 =
                        async { controlModel.appDao.getUserById(email = userEmail ?: "") }
                    val userDataAppDb = deferred2.await()

                    if (userDataAppDb != null) {
                        controlModel.uiState.value.userData.apply {
                            name = userDataAppDb.name
                            email = userDataAppDb.email
                            subscription = userDataAppDb.subscription
                            phone = userDataAppDb.phone
                            profileImageUrl = userDataAppDb.profileImageUrl
                        }
                        controlModel.updateUserIsLogged(true)

                    } else controlModel.updateUserIsLogged(false)
                    loading = false


                } else {
                    coroutineScope.launch {
                        val signInResult = googleSignInClient.signIn()
                        if (signInResult) {

                            val userDataRetrieve = UserData(
                                name = googleSignInClient.name ?: "",
                                email = googleSignInClient.email ?: "",
                                profileImageUrl = googleSignInClient.profileImageUrl.toString(),
                                phone = googleSignInClient.phone.toString()
                            )
                            controlModel.updateUserData(userData = userDataRetrieve)

                            controlModel.appDao.insertUser(
                                userDataAppDb = UserDataAppDb(
                                    name = googleSignInClient.name ?: "",
                                    email = googleSignInClient.email ?: "",
                                    profileImageUrl = googleSignInClient.profileImageUrl.toString(),
                                    phone = googleSignInClient.phone.toString(),
                                    subscription = false
                                )
                            )

                            controlModel.appDao.insertKey(
                                RoomKey(
                                    roomKey = "userEmailLogged",
                                    value = googleSignInClient.email
                                )
                            )

                            controlModel.firestore.saveUserGoogleAuth(userData = userDataRetrieve) {
                                controlModel.updateUserIsLogged(true)
                                loading = false
                            }
                        } else {
                            //TODO
                            loading = false
                            Toast.makeText(context, "Error al iniciar sesión", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
    )

    if(showAlert) {
        AlertDialog(
            onDismissRequest = {
                showAlert = false
            },
            title = { Text(text = titleAlert) },
            text = { Text(text = messageAlert) },
            confirmButton = {
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
}


@Composable
@Preview(showSystemUi = true)
fun MainViewPreview(
    loading: Boolean = false,
    onSignIn: (email: String, password: String, ok: Boolean, errorMessage: String) -> Unit = {_,_,_,_ ->},
    onRegisterButtonClick: () -> Unit = {},
    onGoogleAuthButtonClick: () -> Unit = {},
    onNav: (String) -> Unit = {}
) {
    val customColors = CustomColors()
    val views = Views()
    var showTextFields by remember { mutableStateOf(false) }


    Scaffold { innerPadding ->
        AnimatedVisibility(visible = loading, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "SnapSafe",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold

            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = !showTextFields) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "Bienvenido",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Inicia sesión para continuar"
                        )
                    }
                }
                AnimatedVisibility(visible = showTextFields) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        CredentialsInput(){
                                email, password, ok, errorMessage ->
                            onSignIn(email.trim(), password.trim(), ok, errorMessage)
                        }


                    }

                }
            }

            Column {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(10.dp),
                    onClick = { onGoogleAuthButtonClick(); showTextFields = false },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            painter = painterResource(R.drawable.google_icon),
                            contentDescription = "Google logo"
                        )

                        Text(
                            text = "Iniciar sesión con Google"
                        )
                    }
                }



                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp))

                Button (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(10.dp),
                    onClick = {showTextFields = true},
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {


                        Text(
                            text = "Iniciar sesión"
                        )
                    }
                }


                OutlinedButton (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(10.dp),
                    onClick = { onNav(views.signIn); showTextFields = false },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Registrarse"
                        )
                    }
                }

                val annotatedString = buildAnnotatedString {
                    append("Al presionar en Aceptar términos y resgistrarse, se acepta la ")
                    pushStringAnnotation(tag = "URL", annotation = "https://docs.google.com/document/d/1-Ygr-EKdycfTNnz_CgFZQ4AEyijWASS4/edit?usp=drive_link&ouid=112386923811420787147&rtpof=true&sd=true")
                    withLink(
                        link = LinkAnnotation.Url("https://docs.google.com/document/d/1-Ygr-EKdycfTNnz_CgFZQ4AEyijWASS4/edit?usp=drive_link&ouid=112386923811420787147&rtpof=true&sd=true"),
                        block = {
                            withStyle(style = SpanStyle(color = customColors.dodgerBlue)) {
                                append("política de privacidad")
                            }
                        }
                    )

                    append(" y ")
                    withLink(
                        link = LinkAnnotation.Url("https://docs.google.com/document/d/1-Ygr-EKdycfTNnz_CgFZQ4AEyijWASS4/edit?usp=drive_link&ouid=112386923811420787147&rtpof=true&sd=true"),
                        block = {
                            withStyle(style = SpanStyle(color = customColors.dodgerBlue)) {
                                append("términos de uso.")
                            }
                        }

                    )
                    pop()
                }
                Text(
                    text = annotatedString,
                    fontSize = 13.sp,
                    lineHeight = 13.sp,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .alpha(0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }


    }
}

@Composable
fun CredentialsInput(
    onSignIn: (email: String, password: String, ok: Boolean, errorMessage: String) -> Unit = {_,_,_,_ ->}
){

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false)}

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }

    OptionCard(
        header = "Ingresa los datos para entrar:"
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            BasicTextField(
                value = email,
                onValueChange = { email = it; emailError = false },
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .onFocusEvent {

                    },
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (email.isEmpty()) {
                            Text(
                                text = "Correo",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                lineHeight = 14.sp
                            )
                        }
                        innerTextField()  // This is where the actual text input is displayed
                    }
                }
            )
            AnimatedVisibility(visible = emailError) {
                Icon(
                    painter = painterResource(R.drawable.error),
                    contentDescription = "Error",
                    tint = Color.Red,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = password,
                onValueChange = { password = it; passwordError = false },
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .onFocusEvent {

                    },
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (password.isEmpty()) {
                            Text(
                                text = "Contraseña",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                lineHeight = 14.sp
                            )
                        }
                        innerTextField()  // This is where the actual text input is displayed
                    }
                }
            )

            IconButton(
                onClick = { showPassword = !showPassword },
            ) {
                Icon(
                    painter = if (showPassword) painterResource(R.drawable.visibility) else painterResource(
                        R.drawable.visibility_off
                    ),
                    contentDescription = "Show password",
                    tint = Color.Gray,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }

            AnimatedVisibility(visible = passwordError) {
                Icon(
                    painter = painterResource(R.drawable.error),
                    contentDescription = "Error",
                    tint = Color.Red,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }

        }



    }

    OutlinedButton (onClick = {
        if(email.isEmpty()){
            errorMessage = "El correo no puede estar vacío"
            emailError = true
            onSignIn("", "", false, errorMessage)
        }
        else if(password.isEmpty()){
            errorMessage = "La contraseña no puede estar vacía"
            passwordError = true
            onSignIn("", "", false, errorMessage)
        }
        else if(password.length < 6 ){
            errorMessage = "La contraseña debe tener al menos 6 caracteres"
            passwordError = true
            onSignIn("", "", false, errorMessage)
        }
        else{
            onSignIn(email, password, true, "")
        }


    }, shape = RoundedCornerShape(10.dp)){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Entrar")
            Spacer(modifier = Modifier.size(4.dp))
            Icon(
                painter = painterResource(R.drawable.login_24dp_5f6368_fill0_wght400_grad0_opsz24),
                contentDescription = "logIn"
            )
        }
    }

}