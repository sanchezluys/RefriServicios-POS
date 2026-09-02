package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Microwave
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentStatus
import com.example.data.model.RepairStatus
import com.example.ui.theme.GeoBadgeAlertBg
import com.example.ui.theme.GeoBadgeAlertFg
import com.example.ui.theme.GeoBadgeDeliveredBg
import com.example.ui.theme.GeoBadgeDeliveredFg
import com.example.ui.theme.GeoBadgeProcessBg
import com.example.ui.theme.GeoBadgeProcessFg
import com.example.ui.theme.GeoBadgeReadyBg
import com.example.ui.theme.GeoBadgeReadyFg
import com.example.ui.theme.PurpleOnPrimaryContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurplePrimaryContainer
import com.example.ui.theme.SecondaryContainerPurple

@Composable
fun RepairStatusBadge(status: String, modifier: Modifier = Modifier) {
    // Geometric Balance: crisp rectangular tags with 6dp rounding and high contrast geometric palette
    val (label, bg, fg) = when (status) {
        RepairStatus.PENDING.name -> Triple("EN DIAGNÓSTICO", PurplePrimaryContainer, PurpleOnPrimaryContainer)
        RepairStatus.IN_PROGRESS.name -> Triple("EN PROCESO", GeoBadgeProcessBg, GeoBadgeProcessFg)
        RepairStatus.WAITING_PARTS.name -> Triple("ESPERA REPUESTO", GeoBadgeAlertBg, GeoBadgeAlertFg)
        RepairStatus.READY.name -> Triple("FINALIZADO", GeoBadgeReadyBg, GeoBadgeReadyFg)
        RepairStatus.DELIVERED.name -> Triple("ENTREGADO", GeoBadgeDeliveredBg, GeoBadgeDeliveredFg)
        RepairStatus.CANCELLED.name -> Triple("CANCELADO", Color(0xFFF9DEDC), Color(0xFFB3261E))
        else -> Triple(status.uppercase(), SecondaryContainerPurple, Color(0xFF1D1B20))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun PaymentStatusBadge(status: String, modifier: Modifier = Modifier) {
    val (label, bg, fg) = when (status) {
        PaymentStatus.PAID.name -> Triple("PAGADA", GeoBadgeReadyBg, GeoBadgeReadyFg)
        PaymentStatus.PARTIAL.name -> Triple("ABONO PARCIAL", GeoBadgeProcessBg, GeoBadgeProcessFg)
        else -> Triple("PENDIENTE", GeoBadgeAlertBg, GeoBadgeAlertFg)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

fun getApplianceIcon(category: String): ImageVector {
    val lower = category.lowercase()
    return when {
        lower.contains("refrig") || lower.contains("nevera") -> Icons.Default.Kitchen
        lower.contains("lavad") -> Icons.Default.LocalLaundryService
        lower.contains("seca") -> Icons.Default.DryCleaning
        lower.contains("aire") || lower.contains("clima") || lower.contains("split") -> Icons.Default.AcUnit
        lower.contains("congel") || lower.contains("freezer") -> Icons.Default.Kitchen
        lower.contains("micro") -> Icons.Default.Microwave
        lower.contains("termo") -> Icons.Default.DeviceThermostat
        else -> Icons.Default.Build
    }
}

@Composable
fun ApplianceIconBox(
    category: String,
    modifier: Modifier = Modifier,
    containerColor: Color = SecondaryContainerPurple,
    iconColor: Color = PurplePrimary
) {
    // Geometric Balance: 48x48dp rounded-xl (12dp) with #E8DEF8 and #6750A4 icon
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getApplianceIcon(category),
            contentDescription = category,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SearchFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .testTag("search_bar_input"),
        placeholder = { Text(placeholder, fontSize = 14.sp) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar búsqueda"
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    isPrimaryHighlight: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Geometric Balance:
    // Stat card 1: bg-[#EADDFF] p-4 rounded-2xl with text-[#21005D] 3xl font-bold and text-xs uppercase
    // Stat card 2: bg-[#F3EDF7] p-4 rounded-2xl border border-[#CAC4D0]
    val containerBg = if (isPrimaryHighlight) PurplePrimaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val valueColor = if (isPrimaryHighlight) PurpleOnPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val labelColor = if (isPrimaryHighlight) PurpleOnPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val borderModifier = if (isPrimaryHighlight) Modifier else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))

    Card(
        modifier = modifier
            .then(borderModifier)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                    lineHeight = 34.sp
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = title.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = labelColor
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = labelColor.copy(alpha = 0.8f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 4.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PurplePrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        shape = RoundedCornerShape(20.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
