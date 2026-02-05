package `in`.jyotirmoy.attendx.settings.data.remote.mapper

import `in`.jyotirmoy.attendx.settings.data.remote.dto.GitHubReleaseDto
import `in`.jyotirmoy.attendx.settings.domain.model.ApkAsset
import `in`.jyotirmoy.attendx.settings.domain.model.GitHubRelease

fun GitHubReleaseDto.toDomain(): GitHubRelease {
    val apkAssets = assets
        .filter { it.name.endsWith(".apk") }
        .map { asset ->
            ApkAsset(
                name = asset.name,
                downloadUrl = asset.browserDownloadUrl,
                architecture = extractArchitecture(asset.name)
            )
        }
    return GitHubRelease(
        tagName = tagName,
        assets = apkAssets
    )
}

private fun extractArchitecture(filename: String): String = when {
    "arm64-v8a" in filename -> "arm64-v8a"
    "armeabi-v7a" in filename -> "armeabi-v7a"
    "x86_64" in filename -> "x86_64"
    "universal" in filename -> "universal"
    else -> "universal" // fallback for unnamed arch
}
