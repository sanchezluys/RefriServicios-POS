package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.InventoryItemEntity
import com.example.data.model.JsonUtils
import com.example.data.model.RepairOrderEntity
import com.example.data.model.RepairStatus
import com.example.ui.components.ApplianceIconBox
import com.example.ui.components.RepairStatusBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairDetailDialog(
    repair: RepairOrderEntity,
    inventoryList: List<InventoryItemEntity>,
    onDismiss: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onAddPart: (InventoryItemEntity, Double) -> Unit,
    onGenerateInvoice: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val usedParts = remember(repair.partsUsedJson) { JsonUtils.jsonToUsedParts(repair.partsUsedJson) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var showAddPartDialog by remember { mutableStateOf(false) }
    var selectedInventoryItem by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var partQuantityInput by remember { mutableStateOf("1") }
    var showStatusMenu by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 20.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ApplianceIconBox(category = repair.applianceCategory)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Orden #${repair.orderNumber}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${repair.applianceCategory} ${repair.brand}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Selector Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ESTADO ACTUAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            RepairStatusBadge(status = repair.status)
                        }

                        ExposedDropdownMenuBox(
                            expanded = showStatusMenu,
                            onExpandedChange = { showStatusMenu = !showStatusMenu }
                        ) {
                            OutlinedButton(
                                onClick = { showStatusMenu = true },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).testTag("change_status_button")
                            ) {
                                Text("Cambiar Estado")
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStatusMenu)
                            }
                            ExposedDropdownMenu(
                                expanded = showStatusMenu,
                                onDismissRequest = { showStatusMenu = false }
                            ) {
                                RepairStatus.values().forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text(st.label) },
                                        onClick = {
                                            onUpdateStatus(st.name)
                                            showStatusMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Client Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("CLIENTE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(repair.clientName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(repair.clientPhone, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (repair.clientAddress.isNotBlank()) {
                                    Text(repair.clientAddress, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            IconButton(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${repair.clientPhone}"))
                                    context.startActivity(dialIntent)
                                },
                                modifier = Modifier.testTag("call_client_button")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Llamar", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Technical Diagnosis & Reported Failure
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("FALLA REPORTADA:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text(repair.reportedFailure, fontSize = 13.sp)

                        if (repair.technicalDiagnosis.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("DIAGNÓSTICO TÉCNICO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(repair.technicalDiagnosis, fontSize = 13.sp)
                        }

                        if (repair.model.isNotBlank() || repair.serialNumber.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                if (repair.model.isNotBlank()) {
                                    Text("Modelo: ${repair.model}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (repair.serialNumber.isNotBlank()) {
                                    Text("Serial: ${repair.serialNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ingreso: ${dateFormat.format(Date(repair.dateReceived))}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Entrega est.: ${dateFormat.format(Date(repair.dateEstimated))}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Parts Used Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Repuestos Utilizados", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    TextButton(
                        onClick = { showAddPartDialog = true },
                        modifier = Modifier.testTag("add_part_to_repair_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Asignar Repuesto")
                    }
                }

                if (usedParts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sin repuestos cargados a esta orden todavía", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            .padding(10.dp)
                    ) {
                        usedParts.forEach { part ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(part.partName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("${part.quantity} ${part.unit} x $${String.format(Locale.US, "%.2f", part.unitPrice)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    String.format(Locale.US, "$%.2f", part.total),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.15f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Costs & Guarantee Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mano de Obra:", fontSize = 13.sp)
                            Text(String.format(Locale.US, "$%.2f", repair.laborCost), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        val partsCost = usedParts.sumOf { it.total }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Repuestos:", fontSize = 13.sp)
                            Text(String.format(Locale.US, "$%.2f", partsCost), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("COSTO TOTAL REPARACIÓN:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                String.format(Locale.US, "$%.2f", repair.laborCost + partsCost),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Garantía: ${repair.warrantyDays} días", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onGenerateInvoice()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("generate_invoice_from_repair_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generar Factura")
                    }

                    OutlinedButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("delete_repair_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    // Dialog for adding part from inventory
    if (showAddPartDialog) {
        AlertDialog(
            onDismissRequest = { showAddPartDialog = false },
            title = { Text("Asignar Repuesto de Inventario", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Selecciona el repuesto a descontar de stock e incluir en el costo:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    var expandedDropdown by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedInventoryItem?.let { "${it.name} (Stock: ${it.stock} ${it.unit})" } ?: "Selecciona un repuesto",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Repuesto / Material") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("select_part_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            inventoryList.forEach { item ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(item.name, fontWeight = FontWeight.SemiBold)
                                            Text("Stock actual: ${item.stock} ${item.unit} | Precio: $${item.salePrice}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        selectedInventoryItem = item
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = partQuantityInput,
                        onValueChange = { partQuantityInput = it },
                        label = { Text("Cantidad a usar (${selectedInventoryItem?.unit ?: "uds"})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("part_quantity_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val item = selectedInventoryItem
                        val qty = partQuantityInput.toDoubleOrNull() ?: 1.0
                        if (item != null && qty > 0) {
                            onAddPart(item, qty)
                            showAddPartDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_part_button")
                ) {
                    Text("Asignar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPartDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
