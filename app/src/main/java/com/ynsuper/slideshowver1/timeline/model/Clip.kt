package com.ynsuper.slideshowver1.timeline.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a media clip in the timeline
 * Based on ARCHITECTURE_CAPCUT_STYLE.md
 */
@Parcelize
data class Clip(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ClipType,
    val source: Uri,  // Uri to media file

    // Timeline
    var startTime: Long = 0L,  // Start time in timeline (ms)
    var duration: Long = 3000L,  // Duration (ms)

    // Transform (can be animated with keyframes)
    var transform: Transform = Transform(),

    // Speed control
    var speed: Float = 1.0f,  // 0.25x, 0.5x, 1x, 2x, 4x

    // Audio (for video clips)
    var volume: Float = 1.0f,
    var audioFadeIn: Long = 0L,
    var audioFadeOut: Long = 0L
) : Parcelable {
    /**
     * End time of the clip (computed property)
     */
    val endTime: Long
        get() = startTime + duration

    /**
     * Get the actual playback time for this clip at timeline time
     */
    fun getPlaybackTime(timelineTime: Long): Long {
        val timeInClip = timelineTime - startTime
        return (timeInClip * speed).toLong()
    }

    /**
     * Check if clip is active at given timeline time
     */
    fun isActiveAt(timelineTime: Long): Boolean {
        return timelineTime >= startTime && timelineTime < endTime
    }
}

enum class ClipType {
    VIDEO,
    IMAGE,
    AUDIO,
    STICKER,
    TEXT,
    EFFECT_OVERLAY
}

@Parcelize
data class Transform(
    var position: PointF = PointF(0.5f, 0.5f),  // normalized 0-1
    var scale: Float = 1.0f,
    var rotation: Float = 0f,
    var opacity: Float = 1.0f
) : Parcelable

@Parcelize
data class PointF(
    var x: Float = 0f,
    var y: Float = 0f
) : Parcelable

