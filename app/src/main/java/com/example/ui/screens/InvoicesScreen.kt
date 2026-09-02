package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClientEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.PaymentStatus
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MetricStatCard
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.components.SearchFilterBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoicesScreen(
    invoices: List<InvoiceEntity>,
    clients: List<ClientEntity>,
    onSelectInvoice: (InvoiceEntity) -> Unit,
    onDeleteInvoice: (InvoiceEntity) -> Unit,
    onSaveInvoice: (InvoiceEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todas") }
    var invoiceToDelete by remember { mutableStateOf<InvoiceEntity?>(null) }
    var showNewInvoiceDialog by remember { mutableStateOf(false) }

    val totalInvoiced = remember(invoices) { invoices.sumOf { it.totalAmount } }
    val totalCollected = remember(invoices) { invoices.sumOf { it.paidAmount } }
    val totalPending = (totalInvoiced - totalCollected).coerceAtLeast(0.0)

    val filteredList = remember(invoices, searchQuery, selectedFilter) {
        invoices.filter { inv ->
            val matchesQuery = searchQuery.isBlank() ||
                inv.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                inv.clientName.contains(searchQuery, ignoreCase = true) ||
                inv.applianceDescription.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "Pagadas" -> inv.paymentStatus == PaymentStatus.PAID.name
                "Pendientes" -> inv.paymentStatus == PaymentStatus.PENDING.name
                "Parciales" -> inv.paymentStatus == PaymentStatus.PARTIAL.name
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewInvoiceDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_invoice_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Factura")
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
            // Financial Summary Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Total Facturado",
                    value = String.format(Locale.US, "$%.0f", totalInvoiced),
                    subtitle = "${invoices.size} comprobantes",
                    icon = Icons.Default.Receipt,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Cobrado",
                    value = String.format(Locale.US, "$%.0f", totalCollected),
                    subtitle = "Ingreso recibido",
                    icon = Icons.Default.CheckCircle,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Por Cobrar",
                    value = String.format(Locale.US, "$%.0f", totalPending),
                    subtitle = "Saldo pendiente",
                    icon = Icons.Default.HourglassEmpty,
                    iconColor = if (totalPending > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Buscar factura por número, cliente o equipo..."
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todas", "Pendientes", "Pagadas", "Parciales").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredList.isEmpty()) {
                EmptyStateCard(
                    title = "Sin facturas",
                    message = "Genera facturas directamente desde las órdenes de reparación o crea una nueva con el botón inferior.",
                    icon = Icons.Default.Receipt
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList, key = { it.id }) { invoice ->
                        InvoiceItemCard(
                            invoice = invoice,
                            onClick = { onSelectInvoice(invoice) },
                            onDelete = { invoiceToDelete = invoice }
                        )
                    }
                }
            }
        }
    }

    if (showNewInvoiceDialog) {
        NewManualInvoiceDialog(
            clients = clients,
            onDismiss = { showNewInvoiceDialog = false },
            onSave = {
                onSaveInvoice(it)
                showNewInvoiceDialog = false
            }
        )
    }

    invoiceToDelete?.let { inv ->
        ConfirmDeleteDialog(
            title = "Eliminar Factura",
            message = "¿Seguro que deseas eliminar la factura ${inv.invoiceNumber}?",
            onConfirm = {
                onDeleteInvoice(inv)
                invoiceToDelete = null
            },
            onDismiss = { invoiceToDelete = null }
        )
    }
}

@Composable
fun InvoiceItemCard(
    invoice: InvoiceEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val balance = (invoice.totalAmount - invoice.paidAmount).coerceAtLeast(0.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("invoice_card_${invoice.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Invoice number and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = invoice.invoiceNumber,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${dateFormat.format(Date(invoice.dateIssued))}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PaymentStatusBadge(status = invoice.paymentStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Client & Appliance
            Text(invoice.clientName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(
                text = invoice.applianceDescription,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // Amounts Row & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format(Locale.US, "$%.2f", invoice.totalAmount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (balance > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = String.format(Locale.US, "Resta: $%.2f", balance),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Text(
                        text = "Método: ${invoice.paymentMethod}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("view_invoice_btn_${invoice.id}")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver", fontSize = 12.sp)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
