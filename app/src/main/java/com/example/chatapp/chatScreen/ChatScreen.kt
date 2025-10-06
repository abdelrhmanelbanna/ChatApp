package com.example.chatapp.chatScreen

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chatapp.chatScreen.components.ChatMessageItem
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel(), modifier: Modifier = Modifier ) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // image picker
    val getImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        viewModel.process(ChatIntent.ImagesSelected(uris.map { it.toString() }))
    }

    // permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            getImagesLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Permission needed to pick images", Toast.LENGTH_SHORT).show()
        }
    }

    // effects listener
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is ChatEffect.OpenImagePicker -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    } else {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
                is ChatEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Chat Room") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // messages list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFF2F2F2)),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(state.messages) { msg ->
                    ChatMessageItem(
                        message = msg,
                        isMine = msg.userId == state.currentUserId,
                        onRetry = { viewModel.process(ChatIntent.RetryMessage(msg.id)) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // preview selected images
            if (state.selectedMediaUris.isNotEmpty()) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("${state.selectedMediaUris.size} صورة مختارة")
                }
            }

            // composer (متعرف تحت)
            ComposerArea(
                composingText = state.composingText,
                onTextChange = { viewModel.process(ChatIntent.UpdateComposingText(it)) },
                onPickImages = { viewModel.process(ChatIntent.PickImagesClicked) },
                onSend = { viewModel.process(ChatIntent.SendText(it)) }
            )
        }
    }
}

@Composable
private fun ComposerArea(
    composingText: String,
    onTextChange: (String) -> Unit,
    onPickImages: () -> Unit,
    onSend: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPickImages) {
            Icon(Icons.Filled.Image, contentDescription = "Pick images")
        }

        OutlinedTextField(
            value = composingText,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message...") },
            singleLine = true
        )

        IconButton(onClick = { onSend(composingText) }) {
            Icon(Icons.Filled.Send, contentDescription = "Send")
        }
    }
}
