package `in`.jyotirmoy.attendx.tools.data.repository

import `in`.jyotirmoy.attendx.tools.data.dao.PreferenceDao
import `in`.jyotirmoy.attendx.tools.data.model.BooleanPreference
import `in`.jyotirmoy.attendx.tools.data.model.IntPreference
import `in`.jyotirmoy.attendx.tools.data.model.StringPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface PreferenceRepository {
    suspend fun saveIntPreference(key: String, value: Int): Int
    suspend fun saveBooleanPreference(key: String, value: Boolean): Boolean
    suspend fun saveStringPreference(key: String, value: String): String
    suspend fun getIntPreference(key: String): Int?
    suspend fun getBooleanPreference(key: String): Boolean?
    suspend fun getStringPreference(key: String): String?
    suspend fun resetSettings()
}

@Singleton
class AppPreferenceRepository @Inject constructor(
    private val preferenceDao: PreferenceDao
) : PreferenceRepository {
    private val ioDispatcher = Dispatchers.IO

    override suspend fun saveIntPreference(key: String, value: Int): Int =
        withContext(ioDispatcher) {
            preferenceDao.insertIntPreference(IntPreference(key, value))
            value
        }

    override suspend fun saveBooleanPreference(key: String, value: Boolean): Boolean =
        withContext(ioDispatcher) {
            preferenceDao.insertBooleanPreference(BooleanPreference(key, value))
            value
        }

    override suspend fun saveStringPreference(key: String, value: String): String =
        withContext(ioDispatcher) {
            preferenceDao.insertStringPreference(StringPreference(key, value))
            value
        }

    override suspend fun getIntPreference(key: String): Int? =
        withContext(ioDispatcher) { preferenceDao.getIntPreference(key) }

    override suspend fun getBooleanPreference(key: String): Boolean? =
        withContext(ioDispatcher) { preferenceDao.getBooleanPreference(key) }

    override suspend fun getStringPreference(key: String): String? =
        withContext(ioDispatcher) { preferenceDao.getStringPreference(key) }

    override suspend fun resetSettings() =
        withContext(ioDispatcher) {
            // rewrites default values
        }
}
