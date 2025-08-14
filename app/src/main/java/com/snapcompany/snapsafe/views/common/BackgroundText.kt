package com.snapcompany.snapsafe.views.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
@Preview(showBackground = true)
fun BackgroundText(
    modifier: Modifier = Modifier,
    text: String = "Cargando...",

) {
    Box(
        modifier = modifier
    ){
        Text(
            text = text,
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}