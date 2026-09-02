package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.JsonUtils
import com.example.data.model.RepairOrderEntity
import com.example.data.model.RepairStatus
import com.example.ui.components.ApplianceIconBox
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MetricStatCard
import com.example.ui.components.RepairStatusBadge
import com.example.ui.components.SearchFilterBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RepairsScreen(
    repairs: List<RepairOrderEntity>,
    clients: List<ClientEntity>,
    inventory: List<InventoryItemEntity>,
    onSaveRepair: (RepairOrderEntity) -> Unit,
    onDeleteRepair: (RepairOrderEntity) -> Unit,
    onUpdateStatus: (RepairOrderEntity, String) -> Unit,
    onAddPartToRepair: (RepairOrderEntity, InventoryItemEntity, Double) -> Unit,
    onGenerateInvoiceFromRepair: (RepairOrderEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<String?>("TODOS") }

    var selectedRepairForDetail by remember { mutableStateOf<RepairOrderEntity?>(null) }
    var showNewRepairDialog by remember { mutableStateOf(false) }
    var repairToDelete by remember { mutableStateOf<RepairOrderEntity?>(null) }

    // Metrics
    val activeRepairs = remember(repairs) {
        repairs.count {
            it.status == RepairStatus.PENDING.name ||
            it.status == RepairStatus.IN_PROGRESS.name ||
            it.status == RepairStatus.WAITING_PARTS.name
        }
    }
    val readyRepairs = remember(repairs) {
        repairs.count { it.status == RepairStatus.READY.name }
    }
    val deliveredRepairs = remember(repairs) {
        repairs.count { it.status == RepairStatus.DELIVERED.name }
    }

    val filteredRepairs = remember(repairs, searchQuery, selectedStatusFilter) {
        repairs.filter { rep ->
            val matchesQuery = searchQuery.isBlank() ||
                rep.orderNumber.contains(searchQuery, ignoreCase = true) ||
                rep.clientName.contains(searchQuery, ignoreCase = true) ||
                rep.brand.contains(searchQuery, ignoreCase = true) ||
                rep.applianceCategory.contains(searchQuery, ignoreCase = true) ||
                rep.reportedFailure.contains(searchQuery, ignoreCase = true)

            val matchesStatus = selectedStatusFilter == null ||
                selectedStatusFilter == "TODOS" ||
                rep.status == selectedStatusFilter

            matchesQuery && matchesStatus
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewRepairDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_repair_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Reparación")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Metrics Overview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "En Taller / Activas",
                    value = "$activeRepairs",
                    subtitle = "Reparaciones en curso",
                    icon = Icons.Default.Handyman,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Listas / Pruebas",
                    value = "$readyRepairs",
                    subtitle = "Listas para entregar",
                    icon = Icons.Default.CheckCircle,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    onClick = { selectedStatusFilter = RepairStatus.READY.name },
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Entregadas",
                    value = "$deliveredRepairs",
                    subtitle = "Finalizadas con éxito",
                    icon = Icons.Default.PendingActions,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    onClick = { selectedStatusFilter = RepairStatus.DELIVERED.name },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Buscar por orden, cliente, marca o falla..."
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedStatusFilter == "TODOS",
                    onClick = { selectedStatusFilter = "TODOS" },
                    label = { Text("Todas (${repairs.size})", fontSize = 12.sp) }
                )
                RepairStatus.values().forEach { st ->
                    val count = repairs.count { it.status == st.name }
                    FilterChip(
                        selected = selectedStatusFilter == st.name,
                        onClick = { selectedStatusFilter = st.name },
                        label = { Text("${st.label} ($count)", fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredRepairs.isEmpty()) {
                EmptyStateCard(
                    title = "Sin órdenes de reparación",
                    message = "No hay órdenes con los filtros aplicados. Toca '+' para crear una nueva orden de servicio técnico.",
                    icon = Icons.Default.Build
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredRepairs, key = { it.id }) { repair ->
                        RepairOrderCard(
                            repair = repair,
                            onClick = { selectedRepairForDetail = repair }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog
    selectedRepairForDetail?.let { currentRepair ->
        RepairDetailDialog(
            repair = currentRepair,
            inventoryList = inventory,
            onDismiss = { selectedRepairForDetail = null },
            onUpdateStatus = { newStatus ->
                onUpdateStatus(currentRepair, newStatus)
                selectedRepairForDetail = currentRepair.copy(status = newStatus)
            },
            onAddPart = { invItem, qty ->
                onAddPartToRepair(currentRepair, invItem, qty)
                // Refresh local state if needed
            },
            onGenerateInvoice = {
                onGenerateInvoiceFromRepair(currentRepair)
                selectedRepairForDetail = null
            },
            onDelete = {
                repairToDelete = currentRepair
                selectedRepairForDetail = null
            }
        )
    }

    // New Repair Dialog
    if (showNewRepairDialog) {
        NewRepairDialog(
            clients = clients,
            onDismiss = { showNewRepairDialog = false },
            onSave = {
                onSaveRepair(it)
                showNewRepairDialog = false
            }
        )
    }

    // Confirm Delete Dialog
    repairToDelete?.let { rep ->
        ConfirmDeleteDialog(
            title = "Eliminar Orden #${rep.orderNumber}",
            message = "¿Estás seguro de eliminar la orden de servicio para ${rep.clientName}?",
            onConfirm = {
                onDeleteRepair(rep)
                repairToDelete = null
            },
            onDismiss = { repairToDelete = null }
        )
    }
}

@Composable
fun RepairOrderCard(
    repair: RepairOrderEntity,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val partsCount = remember(repair.partsUsedJson) {
        JsonUtils.jsonToUsedParts(repair.partsUsedJson).size
    }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("repair_order_card_${repair.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Number, Category & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ApplianceIconBox(category = repair.applianceCategory, modifier = Modifier.size(38.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "#${repair.orderNumber}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${repair.applianceCategory} ${repair.brand}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
                RepairStatusBadge(status = repair.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Client row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cliente: ${repair.clientName}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                    Text(
                        text = repair.clientPhone,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${repair.clientPhone}"))
                        context.startActivity(dialIntent)
                    },
                    modifier = Modifier.size(34.dp).testTag("quick_call_btn_${repair.id}")
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Llamar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Reported Failure & Diagnosis
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "Falla: ${repair.reportedFailure}",
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                    if (repair.technicalDiagnosis.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Diag: ${repair.technicalDiagnosis}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Info: Date, Parts, and Total Cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ingreso: ${dateFormat.format(Date(repair.dateReceived))}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (partsCount > 0) {
                        Text(
                            text = "$partsCount ${if (partsCount == 1) "repuesto asignado" else "repuestos asignados"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.US, "$%.2f", repair.totalCost),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Garantía: ${repair.warrantyDays}d",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
