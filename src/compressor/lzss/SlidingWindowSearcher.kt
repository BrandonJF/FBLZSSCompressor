package compressor.lzss

import util.log

class SlidingWindowSearcher(val compressionParams: CompressionParams) {

    fun findMatch(window: ArrayList<Byte>, buffer: ArrayList<Byte>): MatchResult {
        var bestMatch = MatchResult(0, 0)
        if (window.isEmpty() || buffer.isEmpty()) return bestMatch
        var offset = 0
        var codeLen = 0

        while (offset < window.size && codeLen < buffer.size) {
            if (window[offset] == buffer.first()) {
                codeLen = 1//there's a match
                while (offset + codeLen < window.size && codeLen < buffer.size && window[offset + codeLen] == buffer[codeLen]) {
                    if (codeLen >= compressionParams.maxCodeLengthBytes) {
                        break
                    }
                    codeLen++ //keep building up code
                }
                if (codeLen >= bestMatch.matchedLength) { //update latest long match
                    bestMatch = MatchResult(codeLen, offset)
                }
                if (codeLen >= compressionParams.maxCodeLengthBytes) {
                    bestMatch = MatchResult(compressionParams.maxCodeLengthBytes, bestMatch.offset)
                    break
                }
            }
            offset++
        }
        if (bestMatch.matchedLength < 1) {
            return MatchResult(0, 0)
        } else {

            val result = MatchResult(bestMatch.matchedLength, (window.size - bestMatch.offset) * -1)
//            val result = bestMatch
//            val sub = window.subList(bestMatch.offset, bestMatch.offset + bestMatch.matchedLength)
//            log(sub.toString())
//            sub.map { it.toChar() }.let { it.joinToString("") }.let(System.out::println)
//            log(result.toString())
//            log("Window size = ${window.size}")
            return result
        }
    }
}
