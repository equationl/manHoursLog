package com.equationl.manhourslog.ui.widget

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoadingContent(
    modifier: Modifier = Modifier,
    msg: String = "Loading...",
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        CircularProgressIndicator()
        Text(text = msg)
    }
}

@Composable
fun ListEmptyContent(msg: String, onClick: (() -> Unit)? = null) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(text = msg, modifier = Modifier.clickable { onClick?.invoke() })
    }
}

@Composable
fun LinkText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        modifier = modifier.clickable(onClick = onClick) )
}

@Composable
fun PulsatingCircles(
    text: String,
    isAnimation: Boolean,
    radius: Dp = 130.dp,
    firstBoardCircleRadius: Dp = 150.dp,
    secondBoardCircleRadius: Dp = 200.dp,
    animationOffset: Dp = 10.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.onPrimary,
    onClick: (() -> Unit)? = null
) {
    Column {
        val infiniteTransition = rememberInfiniteTransition(label = "a1")
        val size by infiniteTransition.animateValue(
            initialValue = secondBoardCircleRadius,
            targetValue = secondBoardCircleRadius - animationOffset,
            Dp.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "a2"
        )
        val smallCircle by infiniteTransition.animateValue(
            initialValue = firstBoardCircleRadius,
            targetValue = firstBoardCircleRadius + animationOffset,
            Dp.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "a3"
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                //.height(200.dp)
                .clickable(enabled = onClick != null) {
                    onClick?.invoke()
                },
            contentAlignment = Alignment.Center
        ) {
            if (isAnimation) {
                SimpleCircleShape2(
                    size = size,
                    color = primaryColor.copy(alpha = 0.25f)
                )
                SimpleCircleShape2(
                    size = smallCircle,
                    color = primaryColor.copy(alpha = 0.25f)
                )
            }
            SimpleCircleShape2(
                size = if (isAnimation) radius else secondBoardCircleRadius,
                color = backgroundColor
            )
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = text,
                        color = primaryColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleCircleShape2(
    size: Dp,
    color: Color = Color.White,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.LightGray.copy(alpha = 0.0f)
) {
    Column(
        modifier = Modifier
            .wrapContentSize(Alignment.Center)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    color
                )
                .border(borderWidth, borderColor)
        )
    }
}

@Preview
@Composable
fun PreviewPulsatingCircles() {
    MaterialTheme {
        PulsatingCircles("Finish", isAnimation = true)
    }
}
