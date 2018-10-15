package compressor

import compressor.io.binary.BinaryCompressionInput
import compressor.io.binary.BinaryCompressionOutput
import compressor.models.CompressionParams
import compressor.models.FileConfig
import compressor.models.InOutStreamPair
import compressor.search.SlidingWindowSearch
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Paths
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CompressorTest {
    private val rootPath = Paths.get("").toAbsolutePath().toString()
    private val pristineConfig = FileConfig(rootPath + "/src/samples/pristine/")
    private val compressedConfig = FileConfig(rootPath + "/src/samples/compressed/", ".compressed")
    private val uncompressedConfig = FileConfig(rootPath + "/src/samples/uncompressed/")


    @BeforeEach
    fun setUp() {
        
    }

    @Test
    @DisplayName("Files contents should uncorrupted and identical after compression and decompression")
    fun compressionDoesNotCorrupt() {
        val params = CompressionParams()
        var compressor = Compressor(params, SlidingWindowSearch(params))
        var pristineFileName: String? = null
        var pristineFile: File? = null
        var compressedFile: File? = null
        var compressedFilePath: String? = null
        var uncompressedFilePath: String? = null
        var uncompressedFile: File? = null

        //get a pristine file
        getFiles(pristineConfig.dir)?.take(1)?.first()?.let{
            pristineFileName = it.name
            pristineFile = it
        }

        //compress the pristine file to compressed file
        pristineFile?.let { pristineF ->
            compressedFilePath = compressedConfig.makePathForName(pristineF.name)
            with(InOutStreamPair(pristineF.path, compressedFilePath!!)) {
                compressor.compress("${pristineF.name} Compression", inStream, BinaryCompressionOutput(outStream, params))
            }
        }

        //get the compressed version of the file
        getFiles(compressedConfig.dir)?.first { it.nameWithoutExtension == pristineFileName}?.let{
            compressedFile = it
        }

        //uncompress the compressed version of the file
        compressedFile?.let { compressedF ->
            uncompressedFilePath = uncompressedConfig.makePathForName(compressedF.nameWithoutExtension)
            with(InOutStreamPair(compressedF.path, uncompressedFilePath!!)) {
                compressor.decompress("${compressedF.name} Decompression", BinaryCompressionInput(inStream, params), outStream)
            }
        }

        //get the uncompressed file
        getFiles(uncompressedConfig.dir)?.first{it.name == pristineFileName}?.let {
            uncompressedFile = it
        }

        val filesAreEqual = Arrays.equals(pristineFile?.readBytes(), uncompressedFile?.readBytes())
        //assert that the pristine file and uncompressed file are now the same size
        assertTrue(filesAreEqual)

    }

    @Test
    fun decompress() {
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
                compressor.decompress("${compressedFile.name} Decompression", BinaryCompressionInput(inStream, params), outStream)
            }
        }
    }

    fun getFiles(dirPath: String): Collection<File>? = File(dirPath).listFiles().filterNot { it.name[0] == '.' }

}