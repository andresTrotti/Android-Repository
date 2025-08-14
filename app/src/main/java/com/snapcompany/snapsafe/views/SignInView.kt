package com.snapcompany.snapsafe.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.credentials.Credential
import androidx.navigation.NavController
import com.snapcompany.snapsafe.R
import com.snapcompany.snapsafe.models.ControlModel
import com.snapcompany.snapsafe.utilities.Firestore
import com.snapcompany.snapsafe.utilities.GoogleSignInClient
import com.snapcompany.snapsafe.utilities.RoomKey
import com.snapcompany.snapsafe.utilities.UserDataAppDb
import com.snapcompany.snapsafe.views.common.CustomColors
import com.snapcompany.snapsafe.views.common.LoadingView
import com.snapcompany.snapsafe.views.common.OptionCard
import com.snapcompany.snapsafe.views.common.ResultView
import kotlinx.coroutines.launch


@Composable
fun SignInView(
    navController: NavController,
    controlModel: ControlModel
){
    
    val googleSignInClient = GoogleSignInClient(LocalContext.current)
    val coroutineScope = rememberCoroutineScope()

    var titleAlert by remember {mutableStateOf("")}
    var messageAlert by remember {mutableStateOf("")}
    var showAlert by remember {mutableStateOf(false)}

    var loading by remember { mutableStateOf(false)}
    var showResult by remember { mutableStateOf(false) }
    var resultToShow by remember { mutableStateOf(false) }

    SignInViewPreview(
        onBack = { navController.navigateUp() },
        onGetCredentialResponseResult = {

        },
        onGoogleAuth = {
            coroutineScope.launch {
                googleSignInClient.signIn()
            }

        },
        onSignUp = { name, phone, email, password ->
            loading = true
            controlModel.firestore.signUp(name, phone, email, password){ success, message, userData ->

                    if (success && userData != null) {

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

                            resultToShow = true
                            showResult = true
                        }
                    }
                    else{
                        titleAlert = "Ocurrio un problema"
                        messageAlert = message
                        showAlert = true
                    }

                loading = false

            }
        }
    )

    AnimatedVisibility(visible = loading, enter = fadeIn(), exit = fadeOut()) {
        LoadingView()
    }

    AnimatedVisibility(visible = showResult, enter = fadeIn(), exit = fadeOut()) {
        ResultView(
            result = resultToShow
        ) {
            showResult = false
            navController.navigateUp()
        }
    }


    if(showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun SignInViewPreview(
    onBack: () -> Unit = {},
    onGetCredentialResponseResult: (Credential) -> Unit = {},
    onGoogleAuth: () -> Unit = {},
    onSignUp: (name: String, phone: String, email: String, password: String) -> Unit = { _, _, _, _ ->}
){


    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var titleAlert by remember {mutableStateOf("")}
    var messageAlert by remember {mutableStateOf("")}
    var showAlert by remember {mutableStateOf(false)}

    var nameError by remember { mutableStateOf(false) }
    //var emailError by remember { mutableStateOf(false)}
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }

    val customColors = CustomColors()


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Registro")
                },
                navigationIcon  = {
                    IconButton(
                        onClick = { onBack() }
                    ){
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            OptionCard(
                header = "INFORMACION BÁSICA",
                options = {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = name,
                            onValueChange = { name = it; nameError = false },
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
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
                                    if (name.isEmpty()) {
                                        Text(
                                            text = "Nombre y apellido",
                                            color = Color.Gray,
                                            fontSize = 14.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                    innerTextField()  // This is where the actual text input is displayed
                                }
                            }
                        )
                        AnimatedVisibility(visible = nameError) {
                            Icon(
                                painter = painterResource(R.drawable.error),
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                    BasicTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                                if (phone.isEmpty()) {
                                    Text(
                                        text = "Teléfono (opcional)",
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
            )



            OptionCard(
                header = "CORREO Y CONTRASEÑA"
            ){

                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = email,
                        onValueChange = { email = it },
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
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

                    /*AnimatedVisibility(visible = emailError) {
                        Icon(
                            painter = painterResource(R.drawable.error),
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }*/
                }
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = password,
                        onValueChange = { password = it; passwordError = false },
                        textStyle = TextStyle(color = if(passwordError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground),
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
                            tint = Color.Gray
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
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {

                    BasicTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; confirmPasswordError = false },
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        textStyle = TextStyle(color = if(confirmPasswordError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
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
                                if (confirmPassword.isEmpty()) {
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
                        onClick = { showConfirmPassword = !showConfirmPassword },
                    ) {
                        Icon(
                            painter = if (showConfirmPassword) painterResource(R.drawable.visibility) else painterResource(
                                R.drawable.visibility_off
                            ),
                            contentDescription = "Show password",
                            tint = Color.Gray
                        )
                    }

                    AnimatedVisibility(visible = confirmPasswordError) {
                        Icon(
                            painter = painterResource(R.drawable.error),
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            }


            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 32.dp),
                shape = RoundedCornerShape(10.dp),
                onClick = {

                    if(name.isEmpty()){
                        nameError = true
                        messageAlert = "El nombre no puede estar vacío"
                        titleAlert = "Revisar nombre"
                        showAlert = true
                    }
                    else if(name.length < 3){
                        nameError = true
                        messageAlert = "El nombre es muy corto"
                        titleAlert = "Revisar nombre"
                        showAlert = true
                    }
                    else if(password.isEmpty()){
                        passwordError = true
                        messageAlert = "Introduce una contraseña"
                        titleAlert = "Revisar contraseña"
                        showAlert = true
                    }
                    else if(password.length < 6){
                        passwordError = true
                        messageAlert = "La contraseña es muy. Debe tener al menos 6 dígitos"
                        titleAlert = "Revisar contraseña"
                        showAlert = true
                    }
                    else if(confirmPassword.isEmpty()) {
                        confirmPasswordError = true
                        messageAlert = "Confirma la contraseña"
                        titleAlert = "Revisar contraseña"
                        showAlert = true
                    }
                    else if(confirmPassword != password){
                        confirmPasswordError = true
                        messageAlert = "Las contraseñas no coinciden"
                        titleAlert = "Revisar contraseña"
                        showAlert = true
                    }
                    else{
                        onSignUp(name.trim(), phone.trim(), email.trim(), password.trim())
                    }

                },
            ){
                Text(
                    text = "Aceptar términos y registrarse",
                    maxLines = 3
                )
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
            Text(text = annotatedString,
                fontSize = 13.sp,
                lineHeight = 13.sp,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .alpha(0.7f),
                textAlign = TextAlign.Center
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
    }
}