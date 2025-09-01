package com.example.scanner.app

import android.app.Application
import android.util.Log
import com.example.scanner.BuildConfig
import com.example.scanner.modules.CrashHandler
import fr.bipi.treessence.common.formatter.Formatter
import fr.bipi.treessence.common.os.OsInfoProvider
import fr.bipi.treessence.common.os.OsInfoProviderDefault
import fr.bipi.treessence.common.time.TimeStamper
import fr.bipi.treessence.common.utils.FileUtils
import fr.bipi.treessence.file.FileLoggerTree
import timber.log.Timber
import java.io.File
import java.lang.Exception
import java.util.logging.FileHandler
import java.util.logging.LogRecord
import java.util.logging.Logger

class App:Application() {

    companion object{
        var fileLoggerTree:FileLoggerTree?=null
        val plantFileLoggerTree:(stringPath:String?)->Unit={ stringPath->
            stringPath?.let { path ->
                if (fileLoggerTree ==null){
                    FileLoggerTree.Builder()
                        .withFileName("log.log")
                        .withDirName("$path/scannerLogs")
                        .withSizeLimit(50000)
                        .withFileLimit(10)
                        .withMinPriority(Log.DEBUG)
                        .appendToFile(true)
                        .withFormatter(object :Formatter{
                            private val priorities = mapOf(
                                Log.VERBOSE to "V/",
                                Log.DEBUG to "D/",
                                Log.INFO to "I/",
                                Log.WARN to "W/",
                                Log.ERROR to "E/",
                                Log.ASSERT to "WTF/",
                            )
                            var timeStamper = TimeStamper("MM-dd HH:mm:ss:SSS")
                            var osInfoProvider: OsInfoProvider = OsInfoProviderDefault()

                            override fun format(priority: Int, tag: String?, message: String): String {
                                return "${timeStamper.getCurrentTimeStamp(osInfoProvider.currentTimeMillis)} ${priorities[priority] ?: ""}${tag ?: ""} : ThreadId:(${osInfoProvider.currentThreadId}) ${message}\n"
                            }
                        })
                        .build()
                        .let {
                            fileLoggerTree =it
                            Timber.plant(it)
                            Timber.plant(Timber.DebugTree())
                            Timber.tag("Timber").d("plant:DebugTree,FileLoggerTree")
                        }
                }
            }
        }
    }

    override fun onCreate() {

        Thread.setDefaultUncaughtExceptionHandler(CrashHandler())
        //if (BuildConfig.DEBUG){
            plantFileLoggerTree(externalCacheDir?.canonicalPath)
        //}

        super.onCreate()
    }
}