package app.lounge.ui.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.lounge.ui.theme.Ember
import app.lounge.ui.theme.loungeTween

@Composable
fun GlassTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    refract: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val emphasis by animateFloatAsState(
        if (focused) 1f else 0f,
        loungeTween(),
        label = "text-button",
    )
    LiquidGlass(
        modifier = modifier
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .glassFocus(emphasis, 18.dp, 2.dp)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        corner = 18.dp,
        emphasis = emphasis,
        refract = refract,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            color = Ember.Cream,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun GlassIconButton(
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    size: Dp = 64.dp,
    refract: Boolean = false,
    icon: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val emphasis by animateFloatAsState(
        if (focused) 1f else 0f,
        loungeTween(),
        label = "icon-button",
    )
    val corner = size / 2
    LiquidGlass(
        modifier = modifier
            .size(size)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .glassFocus(emphasis, corner, 3.dp)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = description },
        corner = corner,
        emphasis = emphasis,
        refract = refract,
    ) {
        Box(Modifier.size(size), contentAlignment = Alignment.Center) {
            icon()
        }
    }
}

@Composable
fun PlayPauseGlyph(playing: Boolean, modifier: Modifier = Modifier, tint: Color = Ember.Cream) {
    Canvas(modifier) {
        if (playing) {
            val barW = size.width * 0.22f
            val gap = size.width * 0.16f
            val left = (size.width - (barW * 2f + gap)) / 2f
            val top = size.height * 0.18f
            val height = size.height * 0.64f
            drawRoundRect(
                color = tint,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(barW, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 2f, barW / 2f),
            )
            drawRoundRect(
                color = tint,
                topLeft = Offset(left + barW + gap, top),
                size = androidx.compose.ui.geometry.Size(barW, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 2f, barW / 2f),
            )
        } else {
            val path = Path().apply {
                moveTo(size.width * 0.32f, size.height * 0.18f)
                lineTo(size.width * 0.78f, size.height * 0.5f)
                lineTo(size.width * 0.32f, size.height * 0.82f)
                close()
            }
            drawPath(path, tint)
        }
    }
}

@Composable
fun CloseGlyph(modifier: Modifier = Modifier, tint: Color = Ember.Cream) {
    Canvas(modifier) {
        val inset = size.minDimension * 0.28f
        val stroke = size.minDimension * 0.09f
        drawLine(
            color = tint,
            start = Offset(inset, inset),
            end = Offset(size.width - inset, size.height - inset),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(size.width - inset, inset),
            end = Offset(inset, size.height - inset),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun ChevronGlyph(left: Boolean, modifier: Modifier = Modifier, tint: Color = Ember.Cream) {
    Canvas(modifier) {
        val path = Path().apply {
            if (left) {
                moveTo(size.width * 0.62f, size.height * 0.22f)
                lineTo(size.width * 0.34f, size.height * 0.5f)
                lineTo(size.width * 0.62f, size.height * 0.78f)
            } else {
                moveTo(size.width * 0.38f, size.height * 0.22f)
                lineTo(size.width * 0.66f, size.height * 0.5f)
                lineTo(size.width * 0.38f, size.height * 0.78f)
            }
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round),
        )
    }
}
