package com.example.purrsistence.ui.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.purrsistence.R
import kotlin.random.Random

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private var meowSoundId: Int = 0
    private var purchaseSoundId: Int = 0
    private val handler = Handler(Looper.getMainLooper())

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        //load sounds from raw file directory
        meowSoundId = soundPool.load(context, R.raw.cat_meow, 1)
        purchaseSoundId = soundPool.load(context, R.raw.purchase_success, 1)

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status != 0) {
                Log.e("SoundManager", "Error loading sound with id $sampleId, status $status")
            }
        }
    }

    fun playMeow() {
        if (meowSoundId != 0) {
            //randomized pitch
            val pitch = 0.8f + Random.nextFloat() * (1.2f - 0.8f)
            soundPool.play(meowSoundId, 1.5f, 1.5f, 1, 0, pitch)
        }
    }

    fun playPurchase() {
        if (purchaseSoundId != 0) {
            val streamId = soundPool.play(purchaseSoundId, 1f, 1f, 1, 0, 1f)
            if (streamId != 0) {
                //fading out after 1.5 seconds
                handler.postDelayed({
                    fadeOutStream(streamId, 500)
                }, 1500)
            }
        }
    }

    private fun fadeOutStream(streamId: Int, durationMs: Long) {
        val steps = 10
        val interval = durationMs / steps
        val volumeStep = 1.0f / steps

        var currentVolume = 1.0f

        val fadeRunnable = object : Runnable {
            override fun run() {
                currentVolume -= volumeStep
                if (currentVolume <= 0f) {
                    soundPool.setVolume(streamId, 0f, 0f)
                    soundPool.stop(streamId)
                } else {
                    soundPool.setVolume(streamId, currentVolume, currentVolume)
                    handler.postDelayed(this, interval)
                }
            }
        }
        handler.postDelayed(fadeRunnable, interval)
    }

    fun release() {
        handler.removeCallbacksAndMessages(null)
        soundPool.release()
    }
}
