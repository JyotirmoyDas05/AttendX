package `in`.jyotirmoy.attendx.core.presentation.components.markdown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import `in`.jyotirmoy.attendx.R

private val markdownLinkRegex = Regex("(@[a-zA-Z0-9_-]+)|(https?://[\\w-]+(\\.[\\w-]+)+[\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])")

// lazy dev: markdown parser for changelogs
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val lines = text.split("\n")

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lines.filter { it.isNotBlank() }.forEach { line ->
            val trimmedLine = line.trim()

            if (trimmedLine.startsWith("#")) {
                val level = trimmedLine.takeWhile { it == '#' }.length
                val headerText = trimmedLine.substring(level).trim()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = headerText,
                        style = when (level) {
                            1 -> MaterialTheme.typography.titleLarge
                            2 -> MaterialTheme.typography.titleMedium
                            else -> MaterialTheme.typography.titleSmall
                        },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                val isListItem = trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ")
                val contentText = if (isListItem) {
                    trimmedLine.substring(2).trim()
                } else {
                    trimmedLine
                }

                val primaryColor = MaterialTheme.colorScheme.primary

                val annotatedString = buildAnnotatedString {
                    var lastIndex = 0
                    markdownLinkRegex.findAll(contentText).forEach { result ->
                        val before = contentText.substring(lastIndex, result.range.first)
                        appendFormatted(before)

                        val match = result.value
                        val link = if (match.startsWith("@")) "https://github.com/${match.substring(1)}" else match

                        val linkAnnotation = LinkAnnotation.Url(
                            url = link,
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = primaryColor,
                                    fontWeight = if (match.startsWith("@")) FontWeight.Bold else FontWeight.Normal,
                                    textDecoration = if (match.startsWith("@")) TextDecoration.None else TextDecoration.Underline
                                )
                            )
                        )

                        withLink(linkAnnotation) {
                            append(match)
                        }
                        lastIndex = result.range.last + 1
                    }
                    appendFormatted(contentText.substring(lastIndex))
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        if (isListItem) {
                            Text(
                                text = stringResource(R.string.list_bullet),
                                modifier = Modifier.padding(end = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = annotatedString,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    if (isListItem) {
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendFormatted(text: String) {
    var cursor = 0
    val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
    boldRegex.findAll(text).forEach { match ->
        append(text.substring(cursor, match.range.first))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(match.groupValues[1])
        }
        cursor = match.range.last + 1
    }
    append(text.substring(cursor))
}
