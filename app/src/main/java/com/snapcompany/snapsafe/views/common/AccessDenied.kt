package com.snapcompany.snapsafe.views.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapcompany.snapsafe.R

@Composable
@Preview(showSystemUi = true)
fun AccessDenied(reason: String = "Estás inhabilitado."){
    val customColors = CustomColors()

    Box(
        modifier = Modifier.wrapContentSize(),

    ){
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.lock),
                contentDescription = "lock",
                tint = customColors.crimson,
                modifier = Modifier.size(72.dp)
            )

            Spacer(Modifier.size(8.dp))

            Text(
                text = reason,
                color = customColors.crimson,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

}
