package com.ynsuper.slideshowver1.timeline.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.ynsuper.slideshowver1.timeline.model.Clip
import com.ynsuper.slideshowver1.timeline.model.ClipType
import com.ynsuper.slideshowver1.timeline.model.TrackType

/**
 * Custom Timeline View - Displays timeline with clips
 * Based on ARCHITECTURE_CAPCUT_STYLE.md
 */
class TimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private var timelineController: TimelineController? = null
    private var onTimeChangeListener: ((Long) -> Unit)? = null
    
    // Drawing constants
    private val trackHeight = 80f
    private val trackSpacing = 10f
    private val clipCornerRadius = 8f
    private val playheadWidth = 4f
    private val timeScale: Float = 100f // 100 pixels per second
    
    // Paints
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E1E1E")
    }
    
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2D2D2D")
    }
    
    private val videoClipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
    }
    
    private val imageClipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2196F3")
    }
    
    private val playheadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF5722")
        strokeWidth = playheadWidth
    }
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 24f
        textAlign = Paint.Align.LEFT
    }
    
    private val clipTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }
    
    interface TimelineController {
        fun getTotalDuration(): Long
        fun getCurrentTime(): Long
        fun getClipsInTrack(trackType: TrackType): List<Clip>
        fun seekTo(time: Long)
    }
    
    fun setTimelineController(controller: TimelineController) {
        this.timelineController = controller
        invalidate()
    }
    
    fun setOnTimeChangeListener(listener: (Long) -> Unit) {
        this.onTimeChangeListener = listener
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val controller = timelineController ?: return
        
        // Draw background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        
        // Draw tracks
        val tracks = listOf(
            TrackType.VIDEO to "Video",
            TrackType.AUDIO to "Audio",
            TrackType.OVERLAY to "Overlay",
            TrackType.TEXT to "Text"
        )
        
        var yOffset = trackSpacing
        
        tracks.forEach { (trackType, label) ->
            val trackY = yOffset
            val trackRect = RectF(0f, trackY, width.toFloat(), trackY + trackHeight)
            
            // Draw track background
            canvas.drawRoundRect(trackRect, 4f, 4f, trackPaint)
            
            // Draw track label
            canvas.drawText(label, 10f, trackY + trackHeight / 2 + 8f, textPaint)
            
            // Draw clips in this track
            val clips = controller.getClipsInTrack(trackType)
            clips.forEach { clip ->
                drawClip(canvas, clip, trackY)
            }
            
            yOffset += trackHeight + trackSpacing
        }
        
        // Draw playhead
        val currentTime = controller.getCurrentTime()
        val playheadX = (currentTime / 1000f) * timeScale
        canvas.drawLine(playheadX, 0f, playheadX, height.toFloat(), playheadPaint)
        
        // Draw time labels
        val totalDuration = controller.getTotalDuration()
        val maxTime = (width / timeScale * 1000).toLong().coerceAtMost(totalDuration)
        var time = 0L
        while (time <= maxTime) {
            val x = (time / 1000f) * timeScale
            canvas.drawLine(x, 0f, x, 20f, playheadPaint)
            canvas.drawText(formatTime(time), x + 5f, 15f, textPaint)
            time += 5000 // Every 5 seconds
        }
    }
    
    private fun drawClip(canvas: Canvas, clip: Clip, trackY: Float) {
        val clipX = (clip.startTime / 1000f) * timeScale
        val clipWidth = (clip.duration / 1000f) * timeScale
        val clipRect = RectF(clipX, trackY + 5f, clipX + clipWidth, trackY + trackHeight - 5f)
        
        // Choose paint based on clip type
        val paint = when (clip.type) {
            ClipType.VIDEO -> videoClipPaint
            ClipType.IMAGE -> imageClipPaint
            else -> imageClipPaint
        }
        
        // Draw clip rectangle
        canvas.drawRoundRect(clipRect, clipCornerRadius, clipCornerRadius, paint)
        
        // Draw clip label
        val label = when (clip.type) {
            ClipType.VIDEO -> "Video"
            ClipType.IMAGE -> "Image"
            else -> clip.type.name
        }
        canvas.drawText(
            label,
            clipRect.centerX(),
            clipRect.centerY() + 8f,
            clipTextPaint
        )
    }
    
    private fun formatTime(timeMs: Long): String {
        val seconds = timeMs / 1000
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%d:%02d", minutes, secs)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            val controller = timelineController ?: return false
            
            // Calculate time from X position
            val time = ((event.x / timeScale) * 1000).toLong()
            val clampedTime = time.coerceIn(0, controller.getTotalDuration())
            
            controller.seekTo(clampedTime)
            onTimeChangeListener?.invoke(clampedTime)
            invalidate()
            
            return true
        }
        return super.onTouchEvent(event)
    }
}

