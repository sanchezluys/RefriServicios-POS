package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ClientEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.RepairOrderEntity

@Database(
    entities = [
        ClientEntity::class,
        InventoryItemEntity::class,
        RepairOrderEntity::class,
        InvoiceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun repairOrderDao(): RepairOrderDao
    abstract fun invoiceDao(): InvoiceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "refriservicios_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }

        fun getDatabase(context: Context): AppDatabase = getInstance(context)
    }
}
