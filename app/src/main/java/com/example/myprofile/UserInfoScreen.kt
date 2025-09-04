package com.example.myprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.example.myprofile.network.RetrofitInstance
import com.example.myprofile.network.auth.Jwt
import com.example.myprofile.network.auth.Session
import com.example.myprofile.ui.components.BottomActionButtons
import com.example.myprofile.ui.components.MemberSinceRow
import com.example.myprofile.ui.components.PrimaryButton
import com.example.myprofile.ui.components.UserInfoRow
import com.example.myprofile.ui.theme.MyPrimary
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.example.myprofile.ui.responsive.WithUiMetrics
import com.example.myprofile.ui.responsive.UiMetrics
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.layout.size
import com.example.myprofile.ui.components.BarcodeScanner
import com.example.myprofile.ui.components.SettingsWheelButton

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
        var editMode by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val barcodeScanner = remember { BarcodeScanner(context) }

        Scaffold(
            bottomBar = {
                if (!editMode) {
                    BottomActionButtons(
                        m = m,
                        onScan = {
                            barcodeScanner.ensureInstalled(
                                onReady = {
                                    barcodeScanner.startScan(
                                        onResult = { code ->
                                            Toast.makeText(context, "Scanned: $code", Toast.LENGTH_SHORT).show()
                                            },
                                        onCancel = {},
                                        onError = { e ->
                                            Toast.makeText(context, "Scan error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                },
                                onError = { e ->
                                    Toast.makeText(context, "Install failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onPrint = { /* TODO */ },
                        onLogout = onLogout,
                    )
                }
            }
        ) { innerPadding ->
            Box(Modifier.fillMaxSize()) {
                UserInfoBackground(contentPadding = m.outerPadding) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        UserInfoHeader(
                            m = m,
                            onSettings = { }
                        )
                        Spacer(Modifier.height(m.spacerHeaderCard))

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
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserInfoBackground(contentPadding: Dp, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
        ) {
            content()
        }
    }
}

@Composable
fun UserInfoHeader(
    m: UiMetrics,
    onSettings: () -> Unit
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(Modifier.fillMaxWidth()) {
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

    fun applyDataToFields(user: User) {
        firstName = user.firstName
        lastName = user.lastName
        username = user.userName
        email = user.email
        isActive = user.isActive
        role = user.role
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
                    Session.clear()
                    onLogout()
                } else {
                    error = "Error ${e.code()}"
                }
            } catch (e: Exception) {
                error = "Load failed ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadUser()
    }

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