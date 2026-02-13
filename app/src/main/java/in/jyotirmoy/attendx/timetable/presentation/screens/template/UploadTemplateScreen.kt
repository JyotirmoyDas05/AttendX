package `in`.jyotirmoy.attendx.timetable.presentation.screens.template

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import `in`.jyotirmoy.attendx.timetable.domain.usecase.UploadTemplateUseCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadTemplateScreen(
    onNavigateBack: () -> Unit,
    viewModel: UploadTemplateViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Share Timetable") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Help other students by sharing your class schedule! Your personal attendance data will NOT be shared.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            OutlinedTextField(
                value = state.college,
                onValueChange = { viewModel.onEvent(UploadEvent.EnteredCollege(it)) },
                label = { Text("College Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.department,
                onValueChange = { viewModel.onEvent(UploadEvent.EnteredDepartment(it)) },
                label = { Text("Department / Branch") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = state.semester,
                    onValueChange = { viewModel.onEvent(UploadEvent.EnteredSemester(it)) },
                    label = { Text("Semester") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.section,
                    onValueChange = { viewModel.onEvent(UploadEvent.EnteredSection(it)) },
                    label = { Text("Section") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = state.academicYear,
                onValueChange = { viewModel.onEvent(UploadEvent.EnteredYear(it)) },
                label = { Text("Academic Year (e.g. 2025-26)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.onEvent(UploadEvent.Upload) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Template")
                }
            }
        }
    }
    
    LaunchedEffect(key1 = state.isSuccess) {
        if (state.isSuccess) {
            snackbarHostState.showSnackbar("Template uploaded successfully!")
            onNavigateBack()
        }
    }
    
    LaunchedEffect(key1 = state.error) {
        if (state.error != null) {
            snackbarHostState.showSnackbar(state.error)
            viewModel.onEvent(UploadEvent.ErrorShown)
        }
    }
}
