package com.hitchy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hitchy.core.ui.theme.HitchyTheme
import com.hitchy.core.ui.theme.LocalHitchyColors
import com.hitchy.core.ui.theme.LocalHitchySpacing
import com.hitchy.core.ui.theme.LocalHitchyTypography
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class ChatBubbleType {
    Sent,
    Received,
    Assistant,
}

@Composable
fun ChatBubble(
    message: String,
    timestampText: String,
    type: ChatBubbleType,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHitchyColors.current
    val typography = LocalHitchyTypography.current
    val spacing = LocalHitchySpacing.current

    val isSent = type == ChatBubbleType.Sent
    val bubbleShape = when (type) {
        ChatBubbleType.Sent -> RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
        ChatBubbleType.Received -> RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
        ChatBubbleType.Assistant -> RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    val bgColor = when (type) {
        ChatBubbleType.Sent -> colors.purple
        ChatBubbleType.Received -> colors.white
        ChatBubbleType.Assistant -> colors.lavender
    }
    val textColor = when (type) {
        ChatBubbleType.Sent -> colors.white
        ChatBubbleType.Received -> colors.navy
        ChatBubbleType.Assistant -> colors.navy
    }
    val timeColor = when (type) {
        ChatBubbleType.Sent -> colors.lavender
        ChatBubbleType.Received -> colors.textSecondary
        ChatBubbleType.Assistant -> colors.purple
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(bgColor)
                .padding(horizontal = spacing.space16, vertical = spacing.space8),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = message,
                    style = typography.body,
                    color = textColor,
                )
                Text(
                    text = timestampText,
                    style = typography.caption,
                    color = timeColor,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}

@Preview
@Composable
fun ChatBubbleLtrPreview() {
    HitchyTheme {
        val spacing = LocalHitchySpacing.current
        Surface(color = LocalHitchyColors.current.background) {
            Column(
                modifier = Modifier.padding(spacing.space16),
                verticalArrangement = Arrangement.spacedBy(spacing.space8),
            ) {
                ChatBubble(
                    message = "Hi! Are you waiting near Gate 1?",
                    timestampText = "07:25 AM",
                    type = ChatBubbleType.Sent,
                )
                ChatBubble(
                    message = "Yes, parked right outside Gate 1 in a grey Elantra.",
                    timestampText = "07:26 AM",
                    type = ChatBubbleType.Received,
                )
                ChatBubble(
                    message = "I found 3 rides matching your daily commute to Smart Village.",
                    timestampText = "07:20 AM",
                    type = ChatBubbleType.Assistant,
                )
            }
        }
    }
}

@Preview
@Composable
fun ChatBubbleRtlPreview() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ChatBubbleLtrPreview()
    }
}
