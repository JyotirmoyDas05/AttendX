package `in`.jyotirmoy.attendx.settings.domain.model

data class GitHubRelease(
    val tagName: String,
    val releaseNotes: String = "",
    val assets: List<ApkAsset> = emptyList()
)

data class ApkAsset(
    val name: String,
    val downloadUrl: String,
    val architecture: String // arm64-v8a, armeabi-v7a, x86_64, universal
)
