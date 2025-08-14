package com.snapcompany.snapsafe.views


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapcompany.snapsafe.utilities.GateData
import com.snapcompany.snapsafe.utilities.QrCodeView
import com.snapcompany.snapsafe.views.common.AutoResizableText

@Composable
@Preview
fun ShareQrView(
    sheetHeight: Int = 100,
    encryptedText: String? = null,
    gateName: String = "Gate Name",
    
) {
   // var shareAsGuest by remember { mutableStateOf(true) }


    Column(
        modifier = Modifier
            .height(sheetHeight.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 16.dp)
                .padding(top = 4.dp)
        ) {
            AutoResizableText(
                text = "Qr de $gateName",
                style = TextStyle(fontSize = 24.sp)
            )
        }

        /*Row(
            Modifier.padding(horizontal = 16.dp)
        ) {
            ToggleRow(
                rowText = "Compartir como invitado",
                checked = false,
                onCheckedChange = { }
            )
        }*/

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                /*ToggleRow(
                    rowText = "Permitir acceso como " + if(shareAsGuest) "invitado (acceso limitado)" else "dueño (acceso completo)",
                    checked = shareAsGuest,
                    onCheckedChange = { shareAsGuest = it }

                )*/

                    QrCodeView(
                        encryptedText = encryptedText,
                        size = LocalConfiguration.current.screenWidthDp,
                    )



            }
        }
        Text(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            fontSize = 12.sp,
            lineHeight = 12.sp,
            color = Color.Gray,
            text= "No compartas este código con invitados ni personas desconocidas en cambio" +
                    " se recomienda el uso de extensiones si aún deseas compartir el acceso a tu dispositivo.")
    }
}

