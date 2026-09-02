package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.InvoiceEntity
import com.example.data.model.JsonUtils
import com.example.data.model.PaymentStatus
import com.example.ui.components.PaymentStatusBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoicePreviewDialog(
    invoice: InvoiceEntity,
    onDismiss: () -> Unit,
    onUpdatePayment: (Double, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val items = remember(invoice.itemsJson) { JsonUtils.jsonToInvoiceLines(invoice.itemsJson) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    var showPaymentSheet by remember { mutableStateOf(false) }
    var payAmountInput by remember { mutableStateOf(invoice.totalAmount.toString()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 24.dp)
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
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Comprobante de Servicio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Printable Invoice Paper Style Container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Header of Technician Company
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "REFRI-SERVICIOS & LÍNEA BLANCA",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Servicio Técnico Especializado",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Refrigeración Doméstica, Comercial y Lavado",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = invoice.invoiceNumber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                PaymentStatusBadge(status = invoice.paymentStatus)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Customer & Service Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("DATOS DEL CLIENTE:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(invoice.clientName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(invoice.clientPhone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (invoice.clientAddress.isNotBlank()) {
                                    Text(invoice.clientAddress, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                Text("FECHA:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(dateFormat.format(Date(invoice.dateIssued)), fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("MÉTODO DE PAGO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(invoice.paymentMethod, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Appliance Info Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("EQUIPO / SERVICIO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(invoice.applianceDescription, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("DETALLE DE CONCEPTOS:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Items Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Descripción / Repuesto", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                            Text("Cant.", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                            Text("P.Unit", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                            Text("Total", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                        }

                        // Items List
                        items.forEach { line ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(line.description, fontSize = 12.sp, modifier = Modifier.weight(2f))
                                Text("${line.quantity}", fontSize = 12.sp, modifier = Modifier.weight(0.6f))
                                Text(String.format(Locale.US, "$%.2f", line.unitPrice), fontSize = 12.sp, modifier = Modifier.weight(0.8f))
                                Text(String.format(Locale.US, "$%.2f", line.total), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.8f))
                            }
                            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.2f))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Totals Summary
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(0.65f),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(String.format(Locale.US, "$%.2f", invoice.subtotal), fontSize = 12.sp)
                            }
                            if (invoice.taxAmount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(0.65f),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("IVA (${invoice.taxPercent}%):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(String.format(Locale.US, "$%.2f", invoice.taxAmount), fontSize = 12.sp)
                                }
                            }
                            if (invoice.discountAmount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(0.65f),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Descuento:", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                    Text(String.format(Locale.US, "-$%.2f", invoice.discountAmount), fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(0.65f),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TOTAL:", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    String.format(Locale.US, "$%.2f", invoice.totalAmount),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(0.65f),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Abonado:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(String.format(Locale.US, "$%.2f", invoice.paidAmount), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            val balance = (invoice.totalAmount - invoice.paidAmount).coerceAtLeast(0.0)
                            Row(
                                modifier = Modifier.fillMaxWidth(0.65f),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Saldo Pendiente:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (balance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                Text(String.format(Locale.US, "$%.2f", balance), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (balance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Warranty & Notes Footer
                        Text("TÉRMINOS & GARANTÍA:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(invoice.warrantyNotes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (invoice.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Observaciones: ${invoice.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Payment input if requested
                if (showPaymentSheet) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Registrar Pago / Cobro", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = payAmountInput,
                                onValueChange = { payAmountInput = it },
                                label = { Text("Monto Pagado ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val amount = payAmountInput.toDoubleOrNull() ?: invoice.totalAmount
                                        val status = when {
                                            amount >= invoice.totalAmount -> PaymentStatus.PAID.name
                                            amount > 0 -> PaymentStatus.PARTIAL.name
                                            else -> PaymentStatus.PENDING.name
                                        }
                                        onUpdatePayment(amount, status)
                                        showPaymentSheet = false
                                    },
                                    modifier = Modifier.weight(1f).testTag("save_payment_button")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Guardar Pago")
                                }
                                OutlinedButton(onClick = { showPaymentSheet = false }) {
                                    Text("Cancelar")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            shareInvoiceAsText(context, invoice)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_invoice_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartir")
                    }

                    OutlinedButton(
                        onClick = { showPaymentSheet = !showPaymentSheet },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("record_payment_button")
                    ) {
                        Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Registrar Pago")
                    }
                }
            }
        }
    }
}

private fun shareInvoiceAsText(context: Context, invoice: InvoiceEntity) {
    val items = JsonUtils.jsonToInvoiceLines(invoice.itemsJson)
    val sb = StringBuilder()
    sb.append("📋 *FACTURA / COMPROBANTE DE SERVICIO TÉCNICO*\n")
    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
    sb.append("🔧 *REFRI-SERVICIOS & LÍNEA BLANCA*\n")
    sb.append("📄 Comprobante: ${invoice.invoiceNumber}\n")
    sb.append("📅 Fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(invoice.dateIssued))}\n\n")
    sb.append("👤 *Cliente:* ${invoice.clientName}\n")
    sb.append("📞 *Teléfono:* ${invoice.clientPhone}\n")
    if (invoice.clientAddress.isNotBlank()) {
        sb.append("📍 *Dirección:* ${invoice.clientAddress}\n")
    }
    sb.append("🧊 *Equipo:* ${invoice.applianceDescription}\n\n")
    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
    sb.append("*DESGLOSE DE SERVICIO Y REPUESTOS:*\n")
    items.forEach { item ->
        sb.append("• ${item.description} (x${item.quantity}): $${String.format(Locale.US, "%.2f", item.total)}\n")
    }
    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
    sb.append("Subtotal: $${String.format(Locale.US, "%.2f", invoice.subtotal)}\n")
    if (invoice.taxAmount > 0) {
        sb.append("Impuesto (${invoice.taxPercent}%): $${String.format(Locale.US, "%.2f", invoice.taxAmount)}\n")
    }
    if (invoice.discountAmount > 0) {
        sb.append("Descuento: -$${String.format(Locale.US, "%.2f", invoice.discountAmount)}\n")
    }
    sb.append("💰 *TOTAL A PAGAR: $${String.format(Locale.US, "%.2f", invoice.totalAmount)}*\n")
    sb.append("💳 Pagado: $${String.format(Locale.US, "%.2f", invoice.paidAmount)} | Estado: *${invoice.paymentStatus}*\n")
    val balance = (invoice.totalAmount - invoice.paidAmount).coerceAtLeast(0.0)
    if (balance > 0) {
        sb.append("⚠️ *Saldo Pendiente: $${String.format(Locale.US, "%.2f", balance)}*\n")
    }
    sb.append("\n🛡️ *Garantía:* ${invoice.warrantyNotes}\n")
    if (invoice.notes.isNotBlank()) {
        sb.append("📝 ${invoice.notes}\n")
    }
    sb.append("\n_¡Gracias por confiar en nuestro servicio técnico!_")

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Compartir Factura")
    context.startActivity(shareIntent)
}
