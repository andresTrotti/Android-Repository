package com.snapcompany.snapsafe.utilities

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.snapcompany.snapsafe.R
import com.snapcompany.snapsafe.models.bluetoothGatt
import java.util.UUID

class Ble(val context: Context) {

    private val encryptedValue = byteArrayOf(12,13,65,73,83,87,100,123,123)


    fun isValidMacAddress(macAddress: String): Boolean {
        val pattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})\$".toRegex()
        return pattern.matches(macAddress)
    }

    fun writeRequest(characteristic: String, service: String, request: String, callBack: (info: Int) -> Unit){
        val toWrite = BluetoothGattCharacteristic(
            UUID.fromString(characteristic),
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            for(i in 1..3) {
//                if (ActivityCompat.checkSelfPermission(
//                        context,
//                        Manifest.permission.BLUETOOTH_CONNECT
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    Toast.makeText(context, "Otorga los permisos de bluetooth en ajustes", Toast.LENGTH_SHORT).show()
//                    return
//                }
//                bluetoothGatt!!.writeCharacteristic(
//                    toWrite,
//                    encryptedValue,
//                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
//                )
//            }
//        } else {
//            callBack(R.string.running)
//            val bluetoothGattCharacteristic: BluetoothGattCharacteristic =
//                bluetoothGatt!!.getService(UUID.fromString(service))
//                    .getCharacteristic(UUID.fromString(characteristic.toString()))
//
//            for (i in 1..3) {
//                bluetoothGattCharacteristic.setValue(request)
//                bluetoothGatt!!.writeCharacteristic(bluetoothGattCharacteristic)
//            }
//        }


            val bluetoothGattCharacteristic: BluetoothGattCharacteristic =
                bluetoothGatt!!.getService(UUID.fromString(service))
                    .getCharacteristic(UUID.fromString(characteristic.toString()))

            callBack(R.string.running)

            for (i in 1..3) {
                bluetoothGattCharacteristic.setValue(request)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        Toast.makeText(
                            context,
                            "Otorga los permisos de bluetooth en ajustes",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                }
                bluetoothGatt!!.writeCharacteristic(bluetoothGattCharacteristic)
            }

    }

    fun getPermissions(context: Context): Boolean {
        var result = false
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
        {
            Log.w("Ble", "Without permissions. Asking...")
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } else {
                arrayOf(
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }


        }
        return result
    }

}