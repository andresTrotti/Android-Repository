package com.snapcompany.snapsafe.views.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.snapcompany.snapsafe.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance

@Composable
@Preview
fun OpenDoorButton(
    onClick: () -> Unit = {},
    onRelease: () -> Unit = {}
){
    var isPressed by remember {mutableStateOf(false)}
    val modifierWhenIsPressed = Modifier.doublePulseEffect(targetScale = 2f)



    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions
            .filterIsInstance<PressInteraction.Press>()
            .collect {
                isPressed = true
                delay(400)
                if(isPressed) onClick()
            }
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions
            .filterIsInstance<PressInteraction.Release>()
            .collect {
                isPressed = false
                delay(300)
                onRelease()
            }
    }

    Box(
        modifier = (if(isPressed) modifierWhenIsPressed else Modifier)
            .padding(24.dp)
            .size(120.dp)
            .background(shape = RoundedCornerShape(60.dp), color = if (isPressed) Color.Green else Color.LightGray)

            .clickable(interactionSource = interactionSource, indication = null) {
                // Handle click if needed
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.door_open_24dp),
            contentDescription = "Abrir puerta",
            modifier = Modifier.size(60.dp)
        )
    }


}