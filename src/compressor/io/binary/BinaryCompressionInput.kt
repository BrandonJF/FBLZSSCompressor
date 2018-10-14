package compressor.io.binary

import compressor.io.CompressedIOStream
import compressor.lib.BinaryIn
import compressor.models.Block
import compressor.models.CompressionParams
import java.io.InputStream

class BinaryCompressionInput(val compressedInputStream: InputStream, val params: CompressionParams): CompressedIOStream.Input {
    private val binaryIn: BinaryIn by lazy { BinaryIn(compressedInputStream.buffered()) }


    override fun getBlockIterator(): Iterator<Block> {

        val blocks = ArrayList<Block>()
        loop@ while (!binaryIn.isEmpty && binaryIn.exists()) {
            val flag = binaryIn.readBoolean()
            when (flag) {
                params.UNENCODED_FLAG_BIT -> {
                    val literal = Block.Literal(binaryIn.readByte())
                    blocks.add(literal)
                }
                params.ENCODED_FLAG_BIT -> {
                    val offset = binaryIn.readInt(16)
                    val length = binaryIn.readInt(6)
                    val backReference = Block.BackReference(offset, length)
                    if (offset == params.STOP_OFFSET && length == params.STOP_LENGTH){
                        break@loop
                    }
                    blocks.add(backReference)
                }
            }
        }
        return blocks.iterator()
    }


}