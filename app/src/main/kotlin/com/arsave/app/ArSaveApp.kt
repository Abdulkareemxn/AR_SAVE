package com.arsave.app

import android.app.Application
import android.util.Log
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ArSaveApp : Application() {

    companion object {
        lateinit var instance: ArSaveApp
            private set

        private val _isEngineReady = MutableStateFlow(false)
        val isEngineReady = _isEngineReady.asStateFlow()

        private val _engineInitError = MutableStateFlow<String?>(null)
        val engineInitError = _engineInitError.asStateFlow()

        private val _engineVersion = MutableStateFlow("2026.10.21")
        val engineVersion = _engineVersion.asStateFlow()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Purge legacy Python 3.8 packages if upgrading from an older install
                val packagesDir = File(noBackupFilesDir, "youtubedl-android/packages")
                val oldPy38 = File(packagesDir, "python/usr/lib/python3.8")
                if (oldPy38.exists()) {
                    Log.i("ArSaveApp", "Purging legacy Python 3.8 environment to allow Python 3.12 deployment...")
                    packagesDir.deleteRecursively()
                }

                Log.d("ArSaveApp", "Initializing bundled Python 3.12 runtime and yt-dlp core...")
                YoutubeDL.getInstance().init(this@ArSaveApp)
                Log.d("ArSaveApp", "YoutubeDL native runtime initialized successfully.")

                syncBundledYtDlp()

                try {
                    val ver = YoutubeDL.getInstance().version(this@ArSaveApp)
                    Log.d("ArSaveApp", "Active yt-dlp engine version: $ver")
                    if (!ver.isNullOrBlank()) {
                        _engineVersion.value = ver
                    }
                } catch (ve: Exception) {
                    Log.w("ArSaveApp", "Could not query version: ${ve.message}")
                }

                try {
                    FFmpeg.getInstance().init(this@ArSaveApp)
                    Log.d("ArSaveApp", "FFmpeg native library initialized successfully.")
                } catch (fe: Exception) {
                    Log.w("ArSaveApp", "FFmpeg init warning: ${fe.message}")
                }

                _isEngineReady.value = true
            } catch (e: Exception) {
                Log.e("ArSaveApp", "Failed to initialize YoutubeDL: ${e.message}", e)
                _engineInitError.value = e.message
            }
        }
    }

    fun syncBundledYtDlp() {
        try {
            val ytdlpDir = File(noBackupFilesDir, "youtubedl-android/yt-dlp")
            if (!ytdlpDir.exists()) {
                ytdlpDir.mkdirs()
            }
            val targetFile = File(ytdlpDir, "yt-dlp")

            val rawRes = resources.openRawResource(R.raw.ytdlp)
            val bundledSize = rawRes.available().toLong()

            // If file does not exist, or size differs (e.g. replacing older 2022 binary), overwrite
            if (!targetFile.exists() || targetFile.length() != bundledSize) {
                Log.i("ArSaveApp", "Updating yt-dlp binary from bundled 2026 release ($bundledSize bytes)...")
                FileOutputStream(targetFile).use { out ->
                    rawRes.copyTo(out)
                }
                targetFile.setExecutable(true, false)
                targetFile.setReadable(true, false)
                Log.i("ArSaveApp", "yt-dlp binary synchronized successfully.")
            } else {
                rawRes.close()
            }
        } catch (e: Exception) {
            Log.e("ArSaveApp", "syncBundledYtDlp error: ${e.message}", e)
        }
    }
}

