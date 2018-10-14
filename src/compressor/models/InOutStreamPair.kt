package compressor.models

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class InOutStreamPair(inputFilePath: String, outputFilePath: String) {
    val inStream: FileInputStream = File(inputFilePath).inputStream()
    val outStream: FileOutputStream = File(outputFilePath).outputStream()
}