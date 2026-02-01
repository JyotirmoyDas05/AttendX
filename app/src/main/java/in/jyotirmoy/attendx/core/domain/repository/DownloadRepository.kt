package `in`.jyotirmoy.attendx.core.domain.repository

import `in`.jyotirmoy.attendx.core.domain.model.DownloadState

interface DownloadRepository {
    suspend fun downloadApk(
        url: String,
        fileName: String,
        onProgress: (DownloadState) -> Unit
    )

    fun cancelDownload()
}
