package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ClientEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.RepairOrderEntity
import com.example.ui.components.ApplianceIconBox
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.RepairStatusBadge
import com.example.ui.components.SearchFilterBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClientsScreen(
    clients: List<ClientEntity>,
    repairs: List<RepairOrderEntity>,
    invoices: List<InvoiceEntity>,
    onSaveClient: (ClientEntity) -> Unit,
    onDeleteClient: (ClientEntity) -> Unit,
    onSelectRepair: (RepairOrderEntity) -> Unit,
    onSelectInvoice: (InvoiceEntity) -> Unit,
    onNewRepairForClient: (ClientEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedClientForDetail by remember { mutableStateOf<ClientEntity?>(null) }
    var showNewClientDialog by remember { mutableStateOf(false) }
    var clientToEdit by remember { mutableStateOf<ClientEntity?>(null) }
    var clientToDelete by remember { mutableStateOf<ClientEntity?>(null) }

    val filteredClients = remember(clients, searchQuery) {
        if (searchQuery.isBlank()) clients
        else clients.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery, ignoreCase = true) ||
            it.address.contains(searchQuery, ignoreCase = true) ||
            it.cityOrZone.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    clientToEdit = null
                    showNewClientDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_client_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Cliente")
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
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Buscar cliente por nombre, teléfono o zona..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredClients.isEmpty()) {
                EmptyStateCard(
                    title = "Sin clientes encontrados",
                    message = "Registra a tus clientes para tener acceso rápido a su historial de reparaciones y equipos.",
                    icon = Icons.Default.Person
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredClients, key = { it.id }) { client ->
                        val clientRepairs = repairs.filter { it.clientId == client.id }
                        ClientListItemCard(
                            client = client,
                            repairCount = clientRepairs.size,
                            onClick = { selectedClientForDetail = client },
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.phone}"))
                                context.startActivity(intent)
                            },
                            onWhatsApp = {
                                openWhatsApp(context, client.phone, client.name)
                            },
                            onEdit = {
                                clientToEdit = client
                                showNewClientDialog = true
                            },
                            onDelete = {
                                clientToDelete = client
                            }
                        )
                    }
                }
            }
        }
    }

    // Detail dialog with Full Service History
    selectedClientForDetail?.let { client ->
        val clientRepairs = repairs.filter { it.clientId == client.id }
        val clientInvoices = invoices.filter { it.clientId == client.id }

        ClientHistoryDetailDialog(
            client = client,
            repairs = clientRepairs,
            invoices = clientInvoices,
            onDismiss = { selectedClientForDetail = null },
            onSelectRepair = {
                selectedClientForDetail = null
                onSelectRepair(it)
            },
            onSelectInvoice = {
                selectedClientForDetail = null
                onSelectInvoice(it)
            },
            onNewRepair = {
                selectedClientForDetail = null
                onNewRepairForClient(client)
            },
            onEdit = {
                selectedClientForDetail = null
                clientToEdit = client
                showNewClientDialog = true
            }
        )
    }

    // New/Edit Client Dialog
    if (showNewClientDialog) {
        NewClientDialog(
            clientToEdit = clientToEdit,
            onDismiss = { showNewClientDialog = false },
            onSave = {
                onSaveClient(it)
                showNewClientDialog = false
            }
        )
    }

    // Confirm Delete Dialog
    clientToDelete?.let { client ->
        ConfirmDeleteDialog(
            title = "Eliminar Cliente",
            message = "¿Estás seguro de eliminar a ${client.name}? Esto no borrará sus facturas anteriores.",
            onConfirm = {
                onDeleteClient(client)
                clientToDelete = null
            },
            onDismiss = { clientToDelete = null }
        )
    }
}

@Composable
fun ClientListItemCard(
    client: ClientEntity,
    repairCount: Int,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("client_card_${client.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = client.name.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(client.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(client.phone, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Repair counter badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$repairCount ${if (repairCount == 1) "servicio" else "servicios"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            if (client.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = client.address + if (client.cityOrZone.isNotBlank()) " (${client.cityOrZone})" else "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(4.dp))

            // Fast Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onCall, modifier = Modifier.size(36.dp).testTag("call_client_btn")) {
                        Icon(Icons.Default.Call, contentDescription = "Llamar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onWhatsApp, modifier = Modifier.size(36.dp).testTag("whatsapp_client_btn")) {
                        Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ClientHistoryDetailDialog(
    client: ClientEntity,
    repairs: List<RepairOrderEntity>,
    invoices: List<InvoiceEntity>,
    onDismiss: () -> Unit,
    onSelectRepair: (RepairOrderEntity) -> Unit,
    onSelectInvoice: (InvoiceEntity) -> Unit,
    onNewRepair: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

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
                    Column {
                        Text(client.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(client.phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Contact Details & Address Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Dirección:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(client.address, fontSize = 13.sp)
                        if (client.cityOrZone.isNotBlank()) {
                            Text("Zona: ${client.cityOrZone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (client.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Notas: ${client.notes}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.phone}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Llamar", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { openWhatsApp(context, client.phone, client.name) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action to create new repair for this client
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Historial de Servicios (${repairs.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Button(
                        onClick = onNewRepair,
                        modifier = Modifier.testTag("new_service_for_client_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuevo Servicio", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (repairs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay servicios previos registrados para este cliente.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        repairs.forEach { rep ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectRepair(rep) },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ApplianceIconBox(category = rep.applianceCategory, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("#${rep.orderNumber} - ${rep.applianceCategory}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            RepairStatusBadge(status = rep.status)
                                        }
                                        Text("${rep.brand} ${rep.model}".trim(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(rep.reportedFailure, fontSize = 11.sp, maxLines = 1)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(dateFormat.format(Date(rep.dateReceived)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(String.format(Locale.US, "Total: $%.2f", rep.totalCost), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Client Invoices History
                if (invoices.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Facturas Emitidas (${invoices.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        invoices.forEach { inv ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectInvoice(inv) },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(dateFormat.format(Date(inv.dateIssued)), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(String.format(Locale.US, "$%.2f", inv.totalAmount), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(inv.paymentStatus, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Editar Datos del Cliente")
                }
            }
        }
    }
}

fun openWhatsApp(context: Context, phone: String, clientName: String) {
    val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
    val msg = Uri.encode("Hola ${clientName}, le saludamos de Refri-Servicios & Línea Blanca respecto al servicio de su equipo.")
    val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=$msg"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$cleanPhone")).apply {
            putExtra("sms_body", "Hola ${clientName}, le contactamos de Refri-Servicios respecto a su equipo.")
        }
        context.startActivity(smsIntent)
    }
}
