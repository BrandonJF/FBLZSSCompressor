import compressor.common.BaseCompressor
import compressor.lzss.CompressionParams
import compressor.lzss.LZSSCompressor
import compressor.lzss.SlidingWindowSearcher
import java.io.File
import java.nio.file.Paths

/*
* Driver code for our compressor.
* Will compress all files in the samples folder.
*/
fun main(args: Array<String>) {
//    val compressorLZW = LZWCompressor(verbose = false)
    val params = CompressionParams(maxBytesUncodedLiteral = 2)
    val compressorLZSS = LZSSCompressor(params, SlidingWindowSearcher(params), verbose = true)
    getSampleFiles()?.take(2)?.forEachIndexed { index, file ->
        compress(file, compressorLZSS)
        val newFileName = "testfile$index"
        val path = file.path

//        File(path+newFileName).writeBytes(ByteArray(0))
    }
}

fun getSampleFiles(): Collection<File>? {
    val path = Paths.get("").toAbsolutePath().toString()
    val dir = "/src/samples"
    return File(path + dir).listFiles().filterNot { it.name[0] == '.' }
}

fun compress(uncompressedFile: File, compressor: BaseCompressor){
    compressor.encode(uncompressedFile.name, uncompressedFile)

}