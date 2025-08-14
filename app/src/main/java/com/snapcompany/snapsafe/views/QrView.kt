package com.snapcompany.snapsafe.views

import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.snapcompany.snapsafe.R
import com.snapcompany.snapsafe.models.ControlModel
import com.snapcompany.snapsafe.utilities.QrCodeAnalyzer

@Composable
fun QrView(navController: NavController, controlModel: ControlModel){

    var code by remember{
        mutableStateOf("")
    }
    val lifeCycleOwner = LocalLifecycleOwner.current

    val context = LocalContext.current
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    var hasCamPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(contract = RequestPermission()) { granted ->
        hasCamPermission = granted
    }

    LaunchedEffect(key1 = true) {
        launcher.launch(android.Manifest.permission.CAMERA)
    }
    LaunchedEffect(key1 = code) {
        if(code != "") {
            navController.navigateUp()

            val newGateData = controlModel.qrCodeToGateData(code)
            if(newGateData != null) {
                if (!controlModel.gateDataExists(newGateData)) {
                    controlModel.updateNewGateData(newGateData)
                    controlModel.updateShowNewGateDialog(true)
                } else {
                    controlModel.showGlobalAlert(
                        "Ya existe.",
                        "Estas intentando guardar un dispositivo que ya existe."
                    )
                }
            }
            else {
                controlModel.showGlobalAlert(
                    "Error.",
                    "El código QR no es válido."
                )
            }


        }
    }

    Column {

        if(hasCamPermission) {
            AndroidView(factory = { context ->

                val previewView = PreviewView(context)
                val preview = Preview.Builder().build()
                val selecter = CameraSelector.Builder().requireLensFacing(
                    CameraSelector.LENS_FACING_BACK
                ).build()

                preview.setSurfaceProvider(previewView.surfaceProvider)
                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(
                        Size(
                            previewView.width,
                            previewView.height)
                    ).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()


                imageAnalysis.setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    QrCodeAnalyzer{ result ->
                        code = result
                    }
                )
                try {
                    cameraProviderFuture.get().bindToLifecycle(
                        lifeCycleOwner,
                        selecter,
                        preview,
                        imageAnalysis
                    )
                }catch (e: Exception){
                    e.printStackTrace()
                }
                previewView
            },
                modifier = Modifier.weight(1f)
            )
        }
        else{
            Text(text = stringResource(id = R.string.camera_permissions_needed))
        }
     }
}