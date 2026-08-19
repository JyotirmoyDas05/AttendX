package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter

@Composable
fun FocusBreakdownChart(
    modelProducer: CartesianChartModelProducer,
    hoursFormat: String = "%1\$dh",
    hoursMinutesFormat: String = "%1\$dh %2\$dm",
    minutesFormat: String = "%1\$dm",
    modifier: Modifier = Modifier
) {
    TimeColumnChart(
        modelProducer,
        hoursFormat = hoursFormat,
        hoursMinutesFormat = hoursMinutesFormat,
        minutesFormat = minutesFormat,
        xValueFormatter = remember {
            CartesianValueFormatter { _, value, _ ->
                when (value) {
                    0.0 -> "0 - 6"
                    1.0 -> "6 - 12"
                    2.0 -> "12 - 18"
                    3.0 -> "18 - 24"
                    else -> ""
                }
            }
        },
        goal = 0,
        modifier = modifier
    )
}
