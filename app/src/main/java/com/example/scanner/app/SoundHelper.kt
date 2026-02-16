package com.example.scanner.app

import android.content.Context
import android.media.SoundPool
import com.example.scanner.R

class SoundHelper(context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder().build()
    private var successSoundId: Int = 0

    init {
        successSoundId = soundPool.load(context, R.raw.ok, 1)
    }

    fun playSuccessSound() {
        soundPool.play(successSoundId, 1f, 1f, 1, 0, 1f)
    }

//    fun release() {
//        soundPool.release()
//    }
}