package com.snapcompany.snapsafe.views.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapcompany.snapsafe.views.OptionRow

enum class GateSettingsOptions{
    SHOW_REPORT,
    CHANGE_IMAGE_OR_NAME,
    MANAGE_EXTENSIONS,
    SHOW_QR,
    SHOW_RECORDS,
    OPEN_ON_CONNECT,
    CHANGE_PASSWORD,
    CHANGE_PROPERTY_PASSWORD,
    DELETE_GATE,
}

@Composable
fun OptionCard(
    header: String = "",
    footer: String = "",
    options: @Composable () -> Unit = {},
){

    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    ) {

        if(header != "") {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                    .alpha(0.7f),
                text = header,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,

                )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                //.heightIn(min = 0.dp, max = 150.dp)

        ) {
            options()
        }

        if(footer != "") {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 4.dp, top = 4.dp)
                    .alpha(0.7f),
                text = footer,
                lineHeight = 12.sp,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}