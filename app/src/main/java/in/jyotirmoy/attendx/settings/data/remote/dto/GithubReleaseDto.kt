package `in`.jyotirmoy.attendx.settings.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubReleaseDto(
    @SerialName("tag_name")
    val tagName: String,

    @SerialName("body")
    val body: String? = null,

    @SerialName("assets")
    val assets: List<GitHubAssetDto> = emptyList()
)