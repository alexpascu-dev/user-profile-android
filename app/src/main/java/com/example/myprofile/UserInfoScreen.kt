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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.navigation.compose.rememberNavController
import com.example.myprofile.network.auth.Jwt
import com.example.myprofile.network.auth.Session
import com.example.myprofile.models.UpdateUserDto
import com.example.myprofile.models.User
import com.example.myprofile.ui.components.MemberSinceRow
import com.example.myprofile.ui.components.PrimaryButton
import com.example.myprofile.ui.components.UserInfoRow
import com.example.myprofile.ui.theme.MyPrimary
import com.example.myprofile.ui.theme.MySecondary
import androidx.navigation.NavHostController
import com.example.myprofile.ui.theme.UserInfoBackground

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

//fun saveChanges(
//    dto: UpdateUserDto,
//    onSuccess: () -> Unit,
//    onError: (Throwable) -> Unit
//) {
//    RetrofitInstance.apiService.editUser(dto)
//        .enqueue(object : Callback<Void> {
//            override fun onResponse(call: Call<Void>, response: Response<Void>) {
//                if (response.isSuccessful) {
//                    onSuccess()
//                }
//                else {
//                    onError(Exception("Error code ${response.code()}"))
//                }
//            }
//            override fun onFailure(call: Call<Void>, throwable: Throwable) {
//                onError(throwable)
//            }
//        })
//}

@Composable
fun UserInfoCard(
    onLogout: () -> Unit
) {
    val userState = remember { mutableStateOf<User?>(null) }
    val editMode = remember { mutableStateOf(false) }

    val firstNameState = remember { mutableStateOf("") }
    val lastNameState = remember { mutableStateOf("") }
    val usernameState = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val isActiveState = remember { mutableStateOf(true) }
    val roleState = remember { mutableStateOf("") }

//    fun loadUser() {
//        RetrofitInstance.apiService.getUserById(userId)
//            .enqueue(object : Callback<User> {
//            override fun onResponse(call: Call<User>, response: Response<User>) {
//                response.body()?.let { user ->
//                    userState.value = user
//                    firstNameState.value = user.firstName
//                    lastNameState.value = user.lastName
//                    usernameState.value = user.userName
//                    emailState.value = user.email
//                    isActiveState.value = true
//                }
//            }
//            override fun onFailure(call: Call<User>, throwable: Throwable) {
//                Log.e("API","GetUser Error: ${throwable.message}")
//            }
//        })
//    }

    LaunchedEffect(Unit) {
//        loadUser()
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

            userState.value?.let { user ->
                if (!editMode.value) {
                    //VIEW MODE
                    UserInfoRow(label = "Name", value = user.name)
                    UserInfoRow(label = "Username", value = user.userName)
                    UserInfoRow(label = "Email", value = user.email)
                    MemberSinceRow(
                        monthAndDay = user.monthAndDay,
                        year = user.year
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    PrimaryButton(
                        text = stringResource(R.string.edit_btn),
                        onClick = { editMode.value = true },
                        color = MyPrimary,
                        modifier = Modifier
                            .height(55.dp)
                            .width(150.dp)
                    )
                } else {
                    // EDIT MODE
                    TextField(
                        value = firstNameState.value,
                        onValueChange = { firstNameState.value = it },
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
                        value = lastNameState.value,
                        onValueChange = { lastNameState.value = it },
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
                        value = usernameState.value,
                        onValueChange = { usernameState.value = it },
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
                        value = emailState.value,
                        onValueChange = { emailState.value = it },
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
                                val dto = UpdateUserDto(
                                    userId = user.userId,
                                    userName = usernameState.value,
                                    email = emailState.value,
                                    firstName = firstNameState.value,
                                    lastName = lastNameState.value,
                                    isActive = isActiveState.value
                                )
//                                saveChanges(
//                                    dto,
//                                    onSuccess = {
//                                        loadUser()
//                                        editMode.value = false
//                                    },
//                                    onError = { e ->
//                                        Log.e("Update", e.message ?: "")
//                                    }
//                                )
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
                                userState.value?.let { user ->
                                    firstNameState.value = user.firstName
                                    lastNameState.value = user.lastName
                                    usernameState.value = user.userName
                                    emailState.value = user.email
                                }
                                editMode.value = false
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
