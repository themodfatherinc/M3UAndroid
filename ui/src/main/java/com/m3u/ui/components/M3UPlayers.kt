package com.m3u.ui.components

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@Composable
@OptIn(UnstableApi::class)
fun LivePlayer(
    url: String,
    modifier: Modifier = Modifier,
    useController: Boolean,
    resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT
) {
    val context = LocalContext.current
    val mediaItem = remember(url) {
        MediaItem.fromUri(url)
    }

    val player = remember(url) {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                playWhenReady = true
                setMediaItem(mediaItem)
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                repeatMode = Player.REPEAT_MODE_ONE
            }
    }
    PlayerBackground(modifier) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    hideController()
                    setUseController(useController)
                    setResizeMode(resizeMode)
                    setPlayer(player)
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        )
        DisposableEffect(url) {
            player.prepare()
            onDispose {
                player.release()
            }
        }
    }
}

@Composable
private fun PlayerBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        color = Color.Black,
        contentColor = Color.White,
        modifier = modifier
    ) {
        content()
    }
}