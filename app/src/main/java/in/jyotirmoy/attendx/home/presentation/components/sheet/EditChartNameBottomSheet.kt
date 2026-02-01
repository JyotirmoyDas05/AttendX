package `in`.jyotirmoy.attendx.home.presentation.components.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.core.data.model.SubjectEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChartNameBottomSheet(
    subject: SubjectEntity,
    onDismissRequest: () -> Unit,
    onSave: (String) -> Unit
) {
    var chartName by remember { mutableStateOf(subject.histogramLabel ?: subject.subject.take(5)) }
    var isError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Text(
                text = "Edit Chart Label",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "This 5-letter label will be shown in the Attendance Histogram for '${subject.subject}'.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = chartName,
                onValueChange = {
                    if (it.length <= 5) {
                        chartName = it
                        isError = false
                    }
                },
                label = { Text("Chart Label (Max 5)") },
                singleLine = true,
                isError = isError,
                supportingText = {
                    Text(text = "${chartName.length}/5")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        if (chartName.isNotBlank()) {
                            onSave(chartName)
                        } else {
                            isError = true
                        }
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
}
