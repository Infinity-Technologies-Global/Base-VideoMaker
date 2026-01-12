package com.vanvatcorporation.doubleclips.activities;

import static com.vanvatcorporation.doubleclips.FFmpegEdit.runAnyCommand;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.TextWatcher;
import android.text.Editable;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.arthenica.ffmpegkit.Statistics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import com.vanvatcorporation.doubleclips.FFmpegEdit;
import com.vanvatcorporation.doubleclips.FXCommandEmitter;
import com.vanvatcorporation.doubleclips.R;
import com.vanvatcorporation.doubleclips.activities.editing.BaseEditSpecificAreaScreen;
import com.vanvatcorporation.doubleclips.activities.editing.ClipEditSpecificAreaScreen;
import com.vanvatcorporation.doubleclips.activities.editing.ClipsEditSpecificAreaScreen;
import com.vanvatcorporation.doubleclips.activities.editing.EffectEditSpecificAreaScreen;
import com.vanvatcorporation.doubleclips.activities.editing.TextEditSpecificAreaScreen;
import com.vanvatcorporation.doubleclips.activities.editing.TransitionEditSpecificAreaScreen;
import com.vanvatcorporation.doubleclips.activities.editing.VideoPropertiesEditSpecificAreaScreen;
import com.vanvatcorporation.doubleclips.activities.main.MainAreaScreen;
import com.vanvatcorporation.doubleclips.activities.model.Clip;
import com.vanvatcorporation.doubleclips.activities.model.ClipType;
import com.vanvatcorporation.doubleclips.activities.model.EasingType;
import com.vanvatcorporation.doubleclips.activities.model.EffectTemplate;
import com.vanvatcorporation.doubleclips.activities.model.Keyframe;
import com.vanvatcorporation.doubleclips.activities.model.AnimatedProperty;
import com.vanvatcorporation.doubleclips.activities.model.Timeline;
import com.vanvatcorporation.doubleclips.activities.model.Track;
import com.vanvatcorporation.doubleclips.activities.model.TransitionClip;
import com.vanvatcorporation.doubleclips.activities.model.VideoProperties;
import com.vanvatcorporation.doubleclips.activities.model.VideoSettings;
import com.vanvatcorporation.doubleclips.activities.model.TimelineHelper;
import com.vanvatcorporation.doubleclips.activities.model.ClipHelper;
import com.vanvatcorporation.doubleclips.activities.renderer.ClipRenderer;
import com.vanvatcorporation.doubleclips.activities.renderer.TimelineRenderer;
import com.vanvatcorporation.doubleclips.activities.renderer.ClipInteractionCallback;
import com.vanvatcorporation.doubleclips.activities.renderer.PreviewConversionHelper;
import com.vanvatcorporation.doubleclips.activities.command.UndoRedoManager;
import com.vanvatcorporation.doubleclips.activities.command.DeleteClipCommand;
import com.vanvatcorporation.doubleclips.activities.command.SplitClipCommand;
import com.vanvatcorporation.doubleclips.activities.command.ModifyClipPropertiesCommand;
import com.vanvatcorporation.doubleclips.activities.command.MoveClipCommand;
import com.vanvatcorporation.doubleclips.constants.Constants;
import com.vanvatcorporation.doubleclips.helper.DateHelper;
import com.vanvatcorporation.doubleclips.helper.IOHelper;
import com.vanvatcorporation.doubleclips.helper.IOImageHelper;
import com.vanvatcorporation.doubleclips.helper.ImageHelper;
import com.vanvatcorporation.doubleclips.helper.MathHelper;
import com.vanvatcorporation.doubleclips.helper.ParserHelper;
import com.vanvatcorporation.doubleclips.helper.StringFormatHelper;
import com.vanvatcorporation.doubleclips.impl.AppCompatActivityImpl;
import com.vanvatcorporation.doubleclips.impl.ImageGroupView;
import com.vanvatcorporation.doubleclips.impl.NavigationIconLayout;
import com.vanvatcorporation.doubleclips.impl.TrackFrameLayout;
import com.vanvatcorporation.doubleclips.impl.java.RunnableImpl;
import com.vanvatcorporation.doubleclips.manager.LoggingManager;
import com.vanvatcorporation.doubleclips.utils.TimelineUtils;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.common.util.UnstableApi;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class EditingActivity extends AppCompatActivityImpl implements ClipInteractionCallback {


    //List<Track> trackList = new ArrayList<>();
    Timeline timeline = new Timeline();
    MainAreaScreen.ProjectData properties;
    VideoSettings settings;
    private UndoRedoManager undoRedoManager;

    private LinearLayout timelineTracksContainer, rulerContainer, trackInfoLayout;
    private RelativeLayout timelineWrapper, editingZone, previewZone, editingToolsZone, outerPreviewViewGroup;
    private HorizontalScrollView timelineScroll, rulerScroll;
    private ScrollView timelineVerticalScroll, trackInfoVerticalScroll;
    private TextView currentTimePosText, durationTimePosText, textCanvasControllerInfo;
    private ImageButton addNewTrackButton;
    private FrameLayout previewViewGroup;
    private ImageButton playPauseButton, backButton, settingsButton;
    private Button exportButton;
    private TimelineRenderer timelineRenderer;

    private TrackFrameLayout addNewTrackBlankTrackSpacer;

    private TextEditSpecificAreaScreen textEditSpecificAreaScreen;
    private EffectEditSpecificAreaScreen effectEditSpecificAreaScreen;
    private TransitionEditSpecificAreaScreen transitionEditSpecificAreaScreen;
    private ClipsEditSpecificAreaScreen clipsEditSpecificAreaScreen;
    private ClipEditSpecificAreaScreen clipEditSpecificAreaScreen;
    private VideoPropertiesEditSpecificAreaScreen videoPropertiesEditSpecificAreaScreen;


    private HorizontalScrollView toolbarDefault, toolbarClips, toolbarTrack;


    public int trackCount = 0;
    private final int TRACK_HEIGHT = 100;
    public static final float MIN_CLIP_DURATION = 0.1f; // in seconds
    private static final float CANVAS_DRAG_SENSITIVITY = 1f; // 0.5 = đi chậm hơn tay, mượt hơn
    public static int pixelsPerSecond = 100;

    public static int previewAvailableWidth, previewAvailableHeight;

    public static int centerOffset;
    int currentTimelineEnd = 0; // Keep a variable that tracks the furthest X position of any clip


    float currentTime = 0f; // seconds


    static TransitionClip selectedKnot = null;
    static Clip selectedClip = null;
    static Track selectedTrack = null;

    static ArrayList<Clip> selectedClips = new ArrayList<>();
    boolean isClipSelectMultiple;


    private ScaleGestureDetector scaleDetector;
    private float scaleFactor = 1f;
    private float MIN_SCALE_FACTOR = 0.01f; // 0.05 before
    private float MAX_SCALE_FACTOR = 32f; // 8 before
    private int basePixelsPerSecond = 100;
    private float currentTimeBeforeScrolling = -1;


    Handler playheadHandler = new Handler(Looper.getMainLooper());
    Runnable updatePlayhead = new Runnable() {
        @Override
        public void run() {
//            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//                int currentPositionMs = mediaPlayer.getCurrentPosition();
//                float currentSeconds = currentPositionMs / 1000f;
//
//                int newScrollX = (int) (currentSeconds * pixelsPerSecond);
//                timelineScroll.scrollTo(newScrollX, 0);
//
//                playheadHandler.postDelayed(this, 16); // ~60fps
//            }
        }
    };
    private boolean isPlaying = false;
    private float frameInterval = 1f / 60f; // smaller step for smoother timeline updates
    private float playbackSpeed = 1f; // keep real-time speed for stable A/V sync
    private Handler playbackHandler = new Handler(Looper.getMainLooper());
    private Handler resolutionUpdateHandler = new Handler(Looper.getMainLooper());
    private Runnable resolutionUpdateRunnable;
    private Runnable playbackLoop;


    FFmpegEdit.FfmpegRenderQueue previewRenderQueue = new FFmpegEdit.FfmpegRenderQueue();


    @SuppressLint("Range")
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    int getCurrentTimeInX() {
        return getTimeInX(currentTime);
    }

    public int getTimeInX(float time) {
        return (int) (centerOffset + time * pixelsPerSecond);
    }

    private ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    // Check if multiple files are selected

                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        Uri[] uris = new Uri[count];
                        for (int i = 0; i < count; i++) {
                            uris[i] = data.getClipData().getItemAt(i).getUri();
                        }
                        processingMultiPreview(uris);
                    } else if (data.getData() != null) {
                        // Single file selected
                        Uri fileUri = data.getData();
                        // Process the file URI on background thread
                        parseFileIntoWorkPathAndAddToTrackAsync(fileUri, 0);
                    }
                }
            }
    );

    float parseFileIntoWorkPathAndAddToTrackAsync(Uri uri, float offsetTime) {
        if (uri == null) return offsetTime;
        if (selectedTrack == null) return offsetTime;

        // Process on background thread to avoid ANR
        importExecutor.execute(() -> {
            parseFileIntoWorkPathAndAddToTrack(uri, offsetTime);
        });

        return offsetTime; // Return immediately
    }

    float parseFileIntoWorkPathAndAddToTrack(Uri uri, float offsetTime) {
        if (uri == null) return offsetTime;
        if (selectedTrack == null) return offsetTime;
        String filename = getFileName(uri);
        String clipPath = IOHelper.CombinePath(properties.getProjectPath(), Constants.DEFAULT_CLIP_DIRECTORY, filename);
        String previewClipPath = IOHelper.CombinePath(properties.getProjectPath(), Constants.DEFAULT_PREVIEW_CLIP_DIRECTORY, filename);

        // Copy file - this is heavy but now on background thread
        IOHelper.writeToFileAsRaw(this, clipPath, IOHelper.readFromFileAsRaw(this, getContentResolver(), uri));

        float duration = 3f; // fallback default if needed
        String mimeType = getContentResolver().getType(uri);

        if (mimeType == null) return offsetTime;


        ClipType type;

        if (mimeType.startsWith("audio/")) type = ClipType.AUDIO;
        else if (mimeType.startsWith("image/")) type = ClipType.IMAGE;
        else if (mimeType.startsWith("video/")) type = ClipType.VIDEO;
        else type = ClipType.EFFECT; // if effect or unknown


        boolean isVideoHasAudio = false;
        int width = 0, height = 0;

        // Use MediaExtractor for metadata - faster and lighter than ExoPlayer for just metadata
        // ExoPlayer is better for playback, but MediaExtractor is faster for metadata extraction
        if (type == ClipType.VIDEO || type == ClipType.AUDIO) {
            try {
                MediaExtractor extractor = new MediaExtractor();
                extractor.setDataSource(clipPath);

                boolean hasVideoTrack = false;
                boolean hasAudioTrack = false;

                for (int i = 0; i < extractor.getTrackCount(); i++) {
                    MediaFormat format = extractor.getTrackFormat(i);
                    String trackMime = format.getString(MediaFormat.KEY_MIME);
                    if (trackMime != null) {
                        if (trackMime.startsWith("video/")) {
                            hasVideoTrack = true;
                            if (format.containsKey(MediaFormat.KEY_WIDTH)) {
                                width = format.getInteger(MediaFormat.KEY_WIDTH);
                            }
                            if (format.containsKey(MediaFormat.KEY_HEIGHT)) {
                                height = format.getInteger(MediaFormat.KEY_HEIGHT);
                            }
                        }
                        if (trackMime.startsWith("audio/")) {
                            hasAudioTrack = true;
                            isVideoHasAudio = true;
                        }
                        if (format.containsKey(MediaFormat.KEY_DURATION)) {
                            long d = format.getLong(MediaFormat.KEY_DURATION);
                            duration = d / 1_000_000f; // microseconds to seconds
                        }
                    }
                }
                extractor.release();
            } catch (Exception e) {
                Log.e("ImportFile", "Error extracting metadata: " + e.getMessage());
            }
        }
        if (type == ClipType.IMAGE) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true; // Don't load the full bitmap
            BitmapFactory.decodeFile(clipPath, options);
            width = options.outWidth;
            height = options.outHeight;
        }


        Clip newClip = new Clip(filename, currentTime + offsetTime, duration, selectedTrack.trackIndex, type, isVideoHasAudio, width, height);

        // Calculate scale to fit clip into preview (fit inside, maintain aspect ratio)
        if (width > 0 && height > 0 && settings != null && settings.videoWidth > 0 && settings.videoHeight > 0) {
            float scaleToFitX = (float) settings.videoWidth / width;
            float scaleToFitY = (float) settings.videoHeight / height;
            // Use minimum scale to fit entirely inside preview (letterbox/pillarbox)
            float fitScale = Math.min(scaleToFitX, scaleToFitY);
            newClip.videoProperties.valueScaleX = fitScale;
            newClip.videoProperties.valueScaleY = fitScale;

            // Center the clip in preview
            float scaledWidth = width * fitScale;
            float scaledHeight = height * fitScale;
            float centerPosX = (settings.videoWidth - scaledWidth) / 2f;
            float centerPosY = (settings.videoHeight - scaledHeight) / 2f;
            newClip.videoProperties.valuePosX = centerPosX;
            newClip.videoProperties.valuePosY = centerPosY;

            Log.d("ClipFit", "Clip " + filename + " | size=" + width + "x" + height
                    + " | preview=" + settings.videoWidth + "x" + settings.videoHeight
                    + " | fitScale=" + fitScale
                    + " | pos=(" + centerPosX + "," + centerPosY + ")");
        }

        // Update UI on main thread
        final Clip clipToAdd = newClip;
        runOnUiThread(() -> {
            addClipToTrack(selectedTrack, clipToAdd);

            // Debounce timeline rebuild - only rebuild after delay to avoid multiple rebuilds
            // Increased debounce to 1000ms to reduce UI updates during heavy imports
            if (timelineRebuildRunnable != null) {
                timelineRebuildHandler.removeCallbacks(timelineRebuildRunnable);
            }
            timelineRebuildRunnable = () -> regeneratingTimelineRenderer();
            timelineRebuildHandler.postDelayed(timelineRebuildRunnable, 1000); // 1000ms debounce for heavy imports
        });

        // Skip preview generation - ExoPlayer will use original files directly
        // previewRenderQueue.enqueue(new FFmpegEdit.FfmpegRenderQueue.FfmpegRenderQueueInfo("Preview Generation",
        //         () -> {
        //             processingPreview(newClip, clipPath, previewClipPath);
        //         }));

        // if(type == ClipType.VIDEO && isVideoHasAudio)
        // {
        //     previewRenderQueue.enqueue(new FFmpegEdit.FfmpegRenderQueue.FfmpegRenderQueueInfo("Preview Generation",
        //             () -> {
        //                 // Extract audio from Video if it has audio
        //                 Clip audioClip = new Clip(newClip);
        //                 audioClip.type = ClipType.AUDIO;
        //                 processingPreview(audioClip, clipPath, previewClipPath);
        //             }));
        // }

        return offsetTime + duration;
    }

    void processingPreview(Clip clip, String originalClipPath, String previewClipPath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate your custom layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.popup_processing_preview, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        builder.setNegativeButton(getText(R.string.alert_processing_without_preview_confirmation),
                (dialog, which) -> {
            // This initial listener might be simple or a placeholder
                    dialog.dismiss();
        });

        // Get references to the EditText and Buttons in your custom layout
        TextView processingText = dialogView.findViewById(R.id.adsDescriptionText);
        TextView processingDescription = dialogView.findViewById(R.id.processingDescription);
        ProgressBar previewProgressBar = dialogView.findViewById(R.id.previewProgressBar);
        TextView processingPercent = dialogView.findViewById(R.id.processingPercent);

        // Create the AlertDialog
        AlertDialog dialog = builder.create();

        // Already prevent using the setCancelable above
        //dialog.setCanceledOnTouchOutside(false);
        // Show the dialog
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);


        processingText.setText(getString(R.string.processing) + " " + "\"" + clip.getClipName() + "\"");


        String previewGeneratedVideoCmd = "-i \"" + originalClipPath +
                "\" -vf \"scale=1280:-2\" -c:v libx264 -preset ultrafast -crf 32 -x264-params keyint=1 -an -y \"" + previewClipPath + "\"";
        String previewGeneratedAudioCmd = "-i \"" + originalClipPath +
                "\" -vn -ac 1 -ar 22050 -c:a pcm_s16le -y \"" + previewClipPath.substring(0, previewClipPath.lastIndexOf('.')) + ".wav\"";


        processingDescription.setTextColor(0xFF00AA00);
        if (clip.type == ClipType.AUDIO) {
            runAnyCommand(this, previewGeneratedAudioCmd, "Exporting Preview Audio",
                    () -> EditingActivity.this.runOnUiThread(() -> {

                        dialog.dismiss();
                        previewRenderQueue.taskCompleted();
                        regeneratingTimelineRenderer();
                    }), () -> {
                        processingDescription.post(() -> {
                            processingDescription.setTextColor(0xFFFF0000);

                            dialog.setCancelable(true);
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
                        });
                    }
                    , new RunnableImpl() {
                        @Override
                        public <T> void runWithParam(T param) {
                            com.arthenica.ffmpegkit.Log log = (com.arthenica.ffmpegkit.Log) param;
                            processingDescription.post(() -> {
                                processingDescription.setText(log.getMessage());
                            });
                        }
                    }, new RunnableImpl() {
                        @Override
                        public <T> void runWithParam(T param) {
                            double duration = clip.duration * 1000;

                            Statistics statistics = (Statistics) param;
                            {
                                if (statistics.getTime() > 0) {
                                    int progress = (int) ((statistics.getTime() * 100) / (int) duration);
                                    previewProgressBar.setMax(100);
                                    previewProgressBar.setProgress(progress);
                                    processingPercent.setText(progress + "%");
                                }
                            }
                        }
                    });
        } else if (clip.type == ClipType.VIDEO) {
            runAnyCommand(this, previewGeneratedVideoCmd, "Exporting Preview Video",
                    () -> EditingActivity.this.runOnUiThread(() -> {
                        dialog.dismiss();
                        previewRenderQueue.taskCompleted();
                        regeneratingTimelineRenderer();
                    }), () -> {
                        processingDescription.post(() -> {
                            processingDescription.setTextColor(0xFFFF0000);

                            dialog.setCancelable(true);
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
                        });
                }
                    , new RunnableImpl() {
                        @Override
                        public <T> void runWithParam(T param) {
                            com.arthenica.ffmpegkit.Log log = (com.arthenica.ffmpegkit.Log) param;
                            processingDescription.post(() -> {
                                processingDescription.setText(log.getMessage());
                            });
                        }
                    }, new RunnableImpl() {
                        @Override
                        public <T> void runWithParam(T param) {
                            double duration = clip.duration * 1000;

                            Statistics statistics = (Statistics) param;
                            {
                                if (statistics.getTime() > 0) {
                                    int progress = (int) ((statistics.getTime() * 100) / (int) duration);
                                    previewProgressBar.setMax(100);
                                    previewProgressBar.setProgress(progress);
                                    processingPercent.setText(progress + "%");
                                }
                            }
                        }
                    });


        } else {
            // Any other type should be drop
            dialog.dismiss();
        }
    }

    void processingMultiPreview(Uri[] uris) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//        // Inflate your custom layout
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View dialogView = inflater.inflate(R.layout.popup_processing_preview, null);
//        builder.setCancelable(false);
//        builder.setView(dialogView);
//        builder.setNegativeButton(getText(R.string.alert_processing_without_preview_confirmation),
//                (dialog, which) -> {
//                    // This initial listener might be simple or a placeholder
//                    dialog.dismiss();
//                });
//
//        // Get references to the EditText and Buttons in your custom layout
//        TextView processingText = dialogView.findViewById(R.id.adsDescriptionText);
//        TextView processingDescription = dialogView.findViewById(R.id.processingDescription);
//        ProgressBar previewProgressBar = dialogView.findViewById(R.id.previewProgressBar);
//        TextView processingPercent = dialogView.findViewById(R.id.processingPercent);
//
//        // Create the AlertDialog
//        AlertDialog dialog = builder.create();
//
//        // Already prevent using the setCancelable above
//        //dialog.setCanceledOnTouchOutside(false);
//        // Show the dialog
//        dialog.show();

        // TODO: Find a way to render 2 uncancelable dialog for these commented line of code to work

        // Process all files sequentially on background thread to avoid ANR
        // This ensures offsetTime is calculated correctly while keeping main thread free
        importExecutor.execute(() -> {
            float offsetTime = 0f;
            for (Uri uri : uris) {
                // Process each file sequentially on background thread
                // This ensures correct offsetTime calculation and prevents ANR
                offsetTime = parseFileIntoWorkPathAndAddToTrack(uri, offsetTime);
            }
        });

    }


    void pickingContent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Media"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        undoRedoManager = new UndoRedoManager(this);

        properties = (MainAreaScreen.ProjectData) createrBundle.getSerializable("ProjectProperties");

        assert properties != null;
        settings = VideoSettings.loadSettings(this, properties);

        if (settings == null) {
            // Default canvas: 1080x608 (~16:9) để full chiều ngang trên thiết bị dọc 1080px
            settings = new VideoSettings(1080, 608, 30, 30, 30, VideoSettings.FfmpegPreset.MEDIUM, VideoSettings.FfmpegTune.ZEROLATENCY);
        }

        pixelsPerSecond = basePixelsPerSecond;


        setContentView(R.layout.layout_editing);
        timelineTracksContainer = findViewById(R.id.timeline_tracks_container);
        trackInfoLayout = findViewById(R.id.trackInfoLayout);

        timelineWrapper = findViewById(R.id.timeline_wrapper);


        editingZone = findViewById(R.id.editingZone);
        previewZone = findViewById(R.id.previewZone);
        editingToolsZone = findViewById(R.id.editingToolsZone);

        currentTimePosText = findViewById(R.id.currentTimePosText);
        durationTimePosText = findViewById(R.id.durationTimePosText);
        textCanvasControllerInfo = findViewById(R.id.textCanvasControllerInfo);

        // Example: Add button to add more tracks
        addNewTrackButton = findViewById(R.id.addTrackButton);
        addNewTrackButton.setOnClickListener(v -> {
            Track track = addNewTrack();
            track.viewRef.trackInfo = track;
        });
        //timelineTracksContainer.addView(addTrackButton);

        previewViewGroup = findViewById(R.id.previewViewGroup);
        outerPreviewViewGroup = findViewById(R.id.outerPreviewViewGroup);

        // Wait for layout to be measured, then scale previewViewGroup to fit outerPreviewViewGroup
        outerPreviewViewGroup.post(() -> {
            previewAvailableWidth = outerPreviewViewGroup.getWidth();
            previewAvailableHeight = outerPreviewViewGroup.getHeight();

            Log.d("PreviewInit", "outerPreviewViewGroup measured"
                    + " | available=" + previewAvailableWidth + "x" + previewAvailableHeight
                    + " | canvas=" + settings.videoWidth + "x" + settings.videoHeight);

            // Update PreviewConversionHelper with available preview size
            PreviewConversionHelper.previewAvailableWidth = previewAvailableWidth;
            PreviewConversionHelper.previewAvailableHeight = previewAvailableHeight;

            // Scale previewViewGroup to fit within outerPreviewViewGroup while maintaining canvas aspect ratio
            updatePreviewViewGroupSize();
        });

        playPauseButton = findViewById(R.id.playPauseButton);
        playPauseButton.setOnClickListener(v -> {

            isPlaying = !isPlaying;

            if (isPlaying) {
                startPlayback();
                playPauseButton.setImageResource(R.drawable.baseline_pause_circle_24);
            } else {
                stopPlayback();
            }
        });
        exportButton = findViewById(R.id.exportButton);
        exportButton.setOnClickListener(v -> {
            TimelineHelper.saveTimeline(this, timeline, properties, settings);
            Intent intent = new Intent(this, ExportActivity.class);
            intent.putExtra("ProjectProperties", properties);
            intent.putExtra("ProjectSettings", settings);
            intent.putExtra("ProjectTimeline", timeline);
            startActivity(intent);
        });
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });
        settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            videoPropertiesEditSpecificAreaScreen.open();
        });


        // Time ruler
        rulerContainer = findViewById(R.id.ruler_container);

        timelineScroll = findViewById(R.id.trackHorizontalScrollView);
        rulerScroll = findViewById(R.id.ruler_scroll);
        timelineScroll.post(() -> {

            centerOffset = timelineScroll.getWidth() / 2;
            timelineScroll.scrollTo(centerOffset, 0);

            // Add initial track after centerOffset is taken
            //addNewTrack();


            timeline = TimelineHelper.loadTimeline(this, this, properties);
        });
        timelineScroll.getViewTreeObserver().addOnScrollChangedListener(() -> {
            rulerScroll.scrollTo(timelineScroll.getScrollX(), 0);

            if (!isPlaying) {
                float newCurrentTime = (timelineScroll.getScrollX()) / (float) pixelsPerSecond;
                currentTime = newCurrentTime;
            }

            // Get time (- centerOffset mean remove the start spacer)
            //float totalSeconds = (timelineScroll.getScrollX()) / (float) pixelsPerSecond;
            currentTimePosText.post(() -> currentTimePosText.setText(DateHelper.convertTimestampToMMSSFormat((long) (currentTime * 1000L)) + String.format(".%02d", ((long) ((currentTime % 1) * 100)))));

            // Update preview immediately for realtime seeking
            if (!isPlaying) {
                // Update preview immediately for realtime feel
                boolean isSeekingOnly = true;
                timelineRenderer.updateTime(currentTime, isSeekingOnly);
            } else {
                // During playback, update immediately
                timelineRenderer.updateTime(currentTime, false);
            }

            // Trigger thumbnail rendering when seeking (debounced to avoid too frequent calls)
            if (!isPlaying) {
                // Debounce: only trigger if time changed significantly or enough time passed since last trigger
                if (Math.abs(currentTime - lastThumbnailTriggerTime) > 0.5f) {
                    triggerThumbnailRenderingDebounced(currentTime);
                    lastThumbnailTriggerTime = currentTime;
                }
            }
        });


        timelineVerticalScroll = findViewById(R.id.trackVerticalScrollView);
        trackInfoVerticalScroll = findViewById(R.id.trackInfoScroll);


        timelineVerticalScroll.getViewTreeObserver().addOnScrollChangedListener(() -> {
            trackInfoVerticalScroll.scrollTo(0, timelineVerticalScroll.getScrollY());
        });


        addNewTrackBlankTrackSpacer = new TrackFrameLayout(this);
        addNewTrackBlankTrackSpacer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,//ViewGroup.LayoutParams.MATCH_PARENT,
                TRACK_HEIGHT
        ));
        addNewTrackBlankTrackSpacer.setBackgroundColor(Color.parseColor("#222222"));
        addNewTrackBlankTrackSpacer.setPadding(4, 4, 4, 4);
        handleTrackInteraction(addNewTrackBlankTrackSpacer);


        setupPreview();

        setupTimelinePinchAndZoom();

        setupSpecificEdit();

        setupToolbars();

        handleEditZoneInteraction(timelineScroll);
    }

    private void editingMultiple() {
        clipsEditSpecificAreaScreen.open();
    }

    private void editingSpecific(ClipType type) {
        switch (type) {
            case TEXT:
                textEditSpecificAreaScreen.open();
                break;
            case EFFECT:
                effectEditSpecificAreaScreen.open();
                break;
            case TRANSITION:
                transitionEditSpecificAreaScreen.open();
                break;
            case VIDEO:
            case IMAGE:
                clipEditSpecificAreaScreen.open();
                break;
        }
    }

    // Bottom Navigation Bar
    private void setupToolbars() {
        // ===========================       CRITICAL ZONE       ====================================

        toolbarDefault = (HorizontalScrollView) LayoutInflater.from(this).inflate(R.layout.view_toolbar_default, null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        editingToolsZone.addView(toolbarDefault, params);

        toolbarTrack = (HorizontalScrollView) LayoutInflater.from(this).inflate(R.layout.view_toolbar_track, null);
        RelativeLayout.LayoutParams paramsTrack = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsTrack.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        editingToolsZone.addView(toolbarTrack, paramsTrack);
        toolbarTrack.setVisibility(View.GONE);

        toolbarClips = (HorizontalScrollView) LayoutInflater.from(this).inflate(R.layout.view_toolbar_clips, null);
        RelativeLayout.LayoutParams paramsClips = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsClips.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        editingToolsZone.addView(toolbarClips, paramsClips);
        toolbarClips.setVisibility(View.GONE);

        // ===========================       CRITICAL ZONE       ====================================


        // ===========================       DEFAULT ZONE       ====================================

        // Undo/Redo buttons in default toolbar
        View undoButtonView = toolbarDefault.findViewById(R.id.undoButton);
        View redoButtonView = toolbarDefault.findViewById(R.id.redoButton);
        if (undoButtonView == null || redoButtonView == null) {
            // If buttons not in toolbar layout, use buttons from main layout
            undoButtonView = findViewById(R.id.undoButton);
            redoButtonView = findViewById(R.id.redoButton);
        }
        if (undoButtonView != null) {
            undoButtonView.setOnClickListener(v -> {
                if (undoRedoManager == null) {
                    undoRedoManager = new UndoRedoManager(this);
                }
                if (undoRedoManager.canUndo()) {
                    undoRedoManager.undo();
                }
            });
        }
        if (redoButtonView != null) {
            redoButtonView.setOnClickListener(v -> {
                if (undoRedoManager == null) {
                    undoRedoManager = new UndoRedoManager(this);
                }
                if (undoRedoManager.canRedo()) {
                    undoRedoManager.redo();
                }
            });
        }

        toolbarDefault.findViewById(R.id.splitMediaButton).setOnClickListener(v -> {
            List<Clip> affectedClips = timeline.getClipAtCurrentTime(currentTime);
            if (selectedClip != null && affectedClips.contains(selectedClip)) {
                ClipHelper.splitClip(selectedClip, this, timeline, currentTime);
            } else {
                for (Clip clip : affectedClips) {
                    ClipHelper.splitClip(clip, this, timeline, currentTime);
                }

            }
        });


        // ===========================       DEFAULT ZONE       ====================================


        // ===========================       TRACK ZONE       ====================================


        toolbarTrack.findViewById(R.id.addMediaButton).setOnClickListener(v -> {
            if (selectedTrack != null)
                pickingContent();
            else
                new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a track first!").show();
        });
        toolbarTrack.findViewById(R.id.deleteTrackButton).setOnClickListener(v -> {
            if (selectedTrack != null) {
                selectedTrack.delete(timeline, timelineTracksContainer, trackInfoLayout, this);
                updateCurrentClipEnd();
            } else
                new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();

        });
        toolbarTrack.findViewById(R.id.splitMediaButton).setOnClickListener(v -> {
            List<Clip> affectedClips = timeline.getClipAtCurrentTime(currentTime);
            if (selectedClip != null && affectedClips.contains(selectedClip)) {
                ClipHelper.splitClip(selectedClip, this, timeline, currentTime);
            } else {
                for (Clip clip : affectedClips) {
                    ClipHelper.splitClip(clip, this, timeline, currentTime);
                }

            }
        });

        toolbarTrack.findViewById(R.id.addTextButton).setOnClickListener(v -> {

            if (selectedTrack == null) {
                new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a track first!").show();
                return;
            }

            float duration = 3f; // fallback default if needed
            ClipType type = ClipType.TEXT; // if effect or unknown


            Clip newClip = new Clip("TEXT", currentTime, duration, selectedTrack.trackIndex, type, false, 0, 0);
            newClip.textContent = "Simple text";
            newClip.fontSize = 30;
            addClipToTrack(selectedTrack, newClip);
        });
        toolbarTrack.findViewById(R.id.addEffectButton).setOnClickListener(v -> {

            if (selectedTrack == null) {
                new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a track first!").show();
                return;
            }

            float duration = 3f; // fallback default if needed
            ClipType type = ClipType.EFFECT; // if effect or unknown

            Clip newClip = new Clip("EFFECT", currentTime, duration, selectedTrack.trackIndex, type, false, 0, 0);
            newClip.effect = new EffectTemplate("glitch-pulse", 1.2, 4.0);

            addClipToTrack(selectedTrack, newClip);
        });
        toolbarTrack.findViewById(R.id.selectAllButton).setOnClickListener(v -> {
            // Todo: Not fully implemented yet. The idea is to remake the whole thing, get the "array" of selected clip is completed
            // now if one clip is move then the whole array move along. Also ghost will be as well


            if (selectedTrack != null) {
                isClipSelectMultiple = true;
                ((NavigationIconLayout) toolbarClips.findViewById(R.id.selectMultipleButton)).getIconView().setColorFilter(0xFFFF0000, PorterDuff.Mode.SRC_ATOP);

                deselectingClip();

                for (Clip clip : selectedTrack.clips) {
                    selectingClip(clip);
                }
            } else
                new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a track first!").show();
        });
        toolbarTrack.findViewById(R.id.autoSnapButton).setOnClickListener(v -> {
            if (selectedTrack != null) {
                selectedTrack.sortClips();
                List<Clip> clips = selectedTrack.clips;
                for (int i = 1; i < clips.size(); i++) {
                    Clip clip = clips.get(i);
                    Clip prevClip = clips.get(i - 1);

                    clip.startTime = prevClip.startTime + prevClip.duration;
                }

                updateClipLayouts();
                updateCurrentClipEnd();
            } else
                new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a track first!").show();
        });


        // ===========================       TRACK ZONE       ====================================


        // ===========================       CLIPS ZONE       ====================================

        // Undo/Redo buttons
        ImageButton undoButton = findViewById(R.id.undoButton);
        ImageButton redoButton = findViewById(R.id.redoButton);
        if (undoButton != null) {
            undoButton.setOnClickListener(v -> {
                if (undoRedoManager == null) {
                    undoRedoManager = new UndoRedoManager(this);
                }
                if (undoRedoManager.canUndo()) {
                    undoRedoManager.undo();
                }
            });
        }
        if (redoButton != null) {
            redoButton.setOnClickListener(v -> {
                if (undoRedoManager == null) {
                    undoRedoManager = new UndoRedoManager(this);
                }
                if (undoRedoManager.canRedo()) {
                    undoRedoManager.redo();
                }
            });
        }

        toolbarClips.findViewById(R.id.deleteMediaButton).setOnClickListener(v -> {
            if (selectedClip != null) {
                DeleteClipCommand command = new DeleteClipCommand(selectedClip, timeline);
                undoRedoManager.executeCommand(command);
            } else if (selectedClips != null && !selectedClips.isEmpty()) {
                for (Clip clip : selectedClips) {
                    DeleteClipCommand command = new DeleteClipCommand(clip, timeline);
                    undoRedoManager.executeCommand(command);
                }
            } else {
                new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();
            }
        });
        toolbarClips.findViewById(R.id.splitMediaButton).setOnClickListener(v -> {
            if (undoRedoManager == null) {
                undoRedoManager = new UndoRedoManager(this);
            }
            List<Clip> affectedClips = timeline.getClipAtCurrentTime(currentTime);
            if (selectedClip != null && affectedClips.contains(selectedClip)) {
                SplitClipCommand command = new SplitClipCommand(selectedClip, timeline, currentTime);
                undoRedoManager.executeCommand(command);
            } else {
                new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();
            }
        });
        toolbarClips.findViewById(R.id.editMediaButton).setOnClickListener(v -> {
            if (selectedClip != null) {
                editingSpecific(selectedClip.type);
            } else if (!selectedClips.isEmpty()) {
                editingMultiple();
            } else
                new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();
        });
        toolbarClips.findViewById(R.id.addKeyframeButton).setOnClickListener(v -> {
            if (selectedClip != null) {
                addKeyframe(selectedClip, currentTime);
            } else
                new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();
        });
        toolbarClips.findViewById(R.id.selectMultipleButton).setOnClickListener(v -> {
            // Todo: Not fully implemented yet. The idea is to remake the whole thing, get the "array" of selected clip is completed
            // now if one clip is move then the whole array move along. Also ghost will be as well

            isClipSelectMultiple = !isClipSelectMultiple;

            ((NavigationIconLayout) toolbarClips.findViewById(R.id.selectMultipleButton)).getIconView().setColorFilter((isClipSelectMultiple ? 0xFFFF0000 : 0xFFFFFFFF), PorterDuff.Mode.SRC_ATOP);
        });
        toolbarClips.findViewById(R.id.applyKeyframeToAllClip).setOnClickListener(v -> {

            if (selectedTrack != null && selectedClip != null) {

                List<Clip> clips = selectedTrack.clips;
                for (Clip clip : clips) {
                    if (clip != selectedClip) {
                        clearKeyframe(clip);

                        for (Keyframe keyframe : selectedClip.keyframes.keyframes) {
                            addKeyframe(clip, keyframe);
                        }

                    }
                }
            } else
                new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a track first!").show();

        });
        toolbarClips.findViewById(R.id.restateButton).setOnClickListener(v -> {
            if (selectedClip != null) {
                ClipHelper.restate(selectedClip);

                // TODO: Find a way to specifically build only the edited clip. Not entire timeline
                //  this is just for testing. Resource-consuming asf.
                regeneratingTimelineRenderer();


            } else
                new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();
        });

        // ===========================       CLIPS ZONE       ====================================
    }

    private void setupSpecificEdit() {
        // ===========================       TEXT ZONE       ====================================
        textEditSpecificAreaScreen = (TextEditSpecificAreaScreen) LayoutInflater.from(this).inflate(R.layout.view_edit_specific_text, null);
        editingZone.addView(textEditSpecificAreaScreen);
        // ===========================       TEXT ZONE       ====================================


        // ===========================       EFFECT ZONE       ====================================
        effectEditSpecificAreaScreen = (EffectEditSpecificAreaScreen) LayoutInflater.from(this).inflate(R.layout.view_edit_specific_effect, null);
        editingZone.addView(effectEditSpecificAreaScreen);
        // ===========================       EFFECT ZONE       ====================================


        // ===========================       TRANSITION ZONE       ====================================
        transitionEditSpecificAreaScreen = (TransitionEditSpecificAreaScreen) LayoutInflater.from(this).inflate(R.layout.view_edit_specific_transition, null);
        editingZone.addView(transitionEditSpecificAreaScreen);
        // ===========================       TRANSITION ZONE       ====================================

        // ===========================       MULTIPLE CLIPS ZONE       ====================================
        clipsEditSpecificAreaScreen = (ClipsEditSpecificAreaScreen) LayoutInflater.from(this).inflate(R.layout.view_edit_multiple_clips, null);
        editingZone.addView(clipsEditSpecificAreaScreen);
        // ===========================       MULTIPLE CLIPS ZONE       ====================================

        // ===========================       CLIP ZONE       ====================================
        clipEditSpecificAreaScreen = (ClipEditSpecificAreaScreen) LayoutInflater.from(this).inflate(R.layout.view_edit_specific_clip, null);
        editingZone.addView(clipEditSpecificAreaScreen);
        // ===========================       CLIP ZONE       ====================================


        // ===========================       VIDEO PROPERTIES ZONE       ====================================
        videoPropertiesEditSpecificAreaScreen = (VideoPropertiesEditSpecificAreaScreen) LayoutInflater.from(this).inflate(R.layout.view_edit_specific_video_properties, null);
        previewZone.addView(videoPropertiesEditSpecificAreaScreen);
        // ===========================       VIDEO PROPERTIES ZONE       ====================================


        // ===========================       TEXT ZONE       ====================================

        textEditSpecificAreaScreen.onClose.add(() -> {
            if (selectedClip != null) {
                selectedClip.textContent = textEditSpecificAreaScreen.textEditContent.getText().toString();
                selectedClip.fontSize = ParserHelper.TryParse(textEditSpecificAreaScreen.textSizeContent.getText().toString(), 28f);
            }
        });
        textEditSpecificAreaScreen.onOpen.add(() -> {
            textEditSpecificAreaScreen.textEditContent.setText(selectedClip.textContent);
            textEditSpecificAreaScreen.textSizeContent.setText(String.valueOf(selectedClip.fontSize));
        });

        // ===========================       TEXT ZONE       ====================================


        // ===========================       EFFECT ZONE       ====================================


        effectEditSpecificAreaScreen.onClose.add(() -> {
            if (selectedClip != null) {
                selectedClip.effect = new EffectTemplate((String) FXCommandEmitter.FXRegistry.effectsFXMap.keySet().toArray()[effectEditSpecificAreaScreen.effectEditContent.getSelectedItemPosition()], ParserHelper.TryParse(effectEditSpecificAreaScreen.effectDurationContent.getText().toString(), 1), 1);
            }
        });
        effectEditSpecificAreaScreen.onOpen.add(() -> {
            List<String> stringEffects = Arrays.asList(FXCommandEmitter.FXRegistry.effectsFXMap.values().toArray(new String[0]));
            effectEditSpecificAreaScreen.effectEditContent.setSelection(stringEffects.indexOf(FXCommandEmitter.FXRegistry.effectsFXMap.get(selectedClip.effect.style)));
            effectEditSpecificAreaScreen.effectDurationContent.setText(String.valueOf(selectedClip.effect.duration));
        });

        // ===========================       EFFECT ZONE       ====================================


        // ===========================       TRANSITION ZONE       ====================================


        transitionEditSpecificAreaScreen.onClose.add(() -> {
            if (selectedKnot != null) {
                for (int i = 0; i < timeline.tracks.get(selectedKnot.trackIndex).clips.size(); i++) {
                    TransitionClip clip = timeline.tracks.get(selectedKnot.trackIndex).clips.get(i).endTransition;
                    if (clip == selectedKnot) {
                        clip.duration = ParserHelper.TryParse(transitionEditSpecificAreaScreen.transitionDurationContent.getText().toString(), 0.5f);
                        clip.effect.style = (String) FXCommandEmitter.FXRegistry.transitionFXMap.keySet().toArray()[transitionEditSpecificAreaScreen.transitionEditContent.getSelectedItemPosition()];
                        clip.effect.duration = ParserHelper.TryParse(transitionEditSpecificAreaScreen.transitionDurationContent.getText().toString(), 0.5f);
                        clip.mode = TransitionClip.TransitionMode.values()[transitionEditSpecificAreaScreen.transitionModeEditContent.getSelectedItemPosition()];
//
//                        for (TransitionClip clip2 : timeline.tracks.get(selectedKnot.trackIndex).transitions)
//                            System.err.println(clip2.duration);
                        selectedKnot = null;
                        break;
                    }
                }
            }
        });
        transitionEditSpecificAreaScreen.applyAllTransitionButton.setOnClickListener(v -> {
            transitionEditSpecificAreaScreen.animateLayout(BaseEditSpecificAreaScreen.AnimationType.Close);
            if (selectedKnot != null) {
                for (int i = 0; i < timeline.tracks.get(selectedKnot.trackIndex).clips.size(); i++) {
                    TransitionClip clip = timeline.tracks.get(selectedKnot.trackIndex).clips.get(i).endTransition;
                    if (clip == null) continue;
                    clip.duration = ParserHelper.TryParse(transitionEditSpecificAreaScreen.transitionDurationContent.getText().toString(), 0.5f);
                    clip.effect.style = (String) FXCommandEmitter.FXRegistry.transitionFXMap.keySet().toArray()[transitionEditSpecificAreaScreen.transitionEditContent.getSelectedItemPosition()];
                    clip.effect.duration = ParserHelper.TryParse(transitionEditSpecificAreaScreen.transitionDurationContent.getText().toString(), 0.5f);
                    clip.mode = TransitionClip.TransitionMode.values()[transitionEditSpecificAreaScreen.transitionModeEditContent.getSelectedItemPosition()];
                }
//                for (TransitionClip clip : timeline.tracks.get(selectedKnot.trackIndex).transitions)
//                    System.err.println(clip.duration);
                selectedKnot = null;

            }
        });
        transitionEditSpecificAreaScreen.onOpen.add(() -> {
            List<String> stringTransition = Arrays.asList(FXCommandEmitter.FXRegistry.transitionFXMap.values().toArray(new String[0]));
            transitionEditSpecificAreaScreen.transitionEditContent.setSelection(stringTransition.indexOf(FXCommandEmitter.FXRegistry.transitionFXMap.get(selectedKnot.effect.style)));
            transitionEditSpecificAreaScreen.transitionDurationContent.setText(String.valueOf(selectedKnot.effect.duration));
            transitionEditSpecificAreaScreen.transitionModeEditContent.setSelection(selectedKnot.mode.ordinal());
        });

        // ===========================       TRANSITION ZONE       ====================================


        // ===========================       MULTIPLE CLIPS ZONE       ====================================

        clipsEditSpecificAreaScreen.onClose.add(() -> {
            if (!selectedClips.isEmpty()) {
                for (Clip clip : selectedClips) {
                    float defaultValue = clip.duration;
                    clip.duration = ParserHelper.TryParse(clipsEditSpecificAreaScreen.clipsDurationContent.getText().toString(), defaultValue);
                }
                updateClipLayouts();
                updateCurrentClipEnd();
            }
        });
        clipsEditSpecificAreaScreen.onOpen.add(() -> {
            // TODO: Didn't change following the endClipTrim/startClipTrim rule yet.
            clipsEditSpecificAreaScreen.clipsDurationContent.setText(String.valueOf(selectedClips.get(0).duration));
        });


        // ===========================       MULTIPLE CLIPS ZONE       ====================================


        // ===========================       CLIP ZONE       ====================================

        clipEditSpecificAreaScreen.onClose.add(() -> {
            if (selectedClip != null) {
                VideoProperties newProperties = new VideoProperties();
                newProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.positionXField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.PosX)), VideoProperties.ValueType.PosX);
                newProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.positionYField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.PosY)), VideoProperties.ValueType.PosY);
                newProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.rotationField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.Rot)), VideoProperties.ValueType.Rot);
                newProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.scaleXField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.ScaleX)), VideoProperties.ValueType.ScaleX);
                newProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.scaleYField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.ScaleY)), VideoProperties.ValueType.ScaleY);
                newProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.opacityField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.Opacity)), VideoProperties.ValueType.Opacity);
                newProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.speedField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.Speed)), VideoProperties.ValueType.Speed);

                String newClipName = clipEditSpecificAreaScreen.clipNameField.getText().toString();
                float newDuration = ParserHelper.TryParse(clipEditSpecificAreaScreen.durationContent.getText().toString(), selectedClip.duration);
                boolean newIsMute = clipEditSpecificAreaScreen.muteAudioCheckbox.isChecked();

                if (undoRedoManager == null) {
                    undoRedoManager = new UndoRedoManager(this);
                }
                ModifyClipPropertiesCommand command = new ModifyClipPropertiesCommand(selectedClip, newProperties, newClipName, newDuration, newIsMute);
                undoRedoManager.executeCommand(command);

                clipEditSpecificAreaScreen.keyframeScrollFrame.removeAllViews();
            }
        });
        clipEditSpecificAreaScreen.onOpen.add(() -> {
            clipEditSpecificAreaScreen.totalDurationText.setText(String.valueOf(selectedClip.originalDuration));
            clipEditSpecificAreaScreen.clipNameField.setText(String.valueOf(selectedClip.getClipName()));
            clipEditSpecificAreaScreen.durationContent.setText(String.valueOf(selectedClip.duration));
            clipEditSpecificAreaScreen.positionXField.setText(String.valueOf(selectedClip.videoProperties.getValue(VideoProperties.ValueType.PosX)));
            clipEditSpecificAreaScreen.positionYField.setText(String.valueOf(selectedClip.videoProperties.getValue(VideoProperties.ValueType.PosY)));
            clipEditSpecificAreaScreen.rotationField.setText(String.valueOf(selectedClip.videoProperties.getValue(VideoProperties.ValueType.Rot)));
            clipEditSpecificAreaScreen.scaleXField.setText(String.valueOf(selectedClip.videoProperties.getValue(VideoProperties.ValueType.ScaleX)));
            clipEditSpecificAreaScreen.scaleYField.setText(String.valueOf(selectedClip.videoProperties.getValue(VideoProperties.ValueType.ScaleY)));
            clipEditSpecificAreaScreen.opacityField.setText(String.valueOf(selectedClip.videoProperties.getValue(VideoProperties.ValueType.Opacity)));
            clipEditSpecificAreaScreen.speedField.setText(String.valueOf(selectedClip.videoProperties.getValue(VideoProperties.ValueType.Speed)));

            clipEditSpecificAreaScreen.muteAudioCheckbox.setChecked(selectedClip.isMute);


            for (Keyframe keyframe : selectedClip.keyframes.keyframes) {
                clipEditSpecificAreaScreen.createKeyframeElement(selectedClip, keyframe, () -> {
                    setCurrentTime(keyframe.getGlobalTime(selectedClip));
                }, () -> {
                    removeKeyframe(selectedClip, keyframe);
                });
            }

            clipEditSpecificAreaScreen.clearKeyframeButton.setOnClickListener(v -> {
                if (selectedClip != null) {
                    clearKeyframe(selectedClip);
                }
            });
        });


        // ===========================       CLIP ZONE       ====================================


        // ===========================       VIDEO PROPERTIES ZONE       ====================================

        videoPropertiesEditSpecificAreaScreen.onClose.add(() -> {
            // Default 16:9 canvas ~1080px width nếu người dùng không nhập
            settings.videoWidth = ParserHelper.TryParse(videoPropertiesEditSpecificAreaScreen.resolutionXField.getText().toString(), 1080);
            settings.videoHeight = ParserHelper.TryParse(videoPropertiesEditSpecificAreaScreen.resolutionYField.getText().toString(), 608);
            settings.crf = ParserHelper.TryParse(videoPropertiesEditSpecificAreaScreen.bitrateField.getText().toString(), 30);


            ViewGroup.LayoutParams previewViewGroupParams = previewViewGroup.getLayoutParams();
            previewViewGroupParams.width = settings.videoWidth;
            previewViewGroupParams.height = settings.videoHeight;
            previewViewGroup.setLayoutParams(previewViewGroupParams);
        });
        videoPropertiesEditSpecificAreaScreen.onOpen.add(() -> {
            videoPropertiesEditSpecificAreaScreen.resolutionXField.setText(String.valueOf(settings.getVideoWidth()));
            videoPropertiesEditSpecificAreaScreen.resolutionYField.setText(String.valueOf(settings.getVideoHeight()));
            videoPropertiesEditSpecificAreaScreen.bitrateField.setText(String.valueOf(settings.getCRF()));
        });

        videoPropertiesEditSpecificAreaScreen.resolutionXField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreviewResolution();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        videoPropertiesEditSpecificAreaScreen.resolutionYField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreviewResolution();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // ===========================       VIDEO PROPERTIES ZONE       ====================================

    }

    private void updatePreviewResolution() {
        if (videoPropertiesEditSpecificAreaScreen == null ||
                videoPropertiesEditSpecificAreaScreen.resolutionXField == null ||
                videoPropertiesEditSpecificAreaScreen.resolutionYField == null) {
            return;
        }
        int newWidth = ParserHelper.TryParse(videoPropertiesEditSpecificAreaScreen.resolutionXField.getText().toString(), settings.videoWidth);
        int newHeight = ParserHelper.TryParse(videoPropertiesEditSpecificAreaScreen.resolutionYField.getText().toString(), settings.videoHeight);
        if (newWidth > 0 && newHeight > 0) {
            float oldRatio = (float) settings.videoWidth / settings.videoHeight;
            float newRatio = (float) newWidth / newHeight;

            Log.e("ResolutionUpdate", "=== RESOLUTION UPDATE DEBUG ===");
            Log.e("ResolutionUpdate", "Old resolution: " + settings.videoWidth + "x" + settings.videoHeight + " (ratio: " + oldRatio + ")");
            Log.e("ResolutionUpdate", "New resolution: " + newWidth + "x" + newHeight + " (ratio: " + newRatio + ")");
            Log.e("ResolutionUpdate", "PreviewAvailable: " + previewAvailableWidth + "x" + previewAvailableHeight);
            Log.e("ResolutionUpdate", "PreviewViewGroup size: " + previewViewGroup.getWidth() + "x" + previewViewGroup.getHeight());
            Log.e("ResolutionUpdate", "================================");

            settings.videoWidth = newWidth;
            settings.videoHeight = newHeight;
            
            // Update preview size to match new resolution
            updatePreviewViewGroupSize();
            
            if (resolutionUpdateRunnable != null) {
                resolutionUpdateHandler.removeCallbacks(resolutionUpdateRunnable);
            }
            resolutionUpdateRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.e("ResolutionUpdate", "Rebuilding timeline renderer with new resolution: " + settings.videoWidth + "x" + settings.videoHeight);
                    regeneratingTimelineRenderer();
                }
            };
            resolutionUpdateHandler.postDelayed(resolutionUpdateRunnable, 500);
        }
    }

    /**
     * Scale previewViewGroup theo đúng ratio canvas và hành vi:
     * - Video landscape (width >= height): NGANG full, DỌC căn giữa.
     * - Video portrait  (width <  height): DỌC full, NGANG căn giữa.
     * => Preview hiển thị giống export, luôn giữ đúng aspect ratio.
     */
    private void updatePreviewViewGroupSize() {
        if (previewViewGroup == null || outerPreviewViewGroup == null) return;
        if (previewAvailableWidth <= 0 || previewAvailableHeight <= 0) return;
        if (settings.videoWidth <= 0 || settings.videoHeight <= 0) return;

        final int canvasW = settings.videoWidth;
        final int canvasH = settings.videoHeight;

        // Tỉ lệ canvas (video)
        float canvasRatio = (float) canvasW / (float) canvasH;
        float containerRatio = (float) previewAvailableWidth / (float) previewAvailableHeight;

        float fitScale;
        int previewWidth;
        int previewHeight;

        if (canvasW >= canvasH) {
            // LANDSCAPE: full chiều ngang
            fitScale = (float) previewAvailableWidth / canvasW;
            previewWidth = (int) (canvasW * fitScale);   // ≈ previewAvailableWidth
            previewHeight = (int) (canvasH * fitScale);  // <= previewAvailableHeight
        } else {
            // PORTRAIT: full chiều dọc
            fitScale = (float) previewAvailableHeight / canvasH;
            previewHeight = (int) (canvasH * fitScale);  // ≈ previewAvailableHeight
            previewWidth = (int) (canvasW * fitScale);   // <= previewAvailableWidth
        }

        // Set kích thước gốc (chưa scale) = canvas resolution
        ViewGroup.LayoutParams params = previewViewGroup.getLayoutParams();
        params.width = settings.videoWidth;
        params.height = settings.videoHeight;
        previewViewGroup.setLayoutParams(params);

        // Scale đều theo fitScale
        previewViewGroup.setScaleX(fitScale);
        previewViewGroup.setScaleY(fitScale);

        // Pivot ở góc trên trái để translate dễ hiểu
        previewViewGroup.setPivotX(0);
        previewViewGroup.setPivotY(0);

        float offsetX;
        float offsetY;

        if (canvasW >= canvasH) {
            // LANDSCAPE: ngang full, dọc căn giữa
            offsetX = 0f;
            offsetY = (previewAvailableHeight - previewHeight) / 2f;
        } else {
            // PORTRAIT: dọc full, ngang căn giữa
            offsetX = (previewAvailableWidth - previewWidth) / 2f;
            offsetY = 0f;
        }
        previewViewGroup.setTranslationX(offsetX);
        previewViewGroup.setTranslationY(offsetY);

        Log.d("PreviewSize", "Canvas: " + canvasW + "x" + canvasH
                + " | CanvasRatio: " + canvasRatio
                + " | Container: " + previewAvailableWidth + "x" + previewAvailableHeight
                + " | ContainerRatio: " + containerRatio
                + " | fitScale: " + fitScale
                + " | PreviewSize: " + previewWidth + "x" + previewHeight
                + " | Offset: (" + offsetX + "," + offsetY + ")"
                + " | Mode: " + (canvasW >= canvasH ? "LANDSCAPE_FULL_WIDTH" : "PORTRAIT_FULL_HEIGHT"));

        // Hệ toạ độ logic vẫn là canvas resolution (trước khi apply scale transform).
        // Conversion giữa preview <-> render sử dụng kích thước canvas.
        PreviewConversionHelper.previewAvailableWidth = canvasW;
        PreviewConversionHelper.previewAvailableHeight = canvasH;
    }

    public void setupTimelinePinchAndZoom() {
        scaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                if (currentTimeBeforeScrolling == -1) {
                    currentTimeBeforeScrolling = currentTime;
                }

                scaleFactor *= detector.getScaleFactor();

                // Clamp scale factor
                scaleFactor = Math.max(MIN_SCALE_FACTOR, Math.min(scaleFactor, MAX_SCALE_FACTOR));

                pixelsPerSecond = (int) (basePixelsPerSecond * scaleFactor);
                updateTimelineZoom();
                return true;
            }
        });

    }

    private void startPlayback() {

        timelineRenderer.startPlayAt(currentTime);
        playbackLoop = new Runnable() {
            @Override
            public void run() {
                if (!isPlaying) return;
                currentTime += frameInterval * playbackSpeed;

                timelineRenderer.updateTime(currentTime, false);

                // Trigger thumbnail rendering periodically during playback (every 2 seconds)
                if ((int) (currentTime * 10) % 20 == 0) { // Every 2 seconds (20 * 0.1s)
                    if (Math.abs(currentTime - lastThumbnailTriggerTime) > 1.5f) { // Only trigger if at least 1.5s passed
                        triggerThumbnailRenderingDebounced(currentTime);
                        lastThumbnailTriggerTime = currentTime;
                    }
                }

                int newScrollX = (int) (currentTime * pixelsPerSecond);
                timelineScroll.scrollTo(newScrollX, 0);

                if (currentTime >= timeline.duration) {
                    isPlaying = false;
                    currentTime = 0f;
                    stopPlayback();
                }


                playbackHandler.postDelayed(this, (long) (frameInterval * 1000));
            }
        };
        playbackHandler.post(playbackLoop);
    }

    private void stopPlayback() {
        isPlaying = false;

        playbackHandler.removeCallbacks(playbackLoop);
        playPauseButton.setImageResource(R.drawable.baseline_play_circle_24);

        // Stop ExoPlayer for all clip renderers
        if (timelineRenderer != null && timelineRenderer.trackLayers != null) {
            for (List<ClipRenderer> track : timelineRenderer.trackLayers) {
                for (ClipRenderer cr : track) {
                    if (cr.exoPlayer != null) {
                        cr.exoPlayer.setPlayWhenReady(false);
                        cr.isPlaying = false;
                    }
                }
            }
        }

        timelineRenderer.updateTime(currentTime, true);
    }

    private ExecutorService timelineBuildExecutor = Executors.newSingleThreadExecutor();
    private ExecutorService importExecutor = Executors.newFixedThreadPool(1); // Single thread to process files sequentially and avoid overwhelming system
    private ExecutorService thumbnailExecutor = Executors.newFixedThreadPool(4); // Multiple threads for parallel thumbnail rendering
    private Handler timelineRebuildHandler = new Handler(Looper.getMainLooper());
    private Runnable timelineRebuildRunnable;
    private Handler thumbnailTriggerHandler = new Handler(Looper.getMainLooper());
    private Runnable thumbnailTriggerRunnable;
    private float lastThumbnailTriggerTime = -1f; // Track last triggered time to avoid duplicate triggers

    // Track which thumbnails have been rendered for each clip
    // Map<Clip, Set<Integer>> where Integer is the frame index (0-based, 1 frame per second)
    private java.util.Map<Clip, java.util.Set<Integer>> renderedThumbnails = new java.util.concurrent.ConcurrentHashMap<>();

    public void regeneratingTimelineRenderer() {
        Log.d("TimelineRenderer", "regeneratingTimelineRenderer called");
        LoggingManager.LogToToast(this, "Begin prepare for preview!");

        // Build timeline on background thread to avoid ANR
        timelineBuildExecutor.execute(() -> {
            Log.d("TimelineRenderer", "Executing buildTimeline on background thread");
            // DON'T release old renderers here - buildTimeline() will handle migration and release unused ones
            // This prevents exoPlayer from being null when trying to migrate
            runOnUiThread(() -> {
                // Only remove views, don't release renderers yet
                previewViewGroup.removeAllViews();

                // Add black background
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                View blackBox = new View(this);
                blackBox.setBackgroundColor(Color.BLACK);
                previewViewGroup.addView(blackBox, params);
            });

            // Build timeline on background thread
            timelineRenderer.buildTimeline(timeline, properties, settings, EditingActivity.this, previewViewGroup, textCanvasControllerInfo);

            // Re-add textureViews of migrated ClipRenderers to previewViewGroup on UI thread
            runOnUiThread(() -> {
                if (timelineRenderer != null) {
                    timelineRenderer.reAddMigratedViews(previewViewGroup);
                }
            });

            // Update preview on UI thread when done
            runOnUiThread(() -> {
                if (timelineRenderer != null) {
                    timelineRenderer.updateTime(currentTime, true);
                }
            });
        });
    }

    private void setCurrentTime(float value) {
        currentTime = value;
        timelineScroll.scrollTo((int) (currentTime * pixelsPerSecond), 0);
    }

    void setupPreview() {
        timelineRenderer = new TimelineRenderer(this);


        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenHeight = metrics.heightPixels;
        editingZone.getLayoutParams().height = (int) (screenHeight * 0.35);
        editingZone.requestLayout();


        regeneratingTimelineRenderer();
    }

    void reloadPreviewClip() {
//        mediaPlayer.reset();
//        try {
//            mediaPlayer.setDataSource(IOHelper.CombinePath(properties.getProjectPath(), Constants.DEFAULT_PREVIEW_CLIP_FILENAME));
//        } catch (IOException e) {
//            LoggingManager.LogExceptionToNoteOverlay(this, e);
//        }
//        mediaPlayer.prepareAsync();
//
//        mediaPlayer.setOnPreparedListener(mp -> {
//            playPauseButton.setOnClickListener(v -> {
//                if (mediaPlayer.isPlaying()) {
//                    mediaPlayer.pause();
//                    playPauseButton.setImageResource(R.drawable.baseline_play_circle_24);
//                } else {
//                    mediaPlayer.start();
//                    playheadHandler.post(updatePlayhead); // Start syncing
//                    playPauseButton.setImageResource(R.drawable.baseline_pause_circle_24);
//                }
//            });
//        });
//
//        mediaPlayer.setOnCompletionListener(mp -> {
//            playPauseButton.setImageResource(R.drawable.baseline_play_circle_24);
//            playPauseButton.setOnClickListener(v -> {
//                mediaPlayer.start();
//                playheadHandler.post(updatePlayhead); // Start syncing
//                playPauseButton.setImageResource(R.drawable.baseline_pause_circle_24);
//            });
//        });


    }

    @Override
    public void finish() {
        super.finish();

        timelineRenderer.release();
        TimelineHelper.saveTimeline(this, timeline, properties, settings);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (timelineRenderer != null) {
        timelineRenderer.release();
        }
        TimelineHelper.saveTimeline(this, timeline, properties, settings);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (timelineRenderer == null) {
            timelineRenderer = new TimelineRenderer(this);
        }
        regeneratingTimelineRenderer();
        timelineRenderer.updateTime(currentTime, true);
    }

    private Track addNewTrack() {
        Track trackInfo = new Track(trackCount, addNewTrackUi());
        timeline.addTrack(trackInfo);

        return trackInfo;
        // Add a sample clip to the new track
//        addClipToTrack(trackInfo, "/storage/emulated/0/DoubleClips/sample.mp4", 0, Random.Range(1, 6));
    }

    public TrackFrameLayout addNewTrackUi() {
        TrackFrameLayout track = new TrackFrameLayout(this);
        track.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,//ViewGroup.LayoutParams.MATCH_PARENT,
                TRACK_HEIGHT
        ));
        track.setBackgroundColor(Color.parseColor("#222222"));
        track.setPadding(4, 4, 4, 4);

        // 👻 Add spacer to align 0s with center playhead
        View startSpacer = new View(this);
        TrackFrameLayout.LayoutParams spacerParams = new TrackFrameLayout.LayoutParams(
                centerOffset,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        startSpacer.setLayoutParams(spacerParams);
        track.addView(startSpacer); // Add spacer before any clips


        View endSpacer = new View(this);
        TrackFrameLayout.LayoutParams endParams = new TrackFrameLayout.LayoutParams(
                centerOffset,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        endSpacer.setLayoutParams(endParams);
        track.addView(endSpacer); // Add this after all clips


        TextView trackInfoView = new TextView(this);
        LinearLayout.LayoutParams trackInfoParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                TRACK_HEIGHT
        );
        trackInfoView.setLayoutParams(trackInfoParams);
        trackInfoView.setGravity(Gravity.CENTER);
        trackInfoView.setText("Track " + trackCount);


        timelineTracksContainer.removeView(addNewTrackBlankTrackSpacer);
        timelineTracksContainer.addView(track, trackCount);
        timelineTracksContainer.addView(addNewTrackBlankTrackSpacer);
        trackCount++;

        trackInfoLayout.removeView(addNewTrackButton);
        trackInfoLayout.addView(trackInfoView);
        trackInfoLayout.addView(addNewTrackButton);

        handleTrackInteraction(track);

        return track;
    }

    public void addClipToTrack(Track track, Clip data) {
        addClipToTrackUi(track.viewRef, data);

        track.addClip(data);
    }

    public void addClipToTrackUi(TrackFrameLayout trackLayout, Clip data) {
        ImageGroupView clipView = new ImageGroupView(this);
        TrackFrameLayout.LayoutParams params = new TrackFrameLayout.LayoutParams(
                (int) (data.duration * pixelsPerSecond),
                TRACK_HEIGHT - 4 // 16
        );
        //params.leftMargin = getTimeInX(data.startTime);
        //params.topMargin = 4; // 8
        clipView.setX(getTimeInX(data.startTime));
        clipView.setLayoutParams(params);
        clipView.setTag(data);

        // Extract thumbnail on background thread to avoid ANR
        // Set placeholder first, then update with actual thumbnail when ready
        String previewPath = data.getAbsolutePreviewPath(properties);
        String originalPath = data.getAbsolutePath(properties.getProjectPath());
        
        // Check if preview file exists and log warning if not
        boolean previewExists = IOHelper.isFileExist(previewPath);
        if (!previewExists && data.type == ClipType.VIDEO) {
            Log.w("EditingActivity", "Preview file not found for clip: " + data.clipName + ". Preview seeking may be slower. Preview path: " + previewPath);
        }

        importExecutor.execute(() -> {
            try {
                // Try preview path first, fallback to original path if preview doesn't exist
                String thumbnailPath = previewPath;
                if (!previewExists) {
                    thumbnailPath = originalPath;
                }

                // Wait for file to be ready (retry up to 5 times with 200ms delay)
                int retries = 0;
                while (!IOHelper.isFileExist(thumbnailPath) && retries < 5) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    retries++;
                }

                if (!IOHelper.isFileExist(thumbnailPath)) {
                    Log.w("Thumbnail", "File not found after retries: " + thumbnailPath);
                    return; // Skip thumbnail if file doesn't exist
                }

                // Extract thumbnails progressively - only 20 seconds before currentTime, render parallel
                extractThumbnailsProgressively(this, thumbnailPath, data, clipView, currentTime);
            } catch (Exception e) {
                Log.e("Thumbnail", "Error extracting thumbnail from " + previewPath + ": " + e.getMessage(), e);
            }
        });

        ClipHelper.registerClipHandle(data, clipView, this, timelineScroll);

        clipView.post(() -> {
            updateCurrentClipEnd();

            for (Keyframe keyframe : data.keyframes.keyframes) {
                addKeyframeUi(data, keyframe);
            }
        });

        trackLayout.addView(clipView);
        handleClipInteraction(clipView);
    }

    public void addKnotTransition(TransitionClip clip, Clip clipA) {
        View knotView = new View(this);
        knotView.setBackgroundColor(Color.RED);
        knotView.setVisibility(View.VISIBLE);

        knotView.setTag(R.id.transition_knot_tag, clip);
        knotView.setTag(R.id.clip_knot_tag, clipA);
        // Position it between clips
        int width = 50;
        int height = 50;

        knotView.setPivotX((float) width / 2);
        knotView.setPivotY((float) height / 2);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        //params.leftMargin = clipB.getLeft() - (width / 2); // center between clips
        params.topMargin = clipA.viewRef.getTop() + (clipA.viewRef.getHeight() / 2) - (height / 2);
        knotView.setX(clipA.viewRef.getX() + (clipA.duration * pixelsPerSecond) - (width / 2));
        timeline.tracks.get(clip.trackIndex).viewRef.addView(knotView, params);

        handleKnotInteraction(knotView);
    }

    public void addKeyframe(Clip clip, float keyframeTime) {
        addKeyframe(clip, new Keyframe(keyframeTime - clip.startTime, new VideoProperties(
                clip.videoProperties.getValue(VideoProperties.ValueType.PosX), clip.videoProperties.getValue(VideoProperties.ValueType.PosY),
                clip.videoProperties.getValue(VideoProperties.ValueType.Rot),
                clip.videoProperties.getValue(VideoProperties.ValueType.ScaleX), clip.videoProperties.getValue(VideoProperties.ValueType.ScaleY),
                clip.videoProperties.getValue(VideoProperties.ValueType.Opacity), clip.videoProperties.getValue(VideoProperties.ValueType.Speed)
        ), EasingType.LINEAR));
    }

    public void addKeyframe(Clip clip, Keyframe keyframe) {
        addKeyframeUi(clip, keyframe);

        clip.keyframes.keyframes.add(keyframe);
    }

    public void addKeyframeUi(Clip clip, Keyframe keyframe) {
        View knotView = new View(this);
        knotView.setBackgroundColor(Color.BLUE);
        knotView.setVisibility(View.VISIBLE);

        knotView.setTag(R.id.keyframe_knot_tag, keyframe);
        knotView.setTag(R.id.clip_knot_tag, clip);
        // Position it between clips
        int width = 12;
        int height = clip.viewRef.getHeight();

        knotView.setPivotX((float) width / 2);
        knotView.setPivotY((float) height / 2);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        //params.leftMargin = getCurrentTimeInX();
        //params.topMargin = clip.viewRef.getTop() + (clip.viewRef.getHeight() / 2);
        params.topMargin = clip.viewRef.getTop() + (clip.viewRef.getHeight() / 2) - (height / 2);
        knotView.setX(keyframe.getLocalTime() * pixelsPerSecond);

        //timeline.tracks.get(clip.trackIndex).viewRef.addView(knotView, params);
        clip.viewRef.addView(knotView, params);


        handleKeyframeInteraction(knotView);
    }


    public void removeKeyframe(Clip clip, Keyframe keyframe) {
        removeKeyframeUi(clip, keyframe);

        clip.keyframes.keyframes.remove(keyframe);
    }

    public void removeKeyframeUi(Clip clip, Keyframe keyframe) {
        for (int i = 0; i < clip.viewRef.getChildCount(); i++) {
            View knotView = clip.viewRef.getChildAt(i);
            if (knotView.getTag(R.id.keyframe_knot_tag) instanceof Keyframe && knotView.getTag(R.id.keyframe_knot_tag) == keyframe) {
                clip.viewRef.removeViewAt(i);
                break;
            }
        }
    }

    public void clearKeyframe(Clip clip) {
        clearKeyframeUi(clip);

        clip.keyframes.keyframes.clear();
    }

    public void clearKeyframeUi(Clip clip) {
        for (int i = 0; i < clip.viewRef.getChildCount(); i++) {
            View knotView = clip.viewRef.getChildAt(i);
            if (knotView.getTag(R.id.keyframe_knot_tag) instanceof Keyframe) {
                clip.viewRef.removeView(knotView);
            }
        }
    }

    public void revalidationClipView(Clip data) {
        ImageGroupView clipView = data.viewRef;
        clipView.getLayoutParams().width = (int) (data.duration * pixelsPerSecond);


    }

    private void handleEditZoneInteraction(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (isPlaying)
                        stopPlayback();
                    break;
                // ACTION_UP is the action that invoke only if we clicked
                // that's mean its invoke if we didn't ACTION_MOVE
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    break;
                // ACTION_CANCEL is when you accidentally click something and
                // drag it somewhere so it doesn't recognize that click anymore
                case MotionEvent.ACTION_CANCEL:
                    break;
            }
            return false;
        });
    }

    private void handleKeyframeInteraction(View view) {
        view.setOnClickListener(v -> {
            if (view.getTag(R.id.keyframe_knot_tag) instanceof Keyframe) {
                Keyframe data = (Keyframe) view.getTag(R.id.keyframe_knot_tag);
            }

        });
    }

    private void handleKnotInteraction(View view) {
        view.setOnClickListener(v -> {
            if (view.getTag(R.id.transition_knot_tag) instanceof TransitionClip) {
                selectingKnot((TransitionClip) view.getTag(R.id.transition_knot_tag));
                editingSpecific(ClipType.TRANSITION);
            }

        });
    }

    private void handleTrackInteraction(View view) {
        view.setOnClickListener(v -> {
            if (view instanceof TrackFrameLayout) {
                selectingTrack(((TrackFrameLayout) view).trackInfo);
            }
        });

        view.setOnTouchListener((v, event) -> {
            scaleDetector.onTouchEvent(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() == 2) {
                        timelineScroll.requestDisallowInterceptTouchEvent(true);
                    }
                    break;
                    // ACTION_UP is the action that invoke only if we clicked
                    // that's mean its invoke if we didn't ACTION_MOVE
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    currentTimeBeforeScrolling = -1;
                    timelineScroll.requestDisallowInterceptTouchEvent(false);
                    break;
                    // ACTION_CANCEL is when you accidentally click something and
                    // drag it somewhere so it doesn't recognize that click anymore
                case MotionEvent.ACTION_CANCEL:
                    currentTimeBeforeScrolling = -1;
                    timelineScroll.requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return true;
        });

    }

    private void handleClipInteraction(View view) {
        DragContext dragContext = new DragContext();
        view.setOnClickListener(v -> {
            Clip clip = (Clip) view.getTag(); // Already stored

            selectingClip(clip);
        });


        view.setOnLongClickListener(v -> {

            Clip clip = (Clip) view.getTag(); // Already stored
            Track track = timeline.tracks.get(clip.trackIndex);


            timelineScroll.requestDisallowInterceptTouchEvent(true);

            // 👻 Create ghost
            ImageGroupView ghost = new ImageGroupView(v.getContext());
            ghost.setLayoutParams(new TrackFrameLayout.LayoutParams(v.getWidth(), v.getHeight()));
            ghost.setFilledImageBitmap(((ImageGroupView) v).getFilledImageBitmap());
            ghost.setAlpha(0.5f);
            android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
            border.setColor(android.graphics.Color.TRANSPARENT);
            border.setStroke((int) (1 * getResources().getDisplayMetrics().density), Color.YELLOW);
            ghost.setBackground(border);


            track.viewRef.addView(ghost);
            ghost.setX(view.getX());
            ghost.setY(view.getY());

            v.setVisibility(View.INVISIBLE); // Hide original

            dragContext.ghost = ghost;
            dragContext.currentTrack = track;
            dragContext.clip = clip;

            return true;
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            float dX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Clip data = (Clip) v.getTag();


                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        //dY = v.getY() - event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (dragContext.ghost != null) {
                            float newX = event.getRawX() + dX;
                            if (newX < centerOffset) newX = centerOffset; // ⛔ Prevent going past 0s

                            float ghostWidth = dragContext.ghost.getWidth();
                            float ghostStart = newX;
                            float ghostEnd = newX + ghostWidth;


                            // 🧲 Check for snapping
                            // Snap the playhead
                            float playheadX = (timelineScroll.getScrollX() + centerOffset) - 2;

                            if (Math.abs(ghostStart - playheadX) < Constants.TRACK_CLIPS_SNAP_THRESHOLD_PIXEL) {
                                newX = playheadX;
                            }
                            if (Math.abs(ghostEnd - playheadX) < Constants.TRACK_CLIPS_SNAP_THRESHOLD_PIXEL) {
                                newX = playheadX - ghostWidth;
                            }


                            // Snap the other track
                            for (Track track : timeline.tracks) {

                                for (int i = 0; i < track.viewRef.getChildCount(); i++) {

                                    View other = track.viewRef.getChildAt(i);
                                if (other == dragContext.ghost || other == v) continue;
                                    if (!(other.getTag() instanceof Clip)) continue;


                                float otherStart = other.getX();
                                float otherEnd = other.getX() + other.getWidth();

                                // Snap ghost start to other end
                                if (Math.abs(ghostStart - otherEnd) <= Constants.TRACK_CLIPS_SNAP_THRESHOLD_PIXEL) {
                                    newX = otherEnd;
                                    break;
                                }

                                // Snap ghost end to other start
                                if (Math.abs(ghostEnd - otherStart) <= Constants.TRACK_CLIPS_SNAP_THRESHOLD_PIXEL) {
                                    newX = otherStart - ghostWidth;
                                    break;
                                }

                                // Optional: Snap start-to-start or end-to-end
                                // Todo: Pending removal as no sense of letting clips overlapping each other in the same track in the near future.
//                                    if (Math.abs(ghostStart - otherStart) <= Constants.TRACK_CLIPS_SNAP_THRESHOLD_PIXEL) {
//                                        newX = otherStart;
//                                        break;
//                                    }
//                                    if (Math.abs(ghostEnd - otherEnd) <= Constants.TRACK_CLIPS_SNAP_THRESHOLD_PIXEL) {
//                                        newX = otherEnd - ghostWidth;
//                                        break;
//                                    }
                                }
                            }
                            dragContext.ghost.setX(newX);

                            // 🔍 Detect track under finger
                            Track targetTrack = null;
                            float touchY = event.getRawY();


                            for (Track track : timeline.tracks) {
                                TrackFrameLayout trackRef = track.viewRef;

                                int[] loc = new int[2];
                                trackRef.getLocationOnScreen(loc);
                                float top = loc[1];
                                float bottom = top + trackRef.getHeight();
                                // 🧲 Move ghost to new track if needed
                                if (touchY >= top && touchY <= bottom && track != dragContext.currentTrack) {
                                    dragContext.currentTrack.viewRef.removeView(dragContext.ghost);
                                    track.viewRef.addView(dragContext.ghost);
                                    dragContext.ghost.setY(4);
                                    dragContext.currentTrack = track;
                                    break;
                                }
                            }
                            return true;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        timelineScroll.requestDisallowInterceptTouchEvent(false);

                        if (dragContext.ghost != null) {
                            float finalX = dragContext.ghost.getX();
                            if (finalX < centerOffset)
                                finalX = centerOffset; // ⛔ Clamp again for safety


                            float finalX1 = finalX;

                            v.post(() -> {
                                // Save old state for undo
                                int oldTrackIndex = dragContext.clip.trackIndex;
                                float oldStartTime = dragContext.clip.startTime;
                                
                                // Move original to new track and position
                                ViewGroup oldParent = (ViewGroup) v.getParent();
                                oldParent.removeView(v);

                                // Add to new track
                                dragContext.currentTrack.viewRef.addView(v);
                                v.setX(finalX1);
                                v.setVisibility(View.VISIBLE);

                                // Update metadata
                                float newStartTime = (finalX1 - centerOffset) / pixelsPerSecond;
                                dragContext.clip.startTime = Math.max(0, newStartTime); // Clamp to 0
                                timeline.tracks.get(dragContext.clip.trackIndex).removeClip(dragContext.clip);
                                dragContext.clip.trackIndex = dragContext.currentTrack.trackIndex;
                                timeline.tracks.get(dragContext.clip.trackIndex).addClip((dragContext.clip));

                                // Add move command for undo/redo (move already done, just record it)
                                if (undoRedoManager == null) {
                                    undoRedoManager = new UndoRedoManager(EditingActivity.this);
                                }
                                MoveClipCommand moveCommand = new MoveClipCommand(
                                    dragContext.clip, 
                                    timeline, 
                                    oldTrackIndex, 
                                    dragContext.clip.trackIndex, 
                                    oldStartTime, 
                                    dragContext.clip.startTime
                                );
                                undoRedoManager.addCommand(moveCommand);

                                updateCurrentClipEnd();

                                // Remove ghost
                                dragContext.currentTrack.viewRef.removeView(dragContext.ghost);
                                dragContext.ghost = null;

                                timeline.tracks.get(dragContext.clip.trackIndex).sortClips();
                            });

                        }
                        break;
                }
                return false;
            }
        });
    }


    public void updateCurrentClipEnd() {
        updateCurrentClipEnd(true);
    }

    void updateCurrentClipEnd(boolean updateRuler) {
        int newTimelineEnd = 0;
        // 🧠 Recalculate max right edge of all clips in all tracks
        for (Track trackCpn : timeline.tracks) {
            for (int i = 0; i < trackCpn.viewRef.getChildCount(); i++) {
                View child = trackCpn.viewRef.getChildAt(i);
                if (child.getTag() != null && child.getTag() instanceof Clip) { // It's a clip
                    int right = (int) (child.getX() + child.getWidth());
                    if (right > newTimelineEnd) newTimelineEnd = right;

                    // For keyframe syncing
                    if (child instanceof ImageGroupView) {
                        ImageGroupView clipViewRef = ((ImageGroupView) child);
                        for (int j = 0; j < clipViewRef.getChildCount(); j++) {
                            View child1 = clipViewRef.getChildAt(j);
                            if (child1.getTag(R.id.keyframe_knot_tag) instanceof Keyframe) {
                                child1.setX(((Keyframe) child1.getTag(R.id.keyframe_knot_tag)).getLocalTime() * pixelsPerSecond);
                            }
                        }
                    }
                }
            }
        }
        currentTimelineEnd = newTimelineEnd;

        // Get time (- centerOffset mean remove the start spacer)
        float totalSeconds = (currentTimelineEnd - centerOffset) / (float) pixelsPerSecond;
        durationTimePosText.post(() -> durationTimePosText.setText(DateHelper.convertTimestampToMMSSFormat((long) (totalSeconds * 1000L))));
        timeline.duration = totalSeconds;
        if (updateRuler)
            updateRuler(totalSeconds, currentRulerInterval);
        // 🧱 Expand end spacer
        for (Track trackCpn : timeline.tracks) {
            updateEndSpacer(trackCpn);
            updateTrackWidth(trackCpn);
            updateTransitionKnot(trackCpn);
        }


        //refreshPreviewClip();
    }

    void updateTransitionKnot(Track track) {

        //TODO: Use endTransition from clipA and toggle visibility and on/off of the transition
        ArrayList<Clip> snappedClipStart = new ArrayList<>();
        ArrayList<Clip> snappedClipEnd = new ArrayList<>();


//                              ArrayList<View> sortedTrackClips = IntStream.range(0, dragContext.currentTrack.viewRef.getChildCount()).mapToObj(i -> dragContext.currentTrack.viewRef.getChildAt(i)).filter(clipView -> clipView.getTag() instanceof Clip).collect(Collectors.toCollection(ArrayList::new));

//        track.clearTransition();
        track.disableTransitions();

        // Check for snapped track
        for (int i = 1; i < track.clips.size(); i++) {
            Clip at = track.clips.get(i - 1);
            Clip other = track.clips.get(i);
            // Snap ghost start to other end
            if (Math.abs(at.startTime - (other.startTime + other.duration)) <= Constants.TRACK_CLIPS_SNAP_THRESHOLD_PIXEL / pixelsPerSecond) {
                snappedClipEnd.add(at);
                snappedClipStart.add(other);
            }
            // Snap ghost end to other start
            if (Math.abs((at.startTime + at.duration) - other.startTime) <= Constants.TRACK_CLIPS_SNAP_THRESHOLD_PIXEL / pixelsPerSecond) {
                snappedClipEnd.add(other);
                snappedClipStart.add(at);
            }
        }

        for (int i = 0; i < snappedClipStart.size(); i++) {
            addTransitionBridge(snappedClipStart.get(i), snappedClipEnd.get(i), 0.2f);
        }
    }

    void updateTrackWidth(Track track) {
        track.viewRef.setLayoutParams(new LinearLayout.LayoutParams(
                centerOffset + currentTimelineEnd, // End spacer = centerOffset
                TRACK_HEIGHT
        ));
    }

    private void updateEndSpacer(Track track) {
        View existingSpacer = track.viewRef.findViewWithTag("end_spacer");
        if (existingSpacer != null) {
            track.viewRef.removeView(existingSpacer);
        }

        View endSpacer = new View(this);
        endSpacer.setTag("end_spacer");
        TrackFrameLayout.LayoutParams params = new TrackFrameLayout.LayoutParams(
                centerOffset, // Always half screen
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        endSpacer.setLayoutParams(params);
        track.viewRef.addView(endSpacer);
    }

    private void updateRuler(float totalSeconds, float interval) {
        rulerContainer.removeAllViews();


        // 👻 Add spacer to align 0s with center playhead
        View startSpacerRuler = new View(this);
        LinearLayout.LayoutParams spacerRulerParams = new LinearLayout.LayoutParams(
                centerOffset,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        startSpacerRuler.setLayoutParams(spacerRulerParams);
        rulerContainer.addView(startSpacerRuler); // Add spacer before any clips

        for (float i = 0; i <= totalSeconds; i += interval) {
            TextView tick = new TextView(this);
            tick.setText(StringFormatHelper.smartRound(i, 1, true) + "s");
            //tick.setTextColor(Color.BLACK);
            tick.setTextSize(12);
            tick.setGravity(Gravity.START | Gravity.BOTTOM);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (pixelsPerSecond * interval), ViewGroup.LayoutParams.MATCH_PARENT
            );
            tick.setLayoutParams(params);
            rulerContainer.addView(tick);
        }

        // Add end spacer to ruler
        View rulerEndSpacer = new View(this);
        rulerEndSpacer.setLayoutParams(new LinearLayout.LayoutParams(
                centerOffset, ViewGroup.LayoutParams.MATCH_PARENT
        ));
        rulerContainer.addView(rulerEndSpacer);
    }

    float currentRulerInterval = 1f;
    float changedRulerInterval = 1f;

    private void updateRulerEfficiently() {
        if (pixelsPerSecond < 10 && pixelsPerSecond > 5) {
            changedRulerInterval = 32f;
        }
        // Recently updated. Below are tested.
        if (pixelsPerSecond < 15 && pixelsPerSecond > 10) {
            changedRulerInterval = 16f;
        }
        if (pixelsPerSecond < 25 && pixelsPerSecond > 15) {
            changedRulerInterval = 8f;
        }
        if (pixelsPerSecond < 50 && pixelsPerSecond > 25) {
            changedRulerInterval = 4f;
        }
        if (pixelsPerSecond < 100 && pixelsPerSecond > 50) {
            changedRulerInterval = 2f;
        }
        if (pixelsPerSecond < 200 && pixelsPerSecond > 100) {
            changedRulerInterval = 1f;
        }
        if (pixelsPerSecond < 500 && pixelsPerSecond > 200) {
            changedRulerInterval = 0.5f;
        }
        if (pixelsPerSecond < 1000 && pixelsPerSecond > 500) {
            changedRulerInterval = 0.2f;
        }
        if (pixelsPerSecond < 2000 && pixelsPerSecond > 1000) {
            changedRulerInterval = 0.1f;
        }
        if (pixelsPerSecond < 5000 && pixelsPerSecond > 2000) {
            changedRulerInterval = 0.05f;
        }
        // Recently updated. Above are tested.
        if (pixelsPerSecond < 10000 && pixelsPerSecond > 5000) {
            changedRulerInterval = 0.02f;
        }
        if (pixelsPerSecond < 20000 && pixelsPerSecond > 10000) {
            changedRulerInterval = 0.01f;
        }
        if (pixelsPerSecond < 50000 && pixelsPerSecond > 20000) {
            changedRulerInterval = 0.005f;
        }


        for (int i = 0; i < rulerContainer.getChildCount(); i++) {
            View tick = rulerContainer.getChildAt(i);
            if (!(tick instanceof TextView)) continue;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tick.getLayoutParams();
            params.width = (int) (pixelsPerSecond * changedRulerInterval); // or pixelsPerSecond / 2 for 0.5s ticks
            tick.setLayoutParams(params);
        }
    }

    public void updateClipLayouts() {
        for (Track track : timeline.tracks) {
            for (int i = 0; i < track.viewRef.getChildCount(); i++) {
                View clip = track.viewRef.getChildAt(i);
                if (clip.getTag() instanceof Clip) {
                    Clip data = (Clip) clip.getTag();

                    TrackFrameLayout.LayoutParams params = new TrackFrameLayout.LayoutParams(
                            (int) (data.duration * pixelsPerSecond),
                            TRACK_HEIGHT - 4);
                    //params.leftMargin = (int) (centerOffset + data.startTime * pixelsPerSecond);
                    //params.topMargin = 4;\
                    clip.setX(getTimeInX(data.startTime));
                    clip.setLayoutParams(params);
                }
                if (clip.getTag(R.id.transition_knot_tag) instanceof TransitionClip) {
                    TransitionClip data = (TransitionClip) clip.getTag(R.id.transition_knot_tag);
                    //clip.setX(getTimeInX(data.time));
                }
                if (clip.getTag(R.id.keyframe_knot_tag) instanceof Keyframe) {
                    Keyframe data = (Keyframe) clip.getTag(R.id.keyframe_knot_tag);
                    Clip data2 = (Clip) clip.getTag(R.id.clip_knot_tag);

                    clip.setX(getTimeInX(data.getGlobalTime(data2)));
                }
            }
        }
    }

    private void updateEndSpacers() {
        for (Track track : timeline.tracks) {
            updateEndSpacer(track);
        }
    }

    private void updateTimelineZoom() {
        updateRulerEfficiently();
        boolean rulerChanged = false;
        if (changedRulerInterval != currentRulerInterval) {
            currentRulerInterval = changedRulerInterval;
            rulerChanged = true;
            updateRuler(timeline.duration, currentRulerInterval);
        }

        updateClipLayouts();
        updateCurrentClipEnd(false);
        //updateEndSpacers(); // updateCurrentClipEnd did it
        //updateEndSpacers(); // Optional: recalculate based on new width

        // Let playhead froze when scrolling
        setCurrentTime(currentTimeBeforeScrolling);


        //Todo: Still resource-intensive, recode it by cache the created thumbnail, then insert/remove accordingly.
        if (rulerChanged) {
            for (Track track : timeline.tracks) {
                for (Clip clip : track.clips) {
                    //clip.viewRef.setFilledImageBitmap(combineThumbnails(extractThumbnail(this, clip.filePath, clip.type)));
                }
            }
        }
    }

    private void addTransitionBridge(Clip clipA, Clip clipB, float transitionDuration) {
        //if (clipA.startTime + clipA.duration == clipB.startTime)
        {
            // If null then define new transition, else keep the transition from the clip.
            if (clipA.endTransition == null) {
                TransitionClip transition = new TransitionClip();
                transition.trackIndex = clipA.trackIndex;
                transition.startTime = clipB.startTime - transitionDuration / 2;
                transition.duration = transitionDuration;
                transition.effect = new EffectTemplate("fade", transitionDuration, transition.startTime);
                transition.mode = TransitionClip.TransitionMode.OVERLAP;

                clipA.endTransition = transition;
            }

            clipA.endTransitionEnabled = true;

            addTransitionBridgeUi(clipA.endTransition, clipA);
        }

    }

    private void addTransitionBridgeUi(TransitionClip transitionClip, Clip clip) {
        addKnotTransition(transitionClip, clip);
    }


    private void selectingClip(Clip selectedClip) {
        if (isClipSelectMultiple) {
            this.selectedClip = null;
            if (selectedClips.contains(selectedClip)) {
                selectedClips.remove(selectedClip);
                ClipHelper.deselect(selectedClip);
            } else {
                selectedClips.add(selectedClip);
                ClipHelper.select(selectedClip);
                toolbarClips.setVisibility(View.VISIBLE);
            }
        } else {
            deselectingClip();

            if (this.selectedClip != null && this.selectedClip == selectedClip) {
                this.selectedClip = null;
            } else {
                selectingTrack(timeline.getTrackFromClip(selectedClip));

                ClipHelper.select(selectedClip);
                this.selectedClip = selectedClip;
                toolbarClips.setVisibility(View.VISIBLE);

                float clipCenterTime = selectedClip.startTime + selectedClip.duration / 2;
                setCurrentTime(clipCenterTime);
                timelineScroll.post(() -> {
                    int targetScrollX = (int) (clipCenterTime * pixelsPerSecond);
                    timelineScroll.smoothScrollTo(targetScrollX, 0);
                });
            }
            if (this.selectedClip != null && currentTime < this.selectedClip.startTime)
                setCurrentTime(this.selectedClip.startTime);
        }

    }

    private void selectingTrack(Track selectedTrack) {
        deselectingTrack();

        if (selectedTrack == null) {
            deselectingClip();
            this.selectedClip = null;
            return;
        }
        if (this.selectedTrack != null && this.selectedTrack == selectedTrack) {
            this.selectedTrack = null;
            deselectingClip();
            this.selectedClip = null;
        } else {
            deselectingClip();
            this.selectedClip = null;
            selectedTrack.select();
            this.selectedTrack = selectedTrack;
            toolbarTrack.setVisibility(View.VISIBLE);
        }
    }

    private void selectingKnot(TransitionClip selectedKnot) {
        this.selectedKnot = selectedKnot;
    }

    public void deselectingClip() {
        toolbarClips.setVisibility(View.GONE);
        if (isClipSelectMultiple)
            selectedClips.clear();

        for (Track track : timeline.tracks) {
            for (Clip clip : track.clips) {
                ClipHelper.deselect(clip);
            }
        }
    }

    public void deselectingTrack() {
        toolbarTrack.setVisibility(View.GONE);
        for (Track track : timeline.tracks) {
            track.deselect();
        }
    }


    private void refreshPreviewClip() {
        //FFmpegEdit.generatePreviewVideo(this, timeline, settings, properties, this::reloadPreviewClip);
    }


    // Native Android user only
    public static List<Bitmap> extractThumbnail(Context context, String filePath, Clip clip) {
        return extractThumbnail(context, filePath, clip, -1);
    }

    public static List<Bitmap> extractThumbnail(Context context, String filePath, Clip clip, int frameCountOverride) {
        List<Bitmap> thumbnails = new ArrayList<>();
        if (clip.type == null)
            clip.type = ClipType.EFFECT;
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_launcher_background, null);
        switch (clip.type) {
            case VIDEO:
                try {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(filePath);

                    long durationMs = (long) (clip.duration * 1000);
                    int frameCount = frameCountOverride;
                    // Every 1s will have a thumbnail
                    // Math.ceil will make sure 0.1 will be 1, and clamp to 1 using Math.max to make sure not 0 or below
                    // TODO: Convert it to other thread in order to prevent lag
                    if (frameCountOverride == -1)
                        frameCount = (int) Math.max(1, Math.ceil((double) durationMs / 1000));
                    int originalWidth = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));
                    int originalHeight = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));

                    int desiredWidth = originalWidth / Constants.SAMPLE_SIZE_PREVIEW_CLIP;
                    int desiredHeight = originalHeight / Constants.SAMPLE_SIZE_PREVIEW_CLIP;

                    for (int i = 0; i < frameCount; i++) {
                        long timeUs = (long) (clip.startClipTrim * 1_000_000) + ((durationMs * 1000L * i) / frameCount);
                        Bitmap frame;
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                            frame = retriever.getScaledFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, desiredWidth, desiredHeight);
                        } else {
                            frame = Bitmap.createScaledBitmap(
                                    Objects.requireNonNull(retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)),
                                    desiredWidth, desiredHeight, true);

                        }
                        if (frame != null) {
                            thumbnails.add(frame);
                        }
                    }
                    retriever.release();
                    retriever.close();
                } catch (Exception e) {
                    LoggingManager.LogExceptionToNoteOverlay(context, e);
                }
                break;
            case IMAGE:
                thumbnails.add(IOImageHelper.LoadFileAsPNGImage(context, filePath, Constants.SAMPLE_SIZE_PREVIEW_CLIP));
                break;
            case TEXT:
                drawable.setColorFilter(0xAAFF0000, PorterDuff.Mode.SRC_ATOP);
                thumbnails.add(ImageHelper.createBitmapFromDrawable(drawable));
                break;
            case AUDIO:

                drawable.setColorFilter(0xAA0000FF, PorterDuff.Mode.SRC_ATOP);
                thumbnails.add(ImageHelper.createBitmapFromDrawable(drawable));

                //TODO: Failed...Visualizer isn't good
//                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//
//                PlayerVisualizerUtils.drawVisualizer(context, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), IOHelper.readFromFileAsRaw(context, filePath), new Canvas(bitmap));
//
//                thumbnails.add(bitmap);


                break;
            case EFFECT:
                drawable.setColorFilter(0xAAFFFF00, PorterDuff.Mode.SRC_ATOP);
                thumbnails.add(ImageHelper.createBitmapFromDrawable(drawable));
                break;

        }


        return thumbnails;

    }

    private static Bitmap combineThumbnails(List<Bitmap> frames) {
        if (frames == null || frames.isEmpty() || frames.get(0) == null) {
            return null;
        }
        int totalWidth = frames.size() * frames.get(0).getWidth();
        int height = frames.get(0).getHeight();

        Bitmap combined = Bitmap.createBitmap(totalWidth, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(combined);

        int x = 0;
        for (Bitmap bmp : frames) {
            if (bmp != null) {
            canvas.drawBitmap(bmp, x, 0, null);
            x += bmp.getWidth();
            }
        }

        return combined;
    }

    // Extract thumbnails progressively - only 20 seconds before currentTime, render parallel
    private void extractThumbnailsProgressively(Context context, String filePath, Clip clip, ImageGroupView clipView, float currentTime) {
        if (clip.type != ClipType.VIDEO) {
            // For non-video, use normal extraction
            List<Bitmap> thumbnails = extractThumbnail(context, filePath, clip);
            if (thumbnails != null && !thumbnails.isEmpty()) {
                Bitmap thumbnail = combineThumbnails(thumbnails);
                if (thumbnail != null) {
                    runOnUiThread(() -> {
                        if (clipView.getTag() == clip) {
                            clipView.setFilledImageBitmap(thumbnail);
                        }
                    });
                }
            }
            return;
        }

        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);

            long durationMs = (long) (clip.duration * 1000);
            int totalFrameCount = (int) Math.max(1, Math.ceil((double) durationMs / 1000)); // 1 thumbnail per second
            int originalWidth = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));
            int originalHeight = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));

            int desiredWidth = originalWidth / Constants.SAMPLE_SIZE_PREVIEW_CLIP;
            int desiredHeight = originalHeight / Constants.SAMPLE_SIZE_PREVIEW_CLIP;

            // Initialize rendered thumbnails set for this clip if not exists
            renderedThumbnails.putIfAbsent(clip, java.util.concurrent.ConcurrentHashMap.newKeySet());
            java.util.Set<Integer> renderedFrames = renderedThumbnails.get(clip);

            // Calculate which frames to render: 20 seconds before and 10 seconds after currentTime
            // Frame index is calculated from clip.startTime
            float clipTimeInTimeline = currentTime - clip.startTime;
            if (clipTimeInTimeline < 0) clipTimeInTimeline = 0;
            if (clipTimeInTimeline > clip.duration) clipTimeInTimeline = clip.duration;

            // Calculate frame index for currentTime (0-based, 1 frame per second)
            int currentFrameIndex = (int) Math.floor(clipTimeInTimeline);

            // Determine render range: 20 seconds before, 10 seconds after currentTime
            int secondsBefore = 5;
            int secondsAfter = 5;
            int startFrameIndex = Math.max(0, currentFrameIndex - secondsBefore);
            int endFrameIndex = Math.min(totalFrameCount - 1, currentFrameIndex + secondsAfter);

            // Determine which frames need to be rendered
            java.util.List<Integer> framesToRender = new java.util.ArrayList<>();
            synchronized (renderedFrames) {
                for (int i = startFrameIndex; i <= endFrameIndex; i++) {
                    if (!renderedFrames.contains(i)) {
                        framesToRender.add(i);
                    }
                }
            }

            if (framesToRender.isEmpty()) {
                retriever.release();
                retriever.close();
                // Even if no new frames to render, ensure bitmap is displayed
                if (clipView.getFilledImageBitmap() == null) {
                    // Create empty bitmap if none exists
                    int frameWidth = desiredWidth;
                    Bitmap emptyBitmap = Bitmap.createBitmap(frameWidth * totalFrameCount, desiredHeight, Bitmap.Config.ARGB_8888);
                    runOnUiThread(() -> {
                        if (clipView.getTag() == clip) {
                            clipView.setFilledImageBitmap(emptyBitmap);
                        }
                    });
                }
                return; // All needed frames already rendered
            }

            // Create combined bitmap if not exists
            final int frameWidth = desiredWidth;
            final int requiredBitmapWidth = frameWidth * totalFrameCount;
            Bitmap currentCombined = null;

            // Try to get existing combined bitmap from view
            if (clipView.getFilledImageBitmap() != null) {
                Bitmap existingBitmap = clipView.getFilledImageBitmap();
                // Check if existing bitmap has correct size
                if (existingBitmap.getWidth() == requiredBitmapWidth && existingBitmap.getHeight() == desiredHeight) {
                    // Copy to mutable bitmap if size matches
                    currentCombined = existingBitmap.copy(Bitmap.Config.ARGB_8888, true);
                } else {
                    // Size doesn't match, create new bitmap with correct size
                    currentCombined = Bitmap.createBitmap(requiredBitmapWidth, desiredHeight, Bitmap.Config.ARGB_8888);
                    Canvas tempCanvas = new Canvas(currentCombined);
                    // Draw existing bitmap at position 0 if it fits
                    if (existingBitmap.getWidth() <= requiredBitmapWidth) {
                        tempCanvas.drawBitmap(existingBitmap, 0, 0, null);
                    }
                }
            } else {
                // Create new combined bitmap with full size
                currentCombined = Bitmap.createBitmap(requiredBitmapWidth, desiredHeight, Bitmap.Config.ARGB_8888);
            }

            // Create canvas from bitmap (final for lambda)
            final Bitmap finalCombined = currentCombined.isMutable() ? currentCombined : currentCombined.copy(Bitmap.Config.ARGB_8888, true);
            final Canvas canvas = new Canvas(finalCombined);

            // Render frames in parallel
            // Note: MediaMetadataRetriever is not thread-safe, so we create a new retriever for each thread
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(framesToRender.size());
            java.util.concurrent.atomic.AtomicInteger renderedCount = new java.util.concurrent.atomic.AtomicInteger(0);

            for (int frameIndex : framesToRender) {
                final int finalFrameIndex = frameIndex;
                thumbnailExecutor.execute(() -> {
                    MediaMetadataRetriever threadRetriever = null;
                    try {
                        // Create a new retriever for this thread (MediaMetadataRetriever is not thread-safe)
                        threadRetriever = new MediaMetadataRetriever();
                        threadRetriever.setDataSource(filePath);

                        // Calculate time: start from clip.startClipTrim, then each second
                        long timeUs = (long) (clip.startClipTrim * 1_000_000) + ((durationMs * 1000L * finalFrameIndex) / totalFrameCount);

                        Bitmap frame;
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                            frame = threadRetriever.getScaledFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, desiredWidth, desiredHeight);
                        } else {
                            Bitmap originalFrame = threadRetriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                            if (originalFrame != null) {
                                frame = Bitmap.createScaledBitmap(originalFrame, desiredWidth, desiredHeight, true);
                            } else {
                                frame = null;
                            }
                        }

                        if (frame != null) {
                            // Draw frame at its position (thread-safe with synchronized)
                            synchronized (canvas) {
                                int x = finalFrameIndex * frameWidth;
                                canvas.drawBitmap(frame, x, 0, null);
                            }

                            // Mark as rendered
                            synchronized (renderedFrames) {
                                renderedFrames.add(finalFrameIndex);
                            }

                            renderedCount.incrementAndGet();

                            // Update UI immediately with current progress
                            final Bitmap combinedCopy;
                            synchronized (canvas) {
                                combinedCopy = finalCombined.copy(Bitmap.Config.ARGB_8888, false);
                            }
                            if (combinedCopy != null) {
                                runOnUiThread(() -> {
                                    if (clipView.getTag() == clip) {
                                        // Only update if view still exists and tag matches
                                        clipView.setFilledImageBitmap(combinedCopy);
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        Log.e("Thumbnail", "Error extracting frame " + finalFrameIndex + " for clip " + clip.clipName + ": " + e.getMessage(), e);
                    } finally {
                        if (threadRetriever != null) {
                            try {
                                threadRetriever.release();
                                threadRetriever.close();
                            } catch (Exception e) {
                                Log.e("Thumbnail", "Error releasing retriever: " + e.getMessage());
                            }
                        }
                        latch.countDown();
                    }
                });
            }

            // Wait for all frames to be rendered
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            retriever.release();
            retriever.close();

            // Final update: ensure the complete bitmap is displayed after all frames are rendered
            final Bitmap finalBitmapCopy = finalCombined.copy(Bitmap.Config.ARGB_8888, false);
            if (finalBitmapCopy != null) {
                runOnUiThread(() -> {
                    if (clipView.getTag() == clip) {
                        clipView.setFilledImageBitmap(finalBitmapCopy);
                    }
                });
            }

            Log.d("Thumbnail", "Rendered " + renderedCount.get() + " thumbnails for clip " + clip.clipName + " (frames " + startFrameIndex + "-" + endFrameIndex + ", currentFrame=" + currentFrameIndex + ", total=" + totalFrameCount + ")");
        } catch (Exception e) {
            Log.e("Thumbnail", "Error extracting thumbnails progressively: " + e.getMessage(), e);
        }
    }

    // Trigger thumbnail rendering for visible clips when currentTime changes significantly (debounced)
    private void triggerThumbnailRenderingDebounced(float newCurrentTime) {
        // Remove any pending thumbnail trigger
        thumbnailTriggerHandler.removeCallbacks(thumbnailTriggerRunnable);

        // Create new runnable with current time
        final float finalCurrentTime = newCurrentTime;
        thumbnailTriggerRunnable = () -> {
            triggerThumbnailRendering(finalCurrentTime);
        };

        // Debounce: wait 200ms before triggering to avoid too frequent calls during fast scrolling
        thumbnailTriggerHandler.postDelayed(thumbnailTriggerRunnable, 200);
    }

    // Trigger thumbnail rendering for visible clips when currentTime changes significantly
    private void triggerThumbnailRendering(float newCurrentTime) {
        if (timeline == null || timeline.tracks == null) return;

        Log.d("Thumbnail", "Triggering thumbnail rendering for currentTime: " + newCurrentTime);

        for (Track track : timeline.tracks) {
            for (Clip clip : track.clips) {
                // Check if clip is visible at newCurrentTime
                if (newCurrentTime >= clip.startTime && newCurrentTime <= clip.startTime + clip.duration) {
                    if (clip.type == ClipType.VIDEO && clip.viewRef != null) {
                        // Get clip view
                        ImageGroupView clipView = clip.viewRef;
                        if (clipView != null) {
                            // Trigger thumbnail rendering for this clip
                            String previewPath = clip.getAbsolutePreviewPath(properties);
                            String originalPath = clip.getAbsolutePath(properties.getProjectPath());
                            String thumbnailPath = previewPath;
                            if (!IOHelper.isFileExist(thumbnailPath)) {
                                thumbnailPath = originalPath;
                            }

                            if (IOHelper.isFileExist(thumbnailPath)) {
                                // Make all variables final for lambda
                                final String finalThumbnailPath = thumbnailPath;
                                final ImageGroupView finalClipView = clipView;
                                final Clip finalClip = clip;
                                final float finalCurrentTime = newCurrentTime;
                                thumbnailExecutor.execute(() -> {
                                    extractThumbnailsProgressively(this, finalThumbnailPath, finalClip, finalClipView, finalCurrentTime);
                                });
                            }
                        }
                    }
                }
            }
        }
    }


    // End Native Android

    // Conversion methods have been moved to PreviewConversionHelper




    static class DragContext {
        View ghost;
        Track currentTrack;
        Clip clip;
    }

    // ClipRenderer and TimelineRenderer have been moved to separate files:
    // - com.vanvatcorporation.doubleclips.activities.renderer.ClipRenderer
    // - com.vanvatcorporation.doubleclips.activities.renderer.TimelineRenderer

    // Implement ClipInteractionCallback
                            @Override
    public void onClipSelected(Clip clip) {
        selectingClip(clip);
                            }

                            @Override
    public float getPlaybackSpeed() {
        return playbackSpeed;
                            }

                            @Override
    public boolean isClipSelected(Clip clip) {
        return selectedClip == clip;
                            }

                            @Override
    public float getDisplayDensity() {
        return getResources().getDisplayMetrics().density;
    }

}



