package compressor.models

data class FileConfig(val dir : String, val extension: String = ""){
    fun makePathForName(name: String): String {
        return dir + name + extension
    }
}