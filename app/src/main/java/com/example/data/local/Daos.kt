package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ClientEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.RepairOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    fun getClientById(id: Long): Flow<ClientEntity?>

    @Query("SELECT * FROM clients WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchClients(query: String): Flow<List<ClientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(client: ClientEntity): Long

    @Update
    suspend fun update(client: ClientEntity)

    @Delete
    suspend fun delete(client: ClientEntity)
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory ORDER BY name ASC")
    fun getAllInventory(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory WHERE category = :category ORDER BY name ASC")
    fun getInventoryByCategory(category: String): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory WHERE stock <= minStock ORDER BY stock ASC")
    fun getLowStockItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): InventoryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InventoryItemEntity): Long

    @Update
    suspend fun update(item: InventoryItemEntity)

    @Delete
    suspend fun delete(item: InventoryItemEntity)

    @Query("UPDATE inventory SET stock = :newStock WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Double)
}

@Dao
interface RepairOrderDao {
    @Query("SELECT * FROM repairs ORDER BY dateReceived DESC")
    fun getAllRepairOrders(): Flow<List<RepairOrderEntity>>

    @Query("SELECT * FROM repairs WHERE clientId = :clientId ORDER BY dateReceived DESC")
    fun getRepairsByClient(clientId: Long): Flow<List<RepairOrderEntity>>

    @Query("SELECT * FROM repairs WHERE status = :status ORDER BY dateReceived DESC")
    fun getRepairsByStatus(status: String): Flow<List<RepairOrderEntity>>

    @Query("SELECT * FROM repairs WHERE id = :id LIMIT 1")
    fun getRepairById(id: Long): Flow<RepairOrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(repair: RepairOrderEntity): Long

    @Update
    suspend fun update(repair: RepairOrderEntity)

    @Delete
    suspend fun delete(repair: RepairOrderEntity)

    @Query("SELECT COUNT(*) FROM repairs")
    suspend fun getRepairsCount(): Int
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY dateIssued DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE clientId = :clientId ORDER BY dateIssued DESC")
    fun getInvoicesByClient(clientId: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    fun getInvoiceById(id: Long): Flow<InvoiceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: InvoiceEntity): Long

    @Update
    suspend fun update(invoice: InvoiceEntity)

    @Delete
    suspend fun delete(invoice: InvoiceEntity)

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun getInvoicesCount(): Int
}
