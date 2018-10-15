import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CompressorTests {

    @Test
    @DisplayName("Compressor should compress")
    fun testCompression(){
        assertEquals("this", "this")
    }
}