package com.example.chatapp.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.example.chatapp.R
import com.example.chatapp.chatScreen.ChatActivity
import com.example.chatapp.core.datastore.DataStoreManager
import com.example.chatapp.profileScreen.ProfileActivity
import com.example.chatapp.splash.ui.theme.ChatAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            com.example.chatapp.profileScreen.ui.theme.ChatAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Splash SCREEN",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        val dataStore = DataStoreManager(this)

        lifecycleScope.launch {
            delay(2000)

            val userId = dataStore.getUserId().first()
            if (userId != null) {
                startActivity(Intent(this@SplashActivity, ChatActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, ProfileActivity::class.java))
            }
            finish()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {

    ConstraintLayout(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
    ){
        val (icon, title , author) = createRefs()

        Image(
            painter = painterResource(id = R.drawable.ic_app),
            contentDescription = "icon",
            modifier = Modifier
                .fillMaxWidth(0.25f)
                .constrainAs(icon){
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
        )

        Text(
            text = "ChatApp",
            color = Color(0xFF1D74C4),
            fontSize = 25.sp,
            modifier = Modifier.
            padding(16.dp).
            constrainAs(title){
                top.linkTo(icon.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        Text(
            text = "developed by Abdelrahman Elbanna",
            color = Color(0xFF1D74C4),
            fontSize = 16.sp,
            modifier = Modifier.
            padding(vertical = 22.dp).
            constrainAs(author){
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)

            }
        )
    }
}

@Preview(showBackground = true,
    showSystemUi = true)
@Composable
fun GreetingPreview() {
    com.example.chatapp.chatScreen.ui.theme.ChatAppTheme {
        Greeting("Android")
    }
}