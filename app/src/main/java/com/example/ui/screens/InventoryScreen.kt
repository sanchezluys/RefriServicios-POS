package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryItemEntity
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MetricStatCard
import com.example.ui.components.SearchFilterBar
import java.util.Locale

@Composable
fun InventoryScreen(
    inventory: List<InventoryItemEntity>,
    onSaveItem: (InventoryItemEntity) -> Unit,
    onAdjustStock: (Long, Double) -> Unit,
    onDeleteItem: (InventoryItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }
    var showLowStockOnly by remember { mutableStateOf(false) }

    var itemToEdit by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var showNewItemDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<InventoryItemEntity?>(null) }

    val lowStockCount = remember(inventory) { inventory.count { it.stock <= it.minStock } }
    val totalValuation = remember(inventory) { inventory.sumOf { it.stock * it.salePrice } }

    val filteredList = remember(inventory, searchQuery, selectedCategory, showLowStockOnly) {
        inventory.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.sku.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "Todas" || item.category.equals(selectedCategory, ignoreCase = true)
            val matchesLowStock = !showLowStockOnly || item.stock <= item.minStock
            matchesQuery && matchesCategory && matchesLowStock
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    itemToEdit = null
                    showNewItemDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_inventory_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Repuesto")
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
            // Metrics Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Repuestos",
                    value = "${inventory.size}",
                    subtitle = "Ítems en catálogo",
                    icon = Icons.Default.Inventory2,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Stock Bajo",
                    value = "$lowStockCount",
                    subtitle = if (lowStockCount > 0) "Reponer pronto" else "Inventario óptimo",
                    icon = Icons.Default.Warning,
                    iconColor = if (lowStockCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    onClick = { showLowStockOnly = !showLowStockOnly },
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Valor Venta",
                    value = String.format(Locale.US, "$%.0f", totalValuation),
                    subtitle = "Capital en repuestos",
                    icon = Icons.Default.AttachMoney,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1.1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Buscar repuesto, código o categoría..."
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Chips Row
            val allCats = listOf("Todas") + INVENTORY_CATEGORIES
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                allCats.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat && !showLowStockOnly,
                        onClick = {
                            selectedCategory = cat
                            showLowStockOnly = false
                        },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                FilterChip(
                    selected = showLowStockOnly,
                    onClick = { showLowStockOnly = !showLowStockOnly },
                    label = { Text("⚠️ Stock Crítico ($lowStockCount)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredList.isEmpty()) {
                EmptyStateCard(
                    title = "Sin repuestos",
                    message = "No se encontraron repuestos o consumibles con los filtros seleccionados.",
                    icon = Icons.Default.Inventory2
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        InventoryItemCard(
                            item = item,
                            onIncrement = { onAdjustStock(item.id, 1.0) },
                            onDecrement = { onAdjustStock(item.id, -1.0) },
                            onEdit = {
                                itemToEdit = item
                                showNewItemDialog = true
                            },
                            onDelete = {
                                itemToDelete = item
                            }
                        )
                    }
                }
            }
        }
    }

    if (showNewItemDialog) {
        NewInventoryItemDialog(
            itemToEdit = itemToEdit,
            onDismiss = { showNewItemDialog = false },
            onSave = {
                onSaveItem(it)
                showNewItemDialog = false
            }
        )
    }

    itemToDelete?.let { item ->
        ConfirmDeleteDialog(
            title = "Eliminar Repuesto",
            message = "¿Seguro que deseas eliminar '${item.name}' del catálogo?",
            onConfirm = {
                onDeleteItem(item)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItemEntity,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isLowStock = item.stock <= item.minStock

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isLowStock) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(16.dp)
            )
            .testTag("inventory_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: SKU & Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.sku,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (isLowStock) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stock Crítico", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Item Name & Description
            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stock & Price Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Stock Adjuster
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .testTag("decrement_stock_btn_${item.id}")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Restar", modifier = Modifier.size(16.dp))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = "${item.stock} ${item.unit}",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "mín: ${item.minStock} ${item.unit}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .testTag("increment_stock_btn_${item.id}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Sumar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }

                // Prices
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.US, "Venta: $%.2f", item.salePrice),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (item.costPrice > 0) {
                        Text(
                            text = String.format(Locale.US, "Costo: $%.2f", item.costPrice),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(4.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
