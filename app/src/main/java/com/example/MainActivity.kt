package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.model.ClientEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.RepairOrderEntity
import com.example.data.model.RepairStatus
import com.example.data.repository.TechnicianRepository
import com.example.ui.TechnicianViewModel
import com.example.ui.TechnicianViewModelFactory
import com.example.ui.screens.ClientsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.InvoicePreviewDialog
import com.example.ui.screens.InvoicesScreen
import com.example.ui.screens.NewClientDialog
import com.example.ui.screens.NewInventoryItemDialog
import com.example.ui.screens.NewRepairDialog
import com.example.ui.screens.RepairDetailDialog
import com.example.ui.screens.RepairsScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val tag: String) {
    object Dashboard : Screen("dashboard", "Inicio", Icons.Default.Home, "nav_item_dashboard")
    object Repairs : Screen("repairs", "Reparaciones", Icons.Default.Build, "nav_item_repairs")
    object Clients : Screen("clients", "Clientes", Icons.Default.People, "nav_item_clients")
    object Inventory : Screen("inventory", "Inventario", Icons.Default.Inventory2, "nav_item_inventory")
    object Invoices : Screen("invoices", "Facturación", Icons.Default.ReceiptLong, "nav_item_invoices")
}

val NAV_ITEMS = listOf(
    Screen.Dashboard,
    Screen.Repairs,
    Screen.Clients,
    Screen.Inventory,
    Screen.Invoices
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { TechnicianRepository(database) }
    val viewModel: TechnicianViewModel = viewModel(
        factory = TechnicianViewModelFactory(repository)
    )

    // Seed initial mock sample data if DB is empty
    LaunchedEffect(Unit) {
        repository.seedInitialDataIfNeeded()
    }

    val clients by viewModel.clients.collectAsStateWithLifecycle()
    val repairs by viewModel.repairs.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    // Active dialog states
    var previewInvoice by remember { mutableStateOf<InvoiceEntity?>(null) }
    var detailRepair by remember { mutableStateOf<RepairOrderEntity?>(null) }
    var showNewRepairDialog by remember { mutableStateOf(false) }
    var showNewClientDialog by remember { mutableStateOf(false) }
    var showNewInventoryDialog by remember { mutableStateOf(false) }

    // Adaptive screen width detection
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    // Badges counters
    val activeRepairsCount = remember(repairs) {
        repairs.count {
            it.status == RepairStatus.PENDING.name ||
            it.status == RepairStatus.IN_PROGRESS.name ||
            it.status == RepairStatus.WAITING_PARTS.name
        }
    }
    val lowStockCount = remember(inventory) {
        inventory.count { it.stock <= it.minStock }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "RefriServicios",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 19.sp,
                                letterSpacing = (-0.3).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "com.sanchezluys.refriservicios",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* Status info */ },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outline)
                    .testTag("main_top_app_bar")
            )
        },
        bottomBar = {
            if (!isWideScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    NAV_ITEMS.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                if (screen == Screen.Repairs && activeRepairsCount > 0) {
                                    BadgedBox(badge = { Badge { Text("$activeRepairsCount") } }) {
                                        Icon(screen.icon, contentDescription = screen.title)
                                    }
                                } else if (screen == Screen.Inventory && lowStockCount > 0) {
                                    BadgedBox(badge = { Badge { Text("$lowStockCount") } }) {
                                        Icon(screen.icon, contentDescription = screen.title)
                                    }
                                } else {
                                    Icon(screen.icon, contentDescription = screen.title)
                                }
                            },
                            label = {
                                Text(
                                    screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(screen.tag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tablet / Foldable Navigation Rail
            if (isWideScreen) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    NAV_ITEMS.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                if (screen == Screen.Repairs && activeRepairsCount > 0) {
                                    BadgedBox(badge = { Badge { Text("$activeRepairsCount") } }) {
                                        Icon(screen.icon, contentDescription = screen.title)
                                    }
                                } else {
                                    Icon(screen.icon, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title, fontSize = 11.sp) },
                            modifier = Modifier.testTag(screen.tag)
                        )
                    }
                }
            }

            // Screen Navigation Host
            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Dashboard.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Screen.Dashboard.route) {
                        DashboardScreen(
                            repairs = repairs,
                            clients = clients,
                            inventory = inventory,
                            invoices = invoices,
                            onNavigateToRepairs = { navController.navigate(Screen.Repairs.route) },
                            onNavigateToClients = { navController.navigate(Screen.Clients.route) },
                            onNavigateToInventory = { navController.navigate(Screen.Inventory.route) },
                            onNavigateToInvoices = { navController.navigate(Screen.Invoices.route) },
                            onSelectRepair = { detailRepair = it },
                            onSelectInvoice = { previewInvoice = it },
                            onNewRepair = { showNewRepairDialog = true },
                            onNewClient = { showNewClientDialog = true },
                            onNewInventory = { showNewInventoryDialog = true }
                        )
                    }

                    composable(Screen.Repairs.route) {
                        RepairsScreen(
                            repairs = repairs,
                            clients = clients,
                            inventory = inventory,
                            onSaveRepair = { viewModel.saveRepair(it) },
                            onDeleteRepair = { viewModel.deleteRepair(it) },
                            onUpdateStatus = { rep, status -> viewModel.updateRepairStatus(rep, status) },
                            onAddPartToRepair = { rep, item, qty -> viewModel.addPartToRepair(rep, item, qty) },
                            onGenerateInvoiceFromRepair = { rep ->
                                viewModel.generateInvoiceFromRepair(rep) { inv ->
                                    previewInvoice = inv
                                }
                            }
                        )
                    }

                    composable(Screen.Clients.route) {
                        ClientsScreen(
                            clients = clients,
                            repairs = repairs,
                            invoices = invoices,
                            onSaveClient = { viewModel.saveClient(it) },
                            onDeleteClient = { viewModel.deleteClient(it) },
                            onSelectRepair = { detailRepair = it },
                            onSelectInvoice = { previewInvoice = it },
                            onNewRepairForClient = { client ->
                                showNewRepairDialog = true
                            }
                        )
                    }

                    composable(Screen.Inventory.route) {
                        InventoryScreen(
                            inventory = inventory,
                            onSaveItem = { viewModel.saveInventoryItem(it) },
                            onAdjustStock = { id, delta -> viewModel.adjustStock(id, delta) },
                            onDeleteItem = { viewModel.deleteInventoryItem(it) }
                        )
                    }

                    composable(Screen.Invoices.route) {
                        InvoicesScreen(
                            invoices = invoices,
                            clients = clients,
                            onSelectInvoice = { previewInvoice = it },
                            onDeleteInvoice = { viewModel.deleteInvoice(it) },
                            onSaveInvoice = { viewModel.saveInvoice(it) }
                        )
                    }
                }
            }
        }
    }

    // Invoice Preview & Share Dialog
    previewInvoice?.let { inv ->
        InvoicePreviewDialog(
            invoice = inv,
            onDismiss = { previewInvoice = null },
            onUpdatePayment = { paid, status ->
                viewModel.updateInvoicePayment(inv, paid, status)
                previewInvoice = inv.copy(paidAmount = paid, paymentStatus = status)
            },
            onDelete = {
                viewModel.deleteInvoice(inv)
                previewInvoice = null
            }
        )
    }

    // Repair Order Detail Dialog
    detailRepair?.let { currentRepair ->
        // Get the latest entity from state
        val latestRepair = repairs.find { it.id == currentRepair.id } ?: currentRepair
        RepairDetailDialog(
            repair = latestRepair,
            inventoryList = inventory,
            onDismiss = { detailRepair = null },
            onUpdateStatus = { newStatus ->
                viewModel.updateRepairStatus(latestRepair, newStatus)
            },
            onAddPart = { item, qty ->
                viewModel.addPartToRepair(latestRepair, item, qty)
            },
            onGenerateInvoice = {
                viewModel.generateInvoiceFromRepair(latestRepair) { newInv ->
                    previewInvoice = newInv
                }
                detailRepair = null
            },
            onDelete = {
                viewModel.deleteRepair(latestRepair)
                detailRepair = null
            }
        )
    }

    // New Repair Dialog
    if (showNewRepairDialog) {
        NewRepairDialog(
            clients = clients,
            onDismiss = { showNewRepairDialog = false },
            onSave = {
                viewModel.saveRepair(it)
                showNewRepairDialog = false
            }
        )
    }

    // New Client Dialog
    if (showNewClientDialog) {
        NewClientDialog(
            onDismiss = { showNewClientDialog = false },
            onSave = {
                viewModel.saveClient(it)
                showNewClientDialog = false
            }
        )
    }

    // New Inventory Item Dialog
    if (showNewInventoryDialog) {
        NewInventoryItemDialog(
            onDismiss = { showNewInventoryDialog = false },
            onSave = {
                viewModel.saveInventoryItem(it)
                showNewInventoryDialog = false
            }
        )
    }
}
