import java.io.File
import java.nio.file.Paths
import kotlin.collections.HashMap

fun main(args: Array<String>){




//    val filename = "/src/SampleCorpus.txt"
    val filename = "/src/GreenEggsCorpus.txt"
    val path = Paths.get("").toAbsolutePath().toString()
    val uncompressedInput = File(path + filename).readText()

    //setup a new compressor
    val compressor = FBCompressor()
    val encodedLZ77 = compressor.encodeLZ77(uncompressedInput)
    val encodedLZSS = compressor.encodeLZ77(uncompressedInput)
    outputStats("LZ77", uncompressedInput, encodedLZ77)
    outputStats("LZSS", uncompressedInput, encodedLZSS)


}

fun outputStats(type: String, unencodedStream: String, encodedStream: List<DataBlock>){
    val uncompressedSize = unencodedStream.sumBy { 8} / 8
    val compressedSize = encodedStream.sumBy {
        when (it) {
            is DataBlock.Literal -> 9
            is DataBlock.Location -> 23
            is DataBlock.BackReference -> 23
        }
    } / 8

    val difference = uncompressedSize - compressedSize
    val percentDifference: Float = (difference.toFloat()/uncompressedSize) * 100
    log("*$type Compression Stats-------------------------***")
    log("Uncompressed Size in Bits: $uncompressedSize")
    log("Compressed Size in Bits: $compressedSize")
    log("Compressed Delta: $difference ")
    log("Compressed Delta %: $percentDifference ")
    log(encodedStream.joinToString(""))
    log("*END Stats-------------------------***")

}

fun log(string: String){
    System.out.println(string)
}

/*
* A class the encodes and decodes based on a modified version of an LZW algorithm.
* */
class FBCompressor {


    /*
    * Older standard
    * */
    fun encodeLZ77(input: String) : List<DataBlock> {
        //This table contains the sequences we have seen mapped to the index at which we saw them.
        val codes = HashMap<String, DataBlock>()
        val outputStream = mutableListOf<DataBlock>()

        //initialize codes with values of single chars
        (0..255).forEach {
            val c = it.toChar()
            codes.put(c.toString(), DataBlock.Literal(c))
        }

        var symbolSeq = ""

        //not initializing with values so we jump straight to encoding
        input.forEachIndexed { index, symbol ->
            val updatedSymbolSeq = symbolSeq + symbol
            // check to see if we still have a valid code for our updated sequence
            if (codes[updatedSymbolSeq] != null) {
                symbolSeq = updatedSymbolSeq
            } else {
                //dealing with a new symbol, calculate start
                val runLength = updatedSymbolSeq.length
                val startIndex = index - runLength + 1
                val locCode = DataBlock.Location(startIndex, runLength)
                codes.put(updatedSymbolSeq, locCode)
                codes[symbolSeq]?.let (outputStream::add) //output the longest known symbol code
                symbolSeq = symbol.toString() //reset the symbolSeq to this new unseen value
            }
        }

        if (symbolSeq.isNotEmpty()) outputStream.add(codes[symbolSeq]!!) //handle the remaining symbols
        return outputStream
    }

    /*
    * Newer standard
    * */
    fun encodeLZSS(input: String) : List<DataBlock> {
        //This table contains the sequences we have seen mapped to the index at which we saw them.
        val codes = HashMap<String, DataBlock>()
        val outputStream = mutableListOf<DataBlock>()

        //initialize codes with values of single chars
        (0..255).forEach {
            val c = it.toChar()
            codes.put(c.toString(), DataBlock.Literal(c))
        }

        var symbolSeq = ""

        //not initializing with values so we jump straight to encoding
        input.forEachIndexed { index, symbol ->
            val updatedSymbolSeq = symbolSeq + symbol
            // check to see if we still have a valid code for our updated sequence
            if (codes[updatedSymbolSeq] != null) {
                symbolSeq = updatedSymbolSeq
            } else {
                //dealing with a new symbol, calculate start
                val runLength = updatedSymbolSeq.length
                val startIndex = index - runLength + 1
                val locCode = DataBlock.Location(startIndex, runLength)
                codes.put(updatedSymbolSeq, locCode)
                codes[symbolSeq]?.let (outputStream::add) //output the longest known symbol code
                symbolSeq = symbol.toString() //reset the symbolSeq to this new unseen value
            }
        }

        if (symbolSeq.isNotEmpty()) outputStream.add(codes[symbolSeq]!!) //handle the remaining symbols
        return outputStream
    }


    fun writeToOutputStream(outputStream: String, code: Int): String {
        return "$outputStream($code)"
    }

}

/*

(0,'s')(0,'h'),(0,'e'),(0,' '),(0,'s'),(0,'e'),(0,'l'),(0,'l'),(0,'s'),(0,' '),(1,-5,2),(0,'a'),(0,' '),(1,-13,3)

*/


/*

    values are in bits
    windowSize - maximumOffset for a back reference
    minBackReferenceLength - the longest a symbol sequence can be before it is converted to a back reference
    maxBitOffset -  max run length of a back reference
    maxLiteralLength - how long can a literal be before we ref it
*/
data class CompressionParams(
        val windowBitSize: Int = 16 ,
        val minBackReferenceBitLength: Int = 24,
        val maxBitOffset: Int = 6,
        val maxLiteralLength: Int = 8)

sealed class DataBlock {
    class Literal(val bits: Char) : DataBlock(){
        override fun toString(): String {
            return "$bits" //0
//            return "(val, $bits)" //0
        }
    }

    class Location(val startIndex: Int, val runLength: Int): DataBlock() {
        override fun toString(): String {
            return "(loc, $startIndex, $runLength)" //internal
        }
    }

    class BackReference(val distanceBack: Int, val runLength: Int) : DataBlock(){
        override fun toString(): String {
            return "(ref, $distanceBack, $runLength)" //1
        }
    }
}


