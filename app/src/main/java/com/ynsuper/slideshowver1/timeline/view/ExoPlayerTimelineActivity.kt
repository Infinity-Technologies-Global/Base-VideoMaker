package com.ynsuper.slideshowver1.timeline.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.ynsuper.slideshowver1.R
import com.ynsuper.slideshowver1.callback.ImageGroupListener
import com.ynsuper.slideshowver1.model.ImageModel
import com.ynsuper.slideshowver1.timeline.model.Clip
import com.ynsuper.slideshowver1.timeline.model.ClipType
import com.ynsuper.slideshowver1.timeline.model.TrackType
import com.ynsuper.slideshowver1.timeline.viewmodel.ExoPlayerTimelineViewModel
import com.ynsuper.slideshowver1.util.Constant
import com.ynsuper.slideshowver1.view.custom_view.HorizontalThumbnailListView
import java.io.InputStream

/**
 * Demo Activity for ExoPlayer + HorizontalThumbnailListView
 * Based on MASTER_ARCHITECTURE.md and ARCHITECTURE_CAPCUT_STYLE.md
 */
class ExoPlayerTimelineActivity : AppCompatActivity() {
    
    private lateinit var viewModel: ExoPlayerTimelineViewModel
    private lateinit var thumbnailListView: HorizontalThumbnailListView
    private var player: ExoPlayer? = null
    private var playbackHandler: Handler? = null
    private var playbackRunnable: Runnable? = null
    private var currentDisplayClipId: String? = null
    private var isScrollingProgrammatically = false // Flag to prevent recursive sync
    private var wasPlayingBeforeTouch = false // Track if was playing before touch
    private var lastSyncTime = 0L // Throttle sync to avoid excessive updates
    private var isUserScrolling = false // Track if user is manually scrolling
    
    // Pinch-to-zoom gesture detector
    private var scaleGestureDetector: android.view.ScaleGestureDetector? = null
    private var isPinching = false
    private var zoomHandler: Handler? = Handler(Looper.getMainLooper())
    private var pendingZoomRunnable: Runnable? = null
    private var targetPixelsPerSecond: Float = DEFAULT_PIXELS_PER_SECOND
    
    companion object {
        private const val TAG = "ExoPlayerTimeline"
        private const val UPDATE_INTERVAL_MS = 16L // ~60fps
        private const val SEEK_THRESHOLD_MS = 50L // Reduced for smoother scrubbing (was 100L)
        private const val LARGE_SEEK_THRESHOLD_MS = 300L // Reduced for better sync (was 500L)
        
        // Thumbnail size (fixed, matches layout imageWidth/imageHeight)
        private const val THUMBNAIL_SIZE_DP = 80
        
        // Zoom levels
        private const val DEFAULT_PIXELS_PER_SECOND = 100f // 100px = 1 second (1x zoom)
        private const val MIN_PIXELS_PER_SECOND = 25f // 0.25x zoom (4s per 100px)
        private const val MAX_PIXELS_PER_SECOND = 1000f // 10x zoom (0.1s per 100px)
        private const val ZOOM_STEP = 1.5f // 50% zoom step
        
        private const val IMAGE_DURATION_SECONDS = 3 // Default image duration
    }
    
    // Convert DP to PX
    private val thumbnailSizePx: Int by lazy {
        (THUMBNAIL_SIZE_DP * resources.displayMetrics.density).toInt()
    }
    
    // Current zoom level
    private var pixelsPerSecond: Float = DEFAULT_PIXELS_PER_SECOND
    private var savedMediaItems: List<ImageModel> = emptyList()
    
    override fun dispatchTouchEvent(ev: android.view.MotionEvent): Boolean {
        // Intercept touch events on timeline area to pause/resume
        val scrollView = findViewById<android.widget.HorizontalScrollView>(R.id.scrollViewTimeline)
        
        if (scrollView != null) {
            val location = IntArray(2)
            scrollView.getLocationOnScreen(location)
            val x = ev.rawX.toInt()
            val y = ev.rawY.toInt()
            
            // Check if touch is within timeline area
            val isInTimelineArea = x >= location[0] && x <= location[0] + scrollView.width &&
                    y >= location[1] && y <= location[1] + scrollView.height
            
            if (isInTimelineArea) {
                Log.d(TAG, "dispatchTouchEvent: action=${ev.action}, isInTimelineArea=true, wasPlayingBeforeTouch=$wasPlayingBeforeTouch")
                
                when (ev.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        Log.d(TAG, "dispatchTouchEvent: ACTION_DOWN on timeline, current isPlaying: ${viewModel.isPlaying.value}")
                        isUserScrolling = true // Start tracking user scroll
                        wasPlayingBeforeTouch = viewModel.isPlaying.value == true
                        Log.d(TAG, "dispatchTouchEvent: wasPlayingBeforeTouch set to: $wasPlayingBeforeTouch, isUserScrolling=true")
                        if (wasPlayingBeforeTouch) {
                            // Stop timeline progress handler first
                            playbackRunnable?.let { playbackHandler?.removeCallbacks(it) }
                            playbackRunnable = null
                            
                            // Stop player immediately
                            player?.playWhenReady = false
                            player?.pause()
                            
                            // Update ViewModel state
                            viewModel.isPlaying.value = false
                            
                            Log.d(TAG, "Playback paused via dispatchTouchEvent, wasPlayingBeforeTouch: $wasPlayingBeforeTouch")
                        } else {
                            Log.d(TAG, "Not pausing because wasPlayingBeforeTouch is false")
                        }
                    }
                    android.view.MotionEvent.ACTION_UP, 
                    android.view.MotionEvent.ACTION_CANCEL -> {
                        Log.d(TAG, "dispatchTouchEvent: ACTION_UP/CANCEL on timeline, wasPlayingBeforeTouch: $wasPlayingBeforeTouch, isPlaying: ${viewModel.isPlaying.value}")
                        isUserScrolling = false // Stop tracking user scroll
                        
                        // Resume if was playing before touch
                        if (wasPlayingBeforeTouch) {
                            Log.d(TAG, "Resuming playback...")
                            // Set isPlaying to true FIRST
                            viewModel.isPlaying.value = true
                            Log.d(TAG, "Set isPlaying to true before sync")
                            
                            // Sync preview with current scroll position (this will call updateDisplay)
                            syncPreviewWithScrollPosition(scrollView)
                            
                            // Use Handler to ensure sync completes before starting playback
                            Handler(Looper.getMainLooper()).postDelayed({
                                Log.d(TAG, "Starting playback after sync, isPlaying: ${viewModel.isPlaying.value}")
                                
                                // Double check isPlaying is still true
                                if (viewModel.isPlaying.value == true) {
                                    startPlayback()
                                    
                                    // Force playWhenReady after a short delay to ensure it's set
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        val currentClips = viewModel.getCurrentClips()[TrackType.VIDEO] ?: emptyList()
                                        val currentClip = currentClips.firstOrNull()
                                        if (currentClip?.type == ClipType.VIDEO && viewModel.isPlaying.value == true) {
                                            if (player?.playWhenReady != true) {
                                                Log.w(TAG, "Force setting playWhenReady to true after resume")
                                                player?.playWhenReady = true
                                            }
                                            Log.d(TAG, "Final check - playWhenReady: ${player?.playWhenReady}, isPlaying: ${viewModel.isPlaying.value}")
                                        }
                                    }, 200)
                                    
                                    Log.d(TAG, "Playback started successfully at ${formatTime(viewModel.currentTime.value ?: 0L)}")
                                } else {
                                    Log.w(TAG, "isPlaying was set to false, not starting playback")
                                }
                            }, 150) // Increased delay to ensure sync completes
                        } else {
                            Log.d(TAG, "Not resuming because wasPlayingBeforeTouch is false")
                            // Just sync if wasn't playing
                            syncPreviewWithScrollPosition(scrollView)
                        }
                    }
                }
            } else {
                // Log when touch is outside timeline area for debugging
                if (ev.action == android.view.MotionEvent.ACTION_DOWN) {
                    Log.d(TAG, "dispatchTouchEvent: ACTION_DOWN outside timeline area (x=$x, y=$y, timeline: ${location[0]}-${location[0] + scrollView.width}, ${location[1]}-${location[1] + scrollView.height})")
                }
            }
        }
        
        // Continue normal event dispatch
        return super.dispatchTouchEvent(ev)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exoplayer_timeline)
        
        // Get media items from intent
        val mediaItems = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(Constant.EXTRA_ARRAY_IMAGE, ImageModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra<ImageModel>(Constant.EXTRA_ARRAY_IMAGE)
        }
        
        if (mediaItems == null || mediaItems.isEmpty()) {
            Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        Log.d(TAG, "Received ${mediaItems.size} media items")
        
        // Save media items for zoom reload
        savedMediaItems = mediaItems
        
        // Setup ViewModel
        viewModel = ViewModelProvider(this, AndroidViewModelFactory.getInstance(application))[ExoPlayerTimelineViewModel::class.java]
        
        // Find views
        thumbnailListView = findViewById(R.id.horizontalThumbnailListView)
        val scrollView = findViewById<android.widget.HorizontalScrollView>(R.id.scrollViewTimeline)
        val playerView = findViewById<androidx.media3.ui.PlayerView>(R.id.playerView)
        val btnPlayPause = findViewById<android.widget.Button>(R.id.btnPlayPause)
        val tvTime = findViewById<android.widget.TextView>(R.id.tvTime)
        
        // Touch events are handled by dispatchTouchEvent() in Activity
        // No need for additional touch listeners to avoid conflicts
        
        // Setup pinch-to-zoom gesture detector
        setupPinchToZoom(scrollView)
        
        // Listen to scroll changes to sync preview with redline position (REALTIME - OPTIMIZED)
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            // Skip if programmatically scrolling (avoid recursive loop)
            if (isScrollingProgrammatically) {
                return@addOnScrollChangedListener
            }
            
            // Realtime sync when user is manually scrolling
            if (isUserScrolling) {
                // ULTRA SMOOTH: No throttling, direct sync
                // Performance optimized in syncPreviewWithScrollPosition
                syncPreviewWithScrollPosition(scrollView)
            } else if (viewModel.isPlaying.value != true) {
                // Also sync when not playing and not programmatic scroll
                syncPreviewWithScrollPosition(scrollView)
            }
        }
        
        // Setup HorizontalThumbnailListView
        setupThumbnailListView(mediaItems)
        
        // Setup controls
        btnPlayPause.setOnClickListener {
            viewModel.togglePlayback()
            if (viewModel.isPlaying.value == true) {
                startPlayback()
            } else {
                stopPlayback()
            }
        }
        
        // Observe ViewModel
        viewModel.currentTime.observe(this) { time ->
            thumbnailListView.invalidate()
            val totalDuration = viewModel.totalDuration.value ?: 0L
            tvTime.text = "${formatTime(time)} / ${formatTime(totalDuration)}"
            
            // Only update display when clip changes (not every frame)
            // ExoPlayer will handle smooth playback automatically
            val currentClips = viewModel.getCurrentClips()[TrackType.VIDEO] ?: emptyList()
            val currentClip = currentClips.firstOrNull()
            if (currentClip?.id != currentDisplayClipId) {
                Log.d(TAG, "Clip changed from ${currentDisplayClipId} to ${currentClip?.id}")
                updateDisplay()
            }
            
            // Update timeline scroll position (only when playing)
            if (viewModel.isPlaying.value == true) {
                updateTimelineScroll(time, totalDuration)
            }
        }
        
        viewModel.totalDuration.observe(this) { duration ->
            thumbnailListView.invalidate()
            val currentTime = viewModel.currentTime.value ?: 0L
            tvTime.text = "${formatTime(currentTime)} / ${formatTime(duration)}"
        }
        
        viewModel.clips.observe(this) { clips ->
            Log.d(TAG, "Clips updated: ${clips.size}")
            if (clips.isNotEmpty()) {
                setupPlayer()
            }
        }
        
        // Load media items
        viewModel.loadMediaItems(mediaItems)
    }
    
    private fun setupThumbnailListView(mediaItems: List<ImageModel>) {
        thumbnailListView.clear()
        
        // Set padding
        val screenWidth = resources.displayMetrics.widthPixels
        thumbnailListView.setStartPaddingWidth(screenWidth / 2)
        thumbnailListView.setEndPaddingWidth(screenWidth / 2)
        thumbnailListView.setGroupPaddingWidth(10)
        thumbnailListView.setPaddingVerticalHeight(10)
        
        // Set dummy composer and littleBox to avoid NullPointerException
        // HorizontalThumbnailListView expects these but we're using ExoPlayer instead
        try {
            val dummySurfaceTexture = android.graphics.SurfaceTexture(0)
            val dummyLittleBox = com.ynsuper.slideshowver1.view.LittleBox(
                this,
                dummySurfaceTexture,
                720,
                1280
            )
            val dummyComposer = com.ynsuper.slideshowver1.util.VideoComposer(this)
            thumbnailListView.setLittleBox(dummyLittleBox, dummyComposer)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting dummy composer/littleBox", e)
        }
        
        // Setup listener
        thumbnailListView.setImageGroupListener(object : ImageGroupListener() {
            override fun onImageGroupClicked(index: Int) {
                Log.d(TAG, "Image group clicked: $index")
            }
            
            override fun onImageGroupStart(index: Int, fromUser: Boolean) {
                Log.d(TAG, "Image group start: $index, fromUser: $fromUser")
            }
            
            override fun onImageGroupEnd(index: Int, fromUser: Boolean) {
                Log.d(TAG, "Image group end: $index, fromUser: $fromUser")
                // Auto advance to next clip
                if (!fromUser && viewModel.isPlaying.value == true) {
                    val allClips = viewModel.getTimelineController().getAllClips()
                    if (index < allClips.size - 1) {
                        val nextClip = allClips[index + 1]
                        viewModel.seekTo(nextClip.startTime)
                    }
                }
            }
            
            override fun onImageGroupProcess(index: Int, progress: Float, fromUser: Boolean) {
                // This callback is handled by scrollView listener now
                // Keep it for compatibility but don't duplicate logic
            }
            
            override fun onImageGroupSplit(index: Int, progress: Float) {
                Log.d(TAG, "Image group split: $index, progress: $progress")
            }
            
            override fun onImageGroupHidden(index: Int) {
                Log.d(TAG, "Image group hidden: $index")
            }
            
            override fun onImageGroupLeftExpand(index: Int, progress: Float, fromUser: Boolean) {
                Log.d(TAG, "Image group left expand: $index")
            }
            
            override fun onImageGroupLeftShrink(index: Int, progress: Float, fromUser: Boolean) {
                Log.d(TAG, "Image group left shrink: $index")
            }
            
            override fun onImageGroupRightExpand(index: Int, progress: Float, fromUser: Boolean) {
                Log.d(TAG, "Image group right expand: $index")
            }
            
            override fun onImageGroupRightShrink(index: Int, progress: Float, fromUser: Boolean) {
                Log.d(TAG, "Image group right shrink: $index")
            }
        })
        
        // Calculate how many thumbnails per second based on zoom level
        // thumbnailsPerSecond = pixelsPerSecond / thumbnailSizePx
        // Example: 100px/s / 80px = 1.25 thumbnails/s ≈ 1 thumbnail per second
        // Example: 200px/s / 80px = 2.5 thumbnails/s ≈ 2-3 thumbnails per second
        val thumbnailsPerSecond = pixelsPerSecond / thumbnailSizePx
        
        Log.d(TAG, "setupThumbnailListView: zoom=${pixelsPerSecond}px/s, thumbnailSize=${thumbnailSizePx}px, thumbnailsPerSecond=$thumbnailsPerSecond")
        
        // Load thumbnails from media items
        var currentTimeMs = 0L // Track time in milliseconds
        mediaItems.forEachIndexed { index, item ->
            if (item.isVideo) {
                // Video: extract multiple frames based on zoom
                val videoImageItems = loadVideoThumbnails(item, currentTimeMs, thumbnailsPerSecond)
                if (videoImageItems.isNotEmpty()) {
                    thumbnailListView.newImageGroup(videoImageItems, "")
                    // Duration in ms
                    val durationMs = viewModel.getMediaSourceManager().getVideoDuration(item.uriImage)
                    currentTimeMs += durationMs
                }
            } else {
                // Image: replicate thumbnail based on zoom
                val bitmap = loadThumbnail(item)
                if (bitmap != null) {
                    val imageDurationMs = IMAGE_DURATION_SECONDS * 1000L
                    val totalThumbnails = (IMAGE_DURATION_SECONDS * thumbnailsPerSecond).toInt().coerceAtLeast(1)
                    val imageItems = mutableListOf<HorizontalThumbnailListView.ImageItem>()
                    
                    // Create thumbnails evenly distributed across duration
                    for (i in 0 until totalThumbnails) {
                        val timeInSeconds = (currentTimeMs / 1000f + (i.toFloat() / thumbnailsPerSecond)).toInt()
                        val imageItem = HorizontalThumbnailListView.ImageItem(
                            bitmap,
                            thumbnailSizePx, // Fixed size (square)
                            0,
                            thumbnailSizePx,
                            timeInSeconds
                        )
                        imageItems.add(imageItem)
                    }
                    
                    thumbnailListView.newImageGroup(imageItems, "")
                    currentTimeMs += imageDurationMs
                    Log.d(TAG, "Loaded ${imageItems.size} image thumbnails for ${IMAGE_DURATION_SECONDS}s (thumbnailsPerSecond=$thumbnailsPerSecond)")
                }
            }
        }
    }
    
    /**
     * Setup pinch-to-zoom gesture for timeline
     * Strategy 1: Instant visual feedback via scale transform
     * Strategy 2: Debounced reload for quality
     */
    private fun setupPinchToZoom(scrollView: android.widget.HorizontalScrollView) {
        scaleGestureDetector = android.view.ScaleGestureDetector(this, object : android.view.ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private var initialPixelsPerSecond = DEFAULT_PIXELS_PER_SECOND
            private var accumulatedScaleFactor = 1.0f
            
            override fun onScaleBegin(detector: android.view.ScaleGestureDetector): Boolean {
                // Cancel any pending reload
                pendingZoomRunnable?.let { zoomHandler?.removeCallbacks(it) }
                
                initialPixelsPerSecond = pixelsPerSecond
                accumulatedScaleFactor = 1.0f
                isPinching = true
                
                val zoomPercent = ((pixelsPerSecond / DEFAULT_PIXELS_PER_SECOND) * 100).toInt()
                Log.d(TAG, "Pinch zoom started, current: ${zoomPercent}%")
                return true
            }
            
            override fun onScale(detector: android.view.ScaleGestureDetector): Boolean {
                // Accumulate scale factor
                accumulatedScaleFactor *= detector.scaleFactor
                
                // Calculate target scale (clamped to limits)
                targetPixelsPerSecond = (initialPixelsPerSecond * accumulatedScaleFactor)
                    .coerceIn(MIN_PIXELS_PER_SECOND, MAX_PIXELS_PER_SECOND)
                
                // STRATEGY 1: Instant visual feedback via scale transform
                // Scale the thumbnailListView directly (no reload, instant!)
                val visualScale = targetPixelsPerSecond / pixelsPerSecond
                thumbnailListView.scaleX = visualScale
                
                // Keep content centered by adjusting pivot
                thumbnailListView.pivotX = (scrollView.scrollX + scrollView.width / 2f) - thumbnailListView.left
                
                val zoomPercent = ((targetPixelsPerSecond / DEFAULT_PIXELS_PER_SECOND) * 100).toInt()
                Log.v(TAG, "Pinch scaling: ${zoomPercent}%, visualScale=$visualScale")
                
                return true
            }
            
            override fun onScaleEnd(detector: android.view.ScaleGestureDetector) {
                isPinching = false
                
                // Reset visual scale
                thumbnailListView.scaleX = 1.0f
                
                val zoomPercent = ((targetPixelsPerSecond / DEFAULT_PIXELS_PER_SECOND) * 100).toInt()
                Log.d(TAG, "Pinch zoom ended, target: ${zoomPercent}%")
                
                // STRATEGY 2: Debounced reload for quality
                // Only reload if scale changed significantly
                if (kotlin.math.abs(targetPixelsPerSecond - pixelsPerSecond) > 10f) {
                    // Cancel any pending reload
                    pendingZoomRunnable?.let { zoomHandler?.removeCallbacks(it) }
                    
                    // Schedule reload after 300ms (debounce)
                    pendingZoomRunnable = Runnable {
                        // Save current time position
                        val currentTime = viewModel.currentTime.value ?: 0L
                        
                        // Update actual zoom level
                        pixelsPerSecond = targetPixelsPerSecond
                        
                        // Reload timeline with new quality
                        reloadTimeline(currentTime)
                        
                        Log.d(TAG, "Zoom applied: ${zoomPercent}%, pixelsPerSecond=$pixelsPerSecond")
                    }
                    zoomHandler?.postDelayed(pendingZoomRunnable!!, 300)
                }
            }
        })
        
        // Attach gesture detector to scrollView
        scrollView.setOnTouchListener { view, event ->
            scaleGestureDetector?.onTouchEvent(event)
            
            // If pinching, consume the event
            // Otherwise, let scrollView handle it
            if (isPinching) {
                true
            } else {
                view.onTouchEvent(event)
            }
        }
    }
    
    private fun reloadTimeline(maintainTime: Long) {
        Log.d(TAG, "reloadTimeline: maintainTime=${maintainTime}ms, zoom=${pixelsPerSecond}px/s")
        
        // Pause playback during reload
        val wasPlaying = viewModel.isPlaying.value == true
        if (wasPlaying) {
            stopPlayback()
        }
        
        // Clear existing timeline
        thumbnailListView.clear()
        
        // Reload with new scale
        setupThumbnailListView(savedMediaItems)
        
        // Re-setup player to ensure it's properly attached
        Log.d(TAG, "reloadTimeline: Re-setting up player")
        setupPlayer()
        
        // Wait for layout to complete before scrolling
        thumbnailListView.post {
            // Force layout update to get correct width
            thumbnailListView.requestLayout()
            thumbnailListView.invalidate()
            
            // Restore time position
            viewModel.seekTo(maintainTime)
            
            // Wait for layout pass to complete
            thumbnailListView.viewTreeObserver.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // Remove listener to avoid multiple calls
                    thumbnailListView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    
                    Log.d(TAG, "Timeline layout complete, width=${thumbnailListView.width}")
                    
                    // Wait a bit more for player to be ready
                    Handler(Looper.getMainLooper()).postDelayed({
                        // Force update display to refresh player
                        updateDisplayForced()
                        
                        // Update scroll position
                        val totalDuration = viewModel.totalDuration.value ?: 0L
                        updateTimelineScroll(maintainTime, totalDuration)
                        
                        Log.d(TAG, "Timeline reloaded: thumbnails loaded, scroll updated, time restored to ${formatTime(maintainTime)}")
                        
                        // Resume playback if was playing
                        if (wasPlaying) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                viewModel.isPlaying.value = true
                                startPlayback()
                            }, 150)
                        }
                    }, 100)
                }
            })
        }
    }
    
    private fun loadVideoThumbnails(
        item: ImageModel, 
        currentTimeMs: Long, 
        thumbnailsPerSecond: Float
    ): List<HorizontalThumbnailListView.ImageItem> {
        val imageItems = mutableListOf<HorizontalThumbnailListView.ImageItem>()
        
        return try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(this, item.uriImage)
            
            // Get video duration
            val durationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 0L
            val durationSeconds = durationMs / 1000f
            
            // Calculate frame interval in seconds
            // thumbnailsPerSecond = 1.25 → frameInterval = 0.8s (1 frame every 0.8s)
            // thumbnailsPerSecond = 2.5 → frameInterval = 0.4s (1 frame every 0.4s)
            val frameIntervalSeconds = 1.0f / thumbnailsPerSecond.coerceAtLeast(0.1f) // Minimum 0.1 to avoid division by zero
            
            Log.d(TAG, "Loading video thumbnails: duration=${durationSeconds}s, thumbnailsPerSecond=$thumbnailsPerSecond, frameInterval=${frameIntervalSeconds}s")
            
            // Extract frames at regular intervals
            var currentSecond = 0f
            var frameIndex = 0
            while (currentSecond < durationSeconds) {
                val timeUs = (currentSecond * 1000000).toLong() // Convert to microseconds
                val bitmap = retriever.getFrameAtTime(timeUs, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                
                if (bitmap != null) {
                    val timeInSeconds = ((currentTimeMs + currentSecond * 1000) / 1000).toInt()
                    val imageItem = HorizontalThumbnailListView.ImageItem(
                        bitmap,
                        thumbnailSizePx, // Fixed size (square)
                        0,
                        thumbnailSizePx,
                        timeInSeconds
                    )
                    imageItems.add(imageItem)
                    frameIndex++
                } else {
                    Log.w(TAG, "Failed to extract frame at ${currentSecond}s")
                }
                
                currentSecond += frameIntervalSeconds
            }
            
            retriever.release()
            Log.d(TAG, "Loaded ${imageItems.size} video thumbnails (fixed size: ${thumbnailSizePx}px)")
            imageItems
        } catch (e: Exception) {
            Log.e(TAG, "Error loading video thumbnails: ${e.message}", e)
            emptyList()
        }
    }
    
    private fun loadThumbnail(item: ImageModel): Bitmap? {
        return try {
            if (item.isVideo) {
                // Extract frame from video using MediaMetadataRetriever
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(this, item.uriImage)
                val bitmap = retriever.getFrameAtTime(0) // Get first frame
                retriever.release()
                
                if (bitmap != null) {
                    Log.d(TAG, "Loaded video thumbnail: ${item.uriImage}")
                } else {
                    Log.e(TAG, "Failed to extract frame from video: ${item.uriImage}")
                }
                bitmap
            } else {
                // Load image directly
                val inputStream = contentResolver.openInputStream(item.uriImage)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                if (bitmap != null) {
                    Log.d(TAG, "Loaded image thumbnail: ${item.uriImage}")
                } else {
                    Log.e(TAG, "Failed to load image: ${item.uriImage}")
                }
                bitmap
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading thumbnail for ${item.uriImage}: ${e.message}", e)
            null
        }
    }
    
    private fun updateTimelineScroll(currentTime: Long, totalDuration: Long) {
        if (totalDuration <= 0) return
        
        val scrollView = findViewById<android.widget.HorizontalScrollView>(R.id.scrollViewTimeline)
        val screenWidth = resources.displayMetrics.widthPixels
        
        // Calculate scroll position to keep playhead at center
        val currentTimeSeconds = currentTime / 1000f
        val paddingStart = thumbnailListView.getPaddingStartWidth()
        val scrollX = (currentTimeSeconds * pixelsPerSecond - screenWidth / 2 + paddingStart).toInt().coerceAtLeast(0)
        
        // Log timeline dimensions for debugging
        val timelineWidth = thumbnailListView.width
        val expectedWidth = (totalDuration / 1000f * pixelsPerSecond + 2 * paddingStart).toInt()
        Log.d(TAG, "updateTimelineScroll: time=${formatTime(currentTime)}, zoom=${pixelsPerSecond}px/s, scrollX=$scrollX, timelineActualWidth=$timelineWidth, expectedWidth=$expectedWidth")
        
        // Check if scroll position actually changed to avoid unnecessary updates
        if (kotlin.math.abs(scrollView.scrollX - scrollX) < 5) {
            return // Already at correct position
        }
        
        // Set flag to prevent recursive sync
        isScrollingProgrammatically = true
        scrollView.post {
            scrollView.scrollTo(scrollX, 0)
            // Reset flag after scroll completes
            scrollView.postDelayed({
                isScrollingProgrammatically = false
            }, 50) // Small delay to ensure scroll event is processed
        }
    }
    
    private var lastSyncedTime = 0L // Track last synced time to avoid redundant updates
    
    /**
     * Sync preview with scroll position (when user manually scrolls)
     * Redline is at center of screen, calculate time from scroll position
     * ULTRA OPTIMIZED for smooth realtime scrubbing
     */
    private fun syncPreviewWithScrollPosition(scrollView: android.widget.HorizontalScrollView) {
        val screenWidth = resources.displayMetrics.widthPixels
        val scrollX = scrollView.scrollX
        
        // Account for padding start width (thumbnailListView has padding)
        val paddingStart = thumbnailListView.getPaddingStartWidth()
        
        // Redline is at center of screen
        // Time position = (scrollX + screenWidth/2 - paddingStart) / pixelsPerSecond
        val pixelPosition = scrollX + screenWidth / 2 - paddingStart
        
        // Convert pixels to time (seconds)
        val timeSeconds = pixelPosition / pixelsPerSecond
        val timeMs = (timeSeconds * 1000).toLong()
        
        // Update ViewModel time
        val totalDuration = viewModel.totalDuration.value ?: 0L
        val clampedTime = timeMs.coerceIn(0L, totalDuration)
        
        // Ultra smooth: Allow smaller time changes (10ms instead of 50ms)
        val timeDiff = kotlin.math.abs(clampedTime - lastSyncedTime)
        if (timeDiff < 10) { // Skip only very tiny changes
            return
        }
        
        // Update time
        viewModel.seekTo(clampedTime)
        lastSyncedTime = clampedTime
        
        // Get current clip to check if changed (cached for performance)
        val currentClips = viewModel.getCurrentClips()[TrackType.VIDEO] ?: emptyList()
        val currentClip = currentClips.firstOrNull()
        
        // Smart update strategy:
        // 1. Full update if clip changed (need to switch image/video)
        // 2. Light update if same clip (just seek)
        val clipChanged = currentClip?.id != currentDisplayClipId
        
        if (clipChanged) {
            // Clip changed: need full update
            updateDisplayForced()
        } else {
            // Same clip: ultra-light update (direct seek only)
            updateDisplayLightweight(currentClip)
        }
    }
    
    /**
     * Ultra lightweight display update for same clip during scrubbing
     * Skips visibility checks and only seeks player/updates image
     */
    private fun updateDisplayLightweight(currentClip: Clip?) {
        if (currentClip == null) return
        
        when (currentClip.type) {
            ClipType.IMAGE -> {
                // Image already displayed, no need to update
                // (Images don't need seeking)
            }
            ClipType.VIDEO -> {
                // Just seek player without full update
                if (player != null && player?.playbackState == Player.STATE_READY) {
                    val allClips = viewModel.getMediaSourceManager().getClips()
                    val videoClips = allClips.filter { it.type == ClipType.VIDEO }
                    
                    var absolutePosition = 0L
                    for (clip in videoClips) {
                        if (clip.id == currentClip.id) {
                            val playbackTime = currentClip.getPlaybackTime(viewModel.currentTime.value ?: 0L)
                            absolutePosition += playbackTime
                            break
                        } else {
                            absolutePosition += clip.duration
                        }
                    }
                    
                    // Direct seek without logging (for smoothness)
                    player?.seekTo(absolutePosition)
                }
            }
            else -> {}
        }
    }
    
    private fun startPlayback() {
        // Check if should play
        if (viewModel.isPlaying.value != true) {
            Log.w(TAG, "startPlayback called but isPlaying is false, skipping")
            wasPlayingBeforeTouch = false
            return
        }
        
        Log.d(TAG, "startPlayback: Starting playback handler and player")
        stopPlayback()
        
        playbackHandler = Handler(Looper.getMainLooper())
        playbackRunnable = object : Runnable {
            override fun run() {
                val currentTime = viewModel.currentTime.value ?: 0L
                val totalDuration = viewModel.totalDuration.value ?: 0L
                
                if (currentTime < totalDuration && viewModel.isPlaying.value == true) {
                    viewModel.seekTo(currentTime + UPDATE_INTERVAL_MS)
                    playbackHandler?.postDelayed(this, UPDATE_INTERVAL_MS)
                } else {
                    viewModel.isPlaying.value = false
                    stopPlayback()
                }
            }
        }
        
        playbackHandler?.post(playbackRunnable!!)
        
        // Start playing video if current clip is video
        val currentClips = viewModel.getCurrentClips()[TrackType.VIDEO] ?: emptyList()
        val currentClip = currentClips.firstOrNull()
        
        if (currentClip?.type == ClipType.VIDEO) {
            // Force playWhenReady to true
            player?.playWhenReady = true
            Log.d(TAG, "startPlayback: Set player.playWhenReady = true for video clip")
            Log.d(TAG, "startPlayback: Player state - playWhenReady: ${player?.playWhenReady}, playbackState: ${player?.playbackState}, currentTime: ${formatTime(viewModel.currentTime.value ?: 0L)}")
            
            // Double check after a short delay
            Handler(Looper.getMainLooper()).postDelayed({
                if (viewModel.isPlaying.value == true && player?.playWhenReady != true) {
                    Log.w(TAG, "startPlayback: playWhenReady was reset, setting again")
                    player?.playWhenReady = true
                }
                Log.d(TAG, "startPlayback: After delay - playWhenReady: ${player?.playWhenReady}, isPlaying: ${viewModel.isPlaying.value}")
            }, 150)
        } else {
            Log.d(TAG, "startPlayback: Current clip is not video, timeline will progress but no video playback")
        }
        
        // Reset flag after starting playback
        wasPlayingBeforeTouch = false
    }
    
    private fun stopPlayback() {
        playbackRunnable?.let { playbackHandler?.removeCallbacks(it) }
        playbackRunnable = null
        player?.playWhenReady = false
        player?.pause() // Explicitly pause player
        Log.d(TAG, "Playback stopped, player.playWhenReady: ${player?.playWhenReady}")
    }
    
    private var playerListener: Player.Listener? = null
    
    private fun setupPlayer() {
        Log.d(TAG, "setupPlayer: start")
        val playerView = findViewById<androidx.media3.ui.PlayerView>(R.id.playerView)
        
        // Remove old listener if exists
        playerListener?.let { 
            player?.removeListener(it)
            Log.d(TAG, "setupPlayer: removed old listener")
        }
        
        // Get player from MediaSourceManager
        player = viewModel.getMediaSourceManager().getPlayer()
        if (player == null) {
            Log.e(TAG, "setupPlayer: Player is null from MediaSourceManager")
            return
        }
        
        Log.d(TAG, "setupPlayer: Player obtained, duration=${player?.duration}ms, state=${player?.playbackState}")
        
        // Clear old player from view first
        playerView.player = null
        
        // Force invalidate to ensure surface is recreated
        playerView.invalidate()
        
        // Attach player to view
        playerView.player = player
        
        // Force layout update
        playerView.requestLayout()
        
        Log.d(TAG, "setupPlayer: Player attached, forcing layout update")
        
        // Create and add new listener
        playerListener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val stateStr = when (playbackState) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN"
                }
                val videoSize = player?.videoSize
                Log.d(TAG, "Player state: $stateStr, duration: ${player?.duration}ms, videoSize: ${videoSize?.width}x${videoSize?.height}, surfaceSize: ${playerView.width}x${playerView.height}")
                
                when (playbackState) {
                    Player.STATE_READY -> {
                        // Force playerView to show
                        if (playerView.visibility != View.VISIBLE) {
                            Log.w(TAG, "PlayerView not visible when READY, showing it")
                            playerView.visibility = View.VISIBLE
                            playerView.invalidate()
                        }
                        
                        // Ensure playWhenReady is set correctly when ready
                        if (viewModel.isPlaying.value == true && player?.playWhenReady != true) {
                            Log.w(TAG, "Player READY but playWhenReady is false, setting to true")
                            player?.playWhenReady = true
                        }
                    }
                    Player.STATE_ENDED -> {
                        Log.d(TAG, "Player ENDED")
                        viewModel.isPlaying.value = false
                    }
                }
            }
            
            override fun onRenderedFirstFrame() {
                Log.d(TAG, "onRenderedFirstFrame: First frame rendered! Video should be visible now.")
            }
            
            override fun onSurfaceSizeChanged(width: Int, height: Int) {
                Log.d(TAG, "onSurfaceSizeChanged: ${width}x${height}")
            }
            
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Log.d(TAG, "Player onIsPlayingChanged: $isPlaying, playWhenReady: ${player?.playWhenReady}, viewModel.isPlaying: ${viewModel.isPlaying.value}")
            }
            
            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "Player error: ${error.message}", error)
                error.printStackTrace()
            }
            
            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                Log.d(TAG, "Video size changed: ${videoSize.width}x${videoSize.height}, PlayerView size: ${playerView.width}x${playerView.height}")
            }
        }
        
        player?.addListener(playerListener!!)
        
        Log.d(TAG, "setupPlayer: Player attached to PlayerView with new listener")
    }
    
    private fun updateDisplayForced() {
        updateDisplay(forceSync = true)
    }
    
    private fun updateDisplay(forceSync: Boolean = false) {
        val playerView = findViewById<androidx.media3.ui.PlayerView>(R.id.playerView)
        val imageView = findViewById<ImageView>(R.id.imageView)
        
        val currentTime = viewModel.currentTime.value ?: 0L
        val currentClips = viewModel.getCurrentClips()[TrackType.VIDEO] ?: emptyList()
        val currentClip = currentClips.firstOrNull()
        
        Log.d(TAG, "updateDisplay: currentTime=${formatTime(currentTime)}, currentClip=${currentClip?.id}, type=${currentClip?.type}, forceSync=$forceSync")
        
        if (currentClip == null) {
            Log.w(TAG, "updateDisplay: No current clip found, hiding views")
            playerView.visibility = View.GONE
            imageView.visibility = View.GONE
            currentDisplayClipId = null
            player?.pause()
            return
        }
        
        currentDisplayClipId = currentClip.id
        Log.d(TAG, "updateDisplay: Displaying clip ${currentClip.id}, type=${currentClip.type}, startTime=${currentClip.startTime}, duration=${currentClip.duration}")
        
        when (currentClip.type) {
            ClipType.IMAGE -> {
                playerView.visibility = View.GONE
                imageView.visibility = View.VISIBLE
                player?.pause()
                
                try {
                    Glide.with(this)
                        .load(currentClip.source)
                        .centerCrop()
                        .into(imageView)
                    Log.d(TAG, "Displaying image: ${currentClip.source}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading image", e)
                }
            }
            ClipType.VIDEO -> {
                imageView.visibility = View.GONE
                
                // Ensure player is set up BEFORE showing playerView
                if (player == null) {
                    Log.w(TAG, "Player is null, setting up player")
                    setupPlayer()
                }
                
                if (player == null) {
                    Log.e(TAG, "Player is still null after setup, cannot display video")
                    return
                }
                
                // Show playerView and force layout update
                if (playerView.visibility != View.VISIBLE) {
                    Log.d(TAG, "updateDisplay: Showing PlayerView")
                    playerView.visibility = View.VISIBLE
                    playerView.requestLayout()
                    playerView.invalidate()
                    
                    // Log view dimensions
                    playerView.post {
                        Log.d(TAG, "PlayerView dimensions: ${playerView.width}x${playerView.height}, visibility=${playerView.visibility}")
                    }
                }
                
                val shouldPlay = viewModel.isPlaying.value == true
                
                // Always sync playWhenReady with isPlaying state
                // But only update if different to avoid unnecessary changes
                val currentPlayWhenReady = player?.playWhenReady ?: false
                if (currentPlayWhenReady != shouldPlay) {
                    player?.playWhenReady = shouldPlay
                    Log.d(TAG, "updateDisplay: Video clip ${currentClip.id}, changed playWhenReady: $currentPlayWhenReady -> $shouldPlay (isPlaying: ${viewModel.isPlaying.value}, forceSync: $forceSync)")
                } else {
                    Log.d(TAG, "updateDisplay: Video clip ${currentClip.id}, playWhenReady already correct: $shouldPlay")
                }
                
                // Only seek if:
                // 1. Force sync (manual seek or clip change)
                // 2. Player is not playing (need to set initial position)
                // 3. Very out of sync (> 500ms)
                if (forceSync || !shouldPlay) {
                    val allClips = viewModel.getMediaSourceManager().getClips()
                    val videoClips = allClips.filter { it.type == ClipType.VIDEO }
                    
                    var absolutePosition = 0L
                    for (clip in videoClips) {
                        if (clip.id == currentClip.id) {
                            val playbackTime = currentClip.getPlaybackTime(viewModel.currentTime.value ?: 0L)
                            absolutePosition += playbackTime
                            break
                        } else {
                            absolutePosition += clip.duration
                        }
                    }
                    
                    Log.d(TAG, "Video clip: ${currentClip.id}, seeking to ExoPlayer pos: ${absolutePosition}ms (forceSync: $forceSync)")
                    syncPlayerPosition(absolutePosition, forceSeek = forceSync)
                } else {
                    // When playing, only check if very out of sync (don't seek every frame)
                    checkAndSyncIfNeeded(currentClip)
                }
            }
            else -> {
                playerView.visibility = View.GONE
                imageView.visibility = View.GONE
                currentDisplayClipId = null
            }
        }
    }
    
    /**
     * Check if player is out of sync and seek if needed (only for large drifts)
     */
    private fun checkAndSyncIfNeeded(currentClip: Clip) {
        val currentPlayer = player ?: return
        if (currentPlayer.playbackState != Player.STATE_READY) return
        
        val allClips = viewModel.getMediaSourceManager().getClips()
        val videoClips = allClips.filter { it.type == ClipType.VIDEO }
        
        var expectedPosition = 0L
        for (clip in videoClips) {
            if (clip.id == currentClip.id) {
                val playbackTime = currentClip.getPlaybackTime(viewModel.currentTime.value ?: 0L)
                expectedPosition += playbackTime
                break
            } else {
                expectedPosition += clip.duration
            }
        }
        
        val currentPosition = currentPlayer.currentPosition
        val timeDiff = kotlin.math.abs(expectedPosition - currentPosition)
        
        // Only seek if very out of sync (> 500ms) - ExoPlayer handles small drifts automatically
        if (timeDiff > LARGE_SEEK_THRESHOLD_MS) {
            Log.w(TAG, "Player out of sync: expected=$expectedPosition, actual=$currentPosition, diff=$timeDiff")
            try {
                currentPlayer.seekTo(expectedPosition)
                Log.d(TAG, "Corrected player position to: ${expectedPosition}ms")
            } catch (e: Exception) {
                Log.e(TAG, "Error correcting player position", e)
            }
        }
    }
    
    private fun syncPlayerPosition(playbackTime: Long, forceSeek: Boolean = false) {
        val currentPlayer = player ?: run {
            Log.e(TAG, "syncPlayerPosition: player is null")
            return
        }
        
        val stateStr = when (currentPlayer.playbackState) {
            Player.STATE_IDLE -> "IDLE"
            Player.STATE_BUFFERING -> "BUFFERING"
            Player.STATE_READY -> "READY"
            Player.STATE_ENDED -> "ENDED"
            else -> "UNKNOWN"
        }
        
        Log.d(TAG, "syncPlayerPosition: playbackTime=${playbackTime}ms, forceSeek=$forceSeek, playerState=$stateStr, currentPos=${currentPlayer.currentPosition}ms, duration=${currentPlayer.duration}ms")
        
        // If force seek or player is not ready, prepare and seek
        if (forceSeek || currentPlayer.playbackState == Player.STATE_IDLE || currentPlayer.playbackState == Player.STATE_ENDED) {
            try {
                if (currentPlayer.playbackState == Player.STATE_IDLE || currentPlayer.playbackState == Player.STATE_ENDED) {
                    Log.d(TAG, "syncPlayerPosition: preparing player")
                    currentPlayer.prepare()
                }
                currentPlayer.seekTo(playbackTime)
                
                // Force invalidate PlayerView to redraw frame after seek
                val playerView = findViewById<androidx.media3.ui.PlayerView>(R.id.playerView)
                // Reduced delay for smoother scrubbing (33ms = ~30fps response)
                playerView.postDelayed({
                    playerView.invalidate()
                    // Force render frame if not playing
                    if (viewModel.isPlaying.value != true) {
                        forceRenderFrame()
                    }
                }, 33)
                
                Log.d(TAG, "syncPlayerPosition: Force synced to ${playbackTime}ms")
            } catch (e: Exception) {
                Log.e(TAG, "syncPlayerPosition: Error in force sync", e)
            }
            return
        }
        
        // Normal case: only seek if significantly out of sync
        if (currentPlayer.playbackState == Player.STATE_READY || currentPlayer.playbackState == Player.STATE_BUFFERING) {
            val currentPosition = currentPlayer.currentPosition
            val timeDiff = kotlin.math.abs(playbackTime - currentPosition)
            
            if (timeDiff > SEEK_THRESHOLD_MS) {
                try {
                    currentPlayer.seekTo(playbackTime)
                    
                    // Force invalidate PlayerView to redraw frame after seek
                    // Reduced delay for smoother scrubbing
                    val playerView = findViewById<androidx.media3.ui.PlayerView>(R.id.playerView)
                    playerView.postDelayed({
                        playerView.invalidate()
                        // Force render frame if not playing
                        if (viewModel.isPlaying.value != true) {
                            forceRenderFrame()
                        }
                    }, 33) // 33ms = ~30fps response
                    
                    Log.d(TAG, "syncPlayerPosition: Synced to ${playbackTime}ms (diff: ${timeDiff}ms)")
                } catch (e: Exception) {
                    Log.e(TAG, "syncPlayerPosition: Error syncing", e)
                }
            }
        }
    }
    
    /**
     * Force player to render current frame (useful when paused)
     * OPTIMIZED: Minimal delay for smoothness
     */
    private fun forceRenderFrame() {
        val currentPlayer = player ?: return
        if (currentPlayer.playbackState == Player.STATE_READY && currentPlayer.playWhenReady == false) {
            // Trick: briefly play then pause to force frame render
            currentPlayer.playWhenReady = true
            // Ultra-fast frame render (8ms = ~120fps)
            Handler(Looper.getMainLooper()).postDelayed({
                currentPlayer.playWhenReady = false
            }, 8)
        }
    }
    
    private fun formatTime(timeMs: Long): String {
        val seconds = timeMs / 1000
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%d:%02d", minutes, secs)
    }
    
    override fun onPause() {
        super.onPause()
        stopPlayback()
        player?.playWhenReady = false
    }
    
    override fun onResume() {
        super.onResume()
        if (viewModel.isPlaying.value == true) {
            startPlayback()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: cleaning up")
        
        stopPlayback()
        
        // Cleanup zoom handler
        pendingZoomRunnable?.let { zoomHandler?.removeCallbacks(it) }
        zoomHandler = null
        pendingZoomRunnable = null
        
        // Remove listener before releasing
        playerListener?.let { 
            player?.removeListener(it)
            playerListener = null
        }
        
        val playerView = findViewById<androidx.media3.ui.PlayerView>(R.id.playerView)
        playerView.player = null
        player = null
        
        thumbnailListView.clear()
    }
}
