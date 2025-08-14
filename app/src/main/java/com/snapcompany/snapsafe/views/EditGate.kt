package com.snapcompany.snapsafe.views

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.snapcompany.snapsafe.R
import com.snapcompany.snapsafe.models.ControlModel
import com.snapcompany.snapsafe.utilities.CacheUtilities
import com.snapcompany.snapsafe.utilities.GateData
import com.snapcompany.snapsafe.views.common.OptionCard
import kotlinx.coroutines.launch

@Composable
fun EditGate(
    navController: NavController,
    controlModel: ControlModel
){
    val controlUiState by controlModel.uiState.collectAsState()
    val context = LocalContext.current


    EditGatePreview(
        gateData = controlModel.currentGateData,
        onBack = {
            navController.navigateUp()
        },
        onSaveImage = { newImageUri ->
            if(newImageUri != null) {
                val currentGateList = controlUiState.gateList
                val currentGateData =
                    currentGateList.find { it.gateId == controlModel.currentGateData.gateId }

                if(currentGateData != null) {
                    val uniqueName = "image" + currentGateData.gateId

                    controlModel.cacheUtilities.saveImageToCache(
                        context = context,
                        uniqueName = uniqueName,
                        imageUri = newImageUri
                    )
                    val imageCacheUri = controlModel.cacheUtilities.loadImage(context, uniqueName)

                    currentGateData.apply { imageUri = imageCacheUri.toString() }

                    Log.d("GateList", "imageUri: $newImageUri")

                    controlModel
                        .viewModelScope.launch { controlModel.appDao.updateGate(currentGateData) }
                }
                else{
                    Log.e("EditGatePreview", "currentGateData is null")
                }
            }
            else{
                Log.e("EditGatePreview", "newImageUri is null")
            }
        },
        onNameChange = { newName ->
            val currentGateList = controlUiState.gateList
            val currentGateData = currentGateList.find { it.gateId == controlModel.currentGateData.gateId }

            if(currentGateData != null) {
                currentGateData.apply { gateName = newName }
                controlModel.viewModelScope.launch { controlModel.appDao.updateGate(currentGateData) }
            }
            else{
                Log.e("EditGatePreview", "currentGateData is null")
            }

        },
        onDeleteImage = {
            val currentGateList = controlUiState.gateList
            val currentGateData = currentGateList.find { it.gateId == controlModel.currentGateData.gateId }

            if(currentGateData != null) {
                currentGateData.apply { imageUri = null }
                controlModel.viewModelScope.launch { controlModel.appDao.updateGate(currentGateData) }
            }

        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun EditGatePreview(
    gateData: GateData = GateData(),
    onBack: () -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onSaveImage: (Uri?) -> Unit = {},
    onDeleteImage: () -> Unit = {}
){
    if(gateData.gateName == "") gateData.apply { gateName = "Agregar nombre" }


    val viewTitle = remember { mutableStateOf("Editar: ${gateData.gateName}") }
    val newImageUri = remember { mutableStateOf(gateData.imageUri?.toUri()) }


    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            val uri = result.data?.data // The URI of the selected image
            // Handle the selected image URI
            Log.d("EditGate", "imagePicker uri selected: $uri, data: ${result.data}, path: ${uri?.path}")
            newImageUri.value = uri
            onSaveImage(uri)
            //gateData.apply { gateImageUri = uri.toString() }

        }
    }


    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = viewTitle.value) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            painterResource(id = R.drawable.arrow_back),
                            contentDescription = "back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier.padding(innerPadding)
        ) {

            OptionCard(
                header = "Editar nombre",
                options = {
                    ChangeName(
                        gateData = gateData,
                        onNameChange = onNameChange,
                        onViewTitleChange = { viewTitle.value = it }
                    )
                }
            )

            OptionCard(
                header = "Editar imagen",
                footer = "Asigna una imagen que te permita identificar rapidamente a que dispositivo estás dirigiendo la órden.",
                options = {

                    OptionRow(
                        text = "Seleccionar nueva imagen",
                        action = {
                            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            imagePickerLauncher.launch(intent)
                        }
                    )

                    Column (horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()){

                        AsyncImage(
                            model = newImageUri.value,
                            modifier = Modifier.fillMaxWidth().height(200.dp).padding(horizontal = 16.dp).clip(RoundedCornerShape(16.dp)),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            filterQuality = FilterQuality.Low
                        )



                        Row(horizontalArrangement = Arrangement.SpaceAround) {

                            IconButton(onClick = {
                                newImageUri.value = null
                            }) {
                                Icon(
                                    painterResource(id = R.drawable.edit),
                                    contentDescription = "edit",
                                )
                            }

                            IconButton(onClick = {
                                newImageUri.value = null
                            }) {
                                Icon(
                                    painterResource(id = R.drawable.add_photo_alternate),
                                    contentDescription = "camera",)
                            }

                            IconButton(onClick = onDeleteImage) {
                                Icon(
                                    painterResource(id = R.drawable.delete),
                                    contentDescription = "delete",)
                            }

                            /*TextButton(onClick = {
                                //onSaveImage(newImageUri.value)
                            }) {
                                Text(text = "Editar")
                            }
                            TextButton(onClick = {
                                onSaveImage(newImageUri.value)
                            }) {
                                Text(text = "Asignar imagen")
                            }*/
                        }

                    }
                }
            )


        }
    }
}


@Composable
fun ChangeName(gateData: GateData, onNameChange: (String) -> Unit, onViewTitleChange: (String) -> Unit){

    val nameTextFieldValue = remember { mutableStateOf(gateData.gateName) }
    val nameTextFieldFocus = remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Row() {
        BasicTextField(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .weight(1f)
                .onFocusEvent {
                    nameTextFieldFocus.value = it.isFocused
                    if (!it.isFocused && nameTextFieldValue.value == "") {
                        nameTextFieldValue.value = gateData.gateName
                    }
                },
            value = nameTextFieldValue.value,
            onValueChange = {
                if(it.length <= 25) {
                    nameTextFieldValue.value = it
                }
                else {
                    nameTextFieldValue.value = nameTextFieldValue.value.substring(0, 25)
                }
            },
        )
        AnimatedVisibility(visible = nameTextFieldValue.value != gateData.gateName && nameTextFieldValue.value != "") {
            IconButton(onClick = {
                onNameChange(nameTextFieldValue.value)
                nameTextFieldFocus.value = false
                focusManager.clearFocus()
                gateData.apply { gateName = nameTextFieldValue.value }
                onViewTitleChange("Editar: ${gateData.gateName}")

            }) {
                Icon(
                    painterResource(id = R.drawable.done),
                    contentDescription = "save",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        AnimatedVisibility(visible = nameTextFieldFocus.value) {
            IconButton(onClick = {
                focusManager.clearFocus()
                nameTextFieldValue.value = gateData.gateName
                nameTextFieldFocus.value = false
            }) {
                Icon(
                    painterResource(id = R.drawable.close),
                    contentDescription = "close",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

        }

    }
}
