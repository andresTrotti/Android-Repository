package com.snapcompany.snapsafe.views.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp

@Composable
@Preview
fun AutoResizableText(
    modifier: Modifier = Modifier,
    text: String = "text",
    style: TextStyle = TextStyle(fontSize = 32.sp),
    color: Color = MaterialTheme.colorScheme.onBackground,

    ){

    var resizableTextStyle by remember {
        mutableStateOf(style)
    }
    var shouldDraw by remember { mutableStateOf(false) }

    val defaultFontSize = 32.sp

    Text(
        text = text,
        maxLines = 2,
        style = resizableTextStyle,
        color = color,
        softWrap = true,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Normal,
        modifier = modifier.drawWithContent {
            if(shouldDraw) drawContent()
        },
        onTextLayout = {textLayoutResult ->

            if (textLayoutResult.didOverflowHeight) {
                if(style.fontSize.isUnspecified) {
                    resizableTextStyle = resizableTextStyle.copy(
                        fontSize = defaultFontSize
                    )
                }
                resizableTextStyle = resizableTextStyle.copy(fontSize = resizableTextStyle.fontSize * 0.9)
            }
            else{
                shouldDraw = true
            }
        }
    )
}
