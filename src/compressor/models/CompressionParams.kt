package compressor.models

data class CompressionParams(
        val maxBytesUncodedLiteral: Int = 2, //most chars allowed without coding in bits
        val maxCodeLengthBytes: Int = 64, //longest code allowed to copy, buffer size = 2^6 (6bit pointer)
        val dictSizeBytes: Int = 65535/13, //max offset chars to lookback/bytes = 2^16 (16bit pointer)
        val UNENCODED_FLAG_BIT: Boolean = false, //0 bit
        val ENCODED_FLAG_BIT: Boolean = true, //1 bit
        val allocatedOffsetBits: Int = 16,
        val allocatedLengthBits: Int = 6,
        val STOP_OFFSET: Int = 0,
        val STOP_LENGTH: Int = 0
)