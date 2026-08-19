package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components

import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore

class Providers {
    companion object {
        private data class ColumnProviderWithLimit(
            private val limit: Double,
            private val belowLimitComponent: LineComponent,
            private val aboveLimitComponent: LineComponent
        ) : ColumnCartesianLayer.ColumnProvider {
            override fun getColumn(
                entry: ColumnCartesianLayerModel.Entry,
                seriesIndex: Int,
                extraStore: ExtraStore,
            ): LineComponent {
                return if (entry.y < limit) belowLimitComponent
                else aboveLimitComponent
            }

            override fun getWidestSeriesColumn(
                seriesIndex: Int,
                extraStore: ExtraStore
            ): LineComponent {
                return if (belowLimitComponent.thicknessDp > aboveLimitComponent.thicknessDp) belowLimitComponent
                else aboveLimitComponent
            }
        }

        fun columnProviderWithLimit(
            limit: Number,
            belowLimitComponent: LineComponent,
            aboveLimitComponent: LineComponent
        ): ColumnCartesianLayer.ColumnProvider =
            ColumnProviderWithLimit(limit.toDouble(), belowLimitComponent, aboveLimitComponent)
    }
}
