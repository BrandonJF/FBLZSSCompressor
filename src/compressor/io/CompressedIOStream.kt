package compressor.io

import compressor.models.Block

interface CompressedIOStream {

    interface Output {
        fun writeLiteral(byte: Byte)
        fun writeBackReference(offset: Int, length: Int)
        fun finish()
    }

    interface Input {
        fun getBlockIterator(): Iterator<Block>
    }

}