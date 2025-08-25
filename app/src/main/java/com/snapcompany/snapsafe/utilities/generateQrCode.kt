package com.snapcompany.snapsafe.utilities

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.size
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException




fun generateQrCode(text: String, width: Int, height: Int): Bitmap? {
    val writer = MultiFormatWriter()
    try {
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height)
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    } catch (e: WriterException) {
        e.printStackTrace()
    }
    return null
}




@Composable
fun QrCodeView(encryptedText: String? , size: Int) {


    if (encryptedText != null) {
        val qrCodeBitmap = remember { generateQrCode(encryptedText, size, size) }

        qrCodeBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier.size(size.dp)
            )
        }

    }
    else{
        Text("Error generando el QR Code")
    }

}

