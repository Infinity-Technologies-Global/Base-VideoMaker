package com.vanvatcorporation.doubleclips.activities.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.vanvatcorporation.doubleclips.activities.main.MainAreaScreen;
import com.vanvatcorporation.doubleclips.activities.model.Clip;
import com.vanvatcorporation.doubleclips.activities.model.ClipType;
import com.vanvatcorporation.doubleclips.activities.model.VideoProperties;
import com.vanvatcorporation.doubleclips.activities.model.VideoSettings;
import com.vanvatcorporation.doubleclips.constants.Constants;
import com.vanvatcorporation.doubleclips.helper.IOImageHelper;
import com.vanvatcorporation.doubleclips.manager.LoggingManager;

import java.io.File;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.exoplayer.ExoPlayer;

/**
 * Renders a single clip (video, audio, or image) in the preview.
 * Handles ExoPlayer playback, transformations, and gesture controls.
 */
public class ClipRenderer {
    private static final float CANVAS_DRAG_SENSITIVITY = 1f; // 0.5 = đi chậm hơn tay, mượt hơn

    public final Clip clip;

    public ExoPlayer exoPlayer; // ExoPlayer handles both video and audio
    public TextureView textureView;
    private Context context;
    private ClipInteractionCallback callback;
    public FrameLayout previewViewGroupRef;
    private MainAreaScreen.ProjectData projectData;

    public boolean isPlaying;
    private boolean isExoPlayerPrepared = false; // Lazy loading flag
    private Player.Listener exoPlayerListener; // Store listener to remove it later

    private Matrix matrix = new Matrix();
    public float scaleX = 1, scaleY = 1;
    public float rot = 0;
    public float posX = 0, posY = 0;

    private float scaleMatrixX = 1, scaleMatrixY = 1;
    private float rotMatrix = 0;
    private float posMatrixX = 0, posMatrixY = 0;

    private float initialPosX = 0, initialPosY = 0;
    private float startTouchX = 0, startTouchY = 0;

    public View dragBorderView;
    private android.graphics.drawable.GradientDrawable dragBorderDrawable;

    private long lastSeekPositionMs = -1;
    private float lastPlayheadTime = -1f;
    private static final long MIN_SEEK_INTERVAL_MS = 16; // ~60fps, only seek if time changed by at least 16ms for smoother realtime preview

    EditMode currentMode = EditMode.NONE;

    // Use raw coordinates for stable dragging (not affected by view position changes)
    private float lastRawX = 0f;
    private float lastRawY = 0f;

    public ClipRenderer(Context context, Clip clip, MainAreaScreen.ProjectData data, VideoSettings settings, ClipInteractionCallback callback, FrameLayout previewViewGroup, TextView textCanvasControllerInfo) {
        this.context = context;
        this.clip = clip;
        this.callback = callback;
        this.previewViewGroupRef = previewViewGroup;
        this.projectData = data;

        try {
            switch (clip.type) {
                case VIDEO: {
                    // VIDEO
                    textureView = new TextureView(context);
                    RelativeLayout.LayoutParams textureViewLayoutParams =
                            new RelativeLayout.LayoutParams(clip.width, clip.height);
                    previewViewGroup.addView(textureView, textureViewLayoutParams);
                    textureView.setX(0f);
                    textureView.setY(0f);

                    // Initialize ExoPlayer
                    try {
                        exoPlayer = new ExoPlayer.Builder(context).build();
                        if (exoPlayer == null) {
                            Log.e("ClipRenderer", "Failed to create ExoPlayer for clip: " + clip.clipName);
                            break;
                        }

                        exoPlayer.setVideoTextureView(textureView);

                        // Set audio attributes to ensure audio playback
                        // Set handleAudioFocus to false to allow multiple videos to play simultaneously
                        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                .setContentType(C.CONTENT_TYPE_MOVIE)
                                .setUsage(C.USAGE_MEDIA)
                                .build();
                        exoPlayer.setAudioAttributes(audioAttributes, false); // false = don't handle audio focus, allow multiple streams
                        exoPlayer.setVolume(1.0f); // Ensure volume is at maximum

                        // Don't prepare ExoPlayer immediately - lazy load when clip becomes visible
                        // Use preview file (keyint=1) for fast seeking, fallback to original if preview doesn't exist
                        String previewPath = clip.getAbsolutePreviewPath(data);
                        String originalPath = clip.getAbsolutePath(data);
                        String videoPath = previewPath;
                        boolean usingPreview = true;
                        if (videoPath == null || videoPath.isEmpty() || !new File(videoPath).exists()) {
                            videoPath = originalPath;
                            usingPreview = false;
                            Log.w("ClipRenderer", "Preview file not found for clip: " + clip.clipName + ", using original file. Preview seeking may be slower.");
                        }
                        Log.d("ClipRenderer", "Initializing ExoPlayer for clip: " + clip.clipName + ", preview: " + previewPath + ", original: " + originalPath + ", using: " + videoPath + ", isPreview: " + usingPreview);

                        if (videoPath == null || videoPath.isEmpty()) {
                            Log.e("ClipRenderer", "Invalid path (null or empty) for clip: " + clip.clipName);
                            exoPlayer.release();
                            exoPlayer = null;
                            break;
                        }

                        File file = new File(videoPath);
                        if (!file.exists()) {
                            Log.e("ClipRenderer", "File does not exist: " + videoPath + " for clip: " + clip.clipName + ". File size: " + file.length() + ", isDirectory: " + file.isDirectory());
                            exoPlayer.release();
                            exoPlayer = null;
                            break;
                        }

                        Log.d("ClipRenderer", "File exists, size: " + file.length() + " bytes for clip: " + clip.clipName + ", using: " + videoPath);

                        MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(file));
                        exoPlayer.setMediaItem(mediaItem);
                        // Don't call prepare() here - will be called lazily

                        Log.d("ClipRenderer", "ExoPlayer initialized successfully for clip: " + clip.clipName);

                        // Set initial transformations from clip.videoProperties
                        float renderScaleX = clip.videoProperties.getValue(VideoProperties.ValueType.ScaleX);
                        float renderScaleY = clip.videoProperties.getValue(VideoProperties.ValueType.ScaleY);
                        posX = (PreviewConversionHelper.renderToPreviewConversionX(clip.videoProperties.getValue(VideoProperties.ValueType.PosX), settings.videoWidth, settings.videoHeight));
                        posY = (PreviewConversionHelper.renderToPreviewConversionY(clip.videoProperties.getValue(VideoProperties.ValueType.PosY), settings.videoWidth, settings.videoHeight));
                        scaleX = (PreviewConversionHelper.renderToPreviewConversionScalingX(renderScaleX, settings.videoWidth, settings.videoHeight));
                        scaleY = (PreviewConversionHelper.renderToPreviewConversionScalingY(renderScaleY, settings.videoWidth, settings.videoHeight));
                        rot = (clip.videoProperties.getValue(VideoProperties.ValueType.Rot));

                        Log.d("ClipRenderer", "ClipRenderer constructor | clip=" + clip.clipName
                                + " | renderScale=(" + renderScaleX + "," + renderScaleY + ")"
                                + " | previewScale=(" + scaleX + "," + scaleY + ")");

                        applyTransformation();
                        applyPostTransformation();
                    } catch (Exception e) {
                        Log.e("ClipRenderer", "Error initializing ExoPlayer for VIDEO clip " + clip.clipName + ": " + e.getMessage(), e);
                        e.printStackTrace();
                        if (exoPlayer != null) {
                            try {
                                exoPlayer.release();
                            } catch (Exception releaseEx) {
                                Log.e("ClipRenderer", "Error releasing ExoPlayer: " + releaseEx.getMessage());
                            }
                            exoPlayer = null;
                        }
                        break;
                    }

                    // Audio is handled by ExoPlayer automatically
                    break;
                }
                case IMAGE: {
                    textureView = new TextureView(context);
                    RelativeLayout.LayoutParams textureViewLayoutParams =
                            new RelativeLayout.LayoutParams(clip.width, clip.height);
                    previewViewGroup.addView(textureView, textureViewLayoutParams);

                    textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                        @Override
                        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                            // For IMAGE rendering
                            Bitmap image = IOImageHelper.LoadFileAsPNGImage(context, clip.getAbsolutePreviewPath(data), 1);

                            // Resize TextureView to match bitmap size
                            ViewGroup.LayoutParams params = textureView.getLayoutParams();
                            params.width = clip.width;
                            params.height = clip.height;
                            textureView.setLayoutParams(params);

                            // Draw the bitmap onto the TextureView's canvas
                            Canvas canvas = textureView.lockCanvas();
                            if (canvas != null) {
                                canvas.drawBitmap(image, 0, 0, null); // draw at top-left
                                textureView.unlockCanvasAndPost(canvas);
                            }

                            surfaceTexture.setDefaultBufferSize(clip.width, clip.height);

                            posX = (PreviewConversionHelper.renderToPreviewConversionX(clip.videoProperties.getValue(VideoProperties.ValueType.PosX), settings.videoWidth, settings.videoHeight));
                            posY = (PreviewConversionHelper.renderToPreviewConversionY(clip.videoProperties.getValue(VideoProperties.ValueType.PosY), settings.videoWidth, settings.videoHeight));
                            scaleX = (PreviewConversionHelper.renderToPreviewConversionScalingX(clip.videoProperties.getValue(VideoProperties.ValueType.ScaleX), settings.videoWidth, settings.videoHeight));
                            scaleY = (PreviewConversionHelper.renderToPreviewConversionScalingY(clip.videoProperties.getValue(VideoProperties.ValueType.ScaleY), settings.videoWidth, settings.videoHeight));

                            float clipRatio = (float) clip.width / clip.height;
                            float resolutionRatio = (float) settings.videoWidth / settings.videoHeight;
                            float previewAvailableRatio = PreviewConversionHelper.previewAvailableWidth > 0 && PreviewConversionHelper.previewAvailableHeight > 0 ? (float) PreviewConversionHelper.previewAvailableWidth / PreviewConversionHelper.previewAvailableHeight : 0;
                            float scaleRatio = scaleX / scaleY;
                            float actualWidth = clip.width * scaleX;
                            float actualHeight = clip.height * scaleY;
                            float actualRatio = actualHeight > 0 ? actualWidth / actualHeight : 0;

                            Log.e("ClipRatio", "=== IMAGE CLIP RATIO DEBUG ===");
                            Log.e("ClipRatio", "Clip: " + clip.clipName);
                            Log.e("ClipRatio", "Clip original size: " + clip.width + "x" + clip.height);
                            Log.e("ClipRatio", "Clip original ratio: " + clipRatio);
                            Log.e("ClipRatio", "Resolution: " + settings.videoWidth + "x" + settings.videoHeight);
                            Log.e("ClipRatio", "Resolution ratio: " + resolutionRatio);
                            Log.e("ClipRatio", "PreviewAvailable: " + PreviewConversionHelper.previewAvailableWidth + "x" + PreviewConversionHelper.previewAvailableHeight);
                            Log.e("ClipRatio", "PreviewAvailable ratio: " + previewAvailableRatio);
                            Log.e("ClipRatio", "Scale: " + scaleX + "x" + scaleY);
                            Log.e("ClipRatio", "Scale ratio: " + scaleRatio);
                            Log.e("ClipRatio", "Actual size after scale: " + actualWidth + "x" + actualHeight);
                            Log.e("ClipRatio", "Actual ratio after scale: " + actualRatio);
                            Log.e("ClipRatio", "Render ScaleX: " + clip.videoProperties.getValue(VideoProperties.ValueType.ScaleX));
                            Log.e("ClipRatio", "Render ScaleY: " + clip.videoProperties.getValue(VideoProperties.ValueType.ScaleY));
                            Log.e("ClipRatio", "===============================");
                            rot = (clip.videoProperties.getValue(VideoProperties.ValueType.Rot));

                            applyTransformation();
                            applyPostTransformation();
                        }

                        @Override
                        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                        }

                        @Override
                        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                            return true;
                        }

                        @Override
                        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                        }
                    });

                    break;
                }
                case AUDIO: {
                    // Audio-only clips: Use ExoPlayer for audio playback
                    exoPlayer = new ExoPlayer.Builder(context).build();

                    // Set audio attributes to ensure audio playback
                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                            .setUsage(C.USAGE_MEDIA)
                            .build();
                    exoPlayer.setAudioAttributes(audioAttributes, false);
                    exoPlayer.setVolume(1.0f);

                    // Load audio media item from original file (not preview)
                    String originalPath = clip.getAbsolutePath(data);
                    MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(new File(originalPath)));
                    exoPlayer.setMediaItem(mediaItem);
                    // Don't call prepare() here - will be called lazily

                    break;
                }
            }

            if (textureView != null) {
                setPivot();
                attachGestureControls(textureView, clip, settings, callback, textCanvasControllerInfo);
            }

        } catch (Exception e) {
            Log.e("ClipRenderer", "Exception in ClipRenderer constructor for clip: " + (clip != null ? clip.clipName : "unknown") + ", type: " + (clip != null ? clip.type : "unknown"), e);
            e.printStackTrace();
            LoggingManager.LogExceptionToNoteOverlay(context, e);
        }
    }

    public boolean isVisible(float playheadTime) {
        return playheadTime >= clip.startTime &&
                playheadTime <= clip.startTime + clip.duration;
    }

    public void renderFrame(float playheadTime, boolean isSeekingOnly) {
        if (!isVisible(playheadTime)) {
            // Reset seek position when clip is not visible
            if (exoPlayer != null && isPlaying) {
                exoPlayer.setPlayWhenReady(false);
                isPlaying = false;
            }
            lastSeekPositionMs = -1;
            return;
        }

        // Lazy load ExoPlayer - prepare only when clip becomes visible
        ensureExoPlayerPrepared();

        // Check if exoPlayer is initialized before calling startPlayingAt
        if ((clip.type == ClipType.VIDEO || clip.type == ClipType.AUDIO) && exoPlayer == null) {
            Log.w("ClipRenderer", "exoPlayer is null for " + clip.type + " clip: " + clip.clipName + ", skipping playback");
            return;
        }

        startPlayingAt(playheadTime, isSeekingOnly);
    }

    private void ensureExoPlayerPrepared() {
        // If exoPlayer is null, create it first (e.g., after migration)
        if (exoPlayer == null && (clip.type == ClipType.VIDEO || clip.type == ClipType.AUDIO)) {
            synchronized (this) {
                // Double check after acquiring lock
                if (exoPlayer != null) return;

                try {
                    Log.d("ClipRenderer", "Recreating exoPlayer for migrated clip: " + clip.clipName);
                    exoPlayer = new ExoPlayer.Builder(context).build();
                    if (exoPlayer == null) {
                        Log.e("ClipRenderer", "Failed to recreate ExoPlayer for clip: " + clip.clipName);
                        return;
                    }

                    // Reattach textureView if it exists
                    if (textureView != null) {
                        exoPlayer.setVideoTextureView(textureView);
                    }

                    // Set audio attributes
                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                            .setContentType(C.CONTENT_TYPE_MOVIE)
                            .setUsage(C.USAGE_MEDIA)
                            .build();
                    exoPlayer.setAudioAttributes(audioAttributes, false);
                    exoPlayer.setVolume(1.0f);

                    // Set media item - use preview file (keyint=1) for fast seeking
                    String previewPath = clip.getAbsolutePreviewPath(projectData);
                    String originalPath = clip.getAbsolutePath(projectData);
                    String videoPath = previewPath;
                    boolean usingPreview = true;
                    if (videoPath == null || videoPath.isEmpty() || !new File(videoPath).exists()) {
                        videoPath = originalPath;
                        usingPreview = false;
                        Log.w("ClipRenderer", "Preview file not found when recreating ExoPlayer for clip: " + clip.clipName + ", using original file. Preview seeking may be slower.");
                    }
                    if (videoPath != null && !videoPath.isEmpty()) {
                        File file = new File(videoPath);
                        if (file.exists()) {
                            MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(file));
                            exoPlayer.setMediaItem(mediaItem);
                            Log.d("ClipRenderer", "ExoPlayer recreated successfully for clip: " + clip.clipName + ", using: " + videoPath);
                        } else {
                            Log.e("ClipRenderer", "File does not exist when recreating ExoPlayer: " + videoPath);
                            exoPlayer.release();
                            exoPlayer = null;
                            return;
                        }
                    } else {
                        Log.e("ClipRenderer", "Invalid path when recreating ExoPlayer: " + videoPath);
                        exoPlayer.release();
                        exoPlayer = null;
                        return;
                    }
                } catch (Exception e) {
                    Log.e("ClipRenderer", "Error recreating ExoPlayer: " + e.getMessage(), e);
                    if (exoPlayer != null) {
                        exoPlayer.release();
                        exoPlayer = null;
                    }
                    return;
                }
            }
        }

        if (isExoPlayerPrepared || exoPlayer == null) return;

        synchronized (this) {
            if (isExoPlayerPrepared) return; // Double check

            try {
                // Add listener before prepare
                exoPlayerListener = new Player.Listener() {
                    @Override
                    public void onTracksChanged(androidx.media3.common.Tracks tracks) {
                        Log.d("ExoPlayerAudio", "Tracks changed for clip: " + clip.clipName);
                        boolean hasAudioTrack = false;
                        for (int i = 0; i < tracks.getGroups().size(); i++) {
                            androidx.media3.common.Tracks.Group trackGroup = tracks.getGroups().get(i);
                            for (int j = 0; j < trackGroup.length; j++) {
                                androidx.media3.common.Format format = trackGroup.getTrackFormat(j);
                                boolean isAudio = format.sampleMimeType != null && format.sampleMimeType.startsWith("audio/");
                                boolean isSelected = trackGroup.isTrackSelected(j);

                                if (isAudio) {
                                    hasAudioTrack = true;
                                    if (!isSelected) {
                                        exoPlayer.setTrackSelectionParameters(
                                                exoPlayer.getTrackSelectionParameters()
                                                        .buildUpon()
                                                        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
                                                        .build()
                                        );
                                    }
                                }
                            }
                        }

                        if (!hasAudioTrack && clip.isVideoHasAudio) {
                            String originalPath = clip.getAbsolutePath(projectData);
                            Log.w("ExoPlayerAudio", "Video has audio flag but no audio track found in file: " + originalPath);
                        }
                    }

                    @Override
                    public void onPlayerError(androidx.media3.common.PlaybackException error) {
                        Log.e("ExoPlayerAudio", "Player error: " + error.getMessage());
                    }
                };
                exoPlayer.addListener(exoPlayerListener);

                // Prepare ExoPlayer on main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        exoPlayer.prepare();
                        isExoPlayerPrepared = true;
                    } catch (Exception e) {
                        Log.e("ExoPlayerAudio", "Error preparing ExoPlayer: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.e("ExoPlayerAudio", "Error in ensureExoPlayerPrepared: " + e.getMessage());
            }
        }
    }

    public void startPlayingAt(float playheadTime, boolean isSeekingOnly) {
        if (!isVisible(playheadTime)) {
            return;
        }

        try {
            if (textureView != null) {
                float x = clip.keyframes.getValueAtTime(clip, playheadTime, VideoProperties.ValueType.PosX);
                float y = clip.keyframes.getValueAtTime(clip, playheadTime, VideoProperties.ValueType.PosY);
                float rotation = clip.keyframes.getValueAtTime(clip, playheadTime, VideoProperties.ValueType.Rot);
                float sx = clip.keyframes.getValueAtTime(clip, playheadTime, VideoProperties.ValueType.ScaleX);
                float sy = clip.keyframes.getValueAtTime(clip, playheadTime, VideoProperties.ValueType.ScaleY);

                posX = x == -1 ? posX : x;
                posY = y == -1 ? posY : y;
                rot = rotation == -1 ? rot : rotation;
                scaleX = sx == -1 ? scaleX : sx;
                scaleY = sy == -1 ? scaleY : sy;

                applyPostTransformation();
            }

            switch (clip.type) {
                case VIDEO: {
                    if (exoPlayer == null) {
                        Log.w("ClipRenderer", "exoPlayer is null for VIDEO clip: " + clip.clipName);
                        break;
                    }

                    if (!isSeekingOnly) {
                        if (!isPlaying) {
                            float clipTime = playheadTime - clip.startTime + clip.startClipTrim;
                            if (clipTime < 0) clipTime = 0;
                            long positionMs = (long) (clipTime * 1000);
                            exoPlayer.seekTo(positionMs);
                            lastSeekPositionMs = positionMs;

                            exoPlayer.setPlayWhenReady(true);
                            exoPlayer.setPlaybackParameters(
                                    new PlaybackParameters(callback.getPlaybackSpeed())
                            );
                            isPlaying = true;
                        }
                    } else {
                        // When seeking, always seek immediately for realtime preview
                        if (isPlaying) {
                            exoPlayer.setPlayWhenReady(false);
                            isPlaying = false;
                        }
                        float clipTime = playheadTime - clip.startTime + clip.startClipTrim;
                        if (clipTime < 0) clipTime = 0;
                        long positionMs = (long) (clipTime * 1000);
                        // Seek immediately when seeking for realtime preview (no threshold)
                        exoPlayer.seekTo(positionMs);
                        lastSeekPositionMs = positionMs;
                    }
                    break;
                }
                case AUDIO: {
                    if (exoPlayer == null) break;

                    if (!isSeekingOnly) {
                        if (!isPlaying) {
                            float clipTime = playheadTime - clip.startTime + clip.startClipTrim;
                            if (clipTime < 0) clipTime = 0;
                            long positionMs = (long) (clipTime * 1000);
                            exoPlayer.seekTo(positionMs);
                            lastSeekPositionMs = positionMs;

                            exoPlayer.setPlayWhenReady(true);
                            exoPlayer.setPlaybackParameters(
                                    new PlaybackParameters(callback.getPlaybackSpeed())
                            );
                            isPlaying = true;
                        }
                    } else {
                        // When seeking, always seek immediately for realtime preview
                        if (isPlaying) {
                            exoPlayer.setPlayWhenReady(false);
                            isPlaying = false;
                        }
                        float clipTime = playheadTime - clip.startTime + clip.startClipTrim;
                        if (clipTime < 0) clipTime = 0;
                        long positionMs = (long) (clipTime * 1000);
                        // Seek immediately when seeking for realtime preview (no threshold)
                        exoPlayer.seekTo(positionMs);
                        lastSeekPositionMs = positionMs;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            LoggingManager.LogExceptionToNoteOverlay(context, e);
        }
    }

    private void setPivot() {
        textureView.post(() -> {
            textureView.setPivotX(0);
            textureView.setPivotY(0);
        });
    }

    private void attachGestureControls(TextureView tv, Clip clip, VideoSettings settings, ClipInteractionCallback callback, TextView textCanvasControllerInfo) {
        final GestureDetector tapDrag = new GestureDetector(tv.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                initialPosX = posX;
                initialPosY = posY;
                startTouchX = e.getX();
                startTouchY = e.getY();
                lastRawX = e.getRawX();
                lastRawY = e.getRawY();
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
                float rawX = e2.getRawX();
                float rawY = e2.getRawY();
                float deltaX = rawX - lastRawX;
                float deltaY = rawY - lastRawY;
                lastRawX = rawX;
                lastRawY = rawY;

                if (!callback.isClipSelected(clip)) {
                    callback.onClipSelected(clip);
                }
                if (currentMode != EditMode.NONE && currentMode != EditMode.MOVE) {
                    return true;
                }
                currentMode = EditMode.MOVE;

                // Show drag border
                if (dragBorderDrawable == null) {
                    dragBorderDrawable = new android.graphics.drawable.GradientDrawable();
                    dragBorderDrawable.setColor(android.graphics.Color.TRANSPARENT);
                    dragBorderDrawable.setStroke(
                            (int) (1 * callback.getDisplayDensity()),
                            android.graphics.Color.YELLOW
                    );
                }
                if (dragBorderView == null) {
                    dragBorderView = new View(context);
                    FrameLayout.LayoutParams borderParams =
                            new FrameLayout.LayoutParams(textureView.getWidth(), textureView.getHeight());
                    previewViewGroupRef.addView(dragBorderView, borderParams);
                }
                dragBorderView.setBackground(dragBorderDrawable);
                dragBorderView.setVisibility(View.VISIBLE);

                // Calculate new position
                float newPosX = posX + deltaX * CANVAS_DRAG_SENSITIVITY;
                float newPosY = posY + deltaY * CANVAS_DRAG_SENSITIVITY;

                // Clamp to preview bounds
                float clipDisplayWidth = textureView.getWidth() * scaleX;
                float clipDisplayHeight = textureView.getHeight() * scaleY;
                float previewWidth = previewViewGroupRef != null ? previewViewGroupRef.getWidth() : PreviewConversionHelper.previewAvailableWidth;
                float previewHeight = previewViewGroupRef != null ? previewViewGroupRef.getHeight() : PreviewConversionHelper.previewAvailableHeight;

                if (previewWidth > 0 && previewHeight > 0) {
                    newPosX = Math.max(0f, Math.min(previewWidth - clipDisplayWidth, newPosX));
                    newPosY = Math.max(0f, Math.min(previewHeight - clipDisplayHeight, newPosY));
                }

                posX = newPosX;
                posY = newPosY;

                applyPostTransformation();

                // Sync model - convert preview position back to render position
                clip.videoProperties.setValue(PreviewConversionHelper.previewToRenderConversionX(posX, settings.videoWidth, settings.videoHeight), VideoProperties.ValueType.PosX);
                clip.videoProperties.setValue(PreviewConversionHelper.previewToRenderConversionY(posY, settings.videoWidth, settings.videoHeight), VideoProperties.ValueType.PosY);

                textCanvasControllerInfo.setText("Pos X: " + clip.videoProperties.getValue(VideoProperties.ValueType.PosX) + " | Pos Y: " + clip.videoProperties.getValue(VideoProperties.ValueType.PosY));
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                callback.onClipSelected(clip);
                return true;
            }
        });

        // Manual scale and rotation tracking
        final float[] lastSpanManual = {-1f};
        final float[] lastAngle = {Float.NaN};

        tv.setOnTouchListener((v, event) -> {
            // Handle 2-finger gestures (scale + rotate)
            if (event.getPointerCount() >= 2) {
                float x0 = event.getX(0);
                float y0 = event.getY(0);
                float x1 = event.getX(1);
                float y1 = event.getY(1);

                float currentSpan = (float) Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
                float angle = (float) Math.toDegrees(Math.atan2(y1 - y0, x1 - x0));

                if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                    lastSpanManual[0] = currentSpan;
                    lastAngle[0] = angle;
                    currentMode = EditMode.SCALE;
                    if (!callback.isClipSelected(clip)) {
                        callback.onClipSelected(clip);
                    }
                } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE && lastSpanManual[0] > 0) {
                    // Scale
                    float scaleFactor = currentSpan / lastSpanManual[0];
                    scaleX *= scaleFactor;
                    scaleY *= scaleFactor;
                    scaleX = Math.max(0.1f, Math.min(5f, scaleX));
                    scaleY = Math.max(0.1f, Math.min(5f, scaleY));
                    lastSpanManual[0] = currentSpan;

                    // Rotate
                    if (!Float.isNaN(lastAngle[0])) {
                        float deltaAngle = normalizeAngle(angle - lastAngle[0]);
                        rot += deltaAngle;
                        rot = ((rot + 360f) % 720f) - 360f;

                        float nearest = Math.round(rot / Constants.CANVAS_ROTATE_SNAP_DEGREE) * Constants.CANVAS_ROTATE_SNAP_DEGREE;
                        if (Math.abs(rot - nearest) <= Constants.CANVAS_ROTATE_SNAP_THRESHOLD_DEGREE) {
                            rot = nearest;
                        }
                    }
                    lastAngle[0] = angle;

                    // Apply transformations directly
                    textureView.setScaleX(scaleX);
                    textureView.setScaleY(scaleY);
                    textureView.setRotation(rot);

                    // Sync model - convert preview scale back to render scale
                    // IMPORTANT: Use same ratio for both X and Y to maintain aspect ratio!
                    float renderScaleX = PreviewConversionHelper.previewToRenderConversionScalingX(scaleX, settings.videoWidth, settings.videoHeight);
                    float renderScaleY = PreviewConversionHelper.previewToRenderConversionScalingY(scaleY, settings.videoWidth, settings.videoHeight);
                    clip.videoProperties.setValue(renderScaleX, VideoProperties.ValueType.ScaleX);
                    clip.videoProperties.setValue(renderScaleY, VideoProperties.ValueType.ScaleY);
                    clip.videoProperties.setValue(rot, VideoProperties.ValueType.Rot);

                    Log.d("ClipZoomDebug", "Scale sync | clip=" + clip.clipName
                            + " | previewScale=(" + scaleX + "," + scaleY + ")"
                            + " | renderScale=(" + renderScaleX + "," + renderScaleY + ")"
                            + " | shouldBeEqual=" + (Math.abs(renderScaleX - renderScaleY) < 0.001f));

                    textCanvasControllerInfo.setText(
                            "Scale: " + String.format("%.2f", scaleX) +
                                    " | Rot: " + String.format("%.1f", rot) + "°"
                    );
                } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
                    lastSpanManual[0] = -1f;
                    lastAngle[0] = Float.NaN;
                    applyPostTransformation();
                }
            }

            // Handle single finger gestures (drag)
            if (event.getPointerCount() == 1 && currentMode != EditMode.SCALE) {
                tapDrag.onTouchEvent(event);
            }

            // Handle gesture end
            if (event.getActionMasked() == MotionEvent.ACTION_UP ||
                    event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                lastAngle[0] = Float.NaN;
                lastSpanManual[0] = -1f;
                currentMode = EditMode.NONE;
                initialPosX = posX;
                initialPosY = posY;
                applyPostTransformation();
                if (dragBorderView != null) {
                    dragBorderView.setVisibility(View.GONE);
                }
            }
            return true;
        });
    }

    private void applyTransformation() {
        matrix.reset();
        matrix.postScale(scaleMatrixX, scaleMatrixY);
        matrix.postRotate(rotMatrix);
        matrix.postTranslate(posMatrixX, posMatrixY);

        textureView.setTransform(matrix);
        textureView.invalidate();
    }

    public void applyPostTransformation() {
        textureView.post(() -> {
            textureView.setPivotX(0f);
            textureView.setPivotY(0f);

            textureView.setScaleX(scaleX);
            textureView.setScaleY(scaleY);
            textureView.setRotation(rot);

            textureView.setX(posX);
            textureView.setY(posY);

            textureView.invalidate();

            // Sync drag border with textureView
            if (dragBorderView != null && dragBorderView.getVisibility() == View.VISIBLE) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) dragBorderView.getLayoutParams();
                lp.width = textureView.getWidth();
                lp.height = textureView.getHeight();
                dragBorderView.setLayoutParams(lp);
                dragBorderView.setPivotX(0f);
                dragBorderView.setPivotY(0f);
                dragBorderView.setScaleX(scaleX);
                dragBorderView.setScaleY(scaleY);
                dragBorderView.setRotation(rot);
                dragBorderView.setX(posX);
                dragBorderView.setY(posY);
            }
        });
    }

    private float normalizeAngle(float a) {
        while (a > 180f) a -= 360f;
        while (a < -180f) a += 360f;
        return a;
    }

    public enum EditMode {
        MOVE, SCALE, ROTATE, NONE
    }

    public void release() {
        if (exoPlayer != null) {
            if (exoPlayerListener != null) {
                exoPlayer.removeListener(exoPlayerListener);
                exoPlayerListener = null;
            }
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }
        isExoPlayerPrepared = false;
    }
}

