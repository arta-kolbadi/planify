package com.example.focus

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

enum class AmbientSound(val id: String) {
    NONE("NONE"),
    RAIN("RAIN"),
    BROWN_NOISE("BROWN_NOISE"),
    FOREST("FOREST");

    companion object {
        fun fromId(id: String?): AmbientSound =
            entries.firstOrNull { it.id == id } ?: NONE
    }
}

class AmbientSoundPlayer {
    private var playJob: Job? = null
    private var currentSound: AmbientSound = AmbientSound.NONE

    fun start(scope: CoroutineScope, sound: AmbientSound) {
        if (sound == AmbientSound.NONE) {
            stop()
            return
        }
        if (currentSound == sound && playJob?.isActive == true) return
        stop()
        currentSound = sound

        playJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 22050
            val bufferSamples = 2048
            val minBufBytes = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(bufferSamples * 2)

            val track = runCatching {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(sampleRate)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufBytes)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            }.getOrNull() ?: return@launch

            val pcm = ShortArray(bufferSamples)
            var lastOut = 0.0
            var phase = 0.0
            try {
                track.play()
                while (isActive) {
                    for (i in pcm.indices) {
                        val white = Random.nextDouble(-1.0, 1.0)
                        val sample = when (sound) {
                            AmbientSound.BROWN_NOISE -> {
                                lastOut = (lastOut + 0.02 * white) / 1.02
                                (lastOut * 2.4).coerceIn(-0.4, 0.4)
                            }
                            AmbientSound.RAIN -> {
                                lastOut = 0.72 * lastOut + 0.28 * white
                                (lastOut * 0.26).coerceIn(-0.35, 0.35)
                            }
                            AmbientSound.FOREST -> {
                                phase += 0.0004
                                val breeze = 0.5 + 0.5 * sin(phase)
                                lastOut = 0.85 * lastOut + 0.15 * white
                                (lastOut * (0.12 + 0.14 * breeze)).coerceIn(-0.3, 0.3)
                            }
                            AmbientSound.NONE -> 0.0
                        }
                        pcm[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                    }
                    track.write(pcm, 0, pcm.size)
                }
            } catch (_: Exception) {
            } finally {
                runCatching {
                    track.pause()
                    track.flush()
                    track.release()
                }
            }
        }
    }

    fun stop() {
        currentSound = AmbientSound.NONE
        playJob?.cancel()
        playJob = null
    }
}
