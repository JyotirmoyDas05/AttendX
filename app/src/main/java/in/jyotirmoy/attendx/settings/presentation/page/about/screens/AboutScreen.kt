@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.jyotirmoy.attendx.settings.presentation.page.about.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import `in`.jyotirmoy.attendx.BuildConfig
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.core.common.constants.UrlConst
import `in`.jyotirmoy.attendx.core.presentation.components.bottomsheet.ChangelogBottomSheet
import `in`.jyotirmoy.attendx.core.presentation.components.svg.DynamicColorImageVectors
import `in`.jyotirmoy.attendx.core.presentation.components.svg.vectors.LinkedIn
import `in`.jyotirmoy.attendx.core.presentation.components.svg.vectors.TwitterX
import `in`.jyotirmoy.attendx.core.utils.DeviceArchitecture
import `in`.jyotirmoy.attendx.core.utils.openUrl
import `in`.jyotirmoy.attendx.settings.presentation.components.group.Material3SettingsGroup
import `in`.jyotirmoy.attendx.settings.presentation.components.group.Material3SettingsItem
import `in`.jyotirmoy.attendx.settings.presentation.components.image.MorphingProfileAvatar
import `in`.jyotirmoy.attendx.settings.presentation.components.scaffold.SettingsScaffold

// lazy dev: m3 expressive about screen
@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showChangelogBottomSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.about),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // App Header Section
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.bear_with_white),
                                    contentDescription = stringResource(R.string.app_name),
                                    modifier = Modifier.size(56.dp)
                                )
                            }

                            Spacer(Modifier.width(20.dp))

                            Column {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = (-0.5).sp
                                )

                                Spacer(Modifier.height(8.dp))

                                androidx.compose.foundation.layout.FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val versionText = "v" + BuildConfig.VERSION_NAME.removeSuffix("-debug")
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = versionText,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = DeviceArchitecture.get().uppercase(),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    if (BuildConfig.DEBUG) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                                        ) {
                                            Text(
                                                text = "DEBUG",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Lead Developer Hero Card
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                MorphingProfileAvatar(
                                    avatarUrl = "https://github.com/JyotirmoyDas05.png",
                                    avatarSize = 96.dp,
                                    showGlowBorder = true,
                                    contentDescription = "Developer Avatar"
                                )

                                Column(
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Jyotirmoy Das",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 36.sp,
                                        letterSpacing = (-0.5).sp
                                    )
                                    Text(
                                        text = stringResource(R.string.lead_developer),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            // 3 Social Buttons (X, GitHub, LinkedIn)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { openUrl(UrlConst.URL_DEV_X, context) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = DynamicColorImageVectors.TwitterX,
                                        contentDescription = "X",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                FilledTonalButton(
                                    onClick = { openUrl(UrlConst.URL_DEV_GITHUB, context) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_github),
                                        contentDescription = "GitHub",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                FilledTonalButton(
                                    onClick = { openUrl(UrlConst.URL_DEV_LINKEDIN, context) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = DynamicColorImageVectors.LinkedIn,
                                        contentDescription = "LinkedIn",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Sponsor on GitHub Button
                            Button(
                                onClick = { openUrl(UrlConst.URL_DEV_BM_COFFEE, context) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Favorite,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = stringResource(R.string.sponsor_on_github),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                // Community & Info Section
                item {
                    val communityItems = listOf(
                        Material3SettingsItem(
                            imageVector = Icons.Outlined.BugReport,
                            title = { Text(stringResource(R.string.report_a_bug), fontWeight = FontWeight.SemiBold) },
                            description = { Text(stringResource(R.string.report_a_bug_desc)) },
                            onClick = { openUrl(UrlConst.URL_GITHUB_ISSUE_REPORT, context) }
                        ),
                        Material3SettingsItem(
                            imageVector = Icons.Rounded.NewReleases,
                            title = { Text(stringResource(R.string.changelog), fontWeight = FontWeight.SemiBold) },
                            description = { Text(stringResource(R.string.changelog_desc)) },
                            onClick = { showChangelogBottomSheet = true }
                        ),
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.ic_github),
                            title = { Text(stringResource(R.string.view_github_repo), fontWeight = FontWeight.SemiBold) },
                            description = { Text(stringResource(R.string.view_github_repo_desc)) },
                            onClick = { openUrl(UrlConst.URL_GITHUB_REPO, context) }
                        ),
                        Material3SettingsItem(
                            icon = painterResource(R.drawable.ic_license),
                            title = { Text(stringResource(R.string.license_name), fontWeight = FontWeight.SemiBold) },
                            description = { Text(stringResource(R.string.license_desc)) },
                            onClick = { openUrl(UrlConst.URL_GITHUB_REPO_LICENSE, context) }
                        )
                    )

                    Material3SettingsGroup(
                        title = stringResource(R.string.community_and_info),
                        items = communityItems,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Dramatic Quote Section
                item {
                    val developerQuote = stringResource(R.string.developer_quote)
                    val quoteText = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        ) {
                            append("“")
                        }
                        withStyle(
                            SpanStyle(
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp
                            )
                        ) {
                            append(" $developerQuote ")
                        }
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        ) {
                            append("”")
                        }
                    }

                    Text(
                        text = quoteText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 24.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 20.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    )

    if (showChangelogBottomSheet) {
        ChangelogBottomSheet(
            onDismiss = { showChangelogBottomSheet = false }
        )
    }
}