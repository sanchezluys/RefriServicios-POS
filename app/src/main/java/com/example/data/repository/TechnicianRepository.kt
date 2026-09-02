package com.example.data.repository

import com.example.data.local.ClientDao
import com.example.data.local.InventoryDao
import com.example.data.local.InvoiceDao
import com.example.data.local.RepairOrderDao
import com.example.data.model.ClientEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceLineItem
import com.example.data.model.JsonUtils
import com.example.data.model.PaymentStatus
import com.example.data.model.RepairOrderEntity
import com.example.data.model.RepairStatus
import com.example.data.model.UsedPartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TechnicianRepository(
    private val clientDao: ClientDao,
    private val inventoryDao: InventoryDao,
    private val repairOrderDao: RepairOrderDao,
    private val invoiceDao: InvoiceDao
) {
    constructor(db: com.example.data.local.AppDatabase) : this(
        db.clientDao(),
        db.inventoryDao(),
        db.repairOrderDao(),
        db.invoiceDao()
    )

    // Aliases
    suspend fun insertClient(client: ClientEntity) = saveClient(client)
    suspend fun insertRepair(repair: RepairOrderEntity) = saveRepairOrder(repair)
    suspend fun updateRepair(repair: RepairOrderEntity) = saveRepairOrder(repair)
    suspend fun deleteRepair(repair: RepairOrderEntity) = deleteRepairOrder(repair)
    suspend fun insertInventoryItem(item: InventoryItemEntity) = saveInventoryItem(item)
    suspend fun updateInventoryItem(item: InventoryItemEntity) = saveInventoryItem(item)
    suspend fun updateStock(id: Long, delta: Double) = adjustStock(id, delta)
    suspend fun insertInvoice(invoice: InvoiceEntity) = saveInvoice(invoice)
    suspend fun updateInvoice(invoice: InvoiceEntity) = saveInvoice(invoice)
    suspend fun seedInitialDataIfNeeded() = checkAndSeedInitialData()

    // Clients
    val allClients: Flow<List<ClientEntity>> = clientDao.getAllClients()

    suspend fun saveClient(client: ClientEntity): Long = withContext(Dispatchers.IO) {
        if (client.id == 0L) {
            clientDao.insert(client)
        } else {
            clientDao.update(client)
            client.id
        }
    }

    suspend fun deleteClient(client: ClientEntity) = withContext(Dispatchers.IO) {
        clientDao.delete(client)
    }

    // Inventory
    val allInventory: Flow<List<InventoryItemEntity>> = inventoryDao.getAllInventory()
    val lowStockItems: Flow<List<InventoryItemEntity>> = inventoryDao.getLowStockItems()

    suspend fun saveInventoryItem(item: InventoryItemEntity): Long = withContext(Dispatchers.IO) {
        if (item.id == 0L) {
            inventoryDao.insert(item)
        } else {
            inventoryDao.update(item)
            item.id
        }
    }

    suspend fun adjustStock(id: Long, delta: Double) = withContext(Dispatchers.IO) {
        val item = inventoryDao.getItemById(id) ?: return@withContext
        val newStock = (item.stock + delta).coerceAtLeast(0.0)
        inventoryDao.updateStock(id, newStock)
    }

    suspend fun deleteInventoryItem(item: InventoryItemEntity) = withContext(Dispatchers.IO) {
        inventoryDao.delete(item)
    }

    // Repairs
    val allRepairs: Flow<List<RepairOrderEntity>> = repairOrderDao.getAllRepairOrders()

    fun getRepairsForClient(clientId: Long): Flow<List<RepairOrderEntity>> =
        repairOrderDao.getRepairsByClient(clientId)

    suspend fun saveRepairOrder(repair: RepairOrderEntity): Long = withContext(Dispatchers.IO) {
        if (repair.id == 0L) {
            repairOrderDao.insert(repair)
        } else {
            repairOrderDao.update(repair)
            repair.id
        }
    }

    suspend fun deleteRepairOrder(repair: RepairOrderEntity) = withContext(Dispatchers.IO) {
        repairOrderDao.delete(repair)
    }

    suspend fun updateRepairStatus(repair: RepairOrderEntity, newStatus: String) = withContext(Dispatchers.IO) {
        val completedDate = if (newStatus == RepairStatus.READY.name || newStatus == RepairStatus.DELIVERED.name) {
            repair.dateCompleted ?: System.currentTimeMillis()
        } else null

        val updated = repair.copy(
            status = newStatus,
            dateCompleted = completedDate
        )
        repairOrderDao.update(updated)
    }

    suspend fun addPartToRepair(
        repair: RepairOrderEntity,
        inventoryItem: InventoryItemEntity,
        quantity: Double
    ) = withContext(Dispatchers.IO) {
        // Descontar del inventario
        val newStock = (inventoryItem.stock - quantity).coerceAtLeast(0.0)
        inventoryDao.updateStock(inventoryItem.id, newStock)

        // Agregar al JSON de repuestos de la orden
        val currentParts = JsonUtils.jsonToUsedParts(repair.partsUsedJson).toMutableList()
        val existingIndex = currentParts.indexOfFirst { it.inventoryId == inventoryItem.id }

        if (existingIndex >= 0) {
            val existing = currentParts[existingIndex]
            currentParts[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            currentParts.add(
                UsedPartItem(
                    inventoryId = inventoryItem.id,
                    partName = inventoryItem.name,
                    quantity = quantity,
                    unitPrice = inventoryItem.salePrice,
                    unit = inventoryItem.unit
                )
            )
        }

        val partsCost = currentParts.sumOf { it.total }
        val updatedRepair = repair.copy(
            partsUsedJson = JsonUtils.usedPartsToJson(currentParts),
            totalCost = repair.laborCost + partsCost
        )
        repairOrderDao.update(updatedRepair)
    }

    // Invoices
    val allInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()

    fun getInvoicesForClient(clientId: Long): Flow<List<InvoiceEntity>> =
        invoiceDao.getInvoicesByClient(clientId)

    suspend fun saveInvoice(invoice: InvoiceEntity): Long = withContext(Dispatchers.IO) {
        if (invoice.id == 0L) {
            invoiceDao.insert(invoice)
        } else {
            invoiceDao.update(invoice)
            invoice.id
        }
    }

    suspend fun deleteInvoice(invoice: InvoiceEntity) = withContext(Dispatchers.IO) {
        invoiceDao.delete(invoice)
    }

    suspend fun updateInvoicePayment(invoice: InvoiceEntity, paidAmount: Double, status: String) = withContext(Dispatchers.IO) {
        val updated = invoice.copy(
            paidAmount = paidAmount,
            paymentStatus = status
        )
        invoiceDao.update(updated)
    }

    suspend fun createInvoiceFromRepair(
        repair: RepairOrderEntity,
        client: ClientEntity?,
        taxPercent: Double = 0.0,
        discount: Double = 0.0
    ): Long = withContext(Dispatchers.IO) {
        val count = invoiceDao.getInvoicesCount() + 1
        val invoiceNumber = String.format("FAC-%04d", count)

        val items = mutableListOf<InvoiceLineItem>()

        // Add Labor
        if (repair.laborCost > 0) {
            items.add(
                InvoiceLineItem(
                    description = "Mano de Obra Técnica - ${repair.applianceCategory} ${repair.brand}",
                    quantity = 1.0,
                    unitPrice = repair.laborCost
                )
            )
        }

        // Add Parts
        val usedParts = JsonUtils.jsonToUsedParts(repair.partsUsedJson)
        for (part in usedParts) {
            items.add(
                InvoiceLineItem(
                    description = "${part.partName} (${part.quantity} ${part.unit})",
                    quantity = part.quantity,
                    unitPrice = part.unitPrice
                )
            )
        }

        val subtotal = items.sumOf { it.total }
        val taxAmount = subtotal * (taxPercent / 100.0)
        val total = (subtotal + taxAmount - discount).coerceAtLeast(0.0)

        val invoice = InvoiceEntity(
            invoiceNumber = invoiceNumber,
            repairOrderId = repair.id,
            clientId = repair.clientId,
            clientName = repair.clientName,
            clientPhone = repair.clientPhone,
            clientAddress = client?.address ?: repair.clientAddress,
            applianceDescription = "${repair.applianceCategory} ${repair.brand} ${repair.model}".trim(),
            dateIssued = System.currentTimeMillis(),
            itemsJson = JsonUtils.invoiceLinesToJson(items),
            subtotal = subtotal,
            taxPercent = taxPercent,
            taxAmount = taxAmount,
            discountAmount = discount,
            totalAmount = total,
            paidAmount = 0.0,
            paymentStatus = PaymentStatus.PENDING.name,
            paymentMethod = "Efectivo",
            warrantyNotes = "Garantía técnica de ${repair.warrantyDays} días.",
            notes = "Orden de Servicio #${repair.orderNumber}. Diagnóstico: ${repair.technicalDiagnosis.ifBlank { repair.reportedFailure }}"
        )

        invoiceDao.insert(invoice)
    }

    // Prepopulate initial sample data if database is empty
    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val clientCount = clientDao.getAllClients()
        // Check if there are any clients
        val repairsCount = repairOrderDao.getRepairsCount()
        if (repairsCount > 0) return@withContext

        // Seed Inventory
        val initialItems = listOf(
            InventoryItemEntity(
                sku = "GAS-134A",
                name = "Gas Refrigerante R134a (Cilindro)",
                category = "Consumibles/Gas",
                stock = 13.6,
                minStock = 4.0,
                costPrice = 65.0,
                salePrice = 95.0,
                unit = "kg",
                description = "Refrigerante ecológico para refrigeradores domésticos y vitrinas comerciales"
            ),
            InventoryItemEntity(
                sku = "GAS-600A",
                name = "Gas Isobutano R600a (Lata)",
                category = "Consumibles/Gas",
                stock = 8.0,
                minStock = 3.0,
                costPrice = 8.5,
                salePrice = 18.0,
                unit = "latas",
                description = "Gas hidrocarburo para refrigeradores modernos inverter"
            ),
            InventoryItemEntity(
                sku = "COMP-14EMB",
                name = "Compresor Embraco 1/4 HP R134a 115V",
                category = "Refrigeración",
                stock = 3.0,
                minStock = 2.0,
                costPrice = 90.0,
                salePrice = 145.0,
                unit = "uds",
                description = "Motor compresor de alto rendimiento para refrigeradores de 10 a 16 pies"
            ),
            InventoryItemEntity(
                sku = "TERM-K59",
                name = "Termostato Universal Danfoss K59",
                category = "Refrigeración",
                stock = 6.0,
                minStock = 3.0,
                costPrice = 7.0,
                salePrice = 16.0,
                unit = "uds",
                description = "Control de temperatura con capilar largo para congelador y refrigerador"
            ),
            InventoryItemEntity(
                sku = "BIM-L55",
                name = "Bimetal Deshielo L55 con Fusible Térmico",
                category = "Refrigeración",
                stock = 1.0, // Low stock for demonstration!
                minStock = 3.0,
                costPrice = 4.5,
                salePrice = 12.0,
                unit = "uds",
                description = "Sensor de descongelamiento para refrigerador No-Frost"
            ),
            InventoryItemEntity(
                sku = "FILT-14",
                name = "Filtro Deshidratador 1/4 con Válvula",
                category = "Refrigeración",
                stock = 15.0,
                minStock = 5.0,
                costPrice = 2.5,
                salePrice = 6.5,
                unit = "uds",
                description = "Filtro secador molecular con toma de servicio para vacío y carga"
            ),
            InventoryItemEntity(
                sku = "BOM-LAV-UNIV",
                name = "Bomba de Desagüe Universal Lavadora LG/Samsung",
                category = "Lavado",
                stock = 4.0,
                minStock = 2.0,
                costPrice = 14.0,
                salePrice = 28.0,
                unit = "uds",
                description = "Bomba de drenaje con 3 tornillos para lavadoras automáticas"
            ),
            InventoryItemEntity(
                sku = "VAL-SOL-DOB",
                name = "Válvula Entrada de Agua Doble 120V",
                category = "Lavado",
                stock = 5.0,
                minStock = 2.0,
                costPrice = 9.0,
                salePrice = 22.0,
                unit = "uds",
                description = "Electroválvula doble para agua fría y caliente en lavadoras y centros de lavado"
            ),
            InventoryItemEntity(
                sku = "CAP-45UF",
                name = "Capacitor de Marcha 45uF + 5uF 440VAC",
                category = "Climatización",
                stock = 7.0,
                minStock = 3.0,
                costPrice = 8.0,
                salePrice = 19.0,
                unit = "uds",
                description = "Capacitor dual para compresor y ventilador de Aire Acondicionado Split"
            ),
            InventoryItemEntity(
                sku = "SOL-PLATA5",
                name = "Varilla Soldadura de Plata 5% Harris",
                category = "Consumibles/Gas",
                stock = 2.0, // Low stock alert
                minStock = 5.0,
                costPrice = 4.0,
                salePrice = 9.0,
                unit = "uds",
                description = "Varilla fosfocobre con plata para unión de tuberías de cobre de refrigeración"
            )
        )

        for (item in initialItems) {
            inventoryDao.insert(item)
        }

        // Seed Clients
        val client1Id = clientDao.insert(
            ClientEntity(
                name = "Ing. Roberto Salazar",
                phone = "+58 412 555-1234",
                email = "roberto.salazar@correo.com",
                address = "Urb. Los Naranjos, Calle 3, Quinta María",
                cityOrZone = "Zona Norte",
                notes = "Cliente frecuente. Portón eléctrico negro."
            )
        )

        val client2Id = clientDao.insert(
            ClientEntity(
                name = "Dra. Carmen Mendoza",
                phone = "+58 414 789-4321",
                email = "carmen.mendoza@clinica.com",
                address = "Av. Las Américas, Edif. Torre Azul, Piso 6, Apto 6-B",
                cityOrZone = "Centro",
                notes = "Horario preferido: tardes después de las 2:00 PM."
            )
        )

        val client3Id = clientDao.insert(
            ClientEntity(
                name = "Panadería y Delicateses La Suiza",
                phone = "+58 424 901-8877",
                email = "administracion@lasuiza.com",
                address = "Calle Real #45, frente a Plaza Bolívar",
                cityOrZone = "Casco Histórico",
                notes = "Contacto: Sr. Valenzuela (Encargado). Cavas y vitrinas comerciales."
            )
        )

        // Seed Repair Orders
        val partsRep1 = listOf(
            UsedPartItem(1L, "Gas Refrigerante R134a", 0.35, 95.0, "kg"),
            UsedPartItem(6L, "Filtro Deshidratador 1/4 con Válvula", 1.0, 6.5, "uds")
        )
        val repair1Id = repairOrderDao.insert(
            RepairOrderEntity(
                orderNumber = "ORD-101",
                clientId = client1Id,
                clientName = "Ing. Roberto Salazar",
                clientPhone = "+58 412 555-1234",
                clientAddress = "Urb. Los Naranjos, Calle 3, Quinta María",
                applianceCategory = "Refrigerador",
                brand = "Samsung Inverter",
                model = "RT38K5930SL",
                serialNumber = "SAM-99824X",
                reportedFailure = "No enfría la parte inferior del refrigerador. Congelador hace escarcha excesiva.",
                technicalDiagnosis = "Bloqueo de hielo en evaporador por falla de sensor bimetal de deshielo. Sistema sellado con presión correcta.",
                status = RepairStatus.IN_PROGRESS.name,
                laborCost = 45.0,
                partsUsedJson = JsonUtils.usedPartsToJson(partsRep1),
                totalCost = 45.0 + (0.35 * 95.0 + 6.5),
                warrantyDays = 60,
                dateReceived = System.currentTimeMillis() - 86400000L * 1,
                dateEstimated = System.currentTimeMillis() + 86400000L * 1,
                technicianNotes = "Se descongeló el serpentín, se reemplazará sensor y se monitoreará ciclo."
            )
        )

        val partsRep2 = listOf(
            UsedPartItem(7L, "Bomba de Desagüe Universal Lavadora LG/Samsung", 1.0, 28.0, "uds")
        )
        val repair2Id = repairOrderDao.insert(
            RepairOrderEntity(
                orderNumber = "ORD-102",
                clientId = client2Id,
                clientName = "Dra. Carmen Mendoza",
                clientPhone = "+58 414 789-4321",
                clientAddress = "Av. Las Américas, Edif. Torre Azul, Piso 6, Apto 6-B",
                applianceCategory = "Lavadora",
                brand = "LG Direct Drive 18kg",
                model = "WT18WSBP",
                serialNumber = "LG-DD-4501",
                reportedFailure = "Lavadora se detiene con código de error OE y no desagua el agua.",
                technicalDiagnosis = "Bomba de expulsión trabada mecánicamente con desgaste interno en aspas.",
                status = RepairStatus.READY.name,
                laborCost = 35.0,
                partsUsedJson = JsonUtils.usedPartsToJson(partsRep2),
                totalCost = 35.0 + 28.0,
                warrantyDays = 90,
                dateReceived = System.currentTimeMillis() - 86400000L * 2,
                dateEstimated = System.currentTimeMillis(),
                dateCompleted = System.currentTimeMillis(),
                technicianNotes = "Instalada bomba nueva y realizada prueba completa de centrifugado y drenado exitosa."
            )
        )

        val partsRep3 = listOf(
            UsedPartItem(3L, "Compresor Embraco 1/4 HP R134a 115V", 1.0, 145.0, "uds"),
            UsedPartItem(1L, "Gas Refrigerante R134a", 0.6, 95.0, "kg"),
            UsedPartItem(6L, "Filtro Deshidratador 1/4 con Válvula", 1.0, 6.5, "uds")
        )
        repairOrderDao.insert(
            RepairOrderEntity(
                orderNumber = "ORD-103",
                clientId = client3Id,
                clientName = "Panadería y Delicateses La Suiza",
                clientPhone = "+58 424 901-8877",
                clientAddress = "Calle Real #45, frente a Plaza Bolívar",
                applianceCategory = "Congelador",
                brand = "Torrey",
                model = "CH-500",
                serialNumber = "TRY-8812",
                reportedFailure = "Congelador horizontal saltó el térmico y huele a quemado en el motor.",
                technicalDiagnosis = "Compresor aterrizado a masa (cortocircuito interno en bobinas de arranque).",
                status = RepairStatus.WAITING_PARTS.name,
                laborCost = 80.0,
                partsUsedJson = JsonUtils.usedPartsToJson(partsRep3),
                totalCost = 80.0 + 145.0 + (0.6 * 95.0) + 6.5,
                warrantyDays = 90,
                dateReceived = System.currentTimeMillis() - 86400000L * 3,
                dateEstimated = System.currentTimeMillis() + 86400000L * 2,
                technicianNotes = "Presupuesto aprobado por el encargado. Esperando llegada de válvula de expansión."
            )
        )

        // Seed Invoices
        val invoiceItems1 = listOf(
            InvoiceLineItem("Mano de Obra - Reemplazo Bomba de Drenaje Lavadora LG", 1.0, 35.0),
            InvoiceLineItem("Bomba de Desagüe Universal Lavadora LG/Samsung", 1.0, 28.0)
        )
        invoiceDao.insert(
            InvoiceEntity(
                invoiceNumber = "FAC-0001",
                repairOrderId = repair2Id,
                clientId = client2Id,
                clientName = "Dra. Carmen Mendoza",
                clientPhone = "+58 414 789-4321",
                clientAddress = "Av. Las Américas, Edif. Torre Azul, Piso 6, Apto 6-B",
                applianceDescription = "Lavadora LG Direct Drive 18kg (Mod. WT18WSBP)",
                dateIssued = System.currentTimeMillis(),
                itemsJson = JsonUtils.invoiceLinesToJson(invoiceItems1),
                subtotal = 63.0,
                taxPercent = 0.0,
                taxAmount = 0.0,
                discountAmount = 3.0,
                totalAmount = 60.0,
                paidAmount = 60.0,
                paymentStatus = PaymentStatus.PAID.name,
                paymentMethod = "Transferencia",
                warrantyNotes = "Garantía de 90 días sobre bomba instalada y mano de obra.",
                notes = "Servicio completado a satisfacción. Orden ORD-102."
            )
        )
    }
}
