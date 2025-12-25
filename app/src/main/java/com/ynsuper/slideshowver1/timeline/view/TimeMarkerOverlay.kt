package com.ynsuper.slideshowver1.timeline.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Overlay to draw time markers on timeline
 * Markers adapt based on zoom level:
 * - Zoom 0.25x-0.5x: every 10s
 * - Zoom 0.5x-1x: every 5s
 * - Zoom 1x-2x: every 2s
 * - Zoom 2x-4x: every 1s
 * - Zoom 4x-8x: every 500ms
 * - Zoom 8x+: every 100ms (frame level)
 */
class TimeMarkerOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var pixelsPerSecond: Float = 100f
    private var totalDuration: Long = 0L
    private var scrollX: Int = 0
    private var paddingStart: Int = 0
    
    companion object {
        private const val MARKER_HEIGHT_TALL = 40f
        private const val MARKER_HEIGHT_SHORT = 20f
        private const val TEXT_SIZE = 24f
        private const val TEXT_OFFSET_Y = 15f
    }
    
    init {
        paint.color = Color.WHITE
        paint.strokeWidth = 2f
        paint.alpha = 150
        
        textPaint.color = Color.WHITE
        textPaint.textSize = TEXT_SIZE
        textPaint.alpha = 200
    }
    
    fun setTimelineParams(
        pixelsPerSecond: Float,
        totalDuration: Long,
        scrollX: Int,
        paddingStart: Int
    ) {
        this.pixelsPerSecond = pixelsPerSecond
        this.totalDuration = totalDuration
        this.scrollX = scrollX
        this.paddingStart = paddingStart
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (totalDuration == 0L) return
        
        // Determine marker interval based on zoom level
        val zoomLevel = pixelsPerSecond / 100f // 100px/s = 1x zoom
        val intervalMs = when {
            zoomLevel < 0.5f -> 10000L  // 10s
            zoomLevel < 1f -> 5000L     // 5s
            zoomLevel < 2f -> 2000L     // 2s
            zoomLevel < 4f -> 1000L     // 1s
            zoomLevel < 8f -> 500L      // 500ms
            else -> 100L                // 100ms (frame level)
        }
        
        // Calculate visible range
        val visibleStart = (scrollX - paddingStart).coerceAtLeast(0)
        val visibleEnd = visibleStart + width
        
        // Draw markers
        var currentTime = 0L
        while (currentTime <= totalDuration) {
            val xPos = (currentTime / 1000f * pixelsPerSecond + paddingStart - scrollX).toInt()
            
            // Only draw if visible
            if (xPos >= 0 && xPos <= width) {
                // Tall marker every major interval, short for minor
                val isMajor = currentTime % (intervalMs * 5) == 0L
                val markerHeight = if (isMajor) MARKER_HEIGHT_TALL else MARKER_HEIGHT_SHORT
                
                canvas.drawLine(
                    xPos.toFloat(),
                    height - markerHeight,
                    xPos.toFloat(),
                    height.toFloat(),
                    paint
                )
                
                // Draw time text for major markers
                if (isMajor) {
                    val timeText = formatTimeForMarker(currentTime, intervalMs)
                    canvas.drawText(
                        timeText,
                        xPos.toFloat() + 5f,
                        height - MARKER_HEIGHT_TALL - TEXT_OFFSET_Y,
                        textPaint
                    )
                }
            }
            
            currentTime += intervalMs
        }
    }
    
    private fun formatTimeForMarker(timeMs: Long, intervalMs: Long): String {
        return when {
            intervalMs >= 1000 -> {
                // Seconds format
                val seconds = (timeMs / 1000).toInt()
                val minutes = seconds / 60
                val secs = seconds % 60
                if (minutes > 0) {
                    String.format("%d:%02d", minutes, secs)
                } else {
                    "${secs}s"
                }
            }
            else -> {
                // Milliseconds format
                "${timeMs}ms"
            }
        }
    }
}

