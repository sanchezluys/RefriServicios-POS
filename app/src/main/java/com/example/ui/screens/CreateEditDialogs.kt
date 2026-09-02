package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ClientEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceLineItem
import com.example.data.model.JsonUtils
import com.example.data.model.PaymentStatus
import com.example.data.model.RepairOrderEntity
import com.example.data.model.RepairStatus

val APPLIANCE_CATEGORIES = listOf(
    "Refrigerador / Nevera",
    "Lavadora",
    "Secadora",
    "Aire Acondicionado",
    "Congelador / Freezer",
    "Centro de Lavado",
    "Microondas",
    "Lavavajillas",
    "Otro Equipo"
)

val INVENTORY_CATEGORIES = listOf(
    "Refrigeración",
    "Lavado",
    "Climatización",
    "Consumibles/Gas",
    "Electricidad",
    "Otros"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRepairDialog(
    clients: List<ClientEntity>,
    onDismiss: () -> Unit,
    onSave: (RepairOrderEntity) -> Unit
) {
    var selectedClient by remember { mutableStateOf<ClientEntity?>(clients.firstOrNull()) }
    var clientName by remember { mutableStateOf(selectedClient?.name ?: "") }
    var clientPhone by remember { mutableStateOf(selectedClient?.phone ?: "") }
    var clientAddress by remember { mutableStateOf(selectedClient?.address ?: "") }

    var category by remember { mutableStateOf(APPLIANCE_CATEGORIES.first()) }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var reportedFailure by remember { mutableStateOf("") }
    var technicalDiagnosis by remember { mutableStateOf("") }
    var laborCostInput by remember { mutableStateOf("30.0") }
    var warrantyDaysInput by remember { mutableStateOf("30") }

    var expandedClientMenu by remember { mutableStateOf(false) }
    var expandedCatMenu by remember { mutableStateOf(false) }

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
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nueva Orden de Reparación", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Client Picker / Info
                Text("DATOS DEL CLIENTE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))

                if (clients.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedClientMenu,
                        onExpandedChange = { expandedClientMenu = !expandedClientMenu }
                    ) {
                        OutlinedTextField(
                            value = selectedClient?.name ?: "Seleccionar cliente registrado...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Cliente Registrado") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClientMenu) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("select_client_for_repair")
                        )
                        ExposedDropdownMenu(
                            expanded = expandedClientMenu,
                            onDismissRequest = { expandedClientMenu = false }
                        ) {
                            clients.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${c.name} (${c.phone})") },
                                    onClick = {
                                        selectedClient = c
                                        clientName = c.name
                                        clientPhone = c.phone
                                        clientAddress = c.address
                                        expandedClientMenu = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nombre del Cliente *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("repair_client_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Teléfono *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("repair_client_phone_input")
                    )
                    OutlinedTextField(
                        value = clientAddress,
                        onValueChange = { clientAddress = it },
                        label = { Text("Dirección") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("DATOS DEL EQUIPO Y SERVICIO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))

                // Appliance Category
                ExposedDropdownMenuBox(
                    expanded = expandedCatMenu,
                    onExpandedChange = { expandedCatMenu = !expandedCatMenu }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Equipo *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCatMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .testTag("appliance_category_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCatMenu,
                        onDismissRequest = { expandedCatMenu = false }
                    ) {
                        APPLIANCE_CATEGORIES.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expandedCatMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Marca (ej. Samsung, LG) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("appliance_brand_input")
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Modelo") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reportedFailure,
                    onValueChange = { reportedFailure = it },
                    label = { Text("Falla Reportada por el Cliente *") },
                    placeholder = { Text("ej. No enfría abajo, error en display, fuga de gas...") },
                    modifier = Modifier.fillMaxWidth().testTag("reported_failure_input")
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = technicalDiagnosis,
                    onValueChange = { technicalDiagnosis = it },
                    label = { Text("Diagnóstico Técnico Preliminar") },
                    placeholder = { Text("ej. Compresor aterrizado, fuga en evaporador...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = laborCostInput,
                        onValueChange = { laborCostInput = it },
                        label = { Text("Mano de Obra ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("labor_cost_input")
                    )
                    OutlinedTextField(
                        value = warrantyDaysInput,
                        onValueChange = { warrantyDaysInput = it },
                        label = { Text("Garantía (días)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (clientName.isNotBlank() && reportedFailure.isNotBlank()) {
                                val labor = laborCostInput.toDoubleOrNull() ?: 30.0
                                val warranty = warrantyDaysInput.toIntOrNull() ?: 30
                                val orderNumber = "ORD-${(100..999).random()}"
                                val newRepair = RepairOrderEntity(
                                    orderNumber = orderNumber,
                                    clientId = selectedClient?.id ?: 0L,
                                    clientName = clientName,
                                    clientPhone = clientPhone,
                                    clientAddress = clientAddress,
                                    applianceCategory = category,
                                    brand = brand.ifBlank { "General" },
                                    model = model,
                                    serialNumber = serialNumber,
                                    reportedFailure = reportedFailure,
                                    technicalDiagnosis = technicalDiagnosis,
                                    status = RepairStatus.PENDING.name,
                                    laborCost = labor,
                                    totalCost = labor,
                                    warrantyDays = warranty
                                )
                                onSave(newRepair)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("save_repair_button")
                    ) {
                        Text("Crear Orden de Servicio")
                    }

                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

@Composable
fun NewClientDialog(
    clientToEdit: ClientEntity? = null,
    onDismiss: () -> Unit,
    onSave: (ClientEntity) -> Unit
) {
    var name by remember { mutableStateOf(clientToEdit?.name ?: "") }
    var phone by remember { mutableStateOf(clientToEdit?.phone ?: "") }
    var email by remember { mutableStateOf(clientToEdit?.email ?: "") }
    var address by remember { mutableStateOf(clientToEdit?.address ?: "") }
    var cityOrZone by remember { mutableStateOf(clientToEdit?.cityOrZone ?: "") }
    var notes by remember { mutableStateOf(clientToEdit?.notes ?: "") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (clientToEdit == null) "Nuevo Cliente" else "Editar Cliente",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Completo *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("client_name_field")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono / WhatsApp *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("client_phone_field")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico (opcional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección de Domicilio / Taller *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("client_address_field")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = cityOrZone,
                    onValueChange = { cityOrZone = it },
                    label = { Text("Ciudad / Zona o Sector") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas o Referencias (ej. equipos que posee)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                onSave(
                                    ClientEntity(
                                        id = clientToEdit?.id ?: 0L,
                                        name = name.trim(),
                                        phone = phone.trim(),
                                        email = email.trim(),
                                        address = address.trim(),
                                        cityOrZone = cityOrZone.trim(),
                                        notes = notes.trim()
                                    )
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("save_client_button")
                    ) {
                        Text("Guardar Cliente")
                    }
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInventoryItemDialog(
    itemToEdit: InventoryItemEntity? = null,
    onDismiss: () -> Unit,
    onSave: (InventoryItemEntity) -> Unit
) {
    var sku by remember { mutableStateOf(itemToEdit?.sku ?: "") }
    var name by remember { mutableStateOf(itemToEdit?.name ?: "") }
    var category by remember { mutableStateOf(itemToEdit?.category ?: INVENTORY_CATEGORIES.first()) }
    var stockInput by remember { mutableStateOf(itemToEdit?.stock?.toString() ?: "1.0") }
    var minStockInput by remember { mutableStateOf(itemToEdit?.minStock?.toString() ?: "2.0") }
    var costPriceInput by remember { mutableStateOf(itemToEdit?.costPrice?.toString() ?: "0.0") }
    var salePriceInput by remember { mutableStateOf(itemToEdit?.salePrice?.toString() ?: "0.0") }
    var unit by remember { mutableStateOf(itemToEdit?.unit ?: "uds") }
    var description by remember { mutableStateOf(itemToEdit?.description ?: "") }

    var expandedCatMenu by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (itemToEdit == null) "Nuevo Repuesto / Insumo" else "Editar Repuesto",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("Código / SKU") },
                        placeholder = { Text("ej. GAS-134") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("inventory_sku_input")
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unidad (uds, kg, latas)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Repuesto / Material *") },
                    placeholder = { Text("ej. Termostato Danfoss K59") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("inventory_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedCatMenu,
                    onExpandedChange = { expandedCatMenu = !expandedCatMenu }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCatMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCatMenu,
                        onDismissRequest = { expandedCatMenu = false }
                    ) {
                        INVENTORY_CATEGORIES.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expandedCatMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockInput,
                        onValueChange = { stockInput = it },
                        label = { Text("Stock Actual *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("inventory_stock_input")
                    )
                    OutlinedTextField(
                        value = minStockInput,
                        onValueChange = { minStockInput = it },
                        label = { Text("Stock Mínimo (Alerta)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = costPriceInput,
                        onValueChange = { costPriceInput = it },
                        label = { Text("Costo Compra ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = salePriceInput,
                        onValueChange = { salePriceInput = it },
                        label = { Text("Precio Venta ($) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("inventory_price_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción o Compatibilidad") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    InventoryItemEntity(
                                        id = itemToEdit?.id ?: 0L,
                                        sku = sku.ifBlank { "SKU-${(100..999).random()}" },
                                        name = name.trim(),
                                        category = category,
                                        stock = stockInput.toDoubleOrNull() ?: 0.0,
                                        minStock = minStockInput.toDoubleOrNull() ?: 2.0,
                                        costPrice = costPriceInput.toDoubleOrNull() ?: 0.0,
                                        salePrice = salePriceInput.toDoubleOrNull() ?: 0.0,
                                        unit = unit.trim().ifBlank { "uds" },
                                        description = description.trim()
                                    )
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("save_inventory_item_button")
                    ) {
                        Text("Guardar Repuesto")
                    }
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewManualInvoiceDialog(
    clients: List<ClientEntity>,
    onDismiss: () -> Unit,
    onSave: (InvoiceEntity) -> Unit
) {
    var selectedClient by remember { mutableStateOf<ClientEntity?>(clients.firstOrNull()) }
    var clientName by remember { mutableStateOf(selectedClient?.name ?: "") }
    var clientPhone by remember { mutableStateOf(selectedClient?.phone ?: "") }
    var clientAddress by remember { mutableStateOf(selectedClient?.address ?: "") }
    var applianceDescription by remember { mutableStateOf("Refrigerador / Lavadora / Equipo") }

    var concept1 by remember { mutableStateOf("Servicio Técnico y Mano de Obra") }
    var amount1 by remember { mutableStateOf("45.0") }
    var concept2 by remember { mutableStateOf("Repuestos / Insumos") }
    var amount2 by remember { mutableStateOf("0.0") }
    var taxPercentInput by remember { mutableStateOf("0") }
    var discountInput by remember { mutableStateOf("0") }
    var paymentMethod by remember { mutableStateOf("Efectivo") }

    var expandedClientMenu by remember { mutableStateOf(false) }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nueva Factura / Recibo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("DATOS DEL CLIENTE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))

                if (clients.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedClientMenu,
                        onExpandedChange = { expandedClientMenu = !expandedClientMenu }
                    ) {
                        OutlinedTextField(
                            value = selectedClient?.name ?: "Seleccionar cliente...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Cliente Registrado") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClientMenu) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedClientMenu,
                            onDismissRequest = { expandedClientMenu = false }
                        ) {
                            clients.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        selectedClient = c
                                        clientName = c.name
                                        clientPhone = c.phone
                                        clientAddress = c.address
                                        expandedClientMenu = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nombre del Cliente *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Teléfono *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = clientAddress,
                        onValueChange = { clientAddress = it },
                        label = { Text("Dirección") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = applianceDescription,
                    onValueChange = { applianceDescription = it },
                    label = { Text("Descripción del Equipo / Servicio *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text("CONCEPTOS A COBRAR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = concept1,
                        onValueChange = { concept1 = it },
                        label = { Text("Concepto 1") },
                        singleLine = true,
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = amount1,
                        onValueChange = { amount1 = it },
                        label = { Text("Monto ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = concept2,
                        onValueChange = { concept2 = it },
                        label = { Text("Concepto 2 (opcional)") },
                        singleLine = true,
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = amount2,
                        onValueChange = { amount2 = it },
                        label = { Text("Monto ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = taxPercentInput,
                        onValueChange = { taxPercentInput = it },
                        label = { Text("Impuesto / IVA (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = discountInput,
                        onValueChange = { discountInput = it },
                        label = { Text("Descuento ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("Método de Pago (Efectivo, Transferencia, etc.)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (clientName.isNotBlank() && applianceDescription.isNotBlank()) {
                                val a1 = amount1.toDoubleOrNull() ?: 0.0
                                val a2 = amount2.toDoubleOrNull() ?: 0.0
                                val lines = mutableListOf<InvoiceLineItem>()
                                if (a1 > 0) lines.add(InvoiceLineItem(concept1, 1.0, a1))
                                if (a2 > 0) lines.add(InvoiceLineItem(concept2, 1.0, a2))

                                val subtotal = lines.sumOf { it.total }
                                val taxPercent = taxPercentInput.toDoubleOrNull() ?: 0.0
                                val taxAmount = subtotal * (taxPercent / 100.0)
                                val discount = discountInput.toDoubleOrNull() ?: 0.0
                                val total = (subtotal + taxAmount - discount).coerceAtLeast(0.0)

                                val invoiceNumber = "FAC-${(1000..9999).random()}"
                                val newInv = InvoiceEntity(
                                    invoiceNumber = invoiceNumber,
                                    clientId = selectedClient?.id ?: 0L,
                                    clientName = clientName,
                                    clientPhone = clientPhone,
                                    clientAddress = clientAddress,
                                    applianceDescription = applianceDescription,
                                    itemsJson = JsonUtils.invoiceLinesToJson(lines),
                                    subtotal = subtotal,
                                    taxPercent = taxPercent,
                                    taxAmount = taxAmount,
                                    discountAmount = discount,
                                    totalAmount = total,
                                    paidAmount = 0.0,
                                    paymentStatus = PaymentStatus.PENDING.name,
                                    paymentMethod = paymentMethod
                                )
                                onSave(newInv)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("save_manual_invoice_button")
                    ) {
                        Text("Generar Comprobante")
                    }
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}
