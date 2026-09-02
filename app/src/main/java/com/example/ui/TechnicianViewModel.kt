package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ClientEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceLineItem
import com.example.data.model.JsonUtils
import com.example.data.model.PaymentStatus
import com.example.data.model.RepairOrderEntity
import com.example.data.model.RepairStatus
import com.example.data.model.UsedPartItem
import com.example.data.repository.TechnicianRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class TechnicianViewModel(
    private val repository: TechnicianRepository
) : ViewModel() {

    val clients: StateFlow<List<ClientEntity>> = repository.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val repairs: StateFlow<List<RepairOrderEntity>> = repository.allRepairs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventory: StateFlow<List<InventoryItemEntity>> = repository.allInventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Clients ---
    fun saveClient(client: ClientEntity) {
        viewModelScope.launch {
            repository.insertClient(client)
        }
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch {
            repository.deleteClient(client)
        }
    }

    // --- Repairs ---
    fun saveRepair(repair: RepairOrderEntity) {
        viewModelScope.launch {
            repository.insertRepair(repair)
        }
    }

    fun updateRepairStatus(repair: RepairOrderEntity, newStatus: String) {
        viewModelScope.launch {
            val updated = repair.copy(status = newStatus)
            repository.updateRepair(updated)
        }
    }

    fun addPartToRepair(repair: RepairOrderEntity, inventoryItem: InventoryItemEntity, quantity: Double) {
        viewModelScope.launch {
            val existingParts = JsonUtils.jsonToUsedParts(repair.partsUsedJson).toMutableList()
            val totalForPart = quantity * inventoryItem.salePrice
            existingParts.add(
                UsedPartItem(
                    inventoryId = inventoryItem.id,
                    partName = inventoryItem.name,
                    quantity = quantity,
                    unitPrice = inventoryItem.salePrice,
                    unit = inventoryItem.unit
                )
            )

            // Deduct stock in inventory
            val newStock = (inventoryItem.stock - quantity).coerceAtLeast(0.0)
            repository.updateInventoryItem(inventoryItem.copy(stock = newStock))

            // Update repair parts & total cost
            val partsTotal = existingParts.sumOf { it.total }
            val newTotalCost = repair.laborCost + partsTotal
            val updatedRepair = repair.copy(
                partsUsedJson = JsonUtils.usedPartsToJson(existingParts),
                totalCost = newTotalCost
            )
            repository.updateRepair(updatedRepair)
        }
    }

    fun deleteRepair(repair: RepairOrderEntity) {
        viewModelScope.launch {
            repository.deleteRepair(repair)
        }
    }

    // --- Inventory ---
    fun saveInventoryItem(item: InventoryItemEntity) {
        viewModelScope.launch {
            repository.insertInventoryItem(item)
        }
    }

    fun adjustStock(itemId: Long, delta: Double) {
        viewModelScope.launch {
            repository.updateStock(itemId, delta)
        }
    }

    fun deleteInventoryItem(item: InventoryItemEntity) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
        }
    }

    // --- Invoices ---
    fun saveInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch {
            repository.insertInvoice(invoice)
        }
    }

    fun updateInvoicePayment(
        invoice: InvoiceEntity,
        paidAmount: Double,
        status: String,
        paymentMethod: String = invoice.paymentMethod
    ) {
        viewModelScope.launch {
            val updated = invoice.copy(
                paidAmount = paidAmount,
                paymentStatus = status,
                paymentMethod = paymentMethod
            )
            repository.updateInvoice(updated)
        }
    }

    fun deleteInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }

    fun generateInvoiceFromRepair(repair: RepairOrderEntity, onGenerated: (InvoiceEntity) -> Unit = {}) {
        viewModelScope.launch {
            val usedParts = JsonUtils.jsonToUsedParts(repair.partsUsedJson)
            val lineItems = mutableListOf<InvoiceLineItem>()

            // Labor item
            if (repair.laborCost > 0) {
                lineItems.add(
                    InvoiceLineItem(
                        description = "Mano de Obra / Servicio Técnico (${repair.applianceCategory} ${repair.brand})",
                        quantity = 1.0,
                        unitPrice = repair.laborCost
                    )
                )
            }

            // Parts items
            usedParts.forEach { part ->
                lineItems.add(
                    InvoiceLineItem(
                        description = "Repuesto: ${part.partName} (${part.quantity} ${part.unit})",
                        quantity = part.quantity,
                        unitPrice = part.unitPrice
                    )
                )
            }

            val subtotal = lineItems.sumOf { it.total }
            val taxPercent = 0.0 // can be modified in dialog
            val taxAmount = 0.0
            val total = subtotal

            val invoiceNumber = "FAC-${(1000..9999).random()}"
            val newInvoice = InvoiceEntity(
                invoiceNumber = invoiceNumber,
                repairOrderId = repair.id,
                clientId = repair.clientId,
                clientName = repair.clientName,
                clientPhone = repair.clientPhone,
                clientAddress = repair.clientAddress,
                applianceDescription = "${repair.applianceCategory} ${repair.brand} ${repair.model}".trim(),
                itemsJson = JsonUtils.invoiceLinesToJson(lineItems),
                subtotal = subtotal,
                taxPercent = taxPercent,
                taxAmount = taxAmount,
                discountAmount = 0.0,
                totalAmount = total,
                paidAmount = 0.0,
                paymentStatus = PaymentStatus.PENDING.name,
                paymentMethod = "Efectivo",
                warrantyNotes = "Garantía de servicio por ${repair.warrantyDays} días."
            )

            val id = repository.insertInvoice(newInvoice)
            onGenerated(newInvoice.copy(id = id))
        }
    }
}

class TechnicianViewModelFactory(
    private val repository: TechnicianRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TechnicianViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TechnicianViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
