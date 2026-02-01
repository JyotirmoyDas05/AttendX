package `in`.jyotirmoy.attendx.core.domain.provider

import `in`.jyotirmoy.attendx.core.presentation.provider.AppSeedColors
import `in`.jyotirmoy.attendx.core.presentation.provider.SeedColor

object SeedColorProvider {
    val seed = AppSeedColors.GreenMedium.colors

    var primary: Int = seed.primary
    var secondary: Int = seed.secondary
    var tertiary: Int = seed.tertiary

    fun setSeedColor(seed: SeedColor) {
        primary = seed.primary
        secondary = seed.secondary
        tertiary = seed.tertiary
    }
}