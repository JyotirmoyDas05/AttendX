package `in`.jyotirmoy.attendx.core.utils

import android.os.Build
import `in`.jyotirmoy.attendx.settings.domain.model.ApkAsset

object DeviceArchitecture {
    
    // gets primary device architecture
    fun get(): String {
        val abis = Build.SUPPORTED_ABIS
        return when {
            abis.contains("arm64-v8a") -> "arm64-v8a"
            abis.contains("armeabi-v7a") -> "armeabi-v7a"
            abis.contains("x86_64") -> "x86_64"
            abis.contains("x86") -> "x86"
            else -> "universal"
        }
    }
    
    // selects best APK for device: exact match > universal > first available
    fun selectBestApk(assets: List<ApkAsset>): ApkAsset? {
        if (assets.isEmpty()) return null
        
        val deviceArch = get()
        
        // Priority: exact arch match > universal > any
        return assets.find { it.architecture == deviceArch }
            ?: assets.find { it.architecture == "universal" }
            ?: assets.firstOrNull()
    }
    
    // gets display name for architecture
    fun getDisplayName(architecture: String): String = when (architecture) {
        "arm64-v8a" -> "64-bit ARM (Recommended for most modern phones)"
        "armeabi-v7a" -> "32-bit ARM (Older devices)"
        "x86_64" -> "64-bit x86 (Emulators, Chromebooks)"
        "universal" -> "Universal (Works on all devices)"
        else -> architecture
    }
}
