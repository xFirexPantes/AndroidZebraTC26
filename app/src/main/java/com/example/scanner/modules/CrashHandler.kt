package com.example.scanner.modules

import android.content.Intent
import android.os.Build
import com.example.scanner.ui.MainActivity
import com.example.scanner.ui.activities.CrashActivity
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Calendar
import kotlin.system.exitProcess

class CrashHandler : Thread.UncaughtExceptionHandler {

    private val newLine = "\n"
    private val errorMessage = StringBuilder()
    private val softwareInfo = StringBuilder()
    private val dateInfo = StringBuilder()

    override fun uncaughtException(thread: Thread, exception: Throwable) {

        val stackTrace = StringWriter()
        exception.printStackTrace(PrintWriter(stackTrace))

        errorMessage.append(stackTrace.toString())

        softwareInfo.append("SDK: ")
        softwareInfo.append(Build.VERSION.SDK_INT)
        softwareInfo.append(newLine)
        softwareInfo.append("Release: ")
        softwareInfo.append(Build.VERSION.RELEASE)
        softwareInfo.append(newLine)
        softwareInfo.append("Incremental: ")
        softwareInfo.append(Build.VERSION.INCREMENTAL)
        softwareInfo.append(newLine)

        dateInfo.append(Calendar.getInstance().time)
        dateInfo.append(newLine)

        Timber.tag("Error").i("$errorMessage")
//        Timber.tag("Software").i("$softwareInfo")
//        Timber.tag("Date").i("$dateInfo")

        MainActivity.getInstanceSingleton()?.apply { startActivity(Intent(this , CrashActivity::class.java)) }

        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(2)

    }
}