package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val address: String,
    val cityOrZone: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "inventory")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sku: String,
    val name: String,
    val category: String, // Refrigeración, Lavado, Climatización, Consumibles/Gas, Electricidad, Otros
    val stock: Double,
    val minStock: Double = 2.0,
    val costPrice: Double,
    val salePrice: Double,
    val unit: String = "uds", // uds, kg, latas, mts
    val description: String = ""
)

@Entity(tableName = "repairs")
data class RepairOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String, // ej. REP-1001
    val clientId: Long,
    val clientName: String,
    val clientPhone: String,
    val clientAddress: String = "",
    val applianceCategory: String, // Refrigerador, Lavadora, Secadora, Aire Acondicionado, Congelador, Microondas, Otro
    val brand: String,
    val model: String = "",
    val serialNumber: String = "",
    val reportedFailure: String,
    val technicalDiagnosis: String = "",
    val status: String = RepairStatus.PENDING.name,
    val laborCost: Double = 0.0,
    val partsUsedJson: String = "[]", // JSON string de List<UsedPartItem>
    val totalCost: Double = 0.0,
    val warrantyDays: Int = 30,
    val dateReceived: Long = System.currentTimeMillis(),
    val dateEstimated: Long = System.currentTimeMillis() + 86400000L * 2,
    val dateCompleted: Long? = null,
    val technicianNotes: String = ""
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String, // ej. FAC-2024-001
    val repairOrderId: Long? = null,
    val clientId: Long,
    val clientName: String,
    val clientPhone: String,
    val clientAddress: String,
    val applianceDescription: String,
    val dateIssued: Long = System.currentTimeMillis(),
    val itemsJson: String = "[]", // JSON string de List<InvoiceLineItem>
    val subtotal: Double,
    val taxPercent: Double = 0.0,
    val taxAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val paymentStatus: String = PaymentStatus.PENDING.name, // PAID, PENDING, PARTIAL
    val paymentMethod: String = "Efectivo", // Efectivo, Transferencia, Pago Móvil / Tarjeta
    val warrantyNotes: String = "Garantía de 30 días en mano de obra y repuestos especificados.",
    val notes: String = ""
)

enum class RepairStatus(val label: String, val colorHex: Long) {
    PENDING("En Diagnóstico", 0xFFEAB308), // Amarillo
    IN_PROGRESS("En Reparación", 0xFF3B82F6), // Azul
    WAITING_PARTS("Espera Repuestos", 0xFFF97316), // Naranja
    READY("Listo / En Pruebas", 0xFF10B981), // Verde
    DELIVERED("Entregado", 0xFF6B7280), // Gris
    CANCELLED("Cancelado", 0xFFEF4444) // Rojo
}

enum class PaymentStatus(val label: String) {
    PAID("Pagada"),
    PENDING("Pendiente"),
    PARTIAL("Abono Parcial")
}

data class UsedPartItem(
    val inventoryId: Long,
    val partName: String,
    val quantity: Double,
    val unitPrice: Double,
    val unit: String
) {
    val total: Double get() = quantity * unitPrice
}

data class InvoiceLineItem(
    val description: String,
    val quantity: Double,
    val unitPrice: Double
) {
    val total: Double get() = quantity * unitPrice
}
