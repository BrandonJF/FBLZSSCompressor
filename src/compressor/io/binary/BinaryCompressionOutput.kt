package compressor.io.binary

import compressor.io.CompressedIOStream
import compressor.lib.BinaryOut
import compressor.models.CompressionParams
import java.io.OutputStream

class BinaryCompressionOutput(val compressedOutputStream: OutputStream, val params: CompressionParams) : CompressedIOStream.Output {
    private val binaryOut: BinaryOut by lazy { BinaryOut(compressedOutputStream) }

    override fun writeLiteral(byte: Byte) {
        binaryOut.write(params.UNENCODED_FLAG_BIT)
        binaryOut.write(byte)
    }

    override fun writeBackReference(offset: Int, length: Int) {
        binaryOut.write(params.ENCODED_FLAG_BIT)
        binaryOut.write(offset, params.allocatedOffsetBits)
        binaryOut.write(length, params.allocatedLengthBits)
    }

    override fun finish() {
        binaryOut.close()
    }

}