@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.jyotirmoy.attendx.timetable.presentation.screens.template

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.jyotirmoy.attendx.core.presentation.components.button.BackButton
import `in`.jyotirmoy.attendx.core.presentation.components.card.RoundedCornerCard
import `in`.jyotirmoy.attendx.core.presentation.components.state.StatusMessage
import `in`.jyotirmoy.attendx.timetable.data.model.community.CommunityTemplate

private sealed interface MarketplaceContent {
    data object Loading : MarketplaceContent
    data object Empty : MarketplaceContent
    data class Failed(val message: String) : MarketplaceContent
    data class Results(val templates: List<CommunityTemplate>) : MarketplaceContent
}

@Composable
fun MarketplaceScreen(
    onNavigateBack: () -> Unit,
    onTemplateClick: (CommunityTemplate) -> Unit,
    onNavigateToUpload: () -> Unit = {},
    viewModel: MarketplaceViewModel = hiltViewModel()
) {
    val state by viewModel.state
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val fabExpanded by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    val content: MarketplaceContent = when {
        state.isLoading -> MarketplaceContent.Loading
        state.error != null -> MarketplaceContent.Failed(state.error!!)
        state.templates.isEmpty() -> MarketplaceContent.Empty
        else -> MarketplaceContent.Results(state.templates)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Community Templates") },
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToUpload,
                expanded = fabExpanded,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.CloudUpload,
                        contentDescription = "Share",
                        modifier = Modifier.size(20.dp)
                    )
                },
                text = {
                    Text(
                        text = "Share",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(MarketplaceEvent.SearchQueryChanged(it)) },
                placeholder = { Text("Search by College or Branch") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            AnimatedContent(
                targetState = content,
                contentKey = { it::class },
                label = "marketplace-content"
            ) { target ->
                when (target) {
                    is MarketplaceContent.Loading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }

                    is MarketplaceContent.Failed -> StatusMessage(
                        icon = Icons.Rounded.CloudOff,
                        title = "Couldn't load templates",
                        description = target.message,
                        tint = MaterialTheme.colorScheme.error,
                        actionLabel = "Retry",
                        onAction = { viewModel.onEvent(MarketplaceEvent.Refresh) }
                    )

                    is MarketplaceContent.Empty -> StatusMessage(
                        icon = Icons.Rounded.School,
                        title = "No templates found",
                        description = if (state.searchQuery.isBlank()) {
                            "Nobody has shared a timetable yet. Be the first."
                        } else {
                            "Try a different college or branch."
                        }
                    )

                    is MarketplaceContent.Results -> LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(target.templates, key = { it.id }) { template ->
                            TemplateCard(
                                template = template,
                                onClick = { onTemplateClick(template) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: CommunityTemplate,
    onClick: () -> Unit
) {
    RoundedCornerCard(
        roundedCornerShape = RoundedCornerShape(20.dp),
        paddingValues = PaddingValues(0.dp),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${template.college} • ${template.department}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sem ${template.semester} • ${template.classes.size} Classes" +
                        if (template.version > 1) " • v${template.version}" else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.CloudDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${template.downloads}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
