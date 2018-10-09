package compressor.lzss

import util.log

class SlidingWindowSearcher( val compressionParams: CompressionParams) {

    fun findMatch( slidingWindow : ArrayList<Byte>,  buffer: ArrayList<Byte>, windowHead: Int = 0, bufferHead: Int = 0) : LZSSCompressor.MatchResult{
        var matchLength = 0
        var matchOffset = 0

        var i = windowHead
        var j = 0

        while (i < slidingWindow.size) {
            if (slidingWindow[i] == buffer[bufferHead]){
                j = 1//there's a match
                while (slidingWindow[i+j] == buffer[bufferHead+j]){
                    if (j >= compressionParams.maxCodeLengthBytes){
                        break
                    }
                    j++ //keep building up code
                }
                if (j >= matchLength) { //update latest long match
                    matchLength = j
                    matchOffset = i
                }
                if (j >= compressionParams.maxCodeLengthBytes) {
                    matchLength = compressionParams.maxCodeLengthBytes;
                    break
                }
            }



            i++
//            if ()
        }
        val result = LZSSCompressor.MatchResult(matchLength,matchOffset)
        val sub = slidingWindow.subList(result.offset, result.offset + result.matchedLength)
        log(sub.toString())
        sub.map { it.toChar() }.let { it.joinToString("") }.let(System.out::println)
        log(j.toString())

        log(result.toString())
        return result
    }
}
