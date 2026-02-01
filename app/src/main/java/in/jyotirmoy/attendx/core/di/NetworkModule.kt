package `in`.jyotirmoy.attendx.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.jyotirmoy.attendx.core.data.repository.DownloadRepositoryImpl
import `in`.jyotirmoy.attendx.core.di.qualifiers.ApiHttpClient
import `in`.jyotirmoy.attendx.core.domain.repository.DownloadRepository
import `in`.jyotirmoy.attendx.settings.data.remote.api.GitHubApi
import `in`.jyotirmoy.attendx.settings.data.remote.repository.UpdateRepositoryImpl
import `in`.jyotirmoy.attendx.settings.domain.repository.UpdateRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @ApiHttpClient
    fun provideApiHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    @Provides
    @Singleton
    fun provideGitHubApi(@ApiHttpClient client: HttpClient): GitHubApi = GitHubApi(client)

    @Provides
    @Singleton
    fun provideUpdateRepository(api: GitHubApi): UpdateRepository = UpdateRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideDownloadRepository(
        @ApplicationContext context: Context,
    ): DownloadRepository = DownloadRepositoryImpl(context)
}