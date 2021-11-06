package rxhttp.wrapper.utils

import okio.Buffer


/**
 * User: ljx
 * Date: 2021/10/24
 * Time: 21:25
 */
private val HEX_DIGITS =
    charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

private const val PATH_SEGMENT_ALWAYS_ENCODE_SET = " \"<>^`{}|\\?#"

internal fun String.canonicalizeForPath(alreadyEncoded: Boolean): String {
    var codePoint: Int
    var i = 0
    val limit = length
    while (i < limit) {
        codePoint = codePointAt(i)
        if (codePoint < 0x20 ||
            codePoint >= 0x7f ||
            codePoint.toChar() in PATH_SEGMENT_ALWAYS_ENCODE_SET ||
            !alreadyEncoded && (codePoint == '/'.code || codePoint == '%'.code)
        ) {
            // Slow path: the character at i requires encoding!
            val out = Buffer()
            out.writeUtf8(this, 0, i)
            out.canonicalizeForPath(this, i, limit, alreadyEncoded)
            return out.readUtf8()
        }
        i += Character.charCount(codePoint)
    }

    // Fast path: no characters required encoding.
    return this
}

private fun Buffer.canonicalizeForPath(
    input: String,
    pos: Int,
    limit: Int,
    alreadyEncoded: Boolean
) {
    var utf8Buffer: Buffer? = null // Lazily allocated.
    var codePoint: Int
    var i = pos
    while (i < limit) {
        codePoint = input.codePointAt(i)
        if (alreadyEncoded && (codePoint == '\t'.code || codePoint == '\n'.code ||
                codePoint == '\u000c'.code || codePoint == '\r'.code)
        ) {
            // Skip this character.
        } else if (codePoint < 0x20 ||
            codePoint >= 0x7f ||
            codePoint.toChar() in PATH_SEGMENT_ALWAYS_ENCODE_SET ||
            !alreadyEncoded && (codePoint == '/'.code || codePoint == '%'.code)
        ) {
            // Percent encode this character.
            if (utf8Buffer == null) {
                utf8Buffer = Buffer()
            }
            utf8Buffer.writeUtf8CodePoint(codePoint)
            while (!utf8Buffer.exhausted()) {
                val b: Int = utf8Buffer.readByte().toInt() and 0xff
                writeByte('%'.code)
                writeByte(HEX_DIGITS[b shr 4 and 0xf].code)
                writeByte(HEX_DIGITS[b and 0xf].code)
            }
        } else {
            // This character doesn't need encoding. Just copy it over.
            writeUtf8CodePoint(codePoint)
        }
        i += Character.charCount(codePoint)
    }
}