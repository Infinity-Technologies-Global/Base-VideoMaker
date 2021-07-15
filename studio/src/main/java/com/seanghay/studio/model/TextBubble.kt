package com.seanghay.studio.model

import android.graphics.Bitmap

data class TextBubble(val content: String, val bitmap: Bitmap, val width: Int, val height: Int, val x: Int, val y: Int, val startAt: Long, val endAt: Long)