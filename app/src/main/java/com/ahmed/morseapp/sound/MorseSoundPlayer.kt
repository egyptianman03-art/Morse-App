package com.ahmed.morseapp.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.*
import kotlin.math.sin

class MorseSoundPlayer(private val context: Context) {

    private var isPlaying = false
    private var playJob: Job? = null

    // معدل العينات
    private val sampleRate = 44100
    private val frequency = 700.0 // تردد الصوت (Hz)

    fun playMorse(
        morseCode: String,
        speedWpm: Int = 15,
        volume: Float = 0.8f,
        vibrate: Boolean = false,
        onStart: () -> Unit = {},
        onFinish: () -> Unit = {}
    ) {
        stopPlaying()

        playJob = CoroutineScope(Dispatchers.IO).launch {
            isPlaying = true
            withContext(Dispatchers.Main) { onStart() }

            // حساب المدة بالمللي ثانية بناءً على السرعة
            val unitMs = (1200 / speedWpm).toLong() // وحدة واحدة بالمللي ثانية

            val dotDuration = unitMs           // نقطة
            val dashDuration = unitMs * 3      // شرطة
            val symbolGap = unitMs             // فجوة بين الرموز
            val charGap = unitMs * 3           // فجوة بين الحروف
            val wordGap = unitMs * 7           // فجوة بين الكلمات

            val vibrator = getVibrator()

            var i = 0
            while (i < morseCode.length && isPlaying) {
                when (morseCode[i]) {
                    '.' -> {
                        playTone(dotDuration, volume)
                        if (vibrate) vibrateShort(vibrator, dotDuration)
                        delay(symbolGap)
                    }
                    '-' -> {
                        playTone(dashDuration, volume)
                        if (vibrate) vibrateShort(vibrator, dashDuration)
                        delay(symbolGap)
                    }
                    ' ' -> {
                        // التحقق إذا كان فصل كلمة (/)
                        if (i + 1 < morseCode.length && morseCode[i + 1] == '/') {
                            delay(wordGap)
                            i++ // تخطي /
                        } else {
                            delay(charGap)
                        }
                    }
                    '/' -> delay(wordGap)
                }
                i++
            }

            isPlaying = false
            withContext(Dispatchers.Main) { onFinish() }
        }
    }

    private fun playTone(durationMs: Long, volume: Float) {
        val numSamples = (sampleRate * durationMs / 1000).toInt()
        val samples = ShortArray(numSamples)

        // توليد موجة جيبية
        val fadeIn = (numSamples * 0.01).toInt().coerceAtLeast(1)
        val fadeOut = (numSamples * 0.01).toInt().coerceAtLeast(1)

        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * frequency * i / sampleRate
            var amplitude = (Short.MAX_VALUE * volume).toInt()

            // تلاشي للداخل والخارج لتجنب الطقطقة
            when {
                i < fadeIn -> amplitude = (amplitude * i / fadeIn.toDouble()).toInt()
                i > numSamples - fadeOut -> amplitude = (amplitude * (numSamples - i) / fadeOut.toDouble()).toInt()
            }

            samples[i] = (amplitude * sin(angle)).toInt().toShort()
        }

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(samples, 0, samples.size)
        audioTrack.play()
        Thread.sleep(durationMs)
        audioTrack.stop()
        audioTrack.release()
    }

    @Suppress("DEPRECATION")
    private fun getVibrator(): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun vibrateShort(vibrator: Vibrator?, durationMs: Long) {
        vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }

    fun stopPlaying() {
        isPlaying = false
        playJob?.cancel()
        playJob = null
    }

    fun isPlaying() = isPlaying
}
