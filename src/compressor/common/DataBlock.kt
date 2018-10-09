package compressor.common

sealed class DataBlock {
    class Literal(val byte: Byte) : DataBlock(){
        override fun toString(): String {
            return "$byte" //0
//            return "(val, $bits)" //0
        }
    }

    class Location(val startIndex: Int, val runLength: Int): DataBlock() {
        override fun toString(): String {
            return "(loc, $startIndex, $runLength)" //internal
        }
    }

    class BackReference(val distanceBack: Int, val runLength: Int) : DataBlock(){
        override fun toString(): String {
            return "(ref, $distanceBack, $runLength)" //1
        }
    }
}