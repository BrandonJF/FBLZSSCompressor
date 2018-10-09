import compressor.common.BaseCompressor
import compressor.lzss.CompressionParams
import compressor.lzss.LZSSCompressor
import compressor.lzss.SlidingWindowSearcher
import compressor.lzw.LZWCompressor
import java.io.File
import java.nio.file.Paths

/*
* Driver code for our compressor.
* Will compress all files in the samples folder.
*/
fun main(args: Array<String>) {
//    val compressor = LZWCompressor(verbose = false)
    val params = CompressionParams()
    val compressor = LZSSCompressor(params, SlidingWindowSearcher(params), verbose = false)
    getSampleFiles()?.forEach {
        compress(it, compressor)
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