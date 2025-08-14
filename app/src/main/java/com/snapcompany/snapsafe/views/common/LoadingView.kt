package com.snapcompany.snapsafe.views.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun LoadingView(){

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.7f)
            .background(color =  Color.Gray),

        contentAlignment = Alignment.Center
    ){
        CircularProgressIndicator()
    }

}
