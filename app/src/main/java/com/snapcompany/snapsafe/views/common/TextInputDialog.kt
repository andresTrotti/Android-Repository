package com.snapcompany.snapsafe.views.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
@Preview
fun TextInputDialog(
    title: String = "Title",
    message: String = "Message",
    defaultValue: String = "Default value",
    onDismiss: () -> Unit = {},
    onConfirm: (inputText: String) -> Unit = {},
){
    var inputText by remember { mutableStateOf(defaultValue) }
    Dialog(onDismissRequest = { onDismiss() }) {

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(16.dp)
            ){
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextField(
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 16.dp),
                    value = inputText, onValueChange ={
                        inputText = it
                    })


                Row(
                horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = { onDismiss() }) {
                        Text(text = "Cancelar")
                    }
                    Button(onClick = { onConfirm(inputText.trim()) }) {
                        Text(text = "Aceptar")
                    }
                }
            }
        }
    }
}