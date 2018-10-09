package compressor.lzss

import compressor.common.BaseCompressor
import compressor.common.DataBlock
import util.log
import util.logElapsedTime
import java.io.File
import java.awt.SystemColor.window
import kotlin.system.measureTimeMillis


class LZSSCompressor(val params: CompressionParams, val matchingEngine: SlidingWindowSearcher, verbose: Boolean = false): BaseCompressor("LZSS", verbose) {




    override fun doEncode(uncompressedFile: File): List<DataBlock> {
        val uncompressedSize = uncompressedFile.length()
        val uncompressedBytes: ByteArray = uncompressedFile.readBytes()
        val window = ArrayList<Byte>()
        val dict = ArrayList<Byte>()
        val buffer = ArrayList<Byte>()
        var dictSizeInBytes = 0
        val outputStream = mutableListOf<DataBlock>()

        uncompressedBytes.forEachIndexed{ index, byte ->
            if (index % 1 == 0){
                val windowProgress = "Window: ${window.size} / ${params.windowSize}"
                val dictProgress = "Dict: ${dict.size} / ${params.dictSizeBytes}"
                val bufferProgress = "Buffer: ${buffer.size} / ${params.maxCodeLengthBytes}"
                val byteProgress = "Byte: $index / $uncompressedSize"
                log("$windowProgress |\t $dictProgress |\t $bufferProgress |\t $byteProgress")
            }

            /*if our window and buffer are not filled up, fill them with our data*/
//            if (window.size < params.windowSize - 1){
//                if (dictSizeInBytes < params.dictSizeBytes){ //Write the first lookback size as literals
//                    outputStream.add(DataBlock.Literal(byte))
//                }
//                window.add(byte)
//            }

                if (dict.size < params.dictSizeBytes){ //Write the first lookback size as literals
                    outputStream.add(DataBlock.Literal(byte))
                    dict.add(byte)
                } else if (buffer.size < params.maxCodeLengthBytes - 1) {
                    buffer.add(byte)
                } else {
                //now we can begin encoding by adding to open spot in buffer
//                window.add(byte)
                buffer.add(byte)
//                val searchResults = searchForMatch(window)
                val searchResults = matchingEngine.findMatch(dict, buffer, 0, 0)
                var matchLength: Int = searchResults.matchedLength
                if (matchLength <= params.maxBytesUncodedLiteral){ //can't find a long enough match in window
//                    val bufferHead = window[params.dictSizeBytes]
//                    outputStream.add(DataBlock.Literal(bufferHead))
                    outputStream.add(DataBlock.Literal(buffer[0]))
                    matchLength = 1 // set back to amount of bytes we just wrote from buffer
                } else { // we found a long enough match with our search
                    val adjustedLength = matchLength - params.maxBytesUncodedLiteral // save bytes by writing first literals
                    outputStream.add(DataBlock.BackReference(searchResults.offset, adjustedLength))
                }
//                //slide the window
//                window.drop(matchLength)

                measureTimeMillis {
                    (0..matchLength)
                            .map { buffer.removeAt(0)}
                            .let { window.addAll(it)}
                    (0..matchLength).let {
                        window.removeAt(0)
                    }
                }.let { logElapsedTime("Removing $matchLength items", it) }



            }
        }
        return outputStream
    }

//    fun searchForMatch(window: ArrayList<Byte>): MatchResult {
//        var matchLength = 0
//        val firstByte = window[params.dictSizeBytes]
//        var fromIndex = 0
//        var offset = window.indexOf(fromIndex, firstByte)
//        window.in
//
//        while (offset < params.dictSizeBytes && offset != -1) {
//            offset = window.indexOf(fromIndex, firstByte)
//            fromIndex = offset + 1
//            var curMatchLength = 0
//
//            for (m in 1..Math.min(LZSS.BUFF_SIZE, LZSS.DICT_SIZE - offset) - 1) {
//                curMatchLength++
//                if (window[offset + m] !== window[LZSS.DICT_SIZE + m]) {
//                    break
//                }
//            }
//
//            if (curMatchLength > LZSS.MAX_UNCODED) {
//                dictOffset = (if (curMatchLength > matchLength) offset else dictOffset).toShort()
//                matchLength = Math.max(matchLength, curMatchLength)
//            }
//        }
//        return MatchResult(1, 1)
//    }

    data class MatchResult(val matchedLength: Int, val offset: Int)
}






//    static final short DICT_OFFSET_BITS = (short) (Math.log(DICT_SIZE) / Math.log(2));  // so, its 12 bits
//    static final short MATCH_LENGTH_BITS = (short) Math.floor(Math.log(BUFF_SIZE) / Math.log(2));  /