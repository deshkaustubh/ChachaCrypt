package tech.kaustubhdeshpande.chachacrypt.viewmodel

import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import tech.kaustubhdeshpande.chachacrypt.model.ChaCha20Cipher
import tech.kaustubhdeshpande.chachacrypt.util.hexToBytes
import tech.kaustubhdeshpande.chachacrypt.util.toHex

data class EncryptValidation(
    val keyError: String? = null,
    val nonceError: String? = null,
    val counterError: String? = null,
    val genericError: String? = null
)

class EncryptViewModel : ViewModel() {

    // Defaults (deterministic samples) for "Default" buttons
    private val defaultKeyHex =
        "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f"
    private val defaultNonceHex = "000000090000004a00000000"

    var plainText by mutableStateOf("")
    var keyInput by mutableStateOf("")          // pre-filled (random hex by default)
    var nonceInput by mutableStateOf("")
    var counterInput by mutableStateOf("0")

    var encryptedBase64 by mutableStateOf("")
    var encryptedHex by mutableStateOf("")

    var treatKeyAsHex by mutableStateOf(true)
    var treatNonceAsHex by mutableStateOf(true)
    var showHexOutput by mutableStateOf(false)

    var validation by mutableStateOf(EncryptValidation())
    var isProcessing by mutableStateOf(false)

    init {
        randomizeAll()
    }

    fun loadDefaultKey() { keyInput = defaultKeyHex }
    fun loadDefaultNonce() { nonceInput = defaultNonceHex }

    fun randomizeKey() { keyInput = ChaCha20Cipher.randomKey().toHex() }
    fun randomizeNonce() { nonceInput = ChaCha20Cipher.randomNonce().toHex() }
    fun randomizeAll() {
        randomizeKey()
        randomizeNonce()
        counterInput = "0"
    }

    private fun parseKey(): ByteArray? = try {
        if (treatKeyAsHex) keyInput.hexToBytes() else keyInput.encodeToByteArray()
    } catch (_: Exception) { null }

    private fun parseNonce(): ByteArray? = try {
        if (treatNonceAsHex) nonceInput.hexToBytes() else nonceInput.encodeToByteArray()
    } catch (_: Exception) { null }

    fun encrypt() {
        validation = EncryptValidation()
        encryptedBase64 = ""
        encryptedHex = ""

        val keyBytes = parseKey()
        val nonceBytes = parseNonce()
        val counter = counterInput.toIntOrNull()

        var keyErr: String? = null
        var nonceErr: String? = null
        var counterErr: String? = null

        if (keyBytes == null || keyBytes.size != 32) keyErr =
            "Key must be 32 bytes (${if (treatKeyAsHex) "64 hex chars" else "32 characters"})"
        if (nonceBytes == null || nonceBytes.size != 12) nonceErr =
            "Nonce must be 12 bytes (${if (treatNonceAsHex) "24 hex chars" else "12 characters"})"
        if (counter == null || counter < 0) counterErr = "Counter must be non-negative integer"

        if (keyErr != null || nonceErr != null || counterErr != null) {
            validation = EncryptValidation(keyErr, nonceErr, counterErr, null)
            return
        }

        try {
            isProcessing = true
            val cipher = ChaCha20Cipher.encrypt(
                plainText.encodeToByteArray(),
                keyBytes!!,
                nonceBytes!!,
                counter!!
            )
            encryptedBase64 = Base64.encodeToString(cipher, Base64.NO_WRAP)
            encryptedHex = cipher.toHex()
        } catch (e: Exception) {
            validation = EncryptValidation(genericError = "Encryption failed: ${e.message}")
        } finally {
            isProcessing = false
        }
    }
}