package com.ynsuper.slideshowver1.timeline

import com.ynsuper.slideshowver1.timeline.model.Clip
import com.ynsuper.slideshowver1.timeline.model.Track
import com.ynsuper.slideshowver1.timeline.model.TrackType

/**
 * Timeline Controller - Manages multi-track timeline
 * Based on ARCHITECTURE_CAPCUT_STYLE.md
 */
class TimelineController {
    private val tracks = mutableMapOf<TrackType, Track>()
    
    var currentTime: Long = 0L
        private set
    var totalDuration: Long = 0L
        private set
    var playbackSpeed: Float = 1.0f
    
    init {
        // Initialize default tracks
        addTrack(TrackType.VIDEO)
        addTrack(TrackType.AUDIO)
        addTrack(TrackType.OVERLAY)
        addTrack(TrackType.TEXT)
    }
    
    fun addTrack(type: TrackType): Track {
        val track = Track(type)
        tracks[type] = track
        return track
    }
    
    fun getTrack(type: TrackType): Track? = tracks[type]
    
    fun addClip(clip: Clip, trackType: TrackType = TrackType.VIDEO) {
        val track = tracks[trackType] ?: addTrack(trackType)
        track.addClip(clip)
        evaluateDuration()
    }
    
    fun removeClip(clipId: String, trackType: TrackType) {
        tracks[trackType]?.removeClip(clipId)
        evaluateDuration()
    }
    
    fun getClipsAtTime(time: Long): Map<TrackType, List<Clip>> {
        return tracks.mapValues { (_, track) ->
            track.getClipsAtTime(time)
        }
    }
    
    fun getCurrentClips(): Map<TrackType, List<Clip>> {
        return getClipsAtTime(currentTime)
    }
    
    fun seekTo(time: Long) {
        currentTime = time.coerceIn(0, totalDuration)
    }
    
    fun seekBy(delta: Long) {
        seekTo(currentTime + delta)
    }
    
    fun evaluateDuration() {
        totalDuration = tracks.values.maxOfOrNull { it.getEndTime() } ?: 0L
    }
    
    fun getAllClips(): List<Clip> {
        return tracks.values.flatMap { it.clips }
    }
    
    fun getClipsInTrack(trackType: TrackType): List<Clip> {
        return tracks[trackType]?.clips ?: emptyList()
    }
    
    fun clear() {
        tracks.values.forEach { it.clips.clear() }
        currentTime = 0L
        totalDuration = 0L
    }
}

