package com.minercontrol.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minercontrol.app.model.SettingsRepository
import com.minercontrol.app.network.NetworkScanner
import com.minercontrol.app.network.UdpListener
import com.minercontrol.app.utils.AppStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    settingsRepository: SettingsRepository,
    udpListener: UdpListener? = null
) {
    val context = LocalContext.current
    val networkScanner = remember { NetworkScanner(context) }
    val scope = rememberCoroutineScope()

    val currentLanguage by settingsRepository.getLanguageFlow().collectAsState(initial = "pt")
    val appName by settingsRepository.getAppNameFlow().collectAsState(initial = "MinerControl Dashboard")
    val udpPort by settingsRepository.getDiscoveryPortFlow().collectAsState(initial = 12345)
    val strings = if (currentLanguage == "en") AppStrings.English else AppStrings.Portuguese
    
    var appNameInput by remember { mutableStateOf(appName) }
    var udpPortInput by remember { mutableStateOf(udpPort.toString()) }

    // Update input fields when settings change
    LaunchedEffect(appName) { appNameInput = appName }
    LaunchedEffect(udpPort) { udpPortInput = udpPort.toString() }
    
    // Only use the currently connected network
    val currentNetwork by networkScanner.currentNetwork.collectAsState()

    LaunchedEffect(Unit) { networkScanner.detectNetworks() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        strings.settingsTitle,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1a1a1a),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0f0f0f)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Rede atual
            item {
                currentNetwork?.let { network ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1e3a1e)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = strings.currentNetwork,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "SSID: ${network.ssid}", color = Color.White)
                            Text(text = "${strings.ip}: ${network.ip}", color = Color.White)
                            Text(text = "${strings.subnet}: ${network.subnet}", color = Color.White)
                        }
                    }
                }
            }

            // Configurações da App
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1a1a1a)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = strings.appSettings,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // Campo nome da app
                        OutlinedTextField(
                            value = appNameInput,
                            onValueChange = { appNameInput = it },
                            label = { Text(strings.appName, color = Color(0xFF888888)) },
                            placeholder = { Text(strings.appNamePlaceholder, color = Color(0xFF666666)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color(0xFF555555)
                            )
                        )

                        // Seletor de idioma
                        Text(
                            text = strings.language,
                            fontSize = 14.sp,
                            color = Color(0xFF888888)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { 
                                    scope.launch {
                                        settingsRepository.setLanguage("pt")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentLanguage == "pt") 
                                        Color(0xFF4CAF50) else Color(0xFF555555)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(strings.portuguese, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { 
                                    scope.launch {
                                        settingsRepository.setLanguage("en")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentLanguage == "en") 
                                        Color(0xFF4CAF50) else Color(0xFF555555)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(strings.english, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Configurações de Rede
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1a1a1a)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = strings.networkSettings,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        OutlinedTextField(
                            value = udpPortInput,
                            onValueChange = { udpPortInput = it },
                            label = { Text(strings.udpPort, color = Color(0xFF888888)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color(0xFF555555)
                            )
                        )

                        // Nota sobre auto-descoberta
                        Text(
                            text = if (currentLanguage == "en") 
                                "Miners are automatically discovered on the current network" 
                            else 
                                "Os miners são descobertos automaticamente na rede atual",
                            fontSize = 12.sp,
                            color = Color(0xFF888888)
                        )
                    }
                }
            }

            // Informações da App
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1a1a1a)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = strings.aboutApp,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (currentLanguage == "en") "MinerControl Android" else "MinerControl Android", 
                            color = Color(0xFF888888)
                        )
                        Text(text = strings.version, color = Color(0xFF888888))
                        Text(
                            text = "${strings.monitoring} $udpPort)", 
                            color = Color(0xFF888888)
                        )
                    }
                }
            }

            // Botão de salvar
            item {
                Button(
                    onClick = {
                        scope.launch {
                            // Save settings
                            if (appNameInput.isNotBlank()) {
                                settingsRepository.setAppName(appNameInput)
                            }
                            val port = udpPortInput.toIntOrNull()
                            if (port != null && port in 1024..65535) {
                                settingsRepository.setDiscoveryPort(port)
                                udpListener?.start(port)
                            }
                        }
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = strings.saveSettings,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
