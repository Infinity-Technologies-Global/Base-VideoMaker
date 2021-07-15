package com.createchance.imageeditor

import android.annotation.TargetApi
import android.media.*
import android.os.Build
import android.text.TextUtils
import android.util.Log
import java.io.*
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class AudioTransCoder {
    private val mRawQueue: BlockingQueue<RawBuffer> =
        LinkedBlockingQueue(10)
    private var mInputFile: File? = null
    private var mOutputFile: File? = null
    private var mStartPosMs: Long = 0
    private var mDurationMs: Long = 0
    private var decoder: MediaCodec? = null
    private var encoder: MediaCodec? = null
    private var mCallback: Callback? = null
    fun start(callback: Callback?) {
        Log.d(
            TAG,
            "Audio trans code started, start pos: $mStartPosMs, duration: $mDurationMs"
        )
        mCallback = callback
        if (checkRational()) {
            val decodeWorker = DecodeInputWorker()
            decodeWorker.start()
        } else {
            if (mCallback != null) {
                mCallback!!.onFailed()
            }
        }
    }

    private fun checkRational(): Boolean {
        return mInputFile != null &&
                mInputFile!!.exists() &&
                mInputFile!!.isFile && mStartPosMs >= 0 && mDurationMs >= 0
    }

    class Builder {
        private val transCodeAction = AudioTransCoder()
        fun transcode(input: File?): Builder {
            transCodeAction.mInputFile = input
            return this
        }

        fun from(fromMs: Long): Builder {
            transCodeAction.mStartPosMs = fromMs
            return this
        }

        fun duration(durationMs: Long): Builder {
            transCodeAction.mDurationMs = durationMs
            return this
        }

        fun saveAs(output: File?): Builder {
            transCodeAction.mOutputFile = output
            return this
        }

        fun build(): AudioTransCoder {
            return transCodeAction
        }
    }

    private inner class DecodeInputWorker : Thread() {
        private val TIME_OUT: Long = 5000
        private var extractor: MediaExtractor? = null
        override fun run() {
            try {
                prepare()

                // start decode output worker
                val decoderOutputWorker = DecodeOutputWorker()
                decoderOutputWorker.start()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    decodeInput21()
                } else {
                    decodeInput20()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                release()
            }
            Log.d(TAG, "Decode input worker done.")
        }

        private fun release() {
            if (extractor != null) {
                extractor!!.release()
                extractor = null
            }
        }

        @Throws(IOException::class)
        private fun prepare() {
            extractor = MediaExtractor()
            extractor!!.setDataSource(mInputFile!!.absolutePath)
            val numTracks = extractor!!.trackCount
            for (i in 0 until numTracks) {
                val format = extractor!!.getTrackFormat(i)
                val mine = format.getString(MediaFormat.KEY_MIME)
                if (!TextUtils.isEmpty(mine) && mine.startsWith("audio")) {
                    extractor!!.selectTrack(i)
                    if (mDurationMs == 0L) {
                        try {
                            mDurationMs = format.getLong(MediaFormat.KEY_DURATION) / 1000
                        } catch (e: Exception) {
                            e.printStackTrace()
                            val mediaPlayer = MediaPlayer()
                            mediaPlayer.setDataSource(mInputFile!!.absolutePath)
                            mediaPlayer.prepare()
                            mDurationMs = mediaPlayer.duration.toLong()
                            mediaPlayer.release()
                        }
                    }
                    check(mDurationMs != 0L) { "We can not get duration info from input file: $mInputFile" }
                    decoder = MediaCodec.createDecoderByType(mine)
                    decoder!!.configure(format, null, null, 0)
                    decoder!!.start()
                    break
                }
            }
        }

        @TargetApi(20)
        private fun decodeInput20() {
            val inputBuffers = decoder!!.inputBuffers
            extractor!!.seekTo(mStartPosMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            var isEOS = false
            while (true) {
                var timestamp: Long = 0
                if (!isEOS) {
                    val inIndex = decoder!!.dequeueInputBuffer(TIME_OUT)
                    if (inIndex >= 0) {
                        val buffer = inputBuffers[inIndex]
                        var sampleSize = extractor!!.readSampleData(buffer, 0)
                        timestamp = extractor!!.sampleTime
                        if (timestamp > (mStartPosMs + mDurationMs) * 1000) {
                            sampleSize = -1
                        }
                        if (sampleSize <= 0) {
                            Log.d(TAG, "Decode input reach eos.")
                            decoder!!.queueInputBuffer(
                                inIndex,
                                0,
                                0,
                                timestamp,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            isEOS = true
                        } else {
                            decoder!!.queueInputBuffer(
                                inIndex,
                                0,
                                sampleSize,
                                timestamp,
                                0
                            )
                            extractor!!.advance()
                        }
                    }
                } else {
                    break
                }
            }
            Log.d(TAG, "decode done!")
        }

        @TargetApi(21)
        private fun decodeInput21() {
            extractor!!.seekTo(mStartPosMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            var timeStamp: Long
            while (true) {
                val inputBufferId = decoder!!.dequeueInputBuffer(TIME_OUT)
                if (inputBufferId >= 0) {
                    val byteBuffer = decoder!!.getInputBuffer(inputBufferId)
                    var sampleSize = extractor!!.readSampleData(byteBuffer!!, 0)
                    timeStamp = extractor!!.sampleTime
                    if (timeStamp > (mStartPosMs + mDurationMs) * 1000) {
                        sampleSize = -1
                    }
                    if (sampleSize <= 0) {
                        Log.d(TAG, "Decode input reach eos.")
                        decoder!!.queueInputBuffer(
                            inputBufferId,
                            0,
                            0,
                            timeStamp,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        break
                    } else {
                        decoder!!.queueInputBuffer(
                            inputBufferId,
                            0,
                            sampleSize,
                            timeStamp,
                            0
                        )
                        extractor!!.advance()
                    }
                }
            }
        }
    }

    private inner class DecodeOutputWorker : Thread() {
        private val TIME_OUT: Long = 5000
        var outputBuffers = decoder!!.outputBuffers
        var info = MediaCodec.BufferInfo()
        override fun run() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    decodeOutput21()
                } else {
                    decodeOutput20()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (mCallback != null) {
                    mCallback!!.onFailed()
                }
            } finally {
                release()
            }
            Log.d(TAG, "Decode output worker done.")
        }

        @TargetApi(20)
        @Throws(InterruptedException::class)
        private fun decodeOutput20() {
            while (true) {
                val outIndex = decoder!!.dequeueOutputBuffer(info, TIME_OUT)
                when (outIndex) {
                    MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                        Log.d(
                            TAG,
                            "decodeOutput20: INFO_OUTPUT_BUFFERS_CHANGED"
                        )
                        outputBuffers = decoder!!.outputBuffers
                    }
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        Log.d(
                            TAG,
                            "decodeOutput20: INFO_OUTPUT_FORMAT_CHANGED"
                        )
                        val mf = decoder!!.outputFormat
                        // start encode worker
                        val encodeTask = EncodeInputWorker()
                        val sampleRate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        val channelCount = mf.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        encodeTask.setAudioParams(sampleRate, channelCount)
                        encodeTask.start()
                    }
                    MediaCodec.INFO_TRY_AGAIN_LATER -> Log.d(
                        TAG,
                        "dequeueOutputBuffer timed out!"
                    )
                    else -> {
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            mRawQueue.put(RawBuffer(null, true, info.presentationTimeUs))
                        } else {
                            val buffer = outputBuffers[outIndex]
                            val outData = ByteArray(info.size)
                            buffer[outData, 0, info.size]
                            mRawQueue.put(RawBuffer(outData, false, info.presentationTimeUs))
                        }
                        decoder!!.releaseOutputBuffer(outIndex, false)
                    }
                }
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    Log.d(TAG, "Decode output reach eos.")
                    break
                }
            }
        }

        @TargetApi(21)
        @Throws(InterruptedException::class)
        private fun decodeOutput21() {
            while (true) {
                val outputBufferId = decoder!!.dequeueOutputBuffer(info, TIME_OUT)
                when (outputBufferId) {
                    MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> Log.d(
                        TAG,
                        "decodeOutput20: INFO_OUTPUT_BUFFERS_CHANGED"
                    )
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        Log.d(
                            TAG,
                            "decodeOutput20: INFO_OUTPUT_FORMAT_CHANGED"
                        )
                        val mf = decoder!!.outputFormat
                        // start encode worker
                        val encodeTask = EncodeInputWorker()
                        val sampleRate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        val channelCount = mf.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        encodeTask.setAudioParams(sampleRate, channelCount)
                        encodeTask.start()
                    }
                    MediaCodec.INFO_TRY_AGAIN_LATER -> Log.d(
                        TAG,
                        "dequeueOutputBuffer timed out!"
                    )
                    else -> {
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            mRawQueue.put(RawBuffer(null, true, info.presentationTimeUs))
                        } else {
                            val buffer =
                                decoder!!.getOutputBuffer(outputBufferId)
                            val outData = ByteArray(info.size)
                            buffer!![outData, 0, info.size]
                            mRawQueue.put(RawBuffer(outData, false, info.presentationTimeUs))
                        }
                        decoder!!.releaseOutputBuffer(outputBufferId, false)
                    }
                }
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    Log.d(TAG, "Decode output reach eos.")
                    break
                }
            }
        }

        private fun release() {
            if (decoder != null) {
                decoder!!.stop()
                decoder!!.release()
                decoder = null
            }
        }
    }

    private inner class EncodeInputWorker : Thread() {
        private val TIME_OUT: Long = 5000
        private var sampleRate = 0
        private var channelCount = 0
        fun setAudioParams(sampleRate: Int, channelCount: Int) {
            this.sampleRate = sampleRate
            this.channelCount = channelCount
        }

        override fun run() {
            try {
                prepare()

                // start encode output worker
                val encodeOutputWorker = EncodeOutputWorker()
                encodeOutputWorker.start()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    encodeInput21()
                } else {
                    encodeInput20()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (mCallback != null) {
                    mCallback!!.onFailed()
                }
            } finally {
                release()
            }
            Log.d(TAG, "Encode input worker done.")
        }

        @Throws(IOException::class)
        private fun prepare() {
            encoder = MediaCodec.createEncoderByType("audio/mp4a-latm")
            val format =
                MediaFormat.createAudioFormat("audio/mp4a-latm", sampleRate, channelCount)
            format.setInteger(MediaFormat.KEY_BIT_RATE, 96000)
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 512 * 1024)
            format.setInteger(
                MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC
            )
            encoder!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder!!.start()
        }

        @TargetApi(20)
        @Throws(InterruptedException::class)
        private fun encodeInput20() {
            var decodeDone = false
            val inputBuffers = encoder!!.inputBuffers
            while (true) {
                if (!decodeDone) {
                    val inputBufferId = encoder!!.dequeueInputBuffer(TIME_OUT)
                    if (inputBufferId >= 0) {
                        val rawBuffer = mRawQueue.take()
                        if (rawBuffer.isLast) {
                            Log.d(TAG, "Encode input reach eos.")
                            decodeDone = true
                            encoder!!.queueInputBuffer(
                                inputBufferId,
                                0,
                                0,
                                rawBuffer.sampleTime,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                        } else {
                            val inputBuffer = inputBuffers[inputBufferId]
                            inputBuffer.clear()
                            inputBuffer.put(rawBuffer.data)
                            encoder!!.queueInputBuffer(
                                inputBufferId,
                                0,
                                rawBuffer.data!!.size,
                                rawBuffer.sampleTime,
                                0
                            )
                        }
                    }
                } else {
                    break
                }
            }
            Log.d(TAG, "encode done!")
        }

        @TargetApi(21)
        @Throws(InterruptedException::class)
        private fun encodeInput21() {
            var decodeDone = false
            while (true) {
                if (!decodeDone) {
                    val inputBufferId = encoder!!.dequeueInputBuffer(TIME_OUT)
                    if (inputBufferId >= 0) {
                        val rawBuffer = mRawQueue.take()
                        if (rawBuffer.isLast) {
                            Log.d(TAG, "Encode input reach eos.")
                            decodeDone = true
                            encoder!!.queueInputBuffer(
                                inputBufferId,
                                0,
                                0,
                                rawBuffer.sampleTime,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                        } else {
                            val inputBuffer =
                                encoder!!.getInputBuffer(inputBufferId)
                            inputBuffer!!.clear()
                            inputBuffer.put(rawBuffer.data)
                            encoder!!.queueInputBuffer(
                                inputBufferId,
                                0,
                                rawBuffer.data!!.size,
                                rawBuffer.sampleTime,
                                0
                            )
                        }
                    }
                } else {
                    break
                }
            }
        }

        private fun release() {}
    }

    private inner class EncodeOutputWorker : Thread() {
        private val TIME_OUT: Long = 5000
        var outputBuffers = encoder!!.outputBuffers
        var info = MediaCodec.BufferInfo()
        private var mOutput: OutputStream? = null
        override fun run() {
            try {
                prepare()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    encodeOutput21()
                } else {
                    encodeOutput20()
                }
                if (mCallback != null) {
                    mCallback!!.onSucceed(mOutputFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (mCallback != null) {
                    mCallback!!.onFailed()
                }
            } finally {
                release()
            }
            Log.d(TAG, "Encode output worker done.")
        }

        @Throws(FileNotFoundException::class)
        private fun prepare() {
            mOutput = DataOutputStream(FileOutputStream(mOutputFile))
        }

        @TargetApi(20)
        @Throws(IOException::class)
        private fun encodeOutput20() {
            while (true) {
                val outIndex = encoder!!.dequeueOutputBuffer(info, TIME_OUT)
                if (outIndex >= 0) {
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        break
                    }
                    val outputBuffer = outputBuffers[outIndex]
                    val len = info.size + 7
                    val outData = ByteArray(len)
                    addADTStoPacket(outData, len)
                    outputBuffer[outData, 7, info.size]
                    encoder!!.releaseOutputBuffer(outIndex, false)
                    mOutput!!.write(outData)
                    if (mCallback != null) {
                        mCallback!!.onProgress(
                            (info.presentationTimeUs - mStartPosMs * 1000) * 1f /
                                    (mDurationMs * 1000)
                        )
                    }
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        Log.d(
                            TAG,
                            "Encode output reach eos."
                        )
                        if (mCallback != null) {
                            mCallback!!.onProgress(1f)
                        }
                        break
                    }
                }
            }
        }

        @TargetApi(21)
        @Throws(IOException::class)
        private fun encodeOutput21() {
            while (true) {
                val outIndex = encoder!!.dequeueOutputBuffer(info, TIME_OUT)
                if (outIndex >= 0) {
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        break
                    }
                    val outputBuffer = encoder!!.getOutputBuffer(outIndex)
                    val len = info.size + 7
                    val outData = ByteArray(len)
                    addADTStoPacket(outData, len)
                    outputBuffer!![outData, 7, info.size]
                    encoder!!.releaseOutputBuffer(outIndex, false)
                    mOutput!!.write(outData)
                    if (mCallback != null) {
                        mCallback!!.onProgress(
                            (info.presentationTimeUs - mStartPosMs * 1000) * 1f /
                                    (mDurationMs * 1000)
                        )
                    }
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        Log.d(
                            TAG,
                            "Encode output reach eos."
                        )
                        if (mCallback != null) {
                            mCallback!!.onProgress(1f)
                        }
                        break
                    }
                }
            }
        }

        private fun release() {
            if (encoder != null) {
                encoder!!.stop()
                encoder!!.release()
                encoder = null
            }
            if (mOutput != null) {
                try {
                    mOutput!!.flush()
                    mOutput!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                mOutput = null
            }
        }

        /**
         * 给编码出的aac裸流添加adts头字段
         *
         * @param packet    要空出前7个字节，否则会搞乱数据
         * @param packetLen
         */
        private fun addADTStoPacket(packet: ByteArray, packetLen: Int) {
            // AAC LC
            val profile = 2
            // 44.1KHz
            val freqIdx = 4
            // CPE
            val chanCfg = 2
            packet[0] = 0xFF.toByte()
            packet[1] = 0xF9.toByte()
            packet[2] =
                ((profile - 1 shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
            packet[3] = ((chanCfg and 3 shl 6) + (packetLen shr 11)).toByte()
            packet[4] = (packetLen and 0x7FF shr 3).toByte()
            packet[5] = ((packetLen and 7 shl 5) + 0x1F).toByte()
            packet[6] = 0xFC.toByte()
        }
    }

    private inner class RawBuffer(
        var data: ByteArray?,
        var isLast: Boolean,
        var sampleTime: Long
    ) {
        override fun toString(): String {
            return "RawBuffer{" +
                    "data=" + Arrays.toString(data) +
                    ", isLast=" + isLast +
                    '}'
        }

    }

    interface Callback {
        fun onProgress(progress: Float)
        fun onSucceed(output: File?)
        fun onFailed()
    }

    companion object {
        private const val TAG = "AudioTransCoder"
    }
}