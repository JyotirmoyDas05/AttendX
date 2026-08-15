package `in`.jyotirmoy.attendx.settings.presentation.page.changelog.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import `in`.jyotirmoy.attendx.core.presentation.components.bottomsheet.ChangelogBottomSheet
import `in`.jyotirmoy.attendx.navigation.LocalNavController

// lazy dev: changelog screen
@Composable
fun ChangelogScreen(
    modifier: Modifier = Modifier
) {
    val navController = LocalNavController.current
    ChangelogBottomSheet(
        modifier = modifier,
        onDismiss = { navController.popBackStack() }
    )
}
