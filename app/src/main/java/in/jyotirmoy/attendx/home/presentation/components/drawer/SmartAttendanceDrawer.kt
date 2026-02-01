@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.jyotirmoy.attendx.home.presentation.components.drawer

import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.jyotirmoy.attendx.calender.presentation.screens.calculateAttendanceInsight
import `in`.jyotirmoy.attendx.core.presentation.theme.Shape
import `in`.jyotirmoy.attendx.home.presentation.viewmodel.HomeViewModel

@Composable
fun SmartAttendanceDrawer(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit
) {
    val subjects by viewModel.subjectList.collectAsState(initial = emptyList())

    ModalDrawerSheet(
        modifier = modifier.width(340.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
            ) {
                // Header
                Text(
                    text = "Attendance Control",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .padding(top = 16.dp)
                )

                Text(
                    text = "Smart attendance insights for all subjects",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 0.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subject Cards with Smart Grouping and Smooth Scroll
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = 85.dp, // Space for fixed button
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(5.dp), // 5dp gap between cards for grouped appearance
                    flingBehavior = ScrollableDefaults.flingBehavior() // Smooth overscroll
                ) {
                    itemsIndexed(subjects) { index, subject ->
                        val counts by viewModel.getSubjectAttendanceCounts(subject.id)
                            .collectAsState(initial = `in`.jyotirmoy.attendx.core.domain.model.SubjectAttendance())

                        val isFirst = index == 0
                        val isLast = index == subjects.size - 1
                        val isOnly = subjects.size == 1

                        // Smart corner rounding - matching home screen grouped cards
                        val shape = when {
                            isOnly -> RoundedCornerShape(25.dp)
                            isFirst -> RoundedCornerShape(
                                topStart = 25.dp,
                                topEnd = 25.dp,
                                bottomStart = 10.dp,
                                bottomEnd = 10.dp
                            )

                            isLast -> RoundedCornerShape(
                                topStart = 10.dp,
                                topEnd = 10.dp,
                                bottomStart = 25.dp,
                                bottomEnd = 25.dp
                            )

                            else -> RoundedCornerShape(10.dp)
                        }

                        SmartAttendanceCard(
                            subjectName = subject.subject,
                            presentCount = counts.presentCount,
                            totalCount = counts.totalCount,
                            targetPercentage = subject.targetPercentage,
                            shape = shape
                        )
                    }
                }
            }

            // Settings Button - matching Add Subject button positioning and overlay behavior
            androidx.compose.material3.Button(
                onClick = onSettingsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp)
                    .padding(bottom = 25.dp) // Consistent bottom padding
                    .height(56.dp)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(40.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SmartAttendanceCard(
    subjectName: String,
    presentCount: Int,
    totalCount: Int,
    targetPercentage: Float,
    shape: RoundedCornerShape = RoundedCornerShape(25.dp)
) {
    val insight = calculateAttendanceInsight(
        presentCount = presentCount,
        totalCount = totalCount,
        targetPercentage = targetPercentage
    )

    // ZERO ELEVATION Surface for flat design
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Subject Name
            Text(
                text = subjectName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 26.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Smart Message with Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = insight.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = insight.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
