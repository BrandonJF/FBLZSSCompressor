import compressor.Compressor
import compressor.io.binary.BinaryCompressionInput
import compressor.io.binary.BinaryCompressionOutput
import compressor.models.CompressionParams
import compressor.models.FileConfig
import compressor.models.InOutStreamPair
import compressor.search.KMPSearch
import compressor.search.SlidingWindowSearch
import util.log
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.TimeUnit


private val rootPath = Paths.get("").toAbsolutePath().toString()
private val pristineConfig = FileConfig(rootPath + "/src/samples/pristine/")
private val compressedConfig = FileConfig(rootPath + "/src/samples/compressed/", ".compressed")
private val uncompressedConfig = FileConfig(rootPath + "/src/samples/uncompressed/")

/*
* Driver code for our compressor.
* Will compress all files in the "/uncompressed" folder.
*/
fun main(args: Array<String>) {

    val params = CompressionParams()
    val compressorLinear = Compressor(params, SlidingWindowSearch(params), verbose = true)
    val compressor = Compressor(params, KMPSearch(params), verbose = true)

    compressFiles(compressorLinear, params)
//    compressFiles(compressor, params)
    log("\tTotal Compression Time (seconds): ${TimeUnit.MILLISECONDS.toSeconds(Compressor.totalCopressionTime)}")

//    decompressFiles(compressor, params)
}

fun compressFiles(compressor: Compressor, params: CompressionParams) {
    getFiles(pristineConfig.dir)?.forEach { pristineFile ->
        val compressedFilePath = compressedConfig.makePathForName(pristineFile.name)
        with(InOutStreamPair(pristineFile.path, compressedFilePath)) {
            compressor.compress("${pristineFile.name} Compression", inStream, BinaryCompressionOutput(outStream, params))
        }
    }
}

fun decompressFiles(compressor: Compressor, params: CompressionParams) {
    getFiles(compressedConfig.dir)?.forEach { compressedFile ->
        with(InOutStreamPair(compressedFile.path, uncompressedConfig.makePathForName(compressedFile.nameWithoutExtension))) {
            compressor.decompress("${compressedFile.name} Decompression",BinaryCompressionInput(inStream, params), outStream)
        }
    }
}

fun getFiles(dirPath: String): Collection<File>? = File(dirPath).listFiles().filterNot { it.name[0] == '.' }
