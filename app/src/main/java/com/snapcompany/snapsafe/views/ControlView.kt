package com.snapcompany.snapsafe.views

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.snapcompany.snapsafe.R
import com.snapcompany.snapsafe.models.ControlModel
import com.snapcompany.snapsafe.models.ControlUiState
import com.snapcompany.snapsafe.models.Orders
import com.snapcompany.snapsafe.models.currentDeviceAddress
import com.snapcompany.snapsafe.navigation.Views
import com.snapcompany.snapsafe.utilities.GateData
import com.snapcompany.snapsafe.views.common.AccessDenied
import com.snapcompany.snapsafe.views.common.AutoResizableText
import com.snapcompany.snapsafe.views.common.ImageFromUri
import com.snapcompany.snapsafe.views.common.OpenDoorButton

@Composable
fun ControlView(controlModel: ControlModel, navController: NavController) {
    val controlUiState by controlModel.uiState.collectAsState()


    val pagerState = rememberPagerState(initialPage = 0) {
        controlUiState.gateList.size
    }
    val launcher = rememberLauncherForActivityResult(contract = RequestPermission()) { granted ->
        controlModel.updateHasBlePermission(granted)
    }

    LaunchedEffect(controlUiState.bluetoothScanPermissionRequest) {
        controlModel.updateBluetoothScanPermissionRequest(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            launcher.launch(android.Manifest.permission.BLUETOOTH_SCAN)
        } else {
            launcher.launch(android.Manifest.permission.BLUETOOTH)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            launcher.launch(android.Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            launcher.launch(android.Manifest.permission.BLUETOOTH)
        }
    }

    LaunchedEffect(controlUiState.hasBlePermission) {
        if (controlUiState.hasBlePermission) {
            if (controlModel.currentGateData != GateData()) {
                controlModel.updateCurrentDeviceAndGatt()
                controlModel.checkBluetooth()
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {

        Log.d("PagerState", "CurrentPage: ${pagerState.currentPage}")
        if (controlUiState.gateList.isNotEmpty()) {
            if (controlModel.currentGateData != controlUiState.gateList[pagerState.currentPage]) {
                controlModel.currentGateData = controlUiState.gateList[pagerState.currentPage]
                currentDeviceAddress = controlModel.currentGateData.mac
                if(controlModel.currentGateData.independent) controlModel.updateCurrentGateEnableStatus(true, "")
                else {
                    controlModel.updateFirestoreLoading(true)
                    controlModel.checkEnableStatus(controlModel.currentGateData.gateId) { status, reason ->
                        controlModel.updateCurrentGateEnableStatus(status, reason)
                        controlModel.updateFirestoreLoading(false)
                    }
                }
                controlModel.updateCurrentDeviceAndGatt()
            }
        }
        else{ controlModel.endScan(); controlModel.updateIntInfoMessage(R.string.welcome) }

    }

    LaunchedEffect(Unit) {
        if (controlUiState.firstLaunch) {
            if (controlUiState.gateList.isNotEmpty() && controlUiState.hasBlePermission) {
                controlModel.currentGateData = controlUiState.gateList[0]
                currentDeviceAddress = controlModel.currentGateData.mac
                controlModel.updateCurrentDeviceAndGatt()
                controlModel.updateFirstLaunch(false)
            }
        }
    }

    val sharedExtensionFilter = controlUiState.sharedExtensions.filter { it.status == "pending" }

    ControlPreview(
        gateImagesList = controlModel.gateImagesList,
        controlModel = controlModel,
        controlUiState = controlUiState,
        pagerState = pagerState,
        firestoreLoading = controlUiState.firestoreLoading,
        sharedExtensionsCount = sharedExtensionFilter.count().toString(),
        onOrderButton = { controlModel.onBleOrder(it) },
        onNavRequest = { navController.navigate(it) },

    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
//@Preview()
private fun ControlPreview(
    gateImagesList: List<Uri?> = emptyList(),
    controlModel: ControlModel,
    controlUiState: ControlUiState = ControlUiState(),
    firestoreLoading: Boolean = false,
    sharedExtensionsCount: String = "1",
    pagerState: PagerState,
    onOrderButton: (order: Orders) -> Unit = {},
    onNavRequest: (view: String) -> Unit = {},

    ) {
    val views = Views()
    var loadingView by remember { mutableStateOf(false)}

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = {
                    Text("SnapSafe")
                },
                actions = {
                    IconButton(onClick = { onNavRequest(views.newGate) }) {

                        BadgedBox(
                            badge = {
                                this@TopAppBar.AnimatedVisibility(
                                    visible = sharedExtensionsCount != "0" && !firestoreLoading,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    Badge {
                                        Text(
                                            sharedExtensionsCount,
                                            modifier =
                                            Modifier.semantics {
                                                contentDescription =
                                                    "$sharedExtensionsCount new notifications"
                                            }
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.add_box),
                                contentDescription = "add new gate",
                                tint = if (controlUiState.gateList.isNotEmpty()) MaterialTheme.colorScheme.onBackground
                                else Color.Green
                            )
                        }

                    }



                        AnimatedVisibility(visible = controlUiState.userData.profileImageUrl != null && controlUiState.userData.profileImageUrl != "",
                            enter = fadeIn()) {
                            AsyncImage(
                                model = controlUiState.userData.profileImageUrl,
                                contentDescription = "account",
                                filterQuality = FilterQuality.Low,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        onNavRequest(views.account)
                                    }
                            )
                        }

                   AnimatedVisibility(visible = controlUiState.userData.profileImageUrl == null || controlUiState.userData.profileImageUrl == ""){
                        IconButton(onClick = { onNavRequest(views.account) }) {

                            Icon(
                                painter = painterResource(id = R.drawable.person),
                                contentDescription = "account",
                                tint = MaterialTheme.colorScheme.onBackground

                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            ) {
                AnimatedContent(
                    targetState = stringResource(id = controlUiState.infoMessage),
                    label = ""
                ) { newMessage ->

                    if(controlUiState.switchToInfoMessageString){
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AutoResizableText(
                                text = controlUiState.infoMessageString
                            )
                            IconButton(onClick = {onOrderButton(Orders.RESET_COUNTDOWN)}) {
                                Icon(
                                    painter = painterResource(R.drawable.restart_alt),
                                    contentDescription = "reset timer",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .align(Alignment.CenterVertically)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .padding(5.dp)

                                )
                            }
                        }
                    }
                    else {
                        AutoResizableText(
                            text = newMessage
                        )
                    }
                }
            }

            if (controlUiState.gateList.isEmpty()) {
                EmptyGateList()
            } else {

                    HorizontalPager(state = pagerState, beyondViewportPageCount = 10,) { index ->

                        Log.d("ControlView", "Image card loaded")

                        ImageCard(
                            imageUri = gateImagesList[index],
                            gateData = controlUiState.gateList[index],
                            onSettingButton = {
                                onNavRequest(views.gateSettings)
                            },
                        )
                    }



                AnimatedVisibility (visible = !controlUiState.currentGateEnableStatus, enter = fadeIn(), exit = fadeOut()) {
                    AccessDenied(reason = controlUiState.currentDisableReason)
                }
                AnimatedVisibility (visible = controlUiState.currentGateEnableStatus, enter = fadeIn(), exit = fadeOut()) {

                    if ( controlUiState.gateList.getOrNull(pagerState.currentPage) != null) {
                        if(controlUiState.gateList[pagerState.currentPage].gateType == "gate") {
                            ControlButtons(onOrderButton = { onOrderButton(it) })
                        }
                        else if(controlUiState.gateList[pagerState.currentPage].gateType == "door"){
                            OpenDoorButton(
                                onClick = { onOrderButton(Orders.OPEN_DOOR); Log.d("ControlView", "Open door button pressed") },
                                onRelease = {onOrderButton(Orders.LOCK_DOOR); Log.d("ControlView", "Open door button released")}
                            )
                        }

                    }
                    else {
                       Log.e("ControlView", "Your trying to access to a gate that doesnot exists anymore")
                    }

                }
            }
        }
    }
        .run {
            loadingView = false
        }
}

@Composable
fun ControlButtons(onOrderButton: (order: Orders) -> Unit = {}){
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                onClick = { onOrderButton(Orders.OPEN) },
                modifier = Modifier
                    .width(150.dp)
                    .height(75.dp),
                shape = RoundedCornerShape(20.dp)
            ) {

                Text(
                    text = stringResource(id = R.string.open),
                    fontSize = 18.sp
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween

            ) {
                Button(
                    onClick = { onOrderButton(Orders.STOP) },
                    modifier = Modifier
                        .width(120.dp)
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp)
                ) {

                    Text(text = stringResource(id = R.string.stop))
                }
                Button(
                    onClick = { onOrderButton(Orders.CLOSE) },
                    modifier = Modifier
                        .width(120.dp)
                        .height(60.dp),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(text = stringResource(id = R.string.close))
                }
            }
        }
    }
}


@Composable
fun EmptyGateList() {

    Column(
        Modifier.padding(16.dp)
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = "Aún no tienes controles agregados. Agrega una usando el icono '+' ubicado en la parte superior."
        )
    }

}

@Composable
fun ImageCard(
    imageUri: Uri?,
    gateData: GateData = GateData(gateName = "Error getting name"),
    onSettingButton: (gateData: GateData) -> Unit = {},
) {


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(shape = RoundedCornerShape(30.dp))

        ) {
            if (imageUri != null) {

                    /*AsyncImage(

                        model = imageUri,
                        contentDescription = "gate image",
                        filterQuality = FilterQuality.Low,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .height(260.dp)
                            .shadow(elevation = 3.dp, shape = RoundedCornerShape(30.dp))
                    )*/

                ImageFromUri(
                    imageUri = imageUri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .height(260.dp)
                )

            } else {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .height(260.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = "Agrega una imagen en ajustes",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
                IconButton(
                    onClick = { onSettingButton(gateData) }) {

                    Icon(
                        modifier = Modifier
                            .background(
                                shape = RoundedCornerShape(5.dp),
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                            .padding(horizontal = 4.dp),
                        painter = painterResource(id = R.drawable.more_horiz),
                        contentDescription = "gate settings",
                    )
                }


                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .heightIn(min = 0.dp, max = 50.dp)
                        .fillMaxWidth()
                        .background(
                            Color.Gray.copy(alpha = 0.5f)
                        )
                ) {
                    Text(
                        text = gateData.gateName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                }
            }




}