package tech.kaustubhdeshpande.chachacrypt.model

import java.security.SecureRandom
import kotlin.experimental.xor

/**
 * Pure Kotlin ChaCha20 (IETF 12-byte nonce variant) per RFC 8439.
 * NOTE: This is NOT authenticated encryption. For production-grade secure messaging
 * you normally need ChaCha20-Poly1305 (adds authentication).
 */
object ChaCha20Cipher {

    private val secureRandom = SecureRandom()

    private val SIGMA = intArrayOf(
        0x61707865, 0x3320646e, 0x79622d32, 0x6b206574
    )

    fun randomKey(): ByteArray = ByteArray(32).also { secureRandom.nextBytes(it) }
    fun randomNonce(): ByteArray = ByteArray(12).also { secureRandom.nextBytes(it) }

    fun encrypt(input: ByteArray, key: ByteArray, nonce: ByteArray, initialCounter: Int): ByteArray {
        require(key.size == 32) { "Key must be 32 bytes" }
        require(nonce.size == 12) { "Nonce must be 12 bytes" }
        require(initialCounter >= 0) { "Counter must be >= 0" }

        val output = ByteArray(input.size)
        var counter = initialCounter
        var offset = 0
        var remaining = input.size

        while (remaining > 0) {
            val block = chacha20Block(key, counter, nonce)
            counter = (counter + 1) and 0xFFFFFFFF.toInt()
            if (counter == 0) error("Counter wrapped (message too long)")
            val blockSize = minOf(64, remaining)
            for (i in 0 until blockSize) {
                output[offset + i] = input[offset + i] xor block[i]
            }
            offset += blockSize
            remaining -= blockSize
        }
        return output
    }

    fun decrypt(input: ByteArray, key: ByteArray, nonce: ByteArray, initialCounter: Int): ByteArray =
        encrypt(input, key, nonce, initialCounter)

    private fun chacha20Block(key: ByteArray, counter: Int, nonce: ByteArray): ByteArray {
        val state = IntArray(16)
        // Constants
        state[0] = SIGMA[0]; state[1] = SIGMA[1]; state[2] = SIGMA[2]; state[3] = SIGMA[3]
        // Key
        for (i in 0 until 8) state[4 + i] = littleEndianToInt(key, i * 4)
        // Counter
        state[12] = counter
        // Nonce
        state[13] = littleEndianToInt(nonce, 0)
        state[14] = littleEndianToInt(nonce, 4)
        state[15] = littleEndianToInt(nonce, 8)

        val working = state.copyOf()
        repeat(10) {
            // Column rounds
            quarterRound(working, 0, 4, 8, 12)
            quarterRound(working, 1, 5, 9, 13)
            quarterRound(working, 2, 6, 10, 14)
            quarterRound(working, 3, 7, 11, 15)
            // Diagonal rounds
            quarterRound(working, 0, 5, 10, 15)
            quarterRound(working, 1, 6, 11, 12)
            quarterRound(working, 2, 7, 8, 13)
            quarterRound(working, 3, 4, 9, 14)
        }

        val out = ByteArray(64)
        for (i in 0 until 16) {
            val sum = working[i] + state[i]
            intToLittleEndian(sum, out, i * 4)
        }
        return out
    }

    private fun quarterRound(s: IntArray, a: Int, b: Int, c: Int, d: Int) {
        var A = s[a]; var B = s[b]; var C = s[c]; var D = s[d]
        A += B; D = D xor A; D = D.rotateLeft(16)
        C += D; B = B xor C; B = B.rotateLeft(12)
        A += B; D = D xor A; D = D.rotateLeft(8)
        C += D; B = B xor C; B = B.rotateLeft(7)
        s[a] = A; s[b] = B; s[c] = C; s[d] = D
    }

    private fun littleEndianToInt(buf: ByteArray, off: Int): Int =
        (buf[off].toInt() and 0xFF) or
                ((buf[off + 1].toInt() and 0xFF) shl 8) or
                ((buf[off + 2].toInt() and 0xFF) shl 16) or
                ((buf[off + 3].toInt() and 0xFF) shl 24)

    private fun intToLittleEndian(value: Int, out: ByteArray, off: Int) {
        out[off] = (value and 0xFF).toByte()
        out[off + 1] = ((value ushr 8) and 0xFF).toByte()
        out[off + 2] = ((value ushr 16) and 0xFF).toByte()
        out[off + 3] = ((value ushr 24) and 0xFF).toByte()
    }

    private fun Int.rotateLeft(bits: Int): Int = (this shl bits) or (this ushr (32 - bits))

    fun selfTest(): Boolean {
        val key = byteArrayOf(
            0,1,2,3,4,5,6,7,
            8,9,10,11,12,13,14,15,
            16,17,18,19,20,21,22,23,
            24,25,26,27,28,29,30,31
        )
        val nonce = byteArrayOf(
            0,0,0,9,0,0,0,74,0,0,0,0
        )
        val counter = 1
        val expected = intArrayOf(
            0x10,0xf1,0xe7,0xe4,0xd1,0x3b,0x59,0x15,
            0x50,0x0f,0xdd,0x1f,0xa3,0x20,0x71,0xc4,
            0xc7,0xd1,0xf4,0xc7,0x33,0xc0,0x68,0x03,
            0x04,0x22,0xaa,0x9a,0xc3,0xd4,0x6c,0x4e,
            0xd2,0x82,0x64,0x46,0x07,0x9f,0xaa,0x09,
            0x14,0xc2,0xd7,0x05,0xd9,0x8b,0x02,0xa2,
            0xb5,0x12,0x9c,0xd1,0xde,0x16,0x4e,0xb9,
            0xcb,0xd0,0x83,0xe8,0xa2,0x50,0x3c,0x4e
        )
        val block = chacha20Block(key, counter, nonce)
        for (i in expected.indices) {
            if ((block[i].toInt() and 0xFF) != expected[i]) return false
        }
        return true
    }
}