package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.ClientEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceLineItem
import com.example.data.model.JsonUtils
import com.example.data.model.PaymentStatus
import com.example.data.model.RepairOrderEntity
import com.example.data.model.RepairStatus
import com.example.data.model.UsedPartItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TechnicianAppQATest {

    private lateinit var database: AppDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun qa_verifyPackageIdentityAndAppResources() {
        val appName = context.getString(R.string.app_name)
        assertEquals("RefriServicios", appName)
        assertEquals("com.sanchezluys.refriservicios", context.packageName)
    }

    @Test
    fun qa_clientLifecycle_insertAndSearch() = runBlocking {
        val clientDao = database.clientDao()

        val client = ClientEntity(
            name = "Carlos Mendoza",
            phone = "555-1234",
            email = "carlos@example.com",
            address = "Av. Principal #45"
        )
        val id = clientDao.insert(client)
        assertTrue("Client id should be generated", id > 0)

        val clients = clientDao.getAllClients().first()
        assertEquals(1, clients.size)
        assertEquals("Carlos Mendoza", clients.first().name)

        val searchResult = clientDao.searchClients("Mendoza").first()
        assertEquals(1, searchResult.size)
        assertEquals("555-1234", searchResult.first().phone)
    }

    @Test
    fun qa_inventory_stockAlertsAndDeduction() = runBlocking {
        val inventoryDao = database.inventoryDao()

        val gasR134 = InventoryItemEntity(
            sku = "GAS-134A",
            name = "Gas Refrigerante R134a",
            category = "Consumibles/Gas",
            stock = 5.0,
            minStock = 2.0,
            costPrice = 15.0,
            salePrice = 28.0,
            unit = "latas"
        )
        val termostato = InventoryItemEntity(
            sku = "TER-01",
            name = "Termostato Universal",
            category = "Refrigeración",
            stock = 1.0,
            minStock = 3.0,
            costPrice = 8.0,
            salePrice = 18.0,
            unit = "uds"
        )

        inventoryDao.insert(gasR134)
        inventoryDao.insert(termostato)

        val allItems = inventoryDao.getAllInventory().first()
        assertEquals(2, allItems.size)

        // Low stock detection
        val lowStock = inventoryDao.getLowStockItems().first()
        assertEquals(1, lowStock.size)
        assertEquals("Termostato Universal", lowStock.first().name)

        // Deduct stock
        inventoryDao.updateStock(allItems.first().id, 1.0)
        val updatedLowStock = inventoryDao.getLowStockItems().first()
        assertEquals(2, updatedLowStock.size)
    }

    @Test
    fun qa_repairOrder_statusTransitionsAndPartCalculation() = runBlocking {
        val repairDao = database.repairOrderDao()

        val repair = RepairOrderEntity(
            orderNumber = "ORD-001",
            clientId = 1L,
            clientName = "María Gómez",
            clientPhone = "555-9876",
            applianceCategory = "Refrigerador",
            brand = "Samsung",
            reportedFailure = "No enfría en parte inferior",
            status = RepairStatus.PENDING.name,
            laborCost = 45.0,
            totalCost = 45.0
        )
        val id = repairDao.insert(repair)
        assertTrue(id > 0)

        // Advance status to IN_PROGRESS
        val inProgress = repair.copy(id = id, status = RepairStatus.IN_PROGRESS.name)
        repairDao.update(inProgress)

        var loaded = repairDao.getRepairById(id).first()
        assertEquals(RepairStatus.IN_PROGRESS.name, loaded?.status)

        // Add used part
        val usedParts = listOf(
            UsedPartItem(
                inventoryId = 10L,
                partName = "Capacitor 45uF",
                quantity = 2.0,
                unitPrice = 12.50,
                unit = "uds"
            )
        )
        val partsJson = JsonUtils.usedPartsToJson(usedParts)
        val totalCost = inProgress.laborCost + usedParts.sumOf { it.total }

        val ready = inProgress.copy(
            partsUsedJson = partsJson,
            totalCost = totalCost,
            status = RepairStatus.READY.name
        )
        repairDao.update(ready)

        loaded = repairDao.getRepairById(id).first()
        assertEquals(RepairStatus.READY.name, loaded?.status)
        assertEquals(70.0, loaded?.totalCost ?: 0.0, 0.01)

        val deserializedParts = JsonUtils.jsonToUsedParts(loaded?.partsUsedJson)
        assertEquals(1, deserializedParts.size)
        assertEquals(25.0, deserializedParts.first().total, 0.01)
    }

    @Test
    fun qa_invoice_calculationsAndPaymentFlow() = runBlocking {
        val invoiceDao = database.invoiceDao()

        val lines = listOf(
            InvoiceLineItem("Mano de obra", 1.0, 50.0),
            InvoiceLineItem("Filtro Deshidratador", 2.0, 15.0)
        )
        val subtotal = lines.sumOf { it.total }
        val tax = subtotal * 0.16
        val total = subtotal + tax

        val invoice = InvoiceEntity(
            invoiceNumber = "FAC-2001",
            repairOrderId = 1L,
            clientId = 1L,
            clientName = "María Gómez",
            clientPhone = "555-9876",
            clientAddress = "Calle 10 #123",
            applianceDescription = "Refrigerador Samsung",
            itemsJson = JsonUtils.invoiceLinesToJson(lines),
            subtotal = subtotal,
            taxPercent = 16.0,
            taxAmount = tax,
            totalAmount = total,
            paidAmount = 0.0,
            paymentStatus = PaymentStatus.PENDING.name
        )

        val invId = invoiceDao.insert(invoice)
        assertTrue(invId > 0)

        // Partial payment
        val partial = invoice.copy(id = invId, paidAmount = 40.0, paymentStatus = PaymentStatus.PARTIAL.name)
        invoiceDao.update(partial)
        var loaded = invoiceDao.getInvoiceById(invId).first()
        assertEquals(PaymentStatus.PARTIAL.name, loaded?.paymentStatus)
        assertEquals(40.0, loaded?.paidAmount ?: 0.0, 0.01)

        // Full payment
        val paid = partial.copy(paidAmount = total, paymentStatus = PaymentStatus.PAID.name)
        invoiceDao.update(paid)
        loaded = invoiceDao.getInvoiceById(invId).first()
        assertEquals(PaymentStatus.PAID.name, loaded?.paymentStatus)
        assertEquals(total, loaded?.paidAmount ?: 0.0, 0.01)
    }
}
