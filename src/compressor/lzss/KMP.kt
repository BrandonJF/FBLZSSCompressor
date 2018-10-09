package compressor.lzss

class KnutMorrisPrat(R: Int, private val pat: String) {

    private val m = pat.length

    private val dfa: Array<Array<Int>> = Array(R) { Array(m) { 0 } }

    constructor(pat: CharArray) : this(256, String(pat))

    constructor(pat: String) : this(256, pat)

    init {
        dfa[pat[0].toInt()][0] = 1
        var x = 0
        var j = 1
        while(j < m) {
            (0 until R).forEach { c -> dfa[c][j] = dfa[c][x] } // Copy mismatch cases.
            dfa[pat[j].toInt()][j] = j + 1                     // Set match case.
            x = dfa[pat[j].toInt()][x]                         // Update restart state.
            j++                                                // Move to next character in pattern
        }
    }

    fun search(txt: String): Int {
        return doSearch(txt, 0)
    }

    fun search(txt: String, start: Int): Int {
        return doSearch(txt, start)
    }

    private fun doSearch(txt: String, start: Int): Int {
        val m = pat.length
        val n = txt.length
        var i = start
        var j = 0
        while(i < n && j < m) {
            j = dfa[txt[i++].toInt()][j]
        }
        return if (j == m) i - m else -1
    }
}