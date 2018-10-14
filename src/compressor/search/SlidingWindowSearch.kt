package compressor.search

import compressor.models.CompressionParams
import compressor.models.MatchResult

class SlidingWindowSearch(val compressionParams: CompressionParams) : SearchContract {

    override fun findMatch(windowBuffer: ArrayList<Byte>, lookaheadBuffer: ArrayList<Byte>): MatchResult {
        return search(windowBuffer.toByteArray(), lookaheadBuffer.toByteArray())
    }

    fun search(windowBuffer: ByteArray, lookaheadBuffer: ByteArray): MatchResult {
        var bestMatch = MatchResult(0, 0)
        if (windowBuffer.isEmpty() || lookaheadBuffer.isEmpty()) return bestMatch
        var offset = 0
        var codeLen = 0

        while (offset < windowBuffer.size && codeLen < lookaheadBuffer.size) {
            if (windowBuffer[offset] == lookaheadBuffer[0]) {
                codeLen = 1//there's a match
                while (offset + codeLen < windowBuffer.size && codeLen < lookaheadBuffer.size && windowBuffer[offset + codeLen] == lookaheadBuffer[codeLen]) {
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
//                offset += codeLen - 1
            }
            offset++
        }
        if (bestMatch.matchedLength < 1) {
            return MatchResult(0, 0)
        } else {
            val result = MatchResult(bestMatch.matchedLength, (windowBuffer.size - bestMatch.offset))
            return result
        }
    }
}
