package `in`.jyotirmoy.attendx.settings.data.remote.mapper

import `in`.jyotirmoy.attendx.settings.data.remote.dto.GitHubReleaseDto
import `in`.jyotirmoy.attendx.settings.domain.model.GitHubRelease

fun GitHubReleaseDto.toDomain(): GitHubRelease {
    val apkAsset = assets.firstOrNull { it.name.endsWith(".apk") }
    return GitHubRelease(
        tagName = tagName,
        apkUrl = apkAsset?.browserDownloadUrl
    )
}
