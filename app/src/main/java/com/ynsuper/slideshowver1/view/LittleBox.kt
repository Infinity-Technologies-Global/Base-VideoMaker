package com.ynsuper.slideshowver1.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaMuxer
import android.opengl.GLES10.*
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.createchance.imageeditor.AudioTransCoder
import com.seanghay.studio.core.Studio
import com.seanghay.studio.gles.RenderContext
import com.seanghay.studio.utils.clamp
import com.ynsuper.slideshowver1.util.Constants
import com.ynsuper.slideshowver1.util.Mp4Composer
import com.ynsuper.slideshowver1.util.UiThreadUtil
import com.ynsuper.slideshowver1.util.VideoComposer
import java.io.File
import java.nio.ByteBuffer
import java.util.*

class LittleBox(
    val activity: AppCompatActivity,
    val surfaceTexture: SurfaceTexture,
    var width: Int,
    var height: Int
) {

    private lateinit var mContext: Context
    private var mVideoTrackId = -1
    private var mAudioTrackId: Int = -1

    var playProgress: (Float) -> Unit = {}
    var isPlaying = false
        private set

    private var composer: VideoComposer? = null
    private val onReadyQueue: Queue<Runnable> = LinkedList()
    private lateinit var display: Studio.OutputSurface

    private val studio = Studio.create(activity) {
        display = createOutputSurface()
        display.fromSurfaceTexture(surfaceTexture)
        setOutputSurface(display)
        setSize(Size(width, height))
        while (onReadyQueue.isNotEmpty()) onReadyQueue.poll()?.run()
    }


    fun getStudio() = studio

    fun setComposer(videoComposer: VideoComposer) {
        this.composer = videoComposer
        this.composer?.width = width
        this.composer?.height = height

        onReadyQueue.add(Runnable {
            studio.setRenderContext(videoComposer)
            studio.dispatchDraw()
            studio.dispatchDraw()
        })
    }

    fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
        studio.setSize(Size(width, height))
    }

    fun release() {
        studio.release()
    }

    fun draw() {
        studio.dispatchDraw()
    }

    private fun startPlay() {

        if (isPlaying) return
        var lastTime = System.nanoTime()
        var delta = 0.0
        val ns = 1000000000.0 / 60.0
        var timer = System.currentTimeMillis()
        var updates = 0
        var frames = 0

        val totalDuration = composer?.totalDuration ?: 0L

        var startedAt = timer

        isPlaying = true

        val offset = composer?.progress ?: 0f

        while (isPlaying) {
            val elapsed = System.currentTimeMillis() - startedAt
            var progress = (elapsed.toFloat() / totalDuration.toFloat())
            if (progress >= 1.0) startedAt = System.currentTimeMillis()
            progress = progress.clamp(0f, 1f)

            val now = System.nanoTime()

            delta += (now - lastTime) / ns
            lastTime = now

            if (delta >= 1.0) {
                // Set progress
                composer?.progress = offset + progress
                updates++
                delta--
                playProgress(offset + progress)
            }

            studio.draw()

            frames++
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000
                Log.d("LittleBox", "$updates ups, $frames fps")
                updates = 0
                frames = 0
            }
        }
    }

    fun play() {
        studio.post { startPlay() }
    }

    fun stop() {
        isPlaying = false
    }

    fun pause() {
        isPlaying = false
    }


    fun exportToVideo(
        width: Int,
        height: Int,
        path: String,
        audioPath: String,
        progress: ((Float) -> Unit) = {},
        done: () -> Unit = {}
    ) {

        val composer = composer ?: return
        isPlaying = false

        val mp4Composer = Mp4Composer(studio, composer, path, composer.totalDuration,width, height) {
            done()
            studio.setOutputSurface(display)
        }

        mp4Composer.audioPath = audioPath
        mp4Composer.onProgressChange = progress
        mp4Composer.width = composer.width
        mp4Composer.height = composer.height
        mp4Composer.create()

        studio.post {
            mp4Composer.start()
        }
    }

    companion object LittleContext : RenderContext {
        private const val TAG = "LittleContext"

        override fun onCreated() {
            Log.d(TAG, "onCreated")
        }

        override fun onDraw(): Boolean {
            Log.d(TAG, "draw")
            glClearColor(1f, 0f, 1f, 1f)
            glClear(GL_COLOR_BUFFER_BIT)
            return true
        }

        override fun onSizeChanged(size: Size) {
            Log.d(TAG, "onResize")
            glViewport(0, 0, size.width, size.height)
        }
    }

    fun setProgress(float: Float) {
        playProgress = {
            it.toInt()
            Log.d(TAG, "Ynsuper setprogress:" + float)

        }
    }

    fun muxerAudio(
        context: Context,
        audioPath: File,
        mVideoFile: File,
        mAudioFile: File,
        totalDuration: Long,
        onProgressMuxer: (progress: Float) -> Unit,
        onDone: (stringPathResult: String) -> Unit
    ) {
        this.mContext = context
        val audioTransCoder =
            AudioTransCoder.Builder()
                .transcode(audioPath)
                .from(0)
                .duration(totalDuration )
                .saveAs(mAudioFile)
                .build()

        audioTransCoder.start(object : AudioTransCoder.Callback {
            override fun onProgress(progress: Float) {
                Log.d("Ynsuper", "muxerAudio onProgress: $progress - totalduration:   $totalDuration " )
                onProgressMuxer(progress)
//                if (mListener != null) {
//                    UiThreadUtil.post(Runnable { mListener.onSaveProgress(0.5f + progress * 0.5f) })
//                }
            }

            override fun onSucceed(output: File?) {
                Log.d("Ynsuper", "muxerAudio onSucceed: " + output?.absolutePath)
                val outputVideoFinal =
                    File(
                        Constants.PATH_SAVE_FILE_VIDEO,
                        "my-video-${System.currentTimeMillis()}.mp4"
                    )
                outputVideoFinal.createNewFile()
                mergeFile(outputVideoFinal, output!!, mVideoFile, {
                    onDone(it)
                })

            }

            override fun onFailed() {
                Log.d("Ynsuper", "muxerAudio onFailed: ")
                Log.e("Ynsuper", "muxerAudio Save worker failed.")
//                UiThreadUtil.post(Runnable {
//                    if (mListener != null) {
//                        mListener.onSaveFailed()
//                    }
//                })
//
                // delete temp files.
                if (mVideoFile.exists()) {
                    mVideoFile.delete()
                }
                if (mAudioFile.exists()) {
                    mAudioFile.delete()
                }
            }
        })
    }

    private fun mergeFile(
        mOutputFile: File,
        mAudioFile: File,
        mVideoFile: File,
        success: (path : String) -> Unit

    ) {
        var muxer: MediaMuxer? = null
        var videoExtractor: MediaExtractor? = null
        var audioExtractor: MediaExtractor? = null
        val buffer = ByteBuffer.allocate(512 * 1024)
        val bufferInfo = MediaCodec.BufferInfo()
        try {
            muxer = MediaMuxer(
                mOutputFile.getAbsolutePath(),
                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
            )

            // load video media format.
            videoExtractor = MediaExtractor()
            videoExtractor.setDataSource(mVideoFile.getAbsolutePath())
            mVideoTrackId = muxer!!.addTrack(videoExtractor.getTrackFormat(0))
            // load audio media format.
            audioExtractor = MediaExtractor()
            audioExtractor.setDataSource(mAudioFile.getAbsolutePath())
            mAudioTrackId = muxer.addTrack(audioExtractor.getTrackFormat(0))

            // start muxer
            muxer.start()

            // read video first.
            videoExtractor.selectTrack(0)
            while (true) {
                val sampleSize = videoExtractor.readSampleData(buffer, 0)
                if (sampleSize != -1) {
                    bufferInfo.size = sampleSize
                    bufferInfo.flags = videoExtractor.sampleFlags
                    bufferInfo.offset = 0
                    bufferInfo.presentationTimeUs = videoExtractor.sampleTime
                    muxer.writeSampleData(mVideoTrackId, buffer, bufferInfo)
                    videoExtractor.advance()
                } else {
                    Log.d("Ynsuper", "muxerAudio Read video done.")
                    break
                }
            }

            // clear buffer
            buffer.clear()

            // handle audio then
            audioExtractor.selectTrack(0)
            while (true) {
                val sampleSize = audioExtractor.readSampleData(buffer, 0)
                if (sampleSize != -1) {
                    bufferInfo.size = sampleSize
                    bufferInfo.flags = audioExtractor.sampleFlags
                    bufferInfo.offset = 0
                    bufferInfo.presentationTimeUs = audioExtractor.sampleTime
                    muxer.writeSampleData(mAudioTrackId, buffer, bufferInfo)
                    audioExtractor.advance()
                } else {
                    Log.d("Ynsuper ", "muxerAudio Read video done2.")
                    break
                }
            }
            UiThreadUtil.post(Runnable {
//                if (mListener != null) {
//                    mListener.onSaved(mOutputFile)
                Log.d("YNsuper", "muxerAudio Saved:  ")
                val path = mOutputFile.absolutePath
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(path))
//                intent.setDataAndType(Uri.parse(path), "video/mp4")
//                mContext.startActivity(intent)
                success(path)

//                }
            })
            Log.d("YNsuper", "muxerAudio Save worker done.")
        } catch (e: Exception) {
            e.printStackTrace()
            UiThreadUtil.post(Runnable {
//                if (mListener != null) {
//                    mListener.onSaveFailed()
//                }
                Log.d("YNsuper", "muxerAudio Saved error:   " + e.message)

            })
        } finally {
            videoExtractor?.release()
            audioExtractor?.release()
            if (muxer != null) {
                muxer.stop()
                muxer.release()
            }

            // delete temp files.
            if (mVideoFile.exists()) {
                mVideoFile.delete()
            }
            if (mAudioFile.exists()) {
                mAudioFile.delete()
            }
        }
    }

}

