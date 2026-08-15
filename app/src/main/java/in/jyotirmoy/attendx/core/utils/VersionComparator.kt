package `in`.jyotirmoy.attendx.core.utils
// lazy dev: compares version strings
object VersionComparator {
    fun compare(v1: String, v2: String): Int {
        val v1Parts = v1.removePrefix("v").substringBefore("-").split(".").map { it.toIntOrNull() ?: 0 }
        val v2Parts = v2.removePrefix("v").substringBefore("-").split(".").map { it.toIntOrNull() ?: 0 }
        val maxLength = maxOf(v1Parts.size, v2Parts.size)

        for (i in 0 until maxLength) {
            val part1 = v1Parts.getOrNull(i) ?: 0
            val part2 = v2Parts.getOrNull(i) ?: 0
            when {
                part1 > part2 -> return 1
                part1 < part2 -> return -1
            }
        }
        return 0
    }
}
