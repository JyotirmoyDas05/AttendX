package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.VicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.VicoZoomState
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberFadingEdges
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.DashedShape
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.util.millisecondsToHours
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.util.millisecondsToHoursMinutes
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.util.millisecondsToMinutes

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimeColumnChart(
    modelProducer: CartesianChartModelProducer,
    goal: Long,
    hoursFormat: String = "%1\$dh",
    hoursMinutesFormat: String = "%1\$dh %2\$dm",
    minutesFormat: String = "%1\$dm",
    modifier: Modifier = Modifier,
    zoomEnabled: Boolean = true,
    thickness: Dp = 40.dp,
    columnCollectionSpacing: Dp = 4.dp,
    xValueFormatter: CartesianValueFormatter = remember { CartesianValueFormatter.decimal() },
    yValueFormatter: CartesianValueFormatter = remember {
        CartesianValueFormatter { _, value, _ ->
            if (value >= 60 * 60 * 1000) {
                millisecondsToHours(value.toLong(), hoursFormat)
            } else {
                millisecondsToMinutes(value.toLong(), minutesFormat)
            }
        }
    },
    markerValueFormatter: DefaultCartesianMarker.ValueFormatter = remember {
        DefaultCartesianMarker.ValueFormatter { _, targets ->
            val first = targets.firstOrNull()
            val value = if (first is ColumnCartesianLayerMarkerTarget) {
                first.columns.sumOf { it.entry.y.toLong() }
            } else 0L

            if (value >= 60 * 60 * 1000) {
                millisecondsToHoursMinutes(value, hoursMinutesFormat)
            } else {
                millisecondsToMinutes(value, minutesFormat)
            }
        }
    },
    zoomState: VicoZoomState = rememberVicoZoomState(
        zoomEnabled = zoomEnabled,
        initialZoom = Zoom.fixed(),
        minZoom = Zoom.min(Zoom.Content, Zoom.fixed())
    ),
    scrollState: VicoScrollState = rememberVicoScrollState(
        initialScroll = Scroll.Absolute.End,
        autoScrollCondition = AutoScrollCondition.OnModelGrowth,
        autoScrollAnimationSpec = motionScheme.defaultSpatialSpec()
    ),
    animationSpec: AnimationSpec<Float>? = null
) {
    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        Providers.columnProviderWithLimit(
                            limit = goal,
                            belowLimitComponent = rememberLineComponent(
                                fill = Fill(colorScheme.secondary.toArgb()),
                                thickness = thickness,
                                shape = CorneredShape.Pill
                            ),
                            aboveLimitComponent = rememberLineComponent(
                                fill = Fill(colorScheme.primary.toArgb()),
                                thickness = thickness,
                                shape = CorneredShape.Pill
                            )
                        ),
                        columnCollectionSpacing = columnCollectionSpacing
                    ),
                    startAxis = VerticalAxis.rememberStart(
                        line = rememberLineComponent(fill = Fill(Color.Transparent.toArgb()), thickness = 8.dp),
                        label = rememberTextComponent(color = colorScheme.onSurface),
                        tick = null,
                        guideline = null,
                        itemPlacer = VerticalAxis.ItemPlacer.count({ 4 }),
                        valueFormatter = yValueFormatter
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        line = rememberLineComponent(fill = Fill(Color.Transparent.toArgb()), thickness = 8.dp),
                        label = rememberTextComponent(color = colorScheme.onSurface),
                        tick = null,
                        guideline = null,
                        valueFormatter = xValueFormatter
                    ),
                    decorations = if (goal > 0) listOf(
                        HorizontalLine(
                            y = { goal.toDouble() },
                            line = rememberLineComponent(
                                fill = Fill(colorScheme.primary.toArgb()),
                                thickness = 1.dp,
                                shape = DashedShape(
                                    shape = CorneredShape.Pill,
                                    dashLengthDp = 2f,
                                    gapLengthDp = 2f
                                )
                            ),
                            horizontalLabelPosition = Position.Horizontal.Start,
                            verticalLabelPosition = Position.Vertical.Center
                        )
                    )
                    else emptyList(),
                    marker = rememberDefaultCartesianMarker(
                        label = rememberTextComponent(
                            color = colorScheme.inverseOnSurface,
                            background = rememberShapeComponent(
                                fill = Fill(colorScheme.inverseSurface.toArgb()),
                                shape = CorneredShape.rounded(4f)
                            ),
                            padding = Insets(verticalDp = 4f, horizontalDp = 8f),
                            margins = Insets(bottomDp = 2f)
                        ),
                        valueFormatter = markerValueFormatter,
                        guideline = rememberLineComponent(
                            fill = Fill(colorScheme.primary.toArgb()),
                            shape = DashedShape(
                                shape = CorneredShape.Pill,
                                dashLengthDp = 2f,
                                gapLengthDp = 2f
                            )
                        )
                    ),
                    fadingEdges = rememberFadingEdges()
                ),
            modelProducer = modelProducer,
            zoomState = zoomState,
            scrollState = scrollState,
            animationSpec = animationSpec,
            animateIn = false,
            modifier = modifier.height(226.dp),
        )
    }
}
