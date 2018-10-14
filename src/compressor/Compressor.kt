package compressor

import compressor.io.CompressedIOStream
import compressor.models.Block
import compressor.models.CompressionParams
import compressor.search.SearchContract
import util.log
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis


data class CompressionJobStats(var jobName: String, var executionTimeMillis: Long ){
    fun display(){
        log("Stats for $jobName ---------------")
        log("\tExecution Time (seconds): ${TimeUnit.MILLISECONDS.toSeconds(executionTimeMillis)}")
        log("---------------------------------------------------")
    }
}
class Compressor(val params: CompressionParams, val matchingEngine: SearchContract, verbose: Boolean = false) {


    fun compress(tag: String, uncompressedInputStream: InputStream, compressedOutputStream: CompressedIOStream.Output){
        measureTimeMillis { doCompress(uncompressedInputStream, compressedOutputStream) }.apply {
            CompressionJobStats(tag, this).display()
            Compressor.totalCopressionTime += this
        }

    }
    fun decompress(tag: String, compressedInputStream: CompressedIOStream.Input, uncompressedOutputStream: OutputStream) {
        measureTimeMillis { doDecompress(compressedInputStream, uncompressedOutputStream) }.apply {
            CompressionJobStats(tag, this).display()
        }
    }

    fun doCompress(uncompressedInputStream: InputStream, compressedOutputStream: CompressedIOStream.Output) {
        val uncompressedBytes: ByteArray = uncompressedInputStream.readBytes()
        val windowBuffer = ArrayList<Byte>(params.dictSizeBytes + 1)
        val lookaheadBuffer = ArrayList<Byte>(params.maxBytesUncodedLiteral + 1)
        val byteIterator = uncompressedBytes.iterator()
        var totalSearchTime: Long = 0L

        fillBuffer(lookaheadBuffer, params.maxCodeLengthBytes, byteIterator)

        while (lookaheadBuffer.isNotEmpty()) {
            val searchStartTime = System.currentTimeMillis()
            val searchResults = matchingEngine.findMatch(windowBuffer, lookaheadBuffer)
            totalSearchTime += System.currentTimeMillis() - searchStartTime
            if (searchResults.matchedLength <= params.maxBytesUncodedLiteral) {
                compressedOutputStream.writeLiteral(lookaheadBuffer[0])
                transferBuffers(1, fromBuffer = lookaheadBuffer, toBuffer = windowBuffer)
            } else { // we found a long enough match with our search
                val adjustedLen = searchResults.matchedLength - (params.maxBytesUncodedLiteral + 1) //fit len into less bits
                compressedOutputStream.writeBackReference(searchResults.offset, adjustedLen)
                transferBuffers(searchResults.matchedLength, lookaheadBuffer, windowBuffer)
            }
            fillBuffer(lookaheadBuffer, params.maxCodeLengthBytes, byteIterator)
            trimBuffer(windowBuffer, maxSize = params.dictSizeBytes)
        }
        compressedOutputStream.writeBackReference(params.STOP_OFFSET, params.STOP_LENGTH)
        compressedOutputStream.finish()
    }

    fun doDecompress(compressedInputStream: CompressedIOStream.Input, uncompressedOutputStream: OutputStream) {

        val windowBuffer = ArrayList<Byte>(params.dictSizeBytes + 1)
        val outputStream = ArrayList<Byte>()
        val blockIterator = compressedInputStream.getBlockIterator()

        uncompressedOutputStream.buffered().use {
            while (blockIterator.hasNext()) {
                val block = blockIterator.next()
                when (block) {
                    is Block.Literal -> {
                        outputStream.add(block.byte)
                        windowBuffer.add(block.byte)
                    }
                    is Block.BackReference -> {
                        val offset = block.distanceBack
                        val length = block.runLength + (params.maxBytesUncodedLiteral + 1) //len was encoded smaller
                        val decodeBuffer = windowBuffer.subList(windowBuffer.size - offset, windowBuffer.size - offset + length)
                        decodeBuffer.forEach { outputStream.add(it) }
                        windowBuffer.addAll(decodeBuffer)
                        trimBuffer(windowBuffer, maxSize = params.dictSizeBytes )
                    }
                }
            }
            uncompressedOutputStream.write(outputStream.toByteArray())
        }
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


    companion object {
        var totalCopressionTime: Long = 0
    }


}