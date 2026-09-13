package com.example.panitiahub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.panitiahub.ui.theme.ApprovedGreen
import com.example.panitiahub.ui.theme.CompletedBlue
import com.example.panitiahub.ui.theme.OutlineDark
import com.example.panitiahub.ui.theme.PendingYellow
import com.example.panitiahub.ui.theme.SurfaceElevated
import com.example.panitiahub.ui.theme.TextSecondary

fun Modifier.neonGlow(
    color: Color,
    elevation: Dp = 18.dp,
    shape: Shape = RoundedCornerShape(20.dp),
    alpha: Float = 0.35f
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = color.copy(alpha = alpha),
    spotColor = color.copy(alpha = alpha)
)

@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    glowColor: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = RoundedCornerShape(20.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .neonGlow(color = glowColor, shape = shape)
            .clip(shape)
            .background(containerColor)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
        content = content
    )
}

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    colors: List<Color>,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .neonGlow(color = colors.first(), elevation = 24.dp, shape = shape, alpha = 0.45f)
            .clip(shape)
            .background(Brush.linearGradient(colors)),
        content = content
    )
}

@Composable
fun RoleBadge(role: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
        contentColor = MaterialTheme.colorScheme.secondary,
        shape = RoundedCornerShape(50),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Filled.Verified, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(
                text = role.uppercase(),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

private data class StatusStyle(val color: Color, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private fun statusStyleFor(status: String): StatusStyle = when (status.lowercase()) {
    "approved" -> StatusStyle(ApprovedGreen, Icons.Filled.CheckCircle)
    "completed" -> StatusStyle(CompletedBlue, Icons.Filled.Verified)
    else -> StatusStyle(PendingYellow, Icons.Filled.Schedule)
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val style = statusStyleFor(status)
    Surface(
        modifier = modifier,
        color = style.color.copy(alpha = 0.16f),
        contentColor = style.color,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(style.icon, contentDescription = null, modifier = Modifier.size(13.dp))
            Text(
                text = status.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = TextSecondary,
        modifier = modifier
    )
}

@Composable
fun IconAvatar(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = tint.copy(alpha = 0.15f),
        contentColor = tint
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(size * 0.5f))
        }
    }
}