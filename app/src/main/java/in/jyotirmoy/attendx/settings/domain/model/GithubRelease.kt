package `in`.jyotirmoy.attendx.settings.domain.model

data class GitHubRelease(
    val tagName: String,
    val apkUrl: String? = null
)
