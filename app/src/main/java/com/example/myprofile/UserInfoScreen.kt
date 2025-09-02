package com.example.myprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myprofile.models.UpdateUserDto
import com.example.myprofile.models.User
import com.example.myprofile.network.RetrofitInstance
import com.example.myprofile.network.auth.Jwt
import com.example.myprofile.network.auth.Session
import com.example.myprofile.ui.components.MemberSinceRow
import com.example.myprofile.ui.components.PrimaryButton
import com.example.myprofile.ui.components.UserInfoRow
import com.example.myprofile.ui.theme.MyPrimary
import com.example.myprofile.ui.theme.MySecondary
import kotlinx.coroutines.launch
import retrofit2.HttpException

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
    Box(modifier = Modifier.fillMaxSize()) {
        UserInfoBackground {
            Column(modifier = Modifier.fillMaxSize()) {
                UserInfoHeader()
                Spacer(modifier = Modifier.height(100.dp))
                UserInfoCard(onLogout = onLogout)
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            PrimaryButton(
                text = stringResource(R.string.print_btn),
                onClick = {},
                color = MySecondary,
                modifier = Modifier
                    .height(60.dp)
                    .width(150.dp)
            )

            PrimaryButton(
                text = stringResource(R.string.logout_btn),
                onClick = onLogout,
                color = Color.Red,
                modifier = Modifier
                    .height(60.dp)
                    .width(150.dp)
            )
        }
    }
}

@Composable
fun UserInfoBackground(content: @Composable () -> Unit) {
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
                .padding(start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

@Composable
fun UserInfoHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                color = MyPrimary,
                shape = RoundedCornerShape(bottomStart = 300.dp, bottomEnd = 300.dp)
            )
    ) {
        Text(
            text = stringResource(R.string.user_profile),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun UserInfoCard(
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var user by remember { mutableStateOf<User?>(null) }
    var editMode by remember { mutableStateOf(false) }

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
            .padding(start = 26.dp, end = 26.dp, bottom = 70.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(35.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
//          Title
            Text(
                text = stringResource(R.string.personal_info),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                lineHeight = 25.sp,
                textAlign = TextAlign.Center,
                color = Color.DarkGray,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

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

                    Spacer(modifier = Modifier.height(30.dp))

                    PrimaryButton(
                        text = stringResource(R.string.edit_btn),
                        onClick = { editMode = true },
                        color = MyPrimary,
                        modifier = Modifier
                            .height(55.dp)
                            .width(150.dp)
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

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        //SAVE BUTTON
                        PrimaryButton(
                            text = "Save",
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    error = null
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
                                        editMode = false
                                    } catch (e: HttpException) {
                                        if (e.code() == 401) onLogout()
                                        else error = "Error ${e.code()}"
                                    } catch (e: Exception) {
                                        error = "Update failed ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            color = MyPrimary,
                            modifier = Modifier
                                .height(55.dp)
                                .width(110.dp)
                        )
                        // CANCEL
                        PrimaryButton(
                            text = "Cancel",
                            onClick = {
                                user?.let { applyDataToFields(it) }
                                editMode = false
                            },
                            color = Color.Gray,
                            modifier = Modifier.size(110.dp, 55.dp)
                        )
                    }
                }
            }
        }
    }
}
