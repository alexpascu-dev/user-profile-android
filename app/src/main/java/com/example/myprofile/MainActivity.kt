package com.example.myprofile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myprofile.network.RetrofitInstance
import com.example.myprofile.network.auth.Session
import com.example.myprofile.ui.navigation.AppNavHost
import com.example.myprofile.ui.theme.MyProfileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Session.init(applicationContext)
        RetrofitInstance.init(applicationContext)

        setContent {
            MyProfileTheme {
                AppNavHost()
            }
        }
    }
}
