package com.example.mazeball

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mazeball.data.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun MainMenuScreen(
    navController: NavController,
    userPreferencesRepository: UserPreferencesRepository
) {
    val titleColor = Color(0xFFF1F2F6)
    val titleBackgroundColor = Color(0xFF002245)
    val buttonBackgroundColor = Color(0xFF048A81)
    
    val coroutineScope = rememberCoroutineScope()
    var showIpInput by remember { mutableStateOf(false) }
    var ipAddress by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        val serverUrl = userPreferencesRepository.userPreferencesFlow.first().serverUrl
        showIpInput = serverUrl.isEmpty()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.maze_background),
            contentDescription = "Maze Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(titleBackgroundColor)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Maze Ball",
                    style = TextStyle(fontSize = 72.sp, fontWeight = FontWeight.ExtraBold),
                    color = titleColor,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(70.dp))

            if (showIpInput) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text("IP адрес сервера", color = titleColor) },
                        placeholder = { Text("например: 192.168.1.100", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = titleColor,
                            unfocusedTextColor = titleColor,
                            focusedLabelColor = titleColor,
                            unfocusedLabelColor = titleColor,
                            focusedBorderColor = buttonBackgroundColor,
                            unfocusedBorderColor = titleColor
                        )
                    )
                    Button(
                        onClick = {
                            if (ipAddress.isNotBlank()) {
                                val serverUrl = if (ipAddress.startsWith("http://") || ipAddress.startsWith("https://")) {
                                    ipAddress
                                } else {
                                    "http://$ipAddress:8080"
                                }
                                coroutineScope.launch {
                                    userPreferencesRepository.updateServerUrl(serverUrl)
                                    showIpInput = false
                                }
                            }
                        },
                        modifier = Modifier.size(width = 200.dp, height = 50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonBackgroundColor),
                        enabled = ipAddress.isNotBlank()
                    ) {
                        Text("ОК", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = titleColor)
                    }
                }
            } else {
                val buttonTextStyle = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold)
                val buttonModifier = Modifier.size(width = 300.dp, height = 75.dp)
                val buttonColors = ButtonDefaults.buttonColors(containerColor = buttonBackgroundColor)

            Button(
                onClick = { navController.navigate("level_selection") },
                modifier = buttonModifier,
                colors = buttonColors
            ) {
                Text(
                    text = "Играть",
                    style = buttonTextStyle,
                    color = titleColor
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { navController.navigate("leaderboard") },
                modifier = buttonModifier,
                colors = buttonColors
            ) {
                Text(
                    text = "Списки лидеров",
                    style = buttonTextStyle,
                    color = titleColor
                )
            }
            }
        }
    }
}
