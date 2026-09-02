package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.PaymentStatus
import com.example.data.model.RepairOrderEntity
import com.example.data.model.RepairStatus
import com.example.ui.components.ApplianceIconBox
import com.example.ui.components.MetricStatCard
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.components.RepairStatusBadge
import com.example.ui.theme.AlertRed
import com.example.ui.theme.OnTertiaryContainerRose
import com.example.ui.theme.PurpleOnPrimaryContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurplePrimaryContainer
import com.example.ui.theme.SecondaryContainerPurple
import com.example.ui.theme.TertiaryContainerRose
import java.util.Locale

@Composable
fun DashboardScreen(
    repairs: List<RepairOrderEntity>,
    clients: List<ClientEntity>,
    inventory: List<InventoryItemEntity>,
    invoices: List<InvoiceEntity>,
    onNavigateToRepairs: () -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    onSelectRepair: (RepairOrderEntity) -> Unit,
    onSelectInvoice: (InvoiceEntity) -> Unit,
    onNewRepair: () -> Unit,
    onNewClient: () -> Unit,
    onNewInventory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeRepairs = remember(repairs) {
        repairs.filter {
            it.status == RepairStatus.PENDING.name ||
            it.status == RepairStatus.IN_PROGRESS.name ||
            it.status == RepairStatus.WAITING_PARTS.name
        }
    }
    val readyRepairs = remember(repairs) {
        repairs.filter { it.status == RepairStatus.READY.name }
    }
    val lowStockItems = remember(inventory) {
        inventory.filter { it.stock <= it.minStock }
    }
    val totalRevenue = remember(invoices) {
        invoices.sumOf { it.paidAmount }
    }
    val pendingRevenue = remember(invoices) {
        invoices.filter { it.paymentStatus != PaymentStatus.PAID.name }.sumOf { it.totalAmount - it.paidAmount }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Geometric Balance: Top Stats Grid (2 Columns, symmetrical rounded-2xl)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Reparaciones Hoy",
                        value = if (activeRepairs.size < 10) "0${activeRepairs.size}" else "${activeRepairs.size}",
                        subtitle = "${readyRepairs.size} listas para entrega",
                        icon = Icons.Default.Handyman,
                        iconColor = PurpleOnPrimaryContainer,
                        isPrimaryHighlight = true,
                        onClick = onNavigateToRepairs,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Pendientes",
                        value = if (repairs.size < 10) "0${repairs.size}" else "${repairs.size}",
                        subtitle = "${clients.size} clientes registrados",
                        icon = Icons.Default.CheckCircle,
                        iconColor = MaterialTheme.colorScheme.primary,
                        isPrimaryHighlight = false,
                        onClick = onNavigateToRepairs,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Stock Piezas",
                        value = if (lowStockItems.size < 10) "0${lowStockItems.size}" else "${lowStockItems.size}",
                        subtitle = if (lowStockItems.isNotEmpty()) "Crítico para reposición" else "Inventario al día",
                        icon = Icons.Default.Inventory2,
                        iconColor = if (lowStockItems.isNotEmpty()) AlertRed else MaterialTheme.colorScheme.primary,
                        isPrimaryHighlight = false,
                        onClick = onNavigateToInventory,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Cobrado",
                        value = String.format(Locale.US, "$%.0f", totalRevenue),
                        subtitle = String.format(Locale.US, "Resta $%.0f", pendingRevenue),
                        icon = Icons.Default.ReceiptLong,
                        iconColor = PurplePrimary,
                        isPrimaryHighlight = false,
                        onClick = onNavigateToInvoices,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Geometric Stock Alert Card (rounded-3xl #FFD8E4 with red circular icon #B3261E)
        if (lowStockItems.isNotEmpty()) {
            item {
                val firstAlert = lowStockItems.first()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToInventory() },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = TertiaryContainerRose),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(AlertRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Alerta de Stock (${lowStockItems.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = OnTertiaryContainerRose
                                )
                                Text(
                                    text = "${firstAlert.name} bajo (${firstAlert.stock} ${firstAlert.unit})",
                                    fontSize = 13.sp,
                                    color = OnTertiaryContainerRose.copy(alpha = 0.85f),
                                    maxLines = 1
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ver inventario",
                            tint = OnTertiaryContainerRose,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Fast Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionChip(
                    label = "Nueva Orden",
                    icon = Icons.Default.Add,
                    onClick = onNewRepair,
                    modifier = Modifier.weight(1f).testTag("dashboard_new_repair_btn")
                )
                QuickActionChip(
                    label = "Nuevo Cliente",
                    icon = Icons.Default.PersonAdd,
                    onClick = onNewClient,
                    modifier = Modifier.weight(1f).testTag("dashboard_new_client_btn")
                )
                QuickActionChip(
                    label = "Repuesto",
                    icon = Icons.Default.Inventory2,
                    onClick = onNewInventory,
                    modifier = Modifier.weight(1f).testTag("dashboard_new_inventory_btn")
                )
            }
        }

        // Section: ÚLTIMOS SERVICIOS (Geometric Balance typography & layout)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ÚLTIMOS SERVICIOS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigateToRepairs) {
                    Text(
                        text = "Ver Todo (${repairs.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PurplePrimary
                    )
                }
            }
        }

        if (activeRepairs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay órdenes pendientes en este momento.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(activeRepairs.take(4), key = { it.id }) { repair ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .clickable { onSelectRepair(repair) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ApplianceIconBox(
                            category = repair.applianceCategory,
                            modifier = Modifier.size(48.dp),
                            containerColor = SecondaryContainerPurple,
                            iconColor = PurplePrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${repair.applianceCategory} ${repair.brand} ${repair.model}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                RepairStatusBadge(status = repair.status)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Cliente: ${repair.clientName} • #${repair.orderNumber}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section: FACTURAS RECIENTES
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FACTURAS Y COBROS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigateToInvoices) {
                    Text(
                        text = "Ver Todo (${invoices.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PurplePrimary
                    )
                }
            }
        }

        items(invoices.take(3), key = { "dash_inv_${it.id}" }) { inv ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .clickable { onSelectInvoice(inv) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = inv.invoiceNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${inv.clientName} • ${inv.applianceDescription}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format(Locale.US, "$%.2f", inv.totalAmount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PurplePrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        PaymentStatusBadge(status = inv.paymentStatus)
                    }
                }
            }
        }

        // Geometric Primary Action Pill Button at bottom
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onNewRepair,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurplePrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("dashboard_primary_create_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nueva Orden / Factura",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun QuickActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = PurplePrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
