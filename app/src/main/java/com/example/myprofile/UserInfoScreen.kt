package com.example.myprofile

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myprofile.models.UpdateUserDto
import com.example.myprofile.models.User
import com.example.myprofile.models.SavePrinterMacDto
import com.example.myprofile.network.RetrofitInstance
import com.example.myprofile.network.auth.Jwt
import com.example.myprofile.network.auth.Session
import com.example.myprofile.printer.ZebraPrinterService
import com.example.myprofile.ui.components.*
import com.example.myprofile.ui.responsive.WithUiMetrics
import com.example.myprofile.ui.responsive.UiMetrics
import com.example.myprofile.ui.theme.MyPrimary
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.example.myprofile.printer.ZplFactory
import kotlinx.coroutines.delay

@Preview(showSystemUi = true)
@Composable
fun UserInfoPreview() {
    UserInfoScreen(
        navController = rememberNavController(),
        onLogout = {}
    )
}

@Composable
fun UserInfoScreen(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    WithUiMetrics { m ->
        // --- UI state ---
        var editMode by remember { mutableStateOf(false) }
        var showPrinterDialog by remember { mutableStateOf(false) }
        var printerStatus by remember { mutableStateOf("Checking permissions...") }
        val scannedCode = remember { mutableStateOf<String?>(null) }
        var permissionsGranted by remember { mutableStateOf(false) }

        // --- env ---
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val printerSvc = remember { ZebraPrinterService(context) }

        // --- Permission handling ---
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            permissionsGranted = allGranted
            printerStatus = if (allGranted) {
                "Permissions granted, checking printer..."
            } else {
                "Bluetooth permissions required"
            }
        }

        // Check and request permissions on startup
        LaunchedEffect(Unit) {
            when (printerSvc.checkEnvironment()) {
                ZebraPrinterService.PermissionStatus.READY -> {
                    permissionsGranted = true
                    printerStatus = "Checking printer connection."
                }
                ZebraPrinterService.PermissionStatus.MISSING_PERMISSIONS -> {
                    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        arrayOf(
                            android.Manifest.permission.BLUETOOTH_CONNECT,
                            android.Manifest.permission.BLUETOOTH_SCAN
                        )
                    } else {
                        arrayOf(
                            android.Manifest.permission.BLUETOOTH,
                            android.Manifest.permission.BLUETOOTH_ADMIN,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    }
                    permissionLauncher.launch(permissions)
                }
                ZebraPrinterService.PermissionStatus.BLUETOOTH_NOT_AVAILABLE -> {
                    printerStatus = "Bluetooth not available on this device"
                }
                ZebraPrinterService.PermissionStatus.BLUETOOTH_DISABLED -> {
                    printerStatus = "Please enable Bluetooth"
                }
            }
        }

        // -------- Helpers for MAC vs username ----------
        val macRegex = remember { Regex("^[0-9A-F]{2}(:[0-9A-F]{2}){5}\$") }
        val hex12Regex = remember { Regex("^[0-9A-F]{12}\$") }

        fun looksLikeMac(raw: String): Boolean {
            val s = raw.trim().uppercase()
            return macRegex.matches(s) || hex12Regex.matches(s)
        }

        fun normalizeMac(raw: String): String {
            val hex = raw.filter(Char::isLetterOrDigit)
                .uppercase()
                .filter { it in "0123456789ABCDEF" }
                .take(12)
            return hex.chunked(2).joinToString(":")
        }

        // --- connect using server's PrinterInfo (Bluetooth MAC only) ---
        suspend fun ensureConnectedUsingServerInfo(): Boolean {
            val info = runCatching { RetrofitInstance.api.getPrinterInfo() }.getOrNull()
            if (info == null) {
                printerStatus = "No printer info from server"
                return false
            }

            // Try Bluetooth via MAC
            if (!info.mac.isNullOrBlank()) {
                val mac = info.mac
                val paired = printerSvc.ensurePaired(mac)
                if (!paired) {
                    printerStatus = "Pairing failed ($mac)"
                    return false
                }
                val ok = printerSvc.connectBluetooth(mac)
                if (ok) {
                    printerStatus = "Connected ($mac)"
                    return true
                } else {
                    printerStatus = "Connection failed ($mac)"
                    return false
                }
            }

            printerStatus = "No MAC address configured"
            return false
        }

// 2. Simplified connection on startup
        LaunchedEffect(permissionsGranted) {
            if (permissionsGranted && !printerSvc.isConnected()) {
                try {
                    val printerInfo = RetrofitInstance.api.getPrinterInfo()
                    if (!printerInfo.mac.isNullOrBlank()) {
                        val paired = printerSvc.ensurePaired(printerInfo.mac)
                        if (paired) {
                            val connected = printerSvc.connectBluetooth(printerInfo.mac)
                            printerStatus = if (connected) {
                                "Connected to printer"
                            } else {
                                "Printer not connected"
                            }
                        } else {
                            printerStatus = "Printer not paired"
                        }
                    } else {
                        printerStatus = "No printer configured"
                    }
                } catch (e: Exception) {
                    printerStatus = "Connection check failed"
                }
            }
        }

        // --- scan providers ---
        val useDataWedge = remember { hasDataWedge(context) }
        val zebraHelper = remember {
            BarcodeScanHelper(
                context,
                object : BarcodeScanHelper.BarcodeScanListener {
                    override fun onBarcodeScanned(data: String) {
                        handleScannedCode(
                            data = data,
                            scannedCode = scannedCode,
                            saveAndConnect = { normalizedMac ->
                                scope.launch {
                                    // save MAC at pair and connect
                                    val saved = runCatching {
                                        RetrofitInstance.api.savePrinterMac(SavePrinterMacDto(normalizedMac))
                                    }.isSuccess
                                    if (!saved) {
                                        Toast.makeText(context, "Save MAC failed", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }
                                    val paired = printerSvc.ensurePaired(normalizedMac)
                                    if (!paired) {
                                        printerStatus = "Pairing failed ($normalizedMac)"
                                        Toast.makeText(context, "Pairing failed", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }
                                    val ok = printerSvc.connectBluetooth(normalizedMac)
                                    printerStatus = if (ok) "Connected ($normalizedMac)" else "Connection failed"
                                    Toast.makeText(
                                        context,
                                        if (ok) "Printer connected" else "Connect failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onUser = { username ->
                                Toast.makeText(context, "Scanned username: $username", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    override fun onError(error: String) {
                        Toast.makeText(context, "Scan error: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        val mlKit = remember { BarcodeScanMobile(context) }

        // Register/unregister DataWedge receiver
        DisposableEffect(useDataWedge) {
            if (useDataWedge) zebraHelper.registerReceiver()
            onDispose { if (useDataWedge) zebraHelper.unregisterReceiver() }
        }

        // Scan action
        val onScanClick: () -> Unit = remember(useDataWedge) {
            {
                if (useDataWedge) {
                    zebraHelper.triggerScan()
                } else {
                    mlKit.ensureInstalled(
                        onReady = {
                            mlKit.startScan(
                                onResult = { code ->
                                    if (!code.isNullOrBlank()) {
                                        handleScannedCode(
                                            data = code,
                                            scannedCode = scannedCode,
                                            saveAndConnect = { normalizedMac ->
                                                scope.launch {
                                                    val saved = runCatching {
                                                        RetrofitInstance.api.savePrinterMac(SavePrinterMacDto(normalizedMac))
                                                    }.isSuccess
                                                    if (!saved) {
                                                        Toast.makeText(context, "Save MAC failed", Toast.LENGTH_SHORT).show()
                                                        return@launch
                                                    }
                                                    val paired = printerSvc.ensurePaired(normalizedMac)
                                                    if (!paired) {
                                                        printerStatus = "Pairing failed ($normalizedMac)"
                                                        Toast.makeText(context, "Pairing failed", Toast.LENGTH_SHORT).show()
                                                        return@launch
                                                    }
                                                    val ok = printerSvc.connectBluetooth(normalizedMac)
                                                    printerStatus = if (ok) "Connected ($normalizedMac)" else "Connection failed"
                                                    Toast.makeText(
                                                        context,
                                                        if (ok) "Printer connected" else "Connect failed",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            onUser = { username ->
                                                Toast.makeText(context, "Scanned username: $username", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                },
                                onCancel = { Log.d("SCANNED_CODE", "Scan anulat") },
                                onError = { e ->
                                    Toast.makeText(context, "Scan error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onError = { e ->
                            Toast.makeText(context, "Module install failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

// 1. Simplified print function
// Replace your onPrintClick definition with this:

        val onPrintClick: () -> Unit = {
            val code = scannedCode.value?.trim()?.uppercase()
            when {
                code.isNullOrBlank() -> {
                    Toast.makeText(context, "Scan a username first", Toast.LENGTH_SHORT).show()
                }
                looksLikeMac(code) -> {
                    Toast.makeText(context, "This is a printer MAC. Scan a USERNAME to print.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    scope.launch {
                        try {
                            printerStatus = "Preparing to print..."

                            // 1. Ensure printer is connected
                            if (!printerSvc.isConnected()) {
                                printerStatus = "Connecting to printer..."

                                val printerInfo = try {
                                    RetrofitInstance.api.getPrinterInfo()
                                } catch (e: Exception) {
                                    printerStatus = "Cannot get printer info"
                                    Toast.makeText(context, "Cannot get printer info: ${e.message}", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                if (printerInfo.mac.isNullOrBlank()) {
                                    printerStatus = "No printer configured"
                                    Toast.makeText(context, "No printer MAC configured", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                // Ensure paired first
                                printerStatus = "Pairing with printer..."
                                val paired = printerSvc.ensurePaired(printerInfo.mac)
                                if (!paired) {
                                    printerStatus = "Pairing failed"
                                    Toast.makeText(context, "Failed to pair with printer", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                // Connect
                                printerStatus = "Establishing connection..."
                                val connected = printerSvc.connectBluetooth(printerInfo.mac)
                                if (!connected) {
                                    printerStatus = "Connection failed"
                                    Toast.makeText(context, "Failed to connect to printer", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                printerStatus = "Connected to printer"
                                // Give connection time to stabilize
                                delay(1000)
                            }

                            // 2. Get user info
                            printerStatus = "Loading user data..."
                            val user = try {
                                RetrofitInstance.api.getUserByUsername(code)
                            } catch (e: Exception) {
                                printerStatus = "User not found"
                                Toast.makeText(context, "User not found: $code", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            // 3. Generate and print label
                            printerStatus = "Generating label..."
                            val zpl = ZplFactory.userLabel(user, code)
                            Log.d("PRINT_ZPL", "Generated ZPL:\n$zpl")

                            printerStatus = "Printing label..."
                            val printed = printerSvc.printZpl(zpl)

                            if (printed) {
                                printerStatus = "Print successful"
                                Toast.makeText(context, "Label printed successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                printerStatus = "Print failed"
                                Toast.makeText(context, "Print failed - check printer", Toast.LENGTH_SHORT).show()
                            }

                        } catch (e: Exception) {
                            printerStatus = "Print error: ${e.message}"
                            Log.e("PRINT", "Print error", e)
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        Scaffold(
            bottomBar = {
                if (!editMode) {
                    BottomActionButtons(
                        m = m,
                        onScan = onScanClick,
                        onPrint = onPrintClick,
                        onLogout = onLogout,
                    )
                }
            }
        ) { innerPadding ->
            Box(Modifier.fillMaxSize()) {
                UserInfoBackground(contentPadding = m.outerPadding) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
                        UserInfoHeader(
                            m = m,
                            onSettings = { showPrinterDialog = true } // â† opens dialog
                        )

                        Spacer(Modifier.height(m.spacerHeaderCard))
                        Spacer(Modifier.height(8.dp))

                        // Status + last scan
                        Text(
                            text = "Printer: $printerStatus",
                            color = Color.White,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        Text(
                            text = "Last code: ${scannedCode.value ?: "-"}",
                            color = Color.White,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )

                        Column(
                            Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = if (!editMode) 16.dp else 0.dp)
                        ) {
                            UserInfoCard(
                                m = m,
                                editMode = editMode,
                                onEditModeChange = { editMode = it },
                                onLogout = onLogout
                            )
                        }
                    }
                }
            }
        }

// 3. Simplified printer settings dialog handler
        if (showPrinterDialog) {
            PrinterSettingsDialog(
                onDismiss = { showPrinterDialog = false },
                onSaveMac = { mac ->
                    scope.launch {
                        try {
                            // Save MAC to server
                            RetrofitInstance.api.savePrinterMac(SavePrinterMacDto(mac))

                            // Disconnect from current printer if any
                            printerSvc.disconnect()

                            // Pair with new printer
                            val paired = printerSvc.ensurePaired(mac)
                            if (!paired) {
                                Toast.makeText(context, "Failed to pair with printer", Toast.LENGTH_SHORT).show()
                                showPrinterDialog = false
                                return@launch
                            }

                            // Connect to new printer
                            val connected = printerSvc.connectBluetooth(mac)
                            if (connected) {
                                printerStatus = "Connected to printer"
                                Toast.makeText(context, "Printer connected successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                printerStatus = "Failed to connect"
                                Toast.makeText(context, "Failed to connect to printer", Toast.LENGTH_SHORT).show()
                            }

                            showPrinterDialog = false

                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

    }
}

/* ---------------- helpers already in your project ---------------- */

@Composable
fun UserInfoBackground(contentPadding: Dp, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.info_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.15f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = contentPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) { content() }
    }
}

@Composable
fun UserInfoHeader(
    m: UiMetrics,
    onSettings: () -> Unit
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(Modifier.fillMaxWidth()) {
        // paint status bar area
        Box(
            Modifier
                .fillMaxWidth()
                .height(topInset)
                .background(MyPrimary)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(m.headerHeight)
            .background(
                color = MyPrimary,
                shape = RoundedCornerShape(bottomStart = 150.dp, bottomEnd = 150.dp)
            )
    ) {
        Text(
            text = stringResource(R.string.user_profile),
            fontSize = m.titleSize,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )

        SettingsWheelButton(
            onSettings = onSettings,
            iconSize = if (m.buttonFullWidth) 32.dp else 42.dp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
                .size(48.dp)
        )
    }
}

@Composable
fun UserInfoCard(
    m: UiMetrics,
    editMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<User?>(null) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    var role by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun applyDataToFields(u: User) {
        firstName = u.firstName
        lastName = u.lastName
        username = u.userName
        email = u.email
        isActive = u.isActive
        role = u.role
    }

    fun loadUser() {
        scope.launch {
            val token = Session.token()
            if (token.isNullOrBlank() || Jwt.isExpired(token)) {
                Session.clear()
                onLogout()
                return@launch
            }
            isLoading = true
            error = null
            try {
                val response = RetrofitInstance.api.me()
                user = response
                applyDataToFields(response)
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    Session.clear(); onLogout()
                } else error = "Error ${e.code()}"
            } catch (e: Exception) {
                error = "Load failed ${e.message}"
            } finally { isLoading = false }
        }
    }

    LaunchedEffect(Unit) { loadUser() }

    Card(
        modifier = Modifier
            .alpha(0.9f)
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(m.cardCorner),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(m.cardInnerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
//          Title
            Text(
                text = stringResource(R.string.personal_info),
                fontSize = m.titleSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                lineHeight = 25.sp,
                textAlign = TextAlign.Center,
                color = Color.DarkGray,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(m.spacerHeaderCard))

            if (isLoading) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
            }
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            user?.let { u ->
                if (!editMode) {
                    //VIEW MODE
                    UserInfoRow(label = "Name", value = "${u.firstName} ${u.lastName}")
                    UserInfoRow(label = "Username", value = u.userName)
                    UserInfoRow(label = "Email", value = u.email)
                    UserInfoRow(label = "Role", value = u.role)
                    MemberSinceRow(
                        monthAndDay = u.monthAndDay,
                        year = u.year
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    val base = if (m.buttonFullWidth) Modifier.fillMaxWidth() else Modifier.width(150.dp)

                    PrimaryButton(
                        text = stringResource(R.string.edit_btn),
                        onClick = { onEditModeChange(true) },
                        color = MyPrimary,
                        modifier = base.defaultMinSize(minHeight = m.buttonMinHeight)
                    )
                } else {
                    // EDIT MODE
                    TextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 17.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 17.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 17.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 17.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // SAVE / CANCEL
                    if (m.buttonFullWidth) {
                        // Zebra: vertical, full-width
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PrimaryButton(
                                text = "Save",
                                onClick = {
                                    scope.launch {
                                        isLoading = true; error = null
                                        try {
                                            val dto = UpdateUserDto(
                                                userId = u.userId,
                                                userName = username,
                                                email = email,
                                                firstName = firstName,
                                                lastName = lastName,
                                                isActive = isActive
                                            )
                                            RetrofitInstance.api.editUser(dto)
                                            loadUser()
                                            onEditModeChange(false)
                                        } catch (e: HttpException) {
                                            if (e.code() == 401) onLogout() else error = "Error ${e.code()}"
                                        } catch (e: Exception) {
                                            error = "Update failed ${e.message}"
                                        } finally { isLoading = false }
                                    }
                                },
                                color = MyPrimary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = m.buttonMinHeight)
                            )
                            PrimaryButton(
                                text = "Cancel",
                                onClick = {
                                    user?.let { applyDataToFields(it) }
                                    onEditModeChange(false)
                                },
                                color = Color.Gray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = m.buttonMinHeight)
                            )
                        }
                    } else {
                        // Mobile / Tablets - Horizontal
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            PrimaryButton(
                                text = "Save",
                                onClick = {
                                    scope.launch {
                                        isLoading = true; error = null
                                        try {
                                            val dto = UpdateUserDto(
                                                userId = u.userId,
                                                userName = username,
                                                email = email,
                                                firstName = firstName,
                                                lastName = lastName,
                                                isActive = isActive
                                            )
                                            RetrofitInstance.api.editUser(dto)
                                            loadUser()
                                            onEditModeChange(false)
                                        } catch (e: HttpException) {
                                            if (e.code() == 401) onLogout() else error =
                                                "Error ${e.code()}"
                                        } catch (e: Exception) {
                                            error = "Update failed ${e.message}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                },
                                color = MyPrimary,
                                modifier = Modifier
                                    .width(110.dp)
                                    .defaultMinSize(minHeight = m.buttonMinHeight)
                            )
                            PrimaryButton(
                                text = "Cancel",
                                onClick = {
                                    user?.let { applyDataToFields(it) }
                                    onEditModeChange(false)
                                },
                                color = Color.Gray,
                                modifier = Modifier
                                    .width(110.dp)
                                    .defaultMinSize(minHeight = m.buttonMinHeight)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Handle one scanned string: if it's a MAC, normalize/save/connect; else treat as username. */
private fun handleScannedCode(
    data: String,
    scannedCode: MutableState<String?>,
    saveAndConnect: (normalizedMac: String) -> Unit,
    onUser: (username: String) -> Unit
) {
    val raw = data.trim().uppercase()
    val isMac = raw.matches(Regex("^[0-9A-F]{2}(:[0-9A-F]{2}){5}\$")) || raw.matches(Regex("^[0-9A-F]{12}\$"))
    if (isMac) {
        val normalized = raw.filter(Char::isLetterOrDigit).take(12).chunked(2).joinToString(":")
        scannedCode.value = normalized
        saveAndConnect(normalized)
    } else {
        scannedCode.value = raw
        onUser(raw)
    }
}
