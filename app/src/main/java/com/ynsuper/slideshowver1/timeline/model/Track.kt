package com.ynsuper.slideshowver1.timeline.model

/**
 * Represents a track in the timeline
 * Based on ARCHITECTURE_CAPCUT_STYLE.md
 */
data class Track(
    val type: TrackType,
    val clips: MutableList<Clip> = mutableListOf(),
    var locked: Boolean = false,
    var muted: Boolean = false
) {
    fun addClip(clip: Clip) {
        clips.add(clip)
        clips.sortBy { it.startTime }
    }
    
    fun removeClip(clipId: String) {
        clips.removeAll { it.id == clipId }
    }
    
    fun getClipsAtTime(time: Long): List<Clip> {
        return clips.filter { 
            time >= it.startTime && time < it.endTime 
        }
    }
    
    fun getEndTime(): Long {
        return clips.maxOfOrNull { it.endTime } ?: 0L
    }
    
    fun getClipAtTime(time: Long): Clip? {
        return clips.firstOrNull { 
            time >= it.startTime && time < it.endTime 
        }
    }
}

enum class TrackType {
    VIDEO,      // Video/Image clips
    OVERLAY,    // Stickers, frames, effects
    AUDIO,      // Background music, SFX
    TEXT        // Text layers
}

