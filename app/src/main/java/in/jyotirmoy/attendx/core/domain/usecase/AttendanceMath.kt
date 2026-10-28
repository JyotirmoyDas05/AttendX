package `in`.jyotirmoy.attendx.core.domain.usecase

import kotlin.math.ceil
import kotlin.math.floor

/** Classes you can skip and still stay at or above [target]%. Zero when at or below target. */
fun bunkAllowance(present: Int, total: Int, target: Float): Int {
    val p = target / 100.0
    if (p <= 0) return 0
    return floor((present / p) - total).toInt().coerceAtLeast(0)
}

/** Classes you must attend in a row to reach [target]%. Zero when already at or above it. */
fun classesNeeded(present: Int, total: Int, target: Float): Int {
    val p = target / 100.0
    if (p >= 1.0) return 0
    return ceil(((p * total) - present) / (1 - p)).toInt().coerceAtLeast(0)
}
