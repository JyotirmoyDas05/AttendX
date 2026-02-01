@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.jyotirmoy.attendx.settings.presentation.page.lookandfeel.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.core.presentation.components.svg.DynamicColorImageVectors
import `in`.jyotirmoy.attendx.core.presentation.components.svg.vectors.themePicker
import `in`.jyotirmoy.attendx.navigation.LocalNavController
import `in`.jyotirmoy.attendx.settings.data.local.SettingsKeys
import `in`.jyotirmoy.attendx.settings.domain.model.PreferenceGroup
import `in`.jyotirmoy.attendx.settings.presentation.components.bottomsheet.FontStyleBottomSheet
import `in`.jyotirmoy.attendx.settings.presentation.components.item.PreferenceItemView
import `in`.jyotirmoy.attendx.settings.presentation.components.scaffold.SettingsScaffold
import `in`.jyotirmoy.attendx.settings.presentation.components.shape.CardCornerShape.getRoundedShape

import `in`.jyotirmoy.attendx.settings.presentation.event.SettingsUiEvent
import `in`.jyotirmoy.attendx.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun LookAndFeelScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val settings = settingsViewModel.lookAndFeelPageList
    val context = LocalContext.current
    var showFontStyleBottomSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        settingsViewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.LaunchIntent -> {
                    context.startActivity(event.intent)
                }

                is SettingsUiEvent.Navigate -> {
                    navController.navigate(event.route)
                }

                is SettingsUiEvent.ShowBottomSheet -> {
                    if (event.key == SettingsKeys.FONT_FAMILY) {
                        showFontStyleBottomSheet = true
                    }
                }

                else -> {}
            }
        }
    }

    val listState = rememberLazyListState()

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.look_and_feel),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
                    .padding(top = 10.dp),
                state = listState,
                contentPadding = innerPadding
            ) {
                item {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 100.dp, vertical = 25.dp),
                        imageVector = DynamicColorImageVectors.themePicker(),
                        contentDescription = null
                    )
                }



                itemsIndexed(settings) { index, group ->
                    when (group) {
                        is PreferenceGroup.Category -> {
                            Text(
                                text = stringResource(group.categoryNameResId),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .animateItem()
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp,
                                        top = 30.dp,
                                        bottom = 10.dp
                                    )
                            )
                            val visibleItems = group.items.filter { it.isLayoutVisible }

                            visibleItems.forEachIndexed { i, item ->
                                val shape = getRoundedShape(i, visibleItems.size)

                                PreferenceItemView(
                                    item = item,
                                    modifier = Modifier.animateItem(),
                                    roundedShape = shape
                                )
                            }
                        }

                        is PreferenceGroup.Items -> {
                            val visibleItems = group.items.filter { it.isLayoutVisible }

                            visibleItems.forEachIndexed { i, item ->
                                val shape = getRoundedShape(i, visibleItems.size)

                                PreferenceItemView(
                                    item = item,
                                    modifier = Modifier.animateItem(),
                                    roundedShape = shape
                                )
                            }
                        }

                        else -> {}
                    }
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                    )
                }
            }
        },
    )

    if (showFontStyleBottomSheet) {
        FontStyleBottomSheet(onDismiss = { showFontStyleBottomSheet = false })
    }
}