package com.example.scanner.app

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class DownloadCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        if (downloadId != -1L) {
            // Here, you can perform actions based on the completed download.
            // For example, you can query the DownloadManager to get details
            // about the specific download using the downloadId.
            // You can also check the status of the download (successful, failed, etc.)
            // and take appropriate action.
        }
    }

}