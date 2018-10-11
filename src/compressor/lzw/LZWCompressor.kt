//package compressor.lzw
//
//import compressor.common.BaseCompressor
//import compressor.common.DataBlock
//import java.io.File
//import kotlin.collections.HashMap
//
//
///*
//* A class the encodes and decodes based on a modified version of an LZW algorithm.
//* */
//class LZWCompressor(verbose: Boolean = false) : BaseCompressor("LZW", verbose) {
//
//    override fun doEncode(uncompressedFile: File): List<DataBlock> {
//        val unencodedInput = uncompressedFile.readText()
//        //This table contains the sequences we have seen mapped to the index at which we saw them.
//        val codes = HashMap<String, DataBlock>()
//        val outputStream = mutableListOf<DataBlock>()
//        //initialize codes with values of single chars
//        (0..255).map { it.toChar() }.forEach { codes.put(it.toString(), DataBlock.Literal(it.toByte())) }
//        var symbolSeq = ""
//        //not initializing with values so we jump straight to encoding
//        unencodedInput.forEachIndexed { index, symbol ->
//            val updatedSymbolSeq = symbolSeq + symbol
//            // check to see if we still have a valid code for our updated sequence
//            if (codes[updatedSymbolSeq] != null) {
//                symbolSeq = updatedSymbolSeq
//            } else {
//                //dealing with a new symbol, calculate start
//                val runLength = updatedSymbolSeq.length
//                val startIndex = index - runLength + 1
//                val locCode = DataBlock.Location(startIndex, runLength)
//                codes.put(updatedSymbolSeq, locCode)
//                codes[symbolSeq]?.let (outputStream::add) //output the longest known symbol code
//                symbolSeq = symbol.toString() //reset the symbolSeq to this new unseen value
//            }
//        }
//        if (symbolSeq.isNotEmpty()) outputStream.add(codes[symbolSeq]!!) //handle the remaining symbols
//        return outputStream
//    }
//
//    override fun doDecompress(uncompressedFile: File): List<DataBlock> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//}
//
//
//
//
//
