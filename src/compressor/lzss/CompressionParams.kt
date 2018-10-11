package compressor.lzss

import sun.security.util.Length

/*

    values are in bits
    windowSize - maximumOffset for a back reference
    minBackReferenceLength - the longest a symbol sequence can be before it is converted to a back reference
    maxBitOffset -  max run length of a back reference
    maxLiteralLength - how long can a literal be before we ref it
*/
data class CompressionParams(
        val maxBytesUncodedLiteral: Int = 2,//most chars allowed without coding in bits
        val maxCodeLengthBytes: Int = 64, //longest code allowed to copy, buffer size = 2^6 (6bit pointer)
        val dictSizeBytes: Int = 65536//max offset chars to lookback/bytes = 2^16 (16bit pointer)
)