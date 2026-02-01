package `in`.jyotirmoy.attendx.settings.domain.usecase

import android.annotation.SuppressLint
import android.content.Context
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.settings.domain.model.ChangelogItem
import `in`.jyotirmoy.attendx.settings.data.local.source.versionList

class GetAllChangelogsUseCase(
    private val context: Context,
    private val versions: List<String> = versionList
) {
    @SuppressLint("DiscouragedApi")
    operator fun invoke(): List<ChangelogItem> {
        val res = context.resources
        val pkg = context.packageName

        return versions.map { version ->
            val resourceName = "changelogs_" + version.replace('.', '_')

            val resId = res.getIdentifier(resourceName, "string", pkg)

            val text = context.getString(
                if (resId != 0) resId else R.string.no_changelog_found
            )

            ChangelogItem(versionName = version, changelog = text)
        }
    }
}