package compressor.lzss

import compressor.common.BaseCompressor
import compressor.common.DataBlock
import util.log
import java.io.File


class LZSSCompressor(val params: CompressionParams, val matchingEngine: SlidingWindowSearcher, verbose: Boolean = false) : BaseCompressor("LZSS", verbose) {

    override fun doEncode(uncompressedFile: File): List<DataBlock> {
        val uncompressedBytes: ByteArray = uncompressedFile.readBytes()
        val windowBuffer = ArrayList<Byte>(params.dictSizeBytes)
        val lookaheadBuffer = ArrayList<Byte>(params.maxBytesUncodedLiteral)
        val outputStream = mutableListOf<DataBlock>()
        val byteIterator = uncompressedBytes.iterator()

        fillBuffer(lookaheadBuffer, params.maxCodeLengthBytes, byteIterator)

        while (lookaheadBuffer.isNotEmpty()) {
            val searchResults = matchingEngine.findMatch(windowBuffer, lookaheadBuffer)

            if (searchResults.matchedLength <= params.maxBytesUncodedLiteral) {
                writeLiteralToOutput(lookaheadBuffer[0], outputStream)
                transferBuffers(1, fromBuffer = lookaheadBuffer, toBuffer = windowBuffer)
            } else { // we found a long enough match with our search
                writeBackReferenceToOutput(searchResults.offset, searchResults.matchedLength, outputStream)
                transferBuffers(searchResults.matchedLength, lookaheadBuffer, windowBuffer)
            }

            fillBuffer(lookaheadBuffer, params.maxCodeLengthBytes, byteIterator)
            trimBuffer(windowBuffer, maxSize = params.dictSizeBytes)
        }
        log("Bytes left in buffer = ${lookaheadBuffer.size}")
        log("Bytes left in iterator = ${byteIterator.hasNext()} ")
        doDecompress(outputStream)
        return outputStream
    }

    override fun doDecompress(compressionData: List<DataBlock>) {
        val windowBuffer = ArrayList<Byte>()
        val outputStream = ArrayList<Byte>()
        val blockIterator = compressionData.iterator()
        while (blockIterator.hasNext()) {
            val block = blockIterator.next()

            if (block is DataBlock.Literal) {
                outputStream.add(block.byte)
                windowBuffer.add(block.byte)
            } else if (block is DataBlock.BackReference) {
                val offset = block.distanceBack
                val length = block.runLength

                val decodeBuffer = windowBuffer.subList(offset, offset + length)
                decodeBuffer.forEach {
                    outputStream.add(it)
                }

                windowBuffer.addAll(decodeBuffer)
                trimBuffer(windowBuffer, maxSize = params.dictSizeBytes)
            }

        }
        outputStream.map { it.toChar() }.joinToString("").let(System.out::println)

    }

    private fun writeLiteralToOutput(byte: Byte, output: MutableList<DataBlock>) {
        output.add(DataBlock.Literal(byte))
    }

    private fun writeBackReferenceToOutput(offset: Int, matchLength: Int, output: MutableList<DataBlock>) {
        output.add(DataBlock.BackReference(offset, matchLength))
    }

    private fun fillBuffer(buffer: ArrayList<Byte>, maxSize: Int, source: ByteIterator) {
        while (buffer.size < maxSize && source.hasNext()) {
            buffer.add(source.nextByte()) //if the buffer is not filled, fill with file bytes first
        }
    }

    private fun transferBuffers(nBytesToTransfer: Int, fromBuffer: ArrayList<Byte>, toBuffer: ArrayList<Byte>) {
        (1..nBytesToTransfer).forEach { toBuffer.add(fromBuffer.removeAt(0)) }
    }

    private fun trimBuffer(buffer: ArrayList<Byte>, maxSize: Int) {
        while (buffer.size > maxSize) {
            buffer.removeAt(0) //should remove the number of matched items
        }
    }

//    private fun logProgress(){
//        if (index % 10000 == 0 && verbose) {
//            val dictProgress = "Dict: ${dict.size} / ${params.dictSizeBytes}"
//            val bufferProgress = "Buffer: ${buffer.size} / ${params.maxCodeLengthBytes}"
//            val byteProgress = "Byte: $index / $uncompressedSize"
//            log("\t $dictProgress |\t $bufferProgress |\t $byteProgress")
//        }
//    }

}