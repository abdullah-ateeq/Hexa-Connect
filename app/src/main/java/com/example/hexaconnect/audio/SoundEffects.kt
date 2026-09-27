package com.example.hexaconnect.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

enum class Sound { PLACE, TURN, CONNECT, WIN }

/**
 * Restrained synthesized effects. Short sine tones are rendered to tiny WAV files in the cache
 * directory once and played through [SoundPool], so no audio assets ship with the app.
 */
class SoundEffects(context: Context) {

    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val soundIds = HashMap<Sound, Int>()
    private val loaded = HashSet<Int>()

    init {
        pool.setOnLoadCompleteListener { _, id, status -> if (status == 0) synchronized(loaded) { loaded.add(id) } }
        val dir = File(context.cacheDir, "sfx").apply { mkdirs() }
        Sound.entries.forEach { sound ->
            val file = File(dir, "${sound.name.lowercase()}_v1.wav")
            if (!file.exists()) file.writeBytes(wav(render(sound)))
            soundIds[sound] = pool.load(file.absolutePath, 1)
        }
    }

    fun play(sound: Sound) {
        val id = soundIds[sound] ?: return
        if (synchronized(loaded) { id in loaded }) pool.play(id, 1f, 1f, 1, 0, 1f)
    }

    fun release() = pool.release()

    private data class Note(val freq: Double, val start: Double, val length: Double, val gain: Double)

    private fun render(sound: Sound): FloatArray {
        val notes = when (sound) {
            Sound.PLACE -> listOf(Note(587.0, 0.0, 0.09, 0.32), Note(1174.0, 0.0, 0.05, 0.08))
            Sound.TURN -> listOf(Note(392.0, 0.0, 0.07, 0.12))
            Sound.CONNECT -> listOf(Note(523.25, 0.0, 0.16, 0.22), Note(783.99, 0.08, 0.22, 0.22))
            Sound.WIN -> listOf(
                Note(523.25, 0.0, 0.7, 0.16),
                Note(659.25, 0.06, 0.7, 0.14),
                Note(783.99, 0.12, 0.75, 0.14),
                Note(1046.5, 0.18, 0.6, 0.06),
            )
        }
        val total = notes.maxOf { it.start + it.length }
        val out = FloatArray((total * SAMPLE_RATE).toInt() + 1)
        for (note in notes) {
            val start = (note.start * SAMPLE_RATE).toInt()
            val count = (note.length * SAMPLE_RATE).toInt()
            for (i in 0 until count) {
                val t = i / SAMPLE_RATE.toDouble()
                val attack = min(1.0, t / 0.004)
                val decay = exp(-t * 5.0 / note.length)
                out[start + i] += (sin(2 * PI * note.freq * t) * attack * decay * note.gain).toFloat()
            }
        }
        return out
    }

    private fun wav(samples: FloatArray): ByteArray {
        val dataSize = samples.size * 2
        val buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray()).putInt(36 + dataSize).put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray()).putInt(16).putShort(1).putShort(1)
            .putInt(SAMPLE_RATE).putInt(SAMPLE_RATE * 2).putShort(2).putShort(16)
        buffer.put("data".toByteArray()).putInt(dataSize)
        samples.forEach { buffer.putShort((it.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()) }
        return buffer.array()
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
    }
}
