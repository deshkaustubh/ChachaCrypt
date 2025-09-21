package tech.kaustubhdeshpande.chachacrypt.viewmodel

import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import tech.kaustubhdeshpande.chachacrypt.model.ChaCha20Cipher
import tech.kaustubhdeshpande.chachacrypt.util.hexToBytes
import tech.kaustubhdeshpande.chachacrypt.util.toHex

data class DecryptValidation(
    val cipherError: String? = null,
    val keyError: String? = null,
    val nonceError: String? = null,
    val counterError: String? = null,
    val genericError: String? = null
)

class DecryptViewModel : ViewModel() {

    private val defaultKeyHex =
        "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f"
    private val defaultNonceHex = "000000090000004a00000000"

    var cipherInput by mutableStateOf("")
    var cipherIsHex by mutableStateOf(false)

    var keyInput by mutableStateOf("")
    var nonceInput by mutableStateOf("")
    var counterInput by mutableStateOf("0")

    var treatKeyAsHex by mutableStateOf(true)
    var treatNonceAsHex by mutableStateOf(true)

    var decryptedText by mutableStateOf("")
    var validation by mutableStateOf(DecryptValidation())
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

    private fun parseCipher(): ByteArray? = try {
        if (cipherIsHex) cipherInput.hexToBytes()
        else Base64.decode(cipherInput, Base64.NO_WRAP)
    } catch (_: Exception) { null }

    fun decrypt() {
        validation = DecryptValidation()
        decryptedText = ""

        val cipherBytes = parseCipher()
        val keyBytes = parseKey()
        val nonceBytes = parseNonce()
        val counter = counterInput.toIntOrNull()

        var cipherErr: String? = null
        var keyErr: String? = null
        var nonceErr: String? = null
        var counterErr: String? = null

        if (cipherBytes == null) cipherErr = "Invalid ciphertext (${if (cipherIsHex) "HEX" else "Base64"})"
        if (keyBytes == null || keyBytes.size != 32) keyErr = "Invalid key"
        if (nonceBytes == null || nonceBytes.size != 12) nonceErr = "Invalid nonce"
        if (counter == null || counter < 0) counterErr = "Bad counter"

        if (cipherErr != null || keyErr != null || nonceErr != null || counterErr != null) {
            validation = DecryptValidation(cipherErr, keyErr, nonceErr, counterErr, null)
            return
        }

        try {
            isProcessing = true
            val plain = ChaCha20Cipher.decrypt(cipherBytes!!, keyBytes!!, nonceBytes!!, counter!!)
            decryptedText = plain.decodeToString()
        } catch (e: Exception) {
            validation = DecryptValidation(genericError = "Decryption failed: ${e.message}")
        } finally {
            isProcessing = false
        }
    }
}