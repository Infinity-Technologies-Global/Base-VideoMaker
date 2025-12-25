package com.ynsuper.slideshowver1.timeline.viewmodel

import android.app.Application
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ynsuper.slideshowver1.model.ImageModel
import com.ynsuper.slideshowver1.timeline.MediaSourceManager
import com.ynsuper.slideshowver1.timeline.TimelineController
import com.ynsuper.slideshowver1.timeline.model.Clip
import com.ynsuper.slideshowver1.timeline.model.ClipType
import com.ynsuper.slideshowver1.timeline.model.TrackType
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * ViewModel for ExoPlayer Timeline Demo
 * Based on MASTER_ARCHITECTURE.md
 */
class ExoPlayerTimelineViewModel(application: Application) : AndroidViewModel(application) {
    
    private val timelineController = TimelineController()
    private val mediaSourceManager = MediaSourceManager(application)
    
    val currentTime = MutableLiveData<Long>(0L)
    val totalDuration = MutableLiveData<Long>(0L)
    val isPlaying = MutableLiveData<Boolean>(false)
    val clips = MutableLiveData<List<Clip>>(emptyList())
    
    companion object {
        private const val TAG = "ExoPlayerTimelineVM"
    }
    
    fun getTimelineController(): TimelineController = timelineController
    
    fun getMediaSourceManager(): MediaSourceManager = mediaSourceManager
    
    /**
     * Load media items (images + videos) into timeline
     * Based on MASTER_ARCHITECTURE.md Phase 2
     * Uses ConcatenatingMediaSource approach (simpler)
     */
    fun loadMediaItems(mediaItems: List<ImageModel>) {
        if (mediaItems.isEmpty()) {
            Log.w(TAG, "No media items to load")
            return
        }
        
        Single.fromCallable {
            val newClips = mutableListOf<Clip>()
            var currentStartTime = 0L
            
            mediaItems.forEach { imageModel ->
                val clip = if (imageModel.isVideo) {
                    createVideoClip(imageModel.uriImage, currentStartTime)
                } else {
                    createImageClip(imageModel.uriImage, currentStartTime)
                }
                
                newClips.add(clip)
                currentStartTime += clip.duration
            }
            
            newClips
        }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            { newClips ->
                // Add clips to timeline
                newClips.forEach { clip ->
                    timelineController.addClip(clip, TrackType.VIDEO)
                }
                
                // Setup single ExoPlayer with ConcatenatingMediaSource
                mediaSourceManager.setupPlayer(newClips)
                
                timelineController.evaluateDuration()
                totalDuration.value = timelineController.totalDuration
                clips.value = timelineController.getAllClips()
                
                // Set initial time to 0
                currentTime.value = 0L
                
                Log.d(TAG, "Loaded ${newClips.size} clips, total duration: ${timelineController.totalDuration}ms")
            },
            { error ->
                Log.e(TAG, "Error loading media items", error)
            }
        )
    }
    
    private fun createVideoClip(uri: Uri, startTime: Long): Clip {
        val duration = mediaSourceManager.getVideoDuration(uri)
        return Clip(
            type = ClipType.VIDEO,
            source = uri,
            startTime = startTime,
            duration = duration,
            speed = 1.0f
        )
    }
    
    private fun createImageClip(uri: Uri, startTime: Long): Clip {
        val duration = mediaSourceManager.getImageDuration()
        return Clip(
            type = ClipType.IMAGE,
            source = uri,
            startTime = startTime,
            duration = duration,
            speed = 1.0f
        )
    }
    
    /**
     * Seek to specific time in timeline
     */
    fun seekTo(time: Long) {
        timelineController.seekTo(time)
        currentTime.value = timelineController.currentTime
        // Player seek will be handled in Activity
    }
    
    /**
     * Play/Pause toggle
     */
    fun togglePlayback() {
        isPlaying.value = !(isPlaying.value ?: false)
    }
    
    /**
     * Get current active clips
     */
    fun getCurrentClips(): Map<TrackType, List<Clip>> {
        return timelineController.getCurrentClips()
    }
    
    override fun onCleared() {
        super.onCleared()
        mediaSourceManager.releaseAll()
        Log.d(TAG, "ViewModel cleared, resources released")
    }
}

