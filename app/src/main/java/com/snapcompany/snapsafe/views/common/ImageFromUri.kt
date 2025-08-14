package com.snapcompany.snapsafe.views.common

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest

@Composable
fun ImageFromUri(imageUri: Uri?, modifier: Modifier = Modifier) {
    val painter: Painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(data = imageUri)
            .apply(block = fun ImageRequest.Builder.() {
                crossfade(true)
                memoryCachePolicy(CachePolicy.ENABLED)
                diskCachePolicy(CachePolicy.ENABLED)

            }
            ).build(),
        filterQuality = FilterQuality.Low
    )

    Image(
        painter = painter,
        contentDescription = null, // Provide a content description if needed
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
}