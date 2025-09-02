package com.example.myprofile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myprofile.network.RetrofitInstance
import com.example.myprofile.network.auth.Session
import com.example.myprofile.models.LoginDto
import com.example.myprofile.ui.components.PrimaryButton
import com.example.myprofile.ui.theme.MyPrimary
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.example.myprofile.ui.navigation.Screen
import com.example.myprofile.ui.navigation.navigateAndClear

@Preview(showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}

fun isValidUsername(username: String): Boolean {
    return username.length >= 3 && username.matches(Regex("^[a-zA-Z0-9_]+$"))
}

@Composable
fun LoginScreen(navController: NavHostController) {
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LoginBackground {
        Column (
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoginHeader()
            Spacer(modifier = Modifier.height(100.dp))
            LoginCard(
                username = username,
                password = password,
                onUsernameChange = { value ->
                    username = value
                    usernameError = false
                    loginError = null
                                   },
                onPasswordChange = { value ->
                    password = value
                    passwordError = false
                    loginError = null
                                   },
                onLoginClick = onLoginClick@{
                    val userName = username.trim()
                    val pwd = password
                    val isUsernameValid = isValidUsername(userName)
                    val isPasswordValid = pwd.isNotBlank()

                    usernameError = !isUsernameValid
                    passwordError = !isPasswordValid
                    if (!isUsernameValid || !isPasswordValid) return@onLoginClick

                    isLoading = true
                    scope.launch {
                        try {
                            Log.d("LOGIN", "Submitting login for $userName")
                            val token = RetrofitInstance.api.loginUser(
                                LoginDto(
                                    userName,
                                    pwd
                                )
                            ).token
                            Log.d("LOGIN", "Login OK, token len=${token.length}")
                            Session.saveToken(token)

//                            RetrofitInstance.api.me()

                            navController.navigateAndClear(Screen.UserInfo.route)

                        }
                        catch (e: HttpException) {
                            Log.e("LOGIN", "HttpException code=${e.code()} body=${e.response()?.errorBody()?.string()}")
                            loginError = "HTTP ${e.code()}"
                            Session.clear()
                        }
                        catch (e: Exception) {
                            Log.e("LOGIN", "Unexpected", e)
                            loginError = "Unexpected: ${e.message}"
                            Session.clear()
                        }

                        finally {
                            isLoading = false
                        }
                    }
                },
                usernameError = usernameError,
                passwordError = passwordError
            )
        }
    }
}

//        Login Background

@Composable
fun LoginBackground(content: @Composable () -> Unit) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_login),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

//           Login Header

@Composable
fun LoginHeader() {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.account_image),
            contentDescription = "Account icon",
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(id = R.string.myApp),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = Color.White
        )
    }
}

//                Login Card

@Composable
fun LoginCard(
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    usernameError: Boolean,
    passwordError: Boolean
) {

    Card(
        modifier = Modifier
            .alpha(0.9f)
            .padding(start = 26.dp, end = 26.dp, bottom = 70.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(35.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            verticalArrangement = Arrangement.Center
        )
        {
//                Welcome text
            Text(
                text = "Welcome",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = MyPrimary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
//                Info text
            Text(
                text = stringResource(id = R.string.information_login),
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
//                Username label
            Text(
                text = stringResource(R.string.username),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color.Gray,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .alpha(0.7f)
            )
//                Username input
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text(stringResource(R.string.enter_username)) },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.account_image),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
            if (usernameError) {
                Text(
                    text = "Username invalid",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

//                Password label
            Text(
                text = stringResource(R.string.password),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color.Gray,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .alpha(0.7f)
            )
//                Password input
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.enter_password)) },
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.password_icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
            if (passwordError) {
                Text(
                    text = "Password invalid",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.padding(4.dp)
                )
            }

            PrimaryButton(
                text = stringResource(R.string.login_btn),
                onClick = onLoginClick,
                modifier = Modifier
                    .padding(18.dp)
                    .width(180.dp)
                    .height(60.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}