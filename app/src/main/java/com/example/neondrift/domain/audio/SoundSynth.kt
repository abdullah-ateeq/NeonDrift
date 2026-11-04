package com.example.neondrift.domain.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

object SoundSynth {

    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private const val SAMPLE_RATE = 22050

    fun playButtonClick(enabled: Boolean) {
        if (!enabled) return
        audioScope.launch {
            try {
                val durationSec = 0.04f
                val numSamples = (durationSec * SAMPLE_RATE).toInt()
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toFloat() / SAMPLE_RATE
                    val freq = 600f - (t / durationSec) * 200f
                    val env = 1f - (i.toFloat() / numSamples)
                    val sample = (sin(2.0 * PI * freq * t) * env * Short.MAX_VALUE * 0.35).toInt()
                    buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                playBuffer(buffer)
            } catch (_: Exception) {}
        }
    }

    fun playMilestone(enabled: Boolean) {
        if (!enabled) return
        audioScope.launch {
            try {
                val durationSec = 0.25f
                val numSamples = (durationSec * SAMPLE_RATE).toInt()
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toFloat() / SAMPLE_RATE
                    val env = exp(-8.0 * t)
                    // Major triad chord: 523Hz (C5), 659Hz (E5), 784Hz (G5)
                    val s1 = sin(2.0 * PI * 523.25 * t)
                    val s2 = sin(2.0 * PI * 659.25 * t)
                    val s3 = sin(2.0 * PI * 783.99 * t)
                    val mixed = (s1 + s2 + s3) / 3.0
                    val sample = (mixed * env * Short.MAX_VALUE * 0.5).toInt()
                    buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                playBuffer(buffer)
            } catch (_: Exception) {}
        }
    }

    fun playCollision(enabled: Boolean) {
        if (!enabled) return
        audioScope.launch {
            try {
                val durationSec = 0.22f
                val numSamples = (durationSec * SAMPLE_RATE).toInt()
                val buffer = ShortArray(numSamples)
                val rnd = Random(42)
                for (i in 0 until numSamples) {
                    val t = i.toFloat() / SAMPLE_RATE
                    val env = (1f - (i.toFloat() / numSamples))
                    val bassFreq = 140f - (t / durationSec) * 90f
                    val bass = sin(2.0 * PI * bassFreq * t)
                    val noise = (rnd.nextFloat() * 2f - 1f) * 0.4f
                    val sample = ((bass * 0.6f + noise) * env * Short.MAX_VALUE * 0.55).toInt()
                    buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                playBuffer(buffer)
            } catch (_: Exception) {}
        }
    }

    private fun playBuffer(buffer: ShortArray) {
        var track: AudioTrack? = null
        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSizeBytes = maxOf(buffer.size * 2, minBufSize)

            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val format = AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build()

            track = AudioTrack(
                attributes,
                format,
                bufferSizeBytes,
                AudioTrack.MODE_STATIC,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )

            track.write(buffer, 0, buffer.size)
            track.play()
            Thread.sleep((buffer.size * 1000L / SAMPLE_RATE) + 40L)
        } catch (_: Exception) {
        } finally {
            try {
                track?.stop()
                track?.release()
            } catch (_: Exception) {}
        }
    }
}
