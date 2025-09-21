package tech.kaustubhdeshpande.chachacrypt.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import tech.kaustubhdeshpande.chachacrypt.ui.components.*
import tech.kaustubhdeshpande.chachacrypt.viewmodel.DecryptViewModel

@Composable
fun DecryptScreen(
    viewModel: DecryptViewModel,
    onNavigateEncrypt: (() -> Unit)? = null
) {
    val scroll = rememberScrollState()

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
                Text("Decrypt", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                if (onNavigateEncrypt != null) {
                    TextButton(onClick = onNavigateEncrypt) { Text("Go Encrypt") }
                }
            }
            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(18.dp)) {

                    Text("Cipher Encoding", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    ToggleChipsRow(
                        options = listOf("BASE64", "HEX"),
                        selectedIndex = if (viewModel.cipherIsHex) 1 else 0
                    ) { viewModel.cipherIsHex = (it == 1) }
                    Spacer(Modifier.height(8.dp))

                    LabeledTextArea(
                        label = if (viewModel.cipherIsHex) "Ciphertext (HEX)" else "Ciphertext (Base64)",
                        value = viewModel.cipherInput,
                        onChange = { viewModel.cipherInput = it.trim() },
                        error = viewModel.validation.cipherError
                    )

                    Text("Key Format", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    ToggleChipsRow(
                        options = listOf("HEX", "TEXT"),
                        selectedIndex = if (viewModel.treatKeyAsHex) 0 else 1
                    ) { viewModel.treatKeyAsHex = (it == 0) }
                    Spacer(Modifier.height(8.dp))

                    LabeledTextField(
                        label = "Key",
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
                        label = "Nonce",
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
                        label = "Counter",
                        value = viewModel.counterInput,
                        onChange = { viewModel.counterInput = it.filter { c -> c.isDigit() } },
                        keyboardType = KeyboardType.Number,
                        error = viewModel.validation.counterError
                    )

                    if (viewModel.validation.genericError != null) {
                        Text(viewModel.validation.genericError!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                    }

                    Button(
                        onClick = { viewModel.decrypt() },
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
                            Text("Decrypt")
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            OutputBlock(
                title = "Plaintext",
                text = viewModel.decryptedText,
                placeholder = "<No decrypted text>"
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}