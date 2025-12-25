package com.ynsuper.slideshowver1.timeline

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ConcatenatingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.datasource.DefaultDataSource
import com.ynsuper.slideshowver1.timeline.model.Clip
import com.ynsuper.slideshowver1.timeline.model.ClipType

/**
 * Manages ExoPlayer with ConcatenatingMediaSource
 * Simpler approach: one player for all media (videos + images)
 * Based on user's suggestion
 */
class MediaSourceManager(private val context: Context) {
    private var player: ExoPlayer? = null
    private val dataSourceFactory = DefaultDataSource.Factory(context)
    private val clips = mutableListOf<Clip>()
    
    companion object {
        private const val TAG = "MediaSourceManager"
    }
    
    /**
     * Setup ExoPlayer with ConcatenatingMediaSource for VIDEO clips only
     * Images will be handled separately using ImageView
     */
    fun setupPlayer(clipList: List<Clip>): ExoPlayer {
        // Release existing player if any
        player?.release()
        
        clips.clear()
        clips.addAll(clipList)
        
        // Create ExoPlayer
        val newPlayer = ExoPlayer.Builder(context).build()
        
        // Create ConcatenatingMediaSource for videos only
        val concatenatingMediaSource = ConcatenatingMediaSource()
        
        clipList.forEach { clip ->
            if (clip.type == ClipType.VIDEO) {
                val mediaItem = MediaItem.fromUri(clip.source)
                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
                
                concatenatingMediaSource.addMediaSource(mediaSource)
                Log.d(TAG, "Added video source: ${clip.source}, duration: ${clip.duration}ms")
            }
        }
        
        if (concatenatingMediaSource.size > 0) {
            newPlayer.setMediaSource(concatenatingMediaSource)
            newPlayer.prepare()
        }
        newPlayer.playWhenReady = false // Manual control
        
        // Add listener
        newPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> Log.d(TAG, "Player READY, duration: ${newPlayer.duration}ms")
                    Player.STATE_BUFFERING -> Log.d(TAG, "Player BUFFERING")
                    Player.STATE_ENDED -> Log.d(TAG, "Player ENDED")
                }
            }
            
            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "Player error: ${error.message}", error)
            }
            
            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                Log.d(TAG, "Media item transition: $reason, position: ${newPlayer.currentPosition}ms")
            }
        })
        
        player = newPlayer
        Log.d(TAG, "Setup player with ${clipList.filter { it.type == ClipType.VIDEO }.size} video clips")
        return newPlayer
    }
    
    /**
     * Get the single ExoPlayer instance
     */
    fun getPlayer(): ExoPlayer? = player
    
    /**
     * Get clips list
     */
    fun getClips(): List<Clip> = clips
    
    /**
     * Get video duration
     */
    fun getVideoDuration(uri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            duration?.toLongOrNull() ?: 3000L
        } catch (e: Exception) {
            Log.e(TAG, "Error getting video duration", e)
            3000L
        } finally {
            retriever.release()
        }
    }
    
    /**
     * Get image duration (default 3 seconds)
     */
    fun getImageDuration(): Long = 3000L
    
    /**
     * Release all resources
     */
    fun releaseAll() {
        player?.release()
        player = null
        clips.clear()
        Log.d(TAG, "Released all resources")
    }
}

