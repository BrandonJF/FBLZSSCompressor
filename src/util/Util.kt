package util

import java.util.concurrent.TimeUnit

fun String.print() {
    System.out.println(this)
}

fun log(string: String){
    System.out.println(string)
}

fun logElapsedTime(tag: String, timeInMillis: Long){
    log("Stopwatch: $tag took ${TimeUnit.MILLISECONDS.toSeconds(timeInMillis)} seconds.")
}