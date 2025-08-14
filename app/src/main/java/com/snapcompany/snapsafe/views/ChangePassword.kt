package com.snapcompany.snapsafe.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapcompany.snapsafe.R
import com.snapcompany.snapsafe.models.ControlModel
import com.snapcompany.snapsafe.utilities.textAsciiValidation
import com.snapcompany.snapsafe.views.common.OptionCard
import kotlinx.coroutines.delay

@Composable
fun ChangePassword(
    controlModel: ControlModel
) {
    ChangePasswordPreview()
}


@Composable
@Preview(showSystemUi = true)
fun ChangePasswordPreview(
    sheetHeight: Int = 1000,
    enabled: Boolean = false,
    onShowAlert: (message: String) -> Unit = {},
    onSaveNewPassword: (newPassword: String) -> Unit = {}
) {

    var newPassword by remember { mutableStateOf("") }
    var newPasswordConfirm by remember { mutableStateOf("") }

    var blinking by remember { mutableStateOf(true) }
    val alpha by animateFloatAsState(
        targetValue = if (blinking) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    LaunchedEffect(enabled) {
        if(!enabled) onShowAlert("No estas conectado. Intenta acercándote un poco más.")
    }

    Column(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, top = 16.dp)
            .height(sheetHeight.dp)
    ) {
        Text(
            text = "Cambio de clave",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp)
        )

        OptionCard(
            header = "Debe ser de 8 dígitos"
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {

                GatePasswordTextField(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 4.dp, bottom = 4.dp, start = 16.dp, end = 8.dp),
                    hintText = "Nueva clave",
                    enabled = enabled,
                    onShowAlert = {
                        onShowAlert(it)
                    },
                    onNewPasswordValue = {
                        newPassword = it
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
                GatePasswordTextField(
                    modifier = Modifier.padding(
                        top = 4.dp,
                        bottom = 4.dp,
                        start = 16.dp,
                        end = 8.dp
                    ),
                    hintText = "Confirmar clave",
                    enabled = enabled,
                    onNewPasswordValue = {
                        newPasswordConfirm = it
                    },
                    onShowAlert = {
                        onShowAlert(it)
                    }
                )
            }
        }

        Button(
            enabled = enabled,
            onClick = {
                if(newPassword.isNotBlank()) {
                    if (newPassword == newPasswordConfirm) {
                        onSaveNewPassword(newPassword)
                    } else {
                        onShowAlert("Las contraseñas no coinciden")
                    }
                }
                else{
                    onShowAlert("Por favor rellena los campos.")
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Guardar cambios"
            )
        }

        if(!enabled) {

            LaunchedEffect(Unit) {
                while (true) {
                    delay(1000) // Adjust delay as needed
                    blinking = !blinking
                }
            }

            Icon(
                painter = painterResource(R.drawable.signal_disconnected),
                contentDescription = "Warning",
                tint = Color.Gray.copy(alpha = alpha),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
            )
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GatePasswordTextField(
    modifier: Modifier = Modifier,
    hintText: String = "placeHolder",
    enabled: Boolean = false,
    maxLength: Int = 8,
    onShowAlert: (message: String) -> Unit = {},
    onNewPasswordValue: (value: String) -> Unit = {},

) {
    var newPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isOnFocus by remember { mutableStateOf(false) }
    var currentPasswordLength by remember { mutableIntStateOf(0) }
    var alertMessage by remember { mutableStateOf("") }


    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {


        BasicTextField(
            modifier = Modifier
                .weight(weight = 1f)
                .onFocusEvent {
                    isOnFocus = it.isFocused
                },
            enabled = enabled,
            value = newPassword,
            textStyle = TextStyle(fontSize = 14.sp),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            onValueChange = {
                if (textAsciiValidation(it)) {
                    currentPasswordLength = it.length
                    if (currentPasswordLength <= maxLength) {
                        newPassword = it
                        onNewPasswordValue(it)
                    }
                }
            },
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (newPassword.isEmpty()) {
                        Text(
                            text = hintText,
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


        AnimatedVisibility(visible = !isOnFocus && currentPasswordLength < 8 && currentPasswordLength != 0) {

            IconButton(onClick = {
                alertMessage = "La clave debe tener 8 caracteres"
                onShowAlert(alertMessage)
            }) {
                Icon(
                    painter = painterResource(R.drawable.error),
                    contentDescription = "Error",
                    tint = Color.Red
                )
            }
        }

    }

}



