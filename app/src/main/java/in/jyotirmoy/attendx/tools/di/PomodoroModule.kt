package `in`.jyotirmoy.attendx.tools.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import `in`.jyotirmoy.attendx.tools.data.dao.PreferenceDao
import `in`.jyotirmoy.attendx.tools.data.dao.StatDao
import `in`.jyotirmoy.attendx.tools.data.database.PomodoroDatabase
import `in`.jyotirmoy.attendx.tools.data.repository.AppPreferenceRepository
import `in`.jyotirmoy.attendx.tools.data.repository.AppStatRepository
import `in`.jyotirmoy.attendx.tools.data.repository.PreferenceRepository
import `in`.jyotirmoy.attendx.tools.data.repository.StatRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PomodoroDatabaseModule {

    @Provides
    @Singleton
    fun providePomodoroDatabase(@ApplicationContext context: Context): PomodoroDatabase =
        Room.databaseBuilder(
            context,
            PomodoroDatabase::class.java,
            "pomodoro_database"
        )
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides
    fun provideStatDao(db: PomodoroDatabase): StatDao = db.statDao()

    @Provides
    fun providePreferenceDao(db: PomodoroDatabase): PreferenceDao = db.preferenceDao()

    @Provides
    fun provideTopicDao(db: PomodoroDatabase): `in`.jyotirmoy.attendx.tools.data.dao.TopicDao = db.topicDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PomodoroModule {
    @Binds
    @Singleton
    abstract fun bindPreferenceRepository(impl: AppPreferenceRepository): PreferenceRepository

    @Binds
    @Singleton
    abstract fun bindStatRepository(impl: AppStatRepository): StatRepository

    @Binds
    @Singleton
    abstract fun bindTopicRepository(impl: `in`.jyotirmoy.attendx.tools.data.repository.AppTopicRepository): `in`.jyotirmoy.attendx.tools.data.repository.TopicRepository
}
