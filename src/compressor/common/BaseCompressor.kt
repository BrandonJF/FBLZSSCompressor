package compressor.common

import util.log
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

abstract class BaseCompressor(val compressionName: String = "Generic Compression", val verbose: Boolean = false) {

    abstract fun doEncode(uncompressedFile: File): List<DataBlock>

    fun encode(filename: String, uncompressedFile: File) {

        var encodedData: List<DataBlock> = emptyList()
        val executionTime: Long = measureTimeMillis {
            encodedData = doEncode(uncompressedFile)
        }

        logCompressionStats(filename, uncompressedFile, encodedData, TimeUnit.MILLISECONDS.toSeconds(executionTime))
    }


    private fun logCompressionStats(filename: String, uncompressedFile: File, encodedStream: List<DataBlock>, executionTime: Long ) {
//        val uncompressedSize = unencodedStream.sumBy { 8} / 8
        val uncompressedSize = uncompressedFile.length()
        val compressedSize = encodedStream.sumBy {
            when (it) {
                is DataBlock.Literal -> 9
                is DataBlock.Location -> 23
                is DataBlock.BackReference -> 23
            }
        } / 8

        val difference = uncompressedSize - compressedSize
        val percentDifference: Float = (difference.toFloat()/uncompressedSize) * 100


        arrayOf("\n$compressionName Compression of file: '$filename' ---* \n",
        "\tUncompressed Size (Bytes):\t$uncompressedSize",
        "\tCompressed Size (Bytes):\t$compressedSize",
        "\tSpace Saved %:\t$percentDifference \n","\tCompression Time: \t$executionTime seconds\n").forEach(::log)

        if (verbose){
            log(encodedStream.joinToString(""))
        }

        log("END Stats-------------------------*** \n")

    }
}