package com.github.pet.pycharm.state

/** Bounded arithmetic also handles damaged or hand-edited settings without a long UI loop. */
internal object PetProgression {
    const val MAX_LEVEL = 1_000_000
    fun required(level: Int): Long {
        val n = level.coerceIn(1, MAX_LEVEL).toLong()
        return 75L + 25L * n + 5L * (n - 1) * (n - 1)
    }
    fun before(level: Int): Long {
        val n = level.coerceIn(1, MAX_LEVEL).toLong() - 1
        // Divide before multiplying to avoid overflowing the square-sum formula.
        var a = n; var b = (n - 1).coerceAtLeast(0); var c = (2 * n - 1).coerceAtLeast(0)
        if (a % 2 == 0L) a /= 2 else b /= 2
        when { a % 3 == 0L -> a /= 3; b % 3 == 0L -> b /= 3; else -> c /= 3 }
        return 75L * n + 25L * n * (n + 1) / 2 + 5L * a * b * c
    }
    fun levelAt(total: Long, minimum: Int): Int {
        var low = minimum.coerceIn(1, MAX_LEVEL); var high = MAX_LEVEL
        while (low < high) {
            val middle = low + (high - low + 1) / 2
            if (before(middle) <= total) low = middle else high = middle - 1
        }
        return low
    }
    fun add(a: Long, b: Long): Long = a.coerceAtLeast(0).let { it + b.coerceIn(0, Long.MAX_VALUE - it) }
}
