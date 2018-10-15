package compressor.models

import util.log
import java.util.concurrent.TimeUnit

/**
 * Convenience class for displaying execution stats of our individual jobs.
 */
data class CompressionJobStats(var jobName: String, var executionTimeMillis: Long) {
    fun display() {
        log("Stats for $jobName ---------------")
        log("\tExecution Time (millis): ${TimeUnit.MILLISECONDS.toMillis(executionTimeMillis)}")
        log("---------------------------------------------------")
    }
}