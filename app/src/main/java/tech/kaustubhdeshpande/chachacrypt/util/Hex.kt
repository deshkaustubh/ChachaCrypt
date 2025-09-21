package tech.kaustubhdeshpande.chachacrypt.util

private val HEX_CHARS = "0123456789abcdef".toCharArray()

fun ByteArray.toHex(): String {
    val sb = StringBuilder(size * 2)
    forEach { b ->
        val i = b.toInt()
        sb.append(HEX_CHARS[(i ushr 4) and 0x0F])
        sb.append(HEX_CHARS[i and 0x0F])
    }
    return sb.toString()
}

fun String.hexToBytes(): ByteArray {
    val clean = lowercase().replace("[^0-9a-f]".toRegex(), "")
    require(clean.length % 2 == 0) { "Hex length must be even" }
    return ByteArray(clean.length / 2) { i ->
        val idx = i * 2
        ((clean[idx].digitToInt(16) shl 4) + clean[idx + 1].digitToInt(16)).toByte()
    }
}