package com.ynsuper.slideshowver1.util

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.seanghay.studio.gles.annotation.GlContext
import com.seanghay.studio.gles.graphics.texture.Texture2d
import com.seanghay.studio.gles.shader.filter.pack.PackFilter
import com.seanghay.studio.gles.transition.FadeTransition
import com.seanghay.studio.gles.transition.Transition
import com.seanghay.studio.utils.BitmapProcessor

data class Scene (
    var id: String,
    var bitmap: Bitmap,
    var originalPath: String
) {

    val progressBlur: Int = 14
    var duration: Long = 4000L
    var transition: Transition = FadeTransition("fade", 1000L)
    var texture: Texture2d = Texture2d()
    var filter = PackFilter()
    var cropType: BitmapProcessor.CropType = BitmapProcessor.CropType.FIT_CENTER


    @GlContext
    fun setup() {
        texture.initialize()
        texture.use(GLES20.GL_TEXTURE_2D) {
            texture.configure(GLES20.GL_TEXTURE_2D)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        }
    }

    @GlContext
    fun release() {

        texture.release()
    }

}