package tech.kaustubhdeshpande.chachacrypt.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import tech.kaustubhdeshpande.chachacrypt.ui.components.*
import tech.kaustubhdeshpande.chachacrypt.viewmodel.EncryptViewModel

@Composable
fun EncryptScreen(
    viewModel: EncryptViewModel,
    onNavigateDecrypt: (() -> Unit)? = null
) {
    val scroll = rememberScrollState()
    val clipboard: ClipboardManager = LocalClipboardManager.current

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(20.dp)
        ) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Encrypt", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                if (onNavigateDecrypt != null) {
                    TextButton(onClick = onNavigateDecrypt) { Text("Go Decrypt") }
                }
            }
            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp)) {

                    LabeledTextArea(
                        label = "Plaintext",
                        value = viewModel.plainText,
                        onChange = { viewModel.plainText = it },
                        minHeight = 100
                    )

                    Text("Key Format", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    ToggleChipsRow(
                        options = listOf("HEX", "TEXT"),
                        selectedIndex = if (viewModel.treatKeyAsHex) 0 else 1
                    ) { viewModel.treatKeyAsHex = (it == 0) }
                    Spacer(Modifier.height(8.dp))

                    LabeledTextField(
                        label = if (viewModel.treatKeyAsHex) "Key (64 hex chars)" else "Key (32 chars)",
                        value = viewModel.keyInput,
                        onChange = { viewModel.keyInput = it.trim() },
                        error = viewModel.validation.keyError
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.loadDefaultKey() }) { Text("Default") }
                        OutlinedButton(onClick = { viewModel.randomizeKey() }) { Text("Random") }
                    }
                    Spacer(Modifier.height(16.dp))

                    Text("Nonce Format", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    ToggleChipsRow(
                        options = listOf("HEX", "TEXT"),
                        selectedIndex = if (viewModel.treatNonceAsHex) 0 else 1
                    ) { viewModel.treatNonceAsHex = (it == 0) }
                    Spacer(Modifier.height(8.dp))

                    LabeledTextField(
                        label = if (viewModel.treatNonceAsHex) "Nonce (24 hex chars)" else "Nonce (12 chars)",
                        value = viewModel.nonceInput,
                        onChange = { viewModel.nonceInput = it.trim() },
                        error = viewModel.validation.nonceError
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.loadDefaultNonce() }) { Text("Default") }
                        OutlinedButton(onClick = { viewModel.randomizeNonce() }) { Text("Random") }
                        OutlinedButton(onClick = { viewModel.randomizeAll() }) { Text("Random All") }
                    }
                    Spacer(Modifier.height(16.dp))

                    LabeledTextField(
                        label = "Counter (blocks)",
                        value = viewModel.counterInput,
                        onChange = { viewModel.counterInput = it.filter { c -> c.isDigit() } },
                        keyboardType = KeyboardType.Number,
                        error = viewModel.validation.counterError
                    )

                    if (viewModel.validation.genericError != null) {
                        Text(
                            viewModel.validation.genericError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Button(
                        onClick = { viewModel.encrypt() },
                        enabled = !viewModel.isProcessing,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        if (viewModel.isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Working...")
                        } else {
                            Text("Encrypt")
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ciphertext Output", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                AssistChip(
                    onClick = { viewModel.showHexOutput = !viewModel.showHexOutput },
                    label = { Text(if (viewModel.showHexOutput) "HEX" else "BASE64") }
                )
            }
            Spacer(Modifier.height(12.dp))

            OutputBlock(
                title = if (viewModel.showHexOutput) "Cipher (HEX)" else "Cipher (Base64)",
                text = if (viewModel.showHexOutput) viewModel.encryptedHex else viewModel.encryptedBase64,
                placeholder = "<No ciphertext yet>",
                onCopy = {
                    val toCopy = if (viewModel.showHexOutput) viewModel.encryptedHex else viewModel.encryptedBase64
                    if (toCopy.isNotEmpty()) clipboard.setText(AnnotatedString(toCopy))
                }
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}