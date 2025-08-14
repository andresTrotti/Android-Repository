package com.snapcompany.snapsafe.views

import android.widget.Space
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.snapcompany.snapsafe.R
import com.snapcompany.snapsafe.models.ControlModel
import com.snapcompany.snapsafe.views.common.AutoResizableText

@Composable
fun OperationReport(
    navController: NavController,
    controlModel: ControlModel
) {
    val controlUiState by controlModel.uiState.collectAsState()
    OperationReportPreview()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showSystemUi = true)
fun OperationReportPreview(
    sheetHeight: Int = 1000,
    onBack: () -> Unit = {},
    infoMessage: Int = R.string.report,
    opens: String = "",
    closes: String = "",
    stops: String = "",
    position: String = "",
    infraredState: Boolean = false,
    stopOnOpenState: Boolean = false,
    closeOnPassState: Boolean = false,
    timeToPass: Int = 5,
    brakeLevel: Int = 50,
    onInfraredChange: (value: Boolean) -> Unit = {},
    onStopOnOpen: (value: Boolean) -> Unit = {},
    onCloseOnPass: (value: Boolean) -> Unit = {},
    onSetTime: () -> Unit = {},
    onSetBrake: () -> Unit = {},
    onUpdateTime: (value: Int) -> Unit = {},
    onUpdateBrake: (value: Int) -> Unit = {}
) {


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
            AnimatedContent(
                targetState = stringResource(id = infoMessage),
                label = ""
            ) { newMessage ->

                if(infoMessage == R.string.time_adjusted_to){
                    AutoResizableText(
                        text = "$newMessage $timeToPass segundos",
                        style = TextStyle(fontSize = 24.sp)
                    )
                }
                else {
                    AutoResizableText(
                        text = newMessage,
                        style = TextStyle(fontSize = 24.sp)
                    )
                }
            }
        }


    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())

    ) {

        ReportSection(
            header = stringResource(R.string.report_section_header),
            opens = opens,
            closes = closes,
            stops = stops,
            position = position
        )

        Column(

        ) {

            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                )
            ) {

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                        .alpha(0.7f),
                    text = stringResource(R.string.footer_report_section),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        ToggleRow(
                            rowText = "Usar infrarrojos",
                            checked = infraredState,
                            onCheckedChange = { onInfraredChange(it) }
                        )
                        ToggleRow(
                            rowText = "Siempre detener luego de abrir",
                            checked = stopOnOpenState,
                            onCheckedChange = { onStopOnOpen(it) }
                        )
                        ToggleRow(
                            rowText = "Cerrar al pasar",
                            checked = closeOnPassState,
                            onCheckedChange = { onCloseOnPass(it) }
                        )
                        SliderRow(
                            title = "Tiempo antes de cerrar:",
                            textButton = "Ajustar tiempo",
                            startText = "5s",
                            endText = "50s",
                            valueRange = 5f..50f,
                            sliderPosition = timeToPass.toFloat(),
                            onSliderChange = { onUpdateTime(it.toInt()) },
                            onSet = onSetTime
                        )

                        Spacer(modifier = Modifier.size(24.dp))

                        SliderRow(
                            title = "Intensidad del frenado",
                            textButton = "Ajustar freno",
                            startText = "0 %",
                            endText = "100 %",
                            valueRange = 0f..100f,
                            sliderPosition = brakeLevel.toFloat(),
                            onSliderChange = {onUpdateBrake(it.toInt())},
                            onSet = onSetBrake
                        )
                    }
                }

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                        .alpha(0.7f),
                    text = stringResource(R.string.must_be_close_for_apply_changes),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }

}
}

@Composable
fun ToggleRow(
    rowText: String = "",
    checked: Boolean = false,
    iconButton: Int? = null,
    iconTint: Color? = null,
    onCheckedChange: (value: Boolean) -> Unit = {},
    onIconButton: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconButton != null) {
            IconButton(onClick = onIconButton) {
                Icon(
                    painter = painterResource(id = iconButton),
                    contentDescription = null,
                    tint = iconTint ?: MaterialTheme.colorScheme.onBackground,
                    //modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
        Text(
            text = rowText,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            textAlign = TextAlign.Left,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = {
                onCheckedChange(it)
            }
        )
    }
}

@Composable
fun ReportRow(
    rowText: String = "",
    info: String = "-",
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rowText:",
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        )

        Text(
            text = info,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SliderRow(
    title: String = "",
    textButton: String = "",
    startText: String = "5s",
    endText: String = "50s",
    valueRange: ClosedFloatingPointRange<Float> = 5f..50f,
    sliderPosition: Float = 0f,
    onSliderChange: (value: Float) -> Unit = {},
    onSet: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$title ${sliderPosition.toInt()}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = startText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.size(16.dp))

            Slider(
                modifier = Modifier.weight(1f),
                value = sliderPosition,
                valueRange = valueRange,
                onValueChange = { onSliderChange(it) }
            )

            Spacer(modifier = Modifier.size(16.dp))

            Text(
                text = endText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )

        }
        Spacer(modifier = Modifier.size(8.dp))
        Button(onClick = { onSet() }) {
            Text(text = textButton)
        }
    }


}




@Composable
fun ReportSection(
    header: String = "",
    footer: String = "",
    opens: String = "",
    closes: String = "",
    stops: String = "",
    position: String = "",
) {
    Column(
    ) {

        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
            )
        ) {

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                    .alpha(0.7f),
                text = header,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 0.dp, max = 150.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    ReportRow("Posicion actual", position)
                    ReportRow("Aperturas", opens)
                    ReportRow("Cierres", closes)
                    ReportRow("Paradas", stops)
                }
            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                    .alpha(0.7f),
                text = footer,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}