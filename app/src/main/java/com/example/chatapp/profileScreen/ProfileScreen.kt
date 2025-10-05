package com.example.chatapp.profileScreen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onUserSaved: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.handleIntent(ProfileIntent.PickImage(it.toString())) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally // center all content
    ) {
        TextField(
            value = state.username,
            onValueChange = { viewModel.handleIntent(ProfileIntent.EnterUsername(it)) },
            label = { Text("Enter username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Pick Image button centered
        Button(
            onClick = { launcher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D74C4))
        ) {
            Text("Pick Image", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        state.imageUri?.let {
            AsyncImage(
                model = it,
                contentDescription = "Profile Image",
                modifier = Modifier.size(100.dp)
            )
        }

        // Start Chat button
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.handleIntent(ProfileIntent.SaveUser) },
            enabled = state.username.isNotBlank() && state.imageUri != null && !state.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D74C4)),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(48.dp)
        ) {
            Text("Start Chat", color = Color.White)
        }

        // Show error as toast
        if (state.error != null) {
            Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
        }

        // Navigate when saved
        if (state.isSaved) {
            onUserSaved()
        }
    }
}