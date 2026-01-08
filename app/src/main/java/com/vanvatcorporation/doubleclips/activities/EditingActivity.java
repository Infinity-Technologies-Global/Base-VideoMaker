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

public class EditingActivity extends AppCompatActivityImpl {


    //List<Track> trackList = new ArrayList<>();
    Timeline timeline = new Timeline();
    MainAreaScreen.ProjectData properties;
    VideoSettings settings;

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



    private int trackCount = 0;
    private final int TRACK_HEIGHT = 100;
    private static final float MIN_CLIP_DURATION = 0.1f; // in seconds
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
    int getCurrentTimeInX()
    {
        return getTimeInX(currentTime);
    }
    int getTimeInX(float time)
    {
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
        if(uri == null) return offsetTime;
        if(selectedTrack == null) return offsetTime;
        
        // Process on background thread to avoid ANR
        importExecutor.execute(() -> {
            parseFileIntoWorkPathAndAddToTrack(uri, offsetTime);
        });
        
        return offsetTime; // Return immediately
    }

    float parseFileIntoWorkPathAndAddToTrack(Uri uri, float offsetTime)
    {
        if(uri == null) return offsetTime;
        if(selectedTrack == null) return offsetTime;
        String filename = getFileName(uri);
        String clipPath = IOHelper.CombinePath(properties.getProjectPath(), Constants.DEFAULT_CLIP_DIRECTORY, filename);
        String previewClipPath = IOHelper.CombinePath(properties.getProjectPath(), Constants.DEFAULT_PREVIEW_CLIP_DIRECTORY, filename);
        
        // Copy file - this is heavy but now on background thread
        IOHelper.writeToFileAsRaw(this, clipPath, IOHelper.readFromFileAsRaw(this, getContentResolver(), uri));

        float duration = 3f; // fallback default if needed
        String mimeType = getContentResolver().getType(uri);

        if(mimeType == null) return offsetTime;


        ClipType type;

        if (mimeType.startsWith("audio/")) type = ClipType.AUDIO;
        else if (mimeType.startsWith("image/")) type = ClipType.IMAGE;
        else if (mimeType.startsWith("video/")) type = ClipType.VIDEO;
        else type = ClipType.EFFECT; // if effect or unknown



        boolean isVideoHasAudio = false;
        int width = 0, height = 0;
        
        // Use MediaExtractor for metadata - faster and lighter than ExoPlayer for just metadata
        // ExoPlayer is better for playback, but MediaExtractor is faster for metadata extraction
        if(type == ClipType.VIDEO || type == ClipType.AUDIO) {
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
        if(type == ClipType.IMAGE)
        {
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

    void processingPreview(Clip clip, String originalClipPath, String previewClipPath)
    {
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
        if(clip.type == ClipType.AUDIO)
        {
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
        }
        else if(clip.type == ClipType.VIDEO)
        {
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







        }
        else {
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


    void pickingContent()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Media"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        properties = (MainAreaScreen.ProjectData) createrBundle.getSerializable("ProjectProperties");

        //settings = new VideoSettings(1280, 720, 30, 30, VideoSettings.FfmpegPreset.MEDIUM, VideoSettings.FfmpegTune.ZEROLATENCY);
        assert properties != null;
        settings = VideoSettings.loadSettings(this, properties);

        if(settings == null)
        {
            settings = new VideoSettings(1366, 768, 30, 30, 30, VideoSettings.FfmpegPreset.MEDIUM, VideoSettings.FfmpegTune.ZEROLATENCY);
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

        ViewGroup.LayoutParams previewViewGroupParams = previewViewGroup.getLayoutParams();
        previewViewGroupParams.width = settings.videoWidth;
        previewViewGroupParams.height = settings.videoHeight;
        previewViewGroup.setLayoutParams(previewViewGroupParams);


        outerPreviewViewGroup = findViewById(R.id.outerPreviewViewGroup);

        outerPreviewViewGroup.post(() -> {
            previewAvailableWidth = outerPreviewViewGroup.getWidth();
            previewAvailableHeight = outerPreviewViewGroup.getHeight();
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
            Timeline.saveTimeline(this, timeline, properties, settings);
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



            timeline = Timeline.loadTimeline(this, this, properties);
        });
        timelineScroll.getViewTreeObserver().addOnScrollChangedListener(() -> {
            rulerScroll.scrollTo(timelineScroll.getScrollX(), 0);

            if(!isPlaying) {
                float newCurrentTime = (timelineScroll.getScrollX()) / (float) pixelsPerSecond;
                currentTime = newCurrentTime;
            }

            // Get time (- centerOffset mean remove the start spacer)
            //float totalSeconds = (timelineScroll.getScrollX()) / (float) pixelsPerSecond;
            currentTimePosText.post(() -> currentTimePosText.setText(DateHelper.convertTimestampToMMSSFormat((long) (currentTime * 1000L)) + String.format(".%02d", ((long)((currentTime % 1) * 100)))));

            boolean isSeekingOnly = !isPlaying;
            timelineRenderer.updateTime(currentTime, isSeekingOnly);
            
            // Trigger thumbnail rendering when seeking (debounced to avoid too frequent calls)
            if (isSeekingOnly) {
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
        switch (type)
        {
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
    private void setupToolbars()
    {
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


        toolbarDefault.findViewById(R.id.splitMediaButton).setOnClickListener(v -> {
            List<Clip> affectedClips = timeline.getClipAtCurrentTime(currentTime);
            if(selectedClip != null && affectedClips.contains(selectedClip)) {
                selectedClip.splitClip(this, timeline, currentTime);
            }
            else {
                for (Clip clip : affectedClips) {
                    clip.splitClip(this, timeline, currentTime);
                }

            }
        });


        // ===========================       DEFAULT ZONE       ====================================



        // ===========================       TRACK ZONE       ====================================


        toolbarTrack.findViewById(R.id.addMediaButton).setOnClickListener(v -> {
            if(selectedTrack != null)
                pickingContent();
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a track first!").show();
        });
        toolbarTrack.findViewById(R.id.deleteTrackButton).setOnClickListener(v -> {
            if(selectedTrack != null) {
                selectedTrack.delete(timeline, timelineTracksContainer, trackInfoLayout, this);
                updateCurrentClipEnd();
            }
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();

        });
        toolbarTrack.findViewById(R.id.splitMediaButton).setOnClickListener(v -> {
            List<Clip> affectedClips = timeline.getClipAtCurrentTime(currentTime);
            if(selectedClip != null && affectedClips.contains(selectedClip)) {
                selectedClip.splitClip(this, timeline, currentTime);
            }
            else {
                for (Clip clip : affectedClips) {
                    clip.splitClip(this, timeline, currentTime);
                }

            }
        });

        toolbarTrack.findViewById(R.id.addTextButton).setOnClickListener(v -> {

            if(selectedTrack == null) {
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

            if(selectedTrack == null) {
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


            if(selectedTrack != null) {
                isClipSelectMultiple = true;
                ((NavigationIconLayout)toolbarClips.findViewById(R.id.selectMultipleButton)).getIconView().setColorFilter(0xFFFF0000, PorterDuff.Mode.SRC_ATOP);

                deselectingClip();

                for (Clip clip : selectedTrack.clips) {
                    selectingClip(clip);
                }
            }
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a track first!").show();
        });
        toolbarTrack.findViewById(R.id.autoSnapButton).setOnClickListener(v -> {
            if(selectedTrack != null) {
                selectedTrack.sortClips();
                List<Clip> clips = selectedTrack.clips;
                for (int i = 1; i < clips.size(); i++) {
                    Clip clip = clips.get(i);
                    Clip prevClip = clips.get(i - 1);

                    clip.startTime = prevClip.startTime + prevClip.duration;
                }

                updateClipLayouts();
                updateCurrentClipEnd();
            }
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a track first!").show();
        });


        // ===========================       TRACK ZONE       ====================================





        // ===========================       CLIPS ZONE       ====================================


        toolbarClips.findViewById(R.id.deleteMediaButton).setOnClickListener(v -> {
            if(selectedClip != null) {
                selectedClip.deleteClip(timeline, this);
                updateCurrentClipEnd();
            }
            if(selectedClips != null) {
                for (Clip selectedClip : selectedClips) {
                    selectedClip.deleteClip(timeline, this);
                }
                updateCurrentClipEnd();
            }

            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();

        });
        toolbarClips.findViewById(R.id.splitMediaButton).setOnClickListener(v -> {
            List<Clip> affectedClips = timeline.getClipAtCurrentTime(currentTime);
            if(selectedClip != null && affectedClips.contains(selectedClip)) {
                selectedClip.splitClip(this, timeline, currentTime);
            }
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();
        });
        toolbarClips.findViewById(R.id.editMediaButton).setOnClickListener(v -> {
            if(selectedClip != null) {
                editingSpecific(selectedClip.type);
            }
            else if(!selectedClips.isEmpty())
            {
                editingMultiple();
            }
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();
        });
        toolbarClips.findViewById(R.id.addKeyframeButton).setOnClickListener(v -> {
            if(selectedClip != null) {
                addKeyframe(selectedClip, currentTime);
            }
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();
        });
        toolbarClips.findViewById(R.id.selectMultipleButton).setOnClickListener(v -> {
            // Todo: Not fully implemented yet. The idea is to remake the whole thing, get the "array" of selected clip is completed
            // now if one clip is move then the whole array move along. Also ghost will be as well

            isClipSelectMultiple = !isClipSelectMultiple;

            ((NavigationIconLayout)toolbarClips.findViewById(R.id.selectMultipleButton)).getIconView().setColorFilter((isClipSelectMultiple ? 0xFFFF0000 : 0xFFFFFFFF), PorterDuff.Mode.SRC_ATOP);
        });
        toolbarClips.findViewById(R.id.applyKeyframeToAllClip).setOnClickListener(v -> {

            if(selectedTrack != null && selectedClip != null) {

                List<Clip> clips = selectedTrack.clips;
                for (Clip clip : clips) {
                    if(clip != selectedClip)
                    {
                        clearKeyframe(clip);

                        for (Keyframe keyframe : selectedClip.keyframes.keyframes) {
                            addKeyframe(clip, keyframe);
                        }

                    }
                }
            }
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a track first!").show();

        });
        toolbarClips.findViewById(R.id.restateButton).setOnClickListener(v -> {
            if(selectedClip != null) {
                selectedClip.restate();

                // TODO: Find a way to specifically build only the edited clip. Not entire timeline
                //  this is just for testing. Resource-consuming asf.
                regeneratingTimelineRenderer();



            }
            else new AlertDialog.Builder(this).setTitle("Error").setMessage("You need to pick a clip first!").show();
        });

        // ===========================       CLIPS ZONE       ====================================
    }

    private void setupSpecificEdit()
    {
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
            if(selectedClip != null)
            {
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
            if(selectedClip != null)
            {
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
            if(selectedKnot != null)
            {
                for (int i = 0; i < timeline.tracks.get(selectedKnot.trackIndex).clips.size(); i++)
                {
                    TransitionClip clip = timeline.tracks.get(selectedKnot.trackIndex).clips.get(i).endTransition;
                    if(clip == selectedKnot)
                    {
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
            if(selectedKnot != null)
            {
                for (int i = 0; i < timeline.tracks.get(selectedKnot.trackIndex).clips.size(); i++)
                {
                    TransitionClip clip = timeline.tracks.get(selectedKnot.trackIndex).clips.get(i).endTransition;
                    if(clip == null) continue;
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
            if(!selectedClips.isEmpty())
            {
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
            if(selectedClip != null)
            {
                selectedClip.setClipName(clipEditSpecificAreaScreen.clipNameField.getText().toString(), properties);
                selectedClip.duration = ParserHelper.TryParse(clipEditSpecificAreaScreen.durationContent.getText().toString(), selectedClip.duration);
                selectedClip.videoProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.positionXField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.PosX)), VideoProperties.ValueType.PosX);
                selectedClip.videoProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.positionYField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.PosY)), VideoProperties.ValueType.PosY);
                selectedClip.videoProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.rotationField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.Rot)), VideoProperties.ValueType.Rot);
                selectedClip.videoProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.scaleXField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.ScaleX)), VideoProperties.ValueType.ScaleX);
                selectedClip.videoProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.scaleYField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.ScaleY)), VideoProperties.ValueType.ScaleY);
                selectedClip.videoProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.opacityField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.Opacity)), VideoProperties.ValueType.Opacity);
                selectedClip.videoProperties.setValue(ParserHelper.TryParse(clipEditSpecificAreaScreen.speedField.getText().toString(), selectedClip.videoProperties.getValue(VideoProperties.ValueType.Speed)), VideoProperties.ValueType.Speed);

                selectedClip.isMute = clipEditSpecificAreaScreen.muteAudioCheckbox.isChecked();

                updateClipLayouts();
                updateCurrentClipEnd();
                // TODO: Find a way to specifically build only the edited clip. Not entire timeline
                //  this is just for testing. Resource-consuming asf.
                regeneratingTimelineRenderer();


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


            for(Keyframe keyframe : selectedClip.keyframes.keyframes)
            {
                clipEditSpecificAreaScreen.createKeyframeElement(selectedClip, keyframe, () -> {
                    setCurrentTime(keyframe.getGlobalTime(selectedClip));
                }, () -> {
                    removeKeyframe(selectedClip, keyframe);
                });
            }

            clipEditSpecificAreaScreen.clearKeyframeButton.setOnClickListener(v -> {
                if(selectedClip != null)
                {
                    clearKeyframe(selectedClip);
                }
            });
        });


        // ===========================       CLIP ZONE       ====================================



        // ===========================       VIDEO PROPERTIES ZONE       ====================================

        videoPropertiesEditSpecificAreaScreen.onClose.add(() -> {
            settings.videoWidth = ParserHelper.TryParse(videoPropertiesEditSpecificAreaScreen.resolutionXField.getText().toString(), 1366);
            settings.videoHeight = ParserHelper.TryParse(videoPropertiesEditSpecificAreaScreen.resolutionYField.getText().toString(), 768);
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
            ViewGroup.LayoutParams previewViewGroupParams = previewViewGroup.getLayoutParams();
            previewViewGroupParams.width = settings.videoWidth;
            previewViewGroupParams.height = settings.videoHeight;
            previewViewGroup.setLayoutParams(previewViewGroupParams);
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

    public void setupTimelinePinchAndZoom() {
        scaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                if(currentTimeBeforeScrolling == -1)
                {
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
                if ((int)(currentTime * 10) % 20 == 0) { // Every 2 seconds (20 * 0.1s)
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


                playbackHandler.postDelayed(this, (long)(frameInterval * 1000));
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

    private void regeneratingTimelineRenderer()
    {
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
        timelineRenderer.buildTimeline(timeline, properties, settings, this, previewViewGroup, textCanvasControllerInfo);
            
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
    private void setCurrentTime(float value)
    {
        currentTime = value;
        timelineScroll.scrollTo((int) (currentTime * pixelsPerSecond), 0);
    }

    void setupPreview()
    {
        timelineRenderer = new TimelineRenderer(this);


        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenHeight = metrics.heightPixels;
        editingZone.getLayoutParams().height = (int) (screenHeight * 0.35);
        editingZone.requestLayout();



        regeneratingTimelineRenderer();
    }
    void reloadPreviewClip()
    {
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
        Timeline.saveTimeline(this, timeline, properties, settings);
    }
    @Override
    public void onPause() {
        super.onPause();

        if (timelineRenderer != null) {
        timelineRenderer.release();
        }
        Timeline.saveTimeline(this, timeline, properties, settings);
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

    private TrackFrameLayout addNewTrackUi() {
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
    private void addClipToTrackUi(TrackFrameLayout trackLayout, Clip data)
    {
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
        
        importExecutor.execute(() -> {
            try {
                // Try preview path first, fallback to original path if preview doesn't exist
                String thumbnailPath = previewPath;
                if (!IOHelper.isFileExist(thumbnailPath)) {
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

        data.registerClipHandle(clipView, this, timelineScroll);

        clipView.post(() -> {
            updateCurrentClipEnd();

            for (Keyframe keyframe : data.keyframes.keyframes) {
                addKeyframeUi(data, keyframe);
            }
        });

        trackLayout.addView(clipView);
        handleClipInteraction(clipView);
    }
    public void addKnotTransition(TransitionClip clip, Clip clipA)
    {
        View knotView = new View(this);
        knotView.setBackgroundColor(Color.RED);
        knotView.setVisibility(View.VISIBLE);

        knotView.setTag(R.id.transition_knot_tag, clip);
        knotView.setTag(R.id.clip_knot_tag, clipA);
        // Position it between clips
        int width = 50;
        int height = 50;

        knotView.setPivotX((float) width /2);
        knotView.setPivotY((float) height /2);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        //params.leftMargin = clipB.getLeft() - (width / 2); // center between clips
        params.topMargin = clipA.viewRef.getTop() + (clipA.viewRef.getHeight() / 2) - (height / 2);
        knotView.setX(clipA.viewRef.getX() + (clipA.duration * pixelsPerSecond) - (width / 2));
        timeline.tracks.get(clip.trackIndex).viewRef.addView(knotView, params);

        handleKnotInteraction(knotView);
    }

    public void addKeyframe(Clip clip, float keyframeTime)
    {
        addKeyframe(clip, new Keyframe(keyframeTime - clip.startTime, new VideoProperties(
                clip.videoProperties.getValue(VideoProperties.ValueType.PosX), clip.videoProperties.getValue(VideoProperties.ValueType.PosY),
                clip.videoProperties.getValue(VideoProperties.ValueType.Rot),
                clip.videoProperties.getValue(VideoProperties.ValueType.ScaleX), clip.videoProperties.getValue(VideoProperties.ValueType.ScaleY),
                clip.videoProperties.getValue(VideoProperties.ValueType.Opacity), clip.videoProperties.getValue(VideoProperties.ValueType.Speed)
        ), EasingType.LINEAR));
    }
    public void addKeyframe(Clip clip, Keyframe keyframe)
    {
        addKeyframeUi(clip, keyframe);

        clip.keyframes.keyframes.add(keyframe);
    }
    public void addKeyframeUi(Clip clip, Keyframe keyframe)
    {
        View knotView = new View(this);
        knotView.setBackgroundColor(Color.BLUE);
        knotView.setVisibility(View.VISIBLE);

        knotView.setTag(R.id.keyframe_knot_tag, keyframe);
        knotView.setTag(R.id.clip_knot_tag, clip);
        // Position it between clips
        int width = 12;
        int height = clip.viewRef.getHeight();

        knotView.setPivotX((float) width /2);
        knotView.setPivotY((float) height /2);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        //params.leftMargin = getCurrentTimeInX();
        //params.topMargin = clip.viewRef.getTop() + (clip.viewRef.getHeight() / 2);
        params.topMargin = clip.viewRef.getTop() + (clip.viewRef.getHeight() / 2) - (height / 2);
        knotView.setX(keyframe.getLocalTime() * pixelsPerSecond);

        //timeline.tracks.get(clip.trackIndex).viewRef.addView(knotView, params);
        clip.viewRef.addView(knotView, params);



        handleKeyframeInteraction(knotView);
    }


    public void removeKeyframe(Clip clip, Keyframe keyframe)
    {
        removeKeyframeUi(clip, keyframe);

        clip.keyframes.keyframes.remove(keyframe);
    }
    public void removeKeyframeUi(Clip clip, Keyframe keyframe)
    {
        for (int i = 0; i < clip.viewRef.getChildCount(); i++) {
            View knotView = clip.viewRef.getChildAt(i);
            if(knotView.getTag(R.id.keyframe_knot_tag) instanceof Keyframe && knotView.getTag(R.id.keyframe_knot_tag) == keyframe)
            {
                clip.viewRef.removeViewAt(i);
                break;
            }
        }
    }

    public void clearKeyframe(Clip clip)
    {
        clearKeyframeUi(clip);

        clip.keyframes.keyframes.clear();
    }
    public void clearKeyframeUi(Clip clip)
    {
        for (int i = 0; i < clip.viewRef.getChildCount(); i++) {
            View knotView = clip.viewRef.getChildAt(i);
            if(knotView.getTag(R.id.keyframe_knot_tag) instanceof Keyframe)
            {
                clip.viewRef.removeView(knotView);
            }
        }
    }
    public void revalidationClipView(Clip data)
    {
        ImageGroupView clipView = data.viewRef;
        clipView.getLayoutParams().width = (int) (data.duration * pixelsPerSecond);


    }
    private void handleEditZoneInteraction(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_MOVE:
                    if(isPlaying)
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
            if(view.getTag(R.id.keyframe_knot_tag) instanceof Keyframe)
            {
                Keyframe data = (Keyframe) view.getTag(R.id.keyframe_knot_tag);
            }

        });
    }
    private void handleKnotInteraction(View view) {
        view.setOnClickListener(v -> {
            if(view.getTag(R.id.transition_knot_tag) instanceof TransitionClip)
            {
                selectingKnot((TransitionClip) view.getTag(R.id.transition_knot_tag));
                editingSpecific(ClipType.TRANSITION);
            }

        });
    }
    private void handleTrackInteraction(View view) {
        view.setOnClickListener(v -> {
            if(view instanceof TrackFrameLayout)
            {
                selectingTrack(((TrackFrameLayout) view).trackInfo);
            }
        });

        view.setOnTouchListener((v, event) -> {
            scaleDetector.onTouchEvent(event);
            switch (event.getAction())
            {
                case MotionEvent.ACTION_MOVE:
                    if(event.getPointerCount() == 2) {
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
            ghost.setFilledImageBitmap(((ImageGroupView)v).getFilledImageBitmap());
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
                            if (finalX < centerOffset) finalX = centerOffset; // ⛔ Clamp again for safety


                            float finalX1 = finalX;

                            v.post(() -> {
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



    void updateCurrentClipEnd()
    {
        updateCurrentClipEnd(true);
    }
    void updateCurrentClipEnd(boolean updateRuler)
    {
        int newTimelineEnd = 0;
        // 🧠 Recalculate max right edge of all clips in all tracks
        for (Track trackCpn : timeline.tracks) {
            for (int i = 0; i < trackCpn.viewRef.getChildCount(); i++) {
                View child = trackCpn.viewRef.getChildAt(i);
                if (child.getTag() != null && child.getTag() instanceof Clip) { // It's a clip
                    int right = (int) (child.getX() + child.getWidth());
                    if (right > newTimelineEnd) newTimelineEnd = right;

                    // For keyframe syncing
                    if(child instanceof ImageGroupView)
                    {
                        ImageGroupView clipViewRef = ((ImageGroupView) child);
                        for (int j = 0; j < clipViewRef.getChildCount(); j++) {
                            View child1 = clipViewRef.getChildAt(j);
                            if(child1.getTag(R.id.keyframe_knot_tag) instanceof Keyframe) {
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
        if(updateRuler)
            updateRuler(totalSeconds, currentRulerInterval);
        // 🧱 Expand end spacer
        for (Track trackCpn : timeline.tracks) {
            updateEndSpacer(trackCpn);
            updateTrackWidth(trackCpn);
            updateTransitionKnot(trackCpn);
        }



        //refreshPreviewClip();
    }
    void updateTransitionKnot(Track track)
    {

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
    void updateTrackWidth(Track track)
    {
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
        if(pixelsPerSecond < 10 && pixelsPerSecond > 5)
        {
            changedRulerInterval = 32f;
        }
        // Recently updated. Below are tested.
        if(pixelsPerSecond < 15 && pixelsPerSecond > 10)
        {
            changedRulerInterval = 16f;
        }
        if(pixelsPerSecond < 25 && pixelsPerSecond > 15)
        {
            changedRulerInterval = 8f;
        }
        if(pixelsPerSecond < 50 && pixelsPerSecond > 25)
        {
            changedRulerInterval = 4f;
        }
        if(pixelsPerSecond < 100 && pixelsPerSecond > 50)
        {
            changedRulerInterval = 2f;
        }
        if(pixelsPerSecond < 200 && pixelsPerSecond > 100)
        {
            changedRulerInterval = 1f;
        }
        if(pixelsPerSecond < 500 && pixelsPerSecond > 200)
        {
            changedRulerInterval = 0.5f;
        }
        if(pixelsPerSecond < 1000 && pixelsPerSecond > 500)
        {
            changedRulerInterval = 0.2f;
        }
        if(pixelsPerSecond < 2000 && pixelsPerSecond > 1000)
        {
            changedRulerInterval = 0.1f;
        }
        if(pixelsPerSecond < 5000 && pixelsPerSecond > 2000)
        {
            changedRulerInterval = 0.05f;
        }
        // Recently updated. Above are tested.
        if(pixelsPerSecond < 10000 && pixelsPerSecond > 5000)
        {
            changedRulerInterval = 0.02f;
        }
        if(pixelsPerSecond < 20000 && pixelsPerSecond > 10000)
        {
            changedRulerInterval = 0.01f;
        }
        if(pixelsPerSecond < 50000 && pixelsPerSecond > 20000)
        {
            changedRulerInterval = 0.005f;
        }


        for (int i = 0; i < rulerContainer.getChildCount(); i++) {
            View tick = rulerContainer.getChildAt(i);
            if(!(tick instanceof TextView)) continue;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tick.getLayoutParams();
            params.width = (int) (pixelsPerSecond * changedRulerInterval); // or pixelsPerSecond / 2 for 0.5s ticks
            tick.setLayoutParams(params);
        }
    }

    private void updateClipLayouts() {
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
                if(clip.getTag(R.id.transition_knot_tag) instanceof TransitionClip) {
                    TransitionClip data = (TransitionClip) clip.getTag(R.id.transition_knot_tag);
                    //clip.setX(getTimeInX(data.time));
                }
                if(clip.getTag(R.id.keyframe_knot_tag) instanceof Keyframe) {
                    Keyframe data = (Keyframe) clip.getTag(R.id.keyframe_knot_tag);
                    Clip data2 = (Clip) clip.getTag(R.id.clip_knot_tag);

                    clip.setX(getTimeInX(data.getGlobalTime(data2)));
                }
            }
        }
    }
    private void updateEndSpacers()
    {
        for (Track track : timeline.tracks) {
            updateEndSpacer(track);
        }
    }

    private void updateTimelineZoom() {
        updateRulerEfficiently();
        boolean rulerChanged = false;
        if(changedRulerInterval != currentRulerInterval)
        {
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
        if(rulerChanged)
        {
            for (Track track : timeline.tracks) {
                for (Clip clip : track.clips) {
                    //clip.viewRef.setFilledImageBitmap(combineThumbnails(extractThumbnail(this, clip.filePath, clip.type)));
                }
            }
        }
    }
    private void addTransitionBridge(Clip clipA, Clip clipB, float transitionDuration)
    {
        //if (clipA.startTime + clipA.duration == clipB.startTime)
        {
            // If null then define new transition, else keep the transition from the clip.
            if(clipA.endTransition == null)
            {
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
    private void addTransitionBridgeUi(TransitionClip transitionClip, Clip clip)
    {
        addKnotTransition(transitionClip, clip);
    }





    private void selectingClip(Clip selectedClip)
    {
        if(isClipSelectMultiple)
        {
            this.selectedClip = null;
            if(selectedClips.contains(selectedClip))
            {
                selectedClips.remove(selectedClip);
                selectedClip.deselect();
            }
            else
            {
                selectedClips.add(selectedClip);
                selectedClip.select();
                toolbarClips.setVisibility(View.VISIBLE);
            }
        }
        else {
            deselectingClip();

            if(this.selectedClip != null && this.selectedClip == selectedClip){
                this.selectedClip = null;
            }
            else {
                selectingTrack(timeline.getTrackFromClip(selectedClip));

                selectedClip.select();
                this.selectedClip = selectedClip;
                toolbarClips.setVisibility(View.VISIBLE);

                float clipCenterTime = selectedClip.startTime + selectedClip.duration / 2;
                setCurrentTime(clipCenterTime);
                timelineScroll.post(() -> {
                    int targetScrollX = (int) (clipCenterTime * pixelsPerSecond);
                    timelineScroll.smoothScrollTo(targetScrollX, 0);
                });
            }
            if(this.selectedClip != null && currentTime < this.selectedClip.startTime)
                setCurrentTime(this.selectedClip.startTime);
        }

    }
    private void selectingTrack(Track selectedTrack)
    {
        deselectingTrack();

        if(selectedTrack == null) {
            deselectingClip();
            this.selectedClip = null;
            return;
        }
        if(this.selectedTrack != null && this.selectedTrack == selectedTrack){
            this.selectedTrack = null;
            deselectingClip();
            this.selectedClip = null;
        }
        else {
            deselectingClip();
            this.selectedClip = null;
            selectedTrack.select();
            this.selectedTrack = selectedTrack;
            toolbarTrack.setVisibility(View.VISIBLE);
        }
    }
    private void selectingKnot(TransitionClip selectedKnot)
    {
        this.selectedKnot = selectedKnot;
    }
    private void deselectingClip()
    {
        toolbarClips.setVisibility(View.GONE);
        if(isClipSelectMultiple)
            selectedClips.clear();

        for (Track track : timeline.tracks) {
            for (Clip clip : track.clips) {
                clip.deselect();
            }
        }
    }
    private void deselectingTrack()
    {
        toolbarTrack.setVisibility(View.GONE);
        for (Track track : timeline.tracks) {
            track.deselect();
        }
    }




    private void refreshPreviewClip()
    {
        //FFmpegEdit.generatePreviewVideo(this, timeline, settings, properties, this::reloadPreviewClip);
    }


    // Native Android user only
    private static List<Bitmap> extractThumbnail(Context context, String filePath, Clip clip)
    {
        return extractThumbnail(context, filePath, clip, -1);
    }
    private static List<Bitmap> extractThumbnail(Context context, String filePath, Clip clip, int frameCountOverride)
    {
        List<Bitmap> thumbnails = new ArrayList<>();
        if(clip.type == null)
            clip.type = ClipType.EFFECT;
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_launcher_background, null);
        switch (clip.type)
        {
            case VIDEO:
                try {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(filePath);

                    long durationMs = (long) (clip.duration * 1000);
                    int frameCount = frameCountOverride;
                    // Every 1s will have a thumbnail
                    // Math.ceil will make sure 0.1 will be 1, and clamp to 1 using Math.max to make sure not 0 or below
                    // TODO: Convert it to other thread in order to prevent lag
                    if(frameCountOverride == -1) frameCount = (int) Math.max(1, Math.ceil((double) durationMs / 1000));
                    int originalWidth = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));
                    int originalHeight = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));

                    int desiredWidth = originalWidth / Constants.SAMPLE_SIZE_PREVIEW_CLIP;
                    int desiredHeight = originalHeight / Constants.SAMPLE_SIZE_PREVIEW_CLIP;

                    for (int i = 0; i < frameCount; i++) {
                        long timeUs = (long) (clip.startClipTrim * 1_000_000) +  ((durationMs * 1000L * i) / frameCount);
                        Bitmap frame;
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                            frame = retriever.getScaledFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, desiredWidth, desiredHeight);
                        }
                        else {
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

    private static Bitmap combineThumbnails(List<Bitmap> frames)
    {
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




    public static int previewToRenderConversionX(float previewX, float renderResolutionX)
    {
        // Preview is laid out to match render resolution (previewViewGroup size == videoWidth),
        // so 1px trên preview tương ứng 1 đơn vị trong không gian render.
        // Giữ mapping 1:1 để Pos X thay đổi đúng với chuyển động ngón tay.
        return (int) previewX;
    }
    public static int previewToRenderConversionY(float previewY, float renderResolutionY)
    {
        // Tương tự trục Y: 1px preview == 1 đơn vị render.
        return (int) previewY;
    }

    public static int renderToPreviewConversionX(float renderX, float renderResolutionX)
    {
        return (int) ((renderX * Math.min(previewAvailableWidth, renderResolutionX)) / renderResolutionX);
    }
    public static int renderToPreviewConversionY(float renderY, float renderResolutionY)
    {
        return (int) ((renderY * Math.min(previewAvailableHeight, renderResolutionY)) / renderResolutionY);
    }

    // TODO: Using the same ratio system like below because multiplication and division is in the same order, no plus and subtract
    //  the matrix of the preview clip are not using the previewAvailable ratio system yet, so 1366 width
    //  in the 1080px screen the movement will be jittered

    public static float previewToRenderConversionScalingX(float clipScaleX, float renderResolutionX)
    {
        float ratioX = getRenderRatio(previewAvailableWidth, renderResolutionX);
        float ratioY = getRenderRatio(previewAvailableHeight, renderResolutionX);
        float ratio = Math.min(ratioX, ratioY);
        return ratio == 0 ? clipScaleX : clipScaleX / ratio;
    }
    public static float previewToRenderConversionScalingY(float clipScaleY, float renderResolutionY)
    {
        float ratioX = getRenderRatio(previewAvailableWidth, renderResolutionY);
        float ratioY = getRenderRatio(previewAvailableHeight, renderResolutionY);
        float ratio = Math.min(ratioX, ratioY);
        return ratio == 0 ? clipScaleY : clipScaleY / ratio;
    }

    public static float renderToPreviewConversionScalingX(float clipScaleX, float renderResolutionX, float renderResolutionY)
    {
        float ratioX = getRenderRatio(previewAvailableWidth, renderResolutionX);
        float ratioY = getRenderRatio(previewAvailableHeight, renderResolutionY);
        float ratio = Math.min(ratioX, ratioY);
        float result = clipScaleX * ratio;
        Log.d("Conversion", "renderToPreviewConversionScalingX: clipScaleX=" + clipScaleX + ", renderResolutionX=" + renderResolutionX + ", renderResolutionY=" + renderResolutionY + ", previewAvailableWidth=" + previewAvailableWidth + ", previewAvailableHeight=" + previewAvailableHeight + ", ratioX=" + ratioX + ", ratioY=" + ratioY + ", minRatio=" + ratio + ", result=" + result);
        return result;
    }
    public static float renderToPreviewConversionScalingY(float clipScaleY, float renderResolutionX, float renderResolutionY)
    {
        float ratioX = getRenderRatio(previewAvailableWidth, renderResolutionX);
        float ratioY = getRenderRatio(previewAvailableHeight, renderResolutionY);
        float ratio = Math.min(ratioX, ratioY);
        float result = clipScaleY * ratio;
        Log.d("Conversion", "renderToPreviewConversionScalingY: clipScaleY=" + clipScaleY + ", renderResolutionX=" + renderResolutionX + ", renderResolutionY=" + renderResolutionY + ", previewAvailableWidth=" + previewAvailableWidth + ", previewAvailableHeight=" + previewAvailableHeight + ", ratioX=" + ratioX + ", ratioY=" + ratioY + ", minRatio=" + ratio + ", result=" + result);
        return result;
    }

    public static float getRenderRatio(float previewAvailable, float renderResolution)
    {
        float ratio = Math.min(previewAvailable, renderResolution) / renderResolution;
        Log.d("Conversion", "getRenderRatio: previewAvailable=" + previewAvailable + ", renderResolution=" + renderResolution + ", ratio=" + ratio);
        return ratio;
    }

    // TODO: For the scaling. When passing the previewAvailableWidth/Height. We get
    //  the previewAvailable / renderResolution for the ratio. And we divide the render scale by the ratio to
    //  get the preview












    public static class Timeline implements Serializable {
        @Expose
        public List<Track> tracks = new ArrayList<>();
        @Expose
        public float duration;

        public void addTrack(Track track) {
            tracks.add(track);
        }

        public void removeTrack(Track track) {
            tracks.remove(track);
        }

        public List<Clip> getClipAtCurrentTime(float playheadTime) {
            List<Clip> clips = new ArrayList<>();
            for (Track track : tracks) {
                for (Clip clip : track.clips) {
                    if (playheadTime >= clip.startTime && playheadTime < clip.startTime + clip.duration) {
                        clips.add(clip);
                    }
                }
            }
            return clips; // No clip at this time
        }
        public Track getTrackFromClip(Clip selectedClip) {
            for (Track track : tracks) {
                if(track.clips.contains(selectedClip))
                    return track;
            }
            return null;
        }



        public static void saveTimeline(Context context, Timeline timeline, MainAreaScreen.ProjectData data, VideoSettings settings)
        {
            float max = 0;
            for (Track trackCpn : timeline.tracks) {
                float endTime = trackCpn.getTrackEndTime();
                if(endTime > max) max = endTime;

                trackCpn.sortClips();
            }
            timeline.duration = max;

            Clip nearestClip = null;
            for (Track trackCpn : timeline.tracks) {
                for (Clip c : trackCpn.clips)
                {
                    nearestClip = c;
                    break;
                }
                if(nearestClip != null) break;
            }
            if(nearestClip != null)
                IOImageHelper.SaveFileAsPNGImage(context, IOHelper.CombinePath(data.getProjectPath(), "preview.png"), extractThumbnail(context, nearestClip.getAbsolutePreviewPath(data), nearestClip, 1).get(0), 25);

            data.setProjectTimestamp(new Date().getTime());
            data.setProjectDuration((long) (timeline.duration * 1000));
            data.setProjectSize(IOHelper.getFileSize(context, data.getProjectPath()));


            String jsonTimeline = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(timeline); // Save


            data.savePropertiesAtProject(context);
            settings.saveSettings(context, data);
            IOHelper.writeToFile(context, IOHelper.CombinePath(data.getProjectPath(), Constants.DEFAULT_TIMELINE_FILENAME), jsonTimeline);


        }
        public static Timeline loadRawTimeline(Context context, MainAreaScreen.ProjectData data)
        {
            String json = IOHelper.readFromFile(context, IOHelper.CombinePath(data.getProjectPath(), Constants.DEFAULT_TIMELINE_FILENAME));
            return new Gson().fromJson(json, Timeline.class);
        }
        public static Timeline loadTimeline(Context context, EditingActivity instance, MainAreaScreen.ProjectData data)
        {
            return loadTimeline(context, instance, loadRawTimeline(context, data));
        }
        public static Timeline loadTimeline(Context context, EditingActivity instance, Timeline timeline)
        {
            if(timeline == null) return new Timeline();
            for (Track track : timeline.tracks) {
                track.viewRef = instance.addNewTrackUi();
                track.viewRef.trackInfo = track;

                for (Clip clip : track.clips) {
                    clip.filterNullAfterLoad();
                    instance.addClipToTrackUi(track.viewRef, clip);
                }
            }

            return timeline;
        }

        public int getAllClipCount() {
            int clipCount = 0;
            for (EditingActivity.Track track : tracks) {
                clipCount += track.clips.size();
            }
            return clipCount;
        }
    }

    public static class Track implements Serializable {
        @Expose
        public int trackIndex;
        @Expose
        public List<Clip> clips = new ArrayList<>();

        //Not serializing
        public transient TrackFrameLayout viewRef;



        public Track(int trackIndex, TrackFrameLayout viewRef) {
            this.trackIndex = trackIndex;
            this.viewRef = viewRef;
        }

        public void addClip(Clip clip) {
            clips.add(clip);
        }

        public void removeClip(Clip clip) {
            clips.remove(clip);
        }
        public void sortClips()
        {
            clips.sort((o1, o2) -> (Float.compare(o1.startTime, o2.startTime)));
        }

        public float getTrackEndTime() {
            float max = 0f;
            for (Clip clip : clips) {
                float end = clip.startTime + clip.duration;
                if (end > max) max = end;
            }
            return max;
        }

        public void select() {
            viewRef.setBackgroundColor(0xFFAAAAAA);
        }
        public void deselect() {
            viewRef.setBackgroundColor(0xFF222222);
        }

        public void clearTransition() {
            for (Clip clip : clips) {
                clip.endTransition = null;
            }
            removeTransitionUi();
        }
        public void disableTransitions() {
            for (Clip clip : clips) {
                clip.endTransitionEnabled = false;
            }
            removeTransitionUi();
        }

        public void removeTransitionUi() {

            for (int i = 0; i < viewRef.getChildCount(); i++) {
                View targetView = viewRef.getChildAt(i);

                if (targetView != null && targetView.getTag(R.id.transition_knot_tag) != null) {
                    viewRef.removeView(targetView);
                    break;
                }
            }
        }



        public void delete(Timeline timeline, ViewGroup trackContainer, ViewGroup trackInfo, EditingActivity activity) {
            activity.deselectingTrack();

            timeline.removeTrack(timeline.tracks.get(trackIndex));

            // Lower the higher indexes track by 1 to fill up the remove one.
            // When removed, trackIndex element become the next element
            List<Track> tracks = timeline.tracks;
            for (int i = trackIndex; i < tracks.size(); i++) {
                Track higherTrack = tracks.get(i);
                higherTrack.trackIndex--;
            }

            trackContainer.removeView(viewRef);

            // Since the track #n is following the pattern, we just need to delete the last track # text and it does the job
            // -1 for the count to index, like count is 4 but index should be 3
            // -1 for the index for the button
            trackInfo.removeView(trackInfo.getChildAt(trackInfo.getChildCount() - 2));

            // Decrease the trackIndex from the global scope
            activity.trackCount--;
        }

    }

    public static class Clip implements Serializable {
        @Expose
        public ClipType type;
        @Expose
        private String clipName;
        @Expose
        public float startTime; // in seconds
        @Expose
        public float duration;  // in seconds
        @Expose
        public float startClipTrim; // in seconds
        @Expose
        public float endClipTrim; // in seconds
        @Expose
        public float originalDuration;  // in seconds
        @Expose
        public int trackIndex;

        @Expose
        public int width;
        @Expose
        public int height;



        @Expose
        public VideoProperties videoProperties;


        @Expose
        public AnimatedProperty keyframes = new AnimatedProperty();

        // FX support (for EFFECT type)
        @Expose
        public EffectTemplate effect; // for EFFECT type
        @Expose
        public String textContent;    // for TEXT type
        @Expose
        public float fontSize;    // for TEXT type


        // TODO: End transition for clip later that attached to the end of this clip.
        //  this will make more sense than begin Transition as no one would merge the beginning of the clip and call it a transition.
        //  Save this endTransition alongside with this clip.
        @Expose
        public TransitionClip endTransition = null;
        @Expose
        public boolean endTransitionEnabled = false;

        /**
         * For VIDEO type.
         * When import, check whether the clip has audio or not
         * When export, decide to include audio stream or not to prevent "match no stream" ffmpeg error.
         */
        @Expose
        public boolean isVideoHasAudio;    // for VIDEO type

        /**
         * For VIDEO type.
         * Can use this to mute video when export
         */
        @Expose
        public boolean isMute;    // for VIDEO type
        @Expose
        public boolean isLockedForTemplate;    // for VIDEO type


        //Not serializing
        public transient View leftHandle, rightHandle;
        public transient ImageGroupView viewRef;
        public transient ImageView templateLockViewRef;


        public Clip(String clipName, float startTime, float duration, int trackIndex, ClipType type, boolean isVideoHasAudio, int width, int height) {
            this.clipName = clipName;
            this.startTime = startTime;
            this.startClipTrim = 0;
            this.endClipTrim = 0;
            this.duration = duration;
            this.originalDuration = duration;
            this.trackIndex = trackIndex;
            this.type = type;
            this.isVideoHasAudio = isVideoHasAudio;

            this.width = width;
            this.height = height;

            this.videoProperties = new VideoProperties(0, 0, 0, 1, 1, 1, 1);
            this.isMute = false;
        }

        public Clip(Clip clip) {
            this.clipName = clip.clipName;
            this.startTime = clip.startTime;
            this.startClipTrim = clip.startClipTrim;
            this.endClipTrim = clip.endClipTrim;
            this.duration = clip.duration;
            this.originalDuration = clip.originalDuration;
            this.trackIndex = clip.trackIndex;
            this.type = clip.type;
            this.isVideoHasAudio = clip.isVideoHasAudio;

            this.width = clip.width;
            this.height = clip.height;

            this.videoProperties = new VideoProperties(clip.videoProperties);
            this.isMute = clip.isMute;
            this.isLockedForTemplate = clip.isLockedForTemplate;


            if(clip.type == ClipType.TEXT)
            {
                this.textContent = clip.textContent;
                this.fontSize = clip.fontSize;
            }
            if(clip.type == ClipType.EFFECT)
            {
                this.effect = clip.effect;
            }
        }


        /**
         * Full transfer to this new Clip.
         * Safe enough to filter the null when the new update rolled out.
         * @param loadedClip Clip that loaded from JSON and are potentially contains null variables
         */
        public void safeLoad(Clip loadedClip)
        {
            // ? WTF am I writing this for. Make no sense!
        }


        public void filterNullAfterLoad()
        {
            if(type == null) type = ClipType.VIDEO;
            if(videoProperties == null) videoProperties = new VideoProperties();
            if(keyframes == null) keyframes = new AnimatedProperty();
            if(keyframes.keyframes == null) keyframes.keyframes = new ArrayList<>();
        }


        public void registerClipHandle(ImageGroupView clipView, EditingActivity activity, HorizontalScrollView timelineScroll) {
            viewRef = clipView;

            // Lock display for isLockedForTemplate

            templateLockViewRef = new ImageView(activity);
            ImageGroupView.LayoutParams templateLockLayoutParams = new ImageGroupView.LayoutParams(30, 30);
            templateLockLayoutParams.setMargins(5, 5, 0, 0);
            templateLockViewRef.setLayoutParams(templateLockLayoutParams);
            templateLockViewRef.setImageResource(R.drawable.baseline_lock_24);
            viewRef.addView(templateLockViewRef);
            templateLockViewRef.setVisibility(isLockedForTemplate ? View.VISIBLE : View.GONE);

            leftHandle = new View(clipView.getContext());
            leftHandle.setBackgroundColor(Color.WHITE);
            RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(35, ViewGroup.LayoutParams.MATCH_PARENT);
            leftParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            //leftParams.setMarginStart(-35);   for rendering the part outside of clip to match Capcut
            leftHandle.setLayoutParams(leftParams);

            rightHandle = new View(clipView.getContext());
            rightHandle.setBackgroundColor(Color.WHITE);
            RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(35, ViewGroup.LayoutParams.MATCH_PARENT);
            rightParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            //rightParams.setMarginEnd(-35);   for rendering the part outside of clip to match Capcut
            rightHandle.setLayoutParams(rightParams);

            leftHandle.setOnTouchListener(
                    new View.OnTouchListener() {
                        float dX;
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            Clip clip = (Clip) clipView.getTag();
                            int minWidth = (int) (MIN_CLIP_DURATION * pixelsPerSecond);
                            int maxWidth = (int) (clip.originalDuration * pixelsPerSecond);
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    dX = event.getRawX();
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    timelineScroll.requestDisallowInterceptTouchEvent(true);

                                    float deltaX = event.getRawX() - dX;
                                    dX = event.getRawX();


                                    // Clamping only for video and audio as these type has limited duration
                                    if (type == ClipType.VIDEO || type == ClipType.AUDIO)
                                    {
                                        deltaX = (Math.min(-deltaX, clip.startClipTrim * pixelsPerSecond));
                                        deltaX = -deltaX;


                                        int newWidth = clipView.getWidth() - (int) deltaX;

                                        // Clamping
                                        if (newWidth < minWidth) return true;


                                        newWidth = Math.max(minWidth, Math.min(newWidth, maxWidth));

                                        clipView.getLayoutParams().width = newWidth;
                                        clipView.setX(clipView.getX() + deltaX);
                                        clipView.requestLayout();

                                        clip.startTime = (clipView.getX() - centerOffset) / pixelsPerSecond;
                                        clip.startClipTrim += (deltaX) / pixelsPerSecond;
                                        clip.duration = clip.originalDuration - clip.endClipTrim - clip.startClipTrim;//Math.max(MIN_CLIP_DURATION, newWidth / (float) pixelsPerSecond);

                                    }
                                    else {
                                        int newWidth = clipView.getWidth() - (int) deltaX;

                                        clipView.getLayoutParams().width = newWidth;
                                        clipView.setX(clipView.getX() + deltaX);
                                        clipView.requestLayout();

                                        clip.startTime = (clipView.getX() - centerOffset) / pixelsPerSecond;
                                        clip.startClipTrim += (deltaX) / pixelsPerSecond;
                                        clip.duration = clip.originalDuration - clip.endClipTrim - clip.startClipTrim;
                                    }
                                    break;

                                case MotionEvent.ACTION_UP:
                                case MotionEvent.ACTION_CANCEL:
                                    timelineScroll.requestDisallowInterceptTouchEvent(false);

                                    if(clip.startTime < 0)
                                    {
                                        clipView.setX(activity.getTimeInX(0));
                                        clip.startTime = 0;
                                    }

                                    activity.updateCurrentClipEnd();
                                    break;

                            }
                            return true;
                        }
                    }
            );

            rightHandle.setOnTouchListener(
                    new View.OnTouchListener() {
                        float dX;
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            Clip clip = (Clip) clipView.getTag();
                            int minWidth = (int) (MIN_CLIP_DURATION * pixelsPerSecond);
                            int maxWidth = (int) (clip.originalDuration * pixelsPerSecond);
                            switch (event.getAction())
                            {
                                case MotionEvent.ACTION_DOWN:
                                    dX = event.getRawX();
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    timelineScroll.requestDisallowInterceptTouchEvent(true);

                                    float deltaX = event.getRawX() - dX;
                                    dX = event.getRawX();

                                    // Clamping only for video and audio as these type has limited duration
                                    if(type == ClipType.VIDEO || type == ClipType.AUDIO)
                                    {
                                        deltaX = Math.min(deltaX, clip.endClipTrim * pixelsPerSecond);

                                        int newWidth = clipView.getWidth() + (int) deltaX;

                                        // Clamping
                                        if (newWidth < minWidth) return true;

                                        newWidth = Math.max(minWidth, Math.min(newWidth, maxWidth));

                                        clipView.getLayoutParams().width = newWidth;
                                        clipView.requestLayout();

                                        clip.endClipTrim -= (deltaX) / pixelsPerSecond;
                                        clip.duration = clip.originalDuration - clip.endClipTrim - clip.startClipTrim;//Math.max(MIN_CLIP_DURATION, newWidth / (float) pixelsPerSecond);
                                    }
                                    else {
                                        int newWidth = clipView.getWidth() + (int) deltaX;

                                        clipView.getLayoutParams().width = newWidth;
                                        clipView.requestLayout();

                                        clip.endClipTrim -= (deltaX) / pixelsPerSecond;
                                        clip.duration = clip.originalDuration - clip.endClipTrim - clip.startClipTrim;//Math.max(MIN_CLIP_DURATION, newWidth / (float) pixelsPerSecond);
                                    }


                                    break;

                                case MotionEvent.ACTION_UP:
                                case MotionEvent.ACTION_CANCEL:
                                    timelineScroll.requestDisallowInterceptTouchEvent(false);

                                    activity.updateCurrentClipEnd();
                                    break;

                            }

                            return true;
                        }
                    }
            );

            clipView.addView(leftHandle);
            clipView.addView(rightHandle);

            deselect();
        }

        public void select() {
            viewRef.getFilledImageView().setColorFilter(0x77AAAAAA);

            leftHandle.setVisibility(View.VISIBLE);
            rightHandle.setVisibility(View.VISIBLE);
        }
        public void deselect() {
            viewRef.getFilledImageView().setColorFilter(0x00000000);

            leftHandle.setVisibility(View.GONE);
            rightHandle.setVisibility(View.GONE);
        }



        public void deleteClip(Timeline timeline, EditingActivity activity)
        {
            activity.deselectingClip();

            Track currentTrack = timeline.tracks.get(trackIndex);

            currentTrack.removeClip(this);
            currentTrack.viewRef.removeView(viewRef);
        }

        public void splitClip(EditingActivity activity, Timeline timeline, float currentGlobalTime)
        {
            Track currentTrack = timeline.tracks.get(trackIndex);

            float translatedLocalCurrentTime = getLocalClipTime(currentGlobalTime);

            Clip secondaryClip = new Clip(this);

            secondaryClip.startTime = currentGlobalTime;

            float oldEndClipTrim = endClipTrim;
            float oldStartClipTrim = startClipTrim;

            // Primary clip
            endClipTrim = originalDuration - (translatedLocalCurrentTime + oldStartClipTrim);
            duration = originalDuration - endClipTrim - startClipTrim;

            // Secondary clip
            secondaryClip.startClipTrim = translatedLocalCurrentTime + oldStartClipTrim;
            secondaryClip.endClipTrim = oldEndClipTrim;
            secondaryClip.duration = secondaryClip.originalDuration - secondaryClip.endClipTrim - secondaryClip.startClipTrim;

            activity.addClipToTrack(currentTrack, secondaryClip);
            activity.revalidationClipView(this);
        }
        public void restate() {
            videoProperties = new VideoProperties(0, 0, 0, 1, 1, 1, 1);
        }

        public void mergingVideoPropertiesFromSingleKeyframe() {
            if(hasOnlyOneAnimatedProperties())
            {
                VideoProperties videoProperties = keyframes.keyframes.get(0).value;

                applyPropertiesToClip(videoProperties);
            }
        }
        public void applyPropertiesToClip(VideoProperties properties) {
            this.videoProperties = new VideoProperties(properties);
        }
        public boolean isClipTransitionAvailable()
        {
            return endTransitionEnabled && endTransition != null;
        }




        /**
         * To ensure rendering keyframe correctly, there must have more than 2 keyframe to form a line for
         * lerping back and forth.
         * 2 points create 1 line, 3 points create 2 lines, 4 points create 3 lines, etc...
         * @return true if keyframe list has more than 2 element, false otherwise.
         */
        public boolean hasAnimatedProperties() {
            return keyframes.keyframes.size() > 1;
        }

        /**
         * If there's not enough keyframe to render, then we just merging the video properties to
         * the main video.
         * @return true if keyframe list has only 1 element, false otherwise.
         */
        public boolean hasOnlyOneAnimatedProperties() {
            return keyframes.keyframes.size() == 1;
        }


        /**
         * Used for FFmpeg and other output that requires original quality.
         *
         * @param properties The Project Data.
         * @return The path for original file.
         */
        public String getAbsolutePath(MainAreaScreen.ProjectData properties) {
            return getAbsolutePath(properties.getProjectPath());
        }
        public String getAbsolutePath(String projectPath) {
            return IOHelper.CombinePath(projectPath, Constants.DEFAULT_CLIP_DIRECTORY, getClipName());
        }
        /**
         * Used for EditingActivity in which didn't need high quality video. Fit for real-time preview.
         *
         * @param properties The Project Data.
         * @return The path for preview file.
         */
        public String getAbsolutePreviewPath(MainAreaScreen.ProjectData properties) {
            return getAbsolutePreviewPath(properties.getProjectPath());
        }
        public String getAbsolutePreviewPath(String projectPath) {
            String path = IOHelper.CombinePath(projectPath, Constants.DEFAULT_PREVIEW_CLIP_DIRECTORY, getClipName());
            // Fallback if not available yet.
            // TODO: Temporary fix for the soon preview loading. Consider block main thread for preview to have time to load first
            if(!IOHelper.isFileExist(path))
                path = getAbsolutePath(projectPath);
            return path;
        }
        /**
         * Used for EditingActivity in which didn't need high quality video. Fit for real-time preview.
         *
         * @param properties The Project Data.
         * @return The path for preview file.
         */
        public String getAbsolutePreviewPath(MainAreaScreen.ProjectData properties, String previewExtension) {
            return getAbsolutePreviewPath(properties.getProjectPath(), previewExtension);
        }
        public String getAbsolutePreviewPath(String projectPath, String previewExtension) {
            String path = IOHelper.CombinePath(projectPath, Constants.DEFAULT_PREVIEW_CLIP_DIRECTORY, getClipName().substring(0, getClipName().lastIndexOf('.')) + previewExtension);
            // Fallback if not available yet.
            // TODO: Temporary fix for the soon preview loading. Consider block main thread for preview to have time to load first
            if(!IOHelper.isFileExist(path))
                path = getAbsolutePath(projectPath);
            return path;
        }
        public float getLocalClipTime(float playheadTime) {
            return playheadTime - startTime;
        }

        public float getTrimmedLocalTime(float localClipTime) {
            return localClipTime + startClipTrim;
        }

        public float getCutoutDuration() {
            return duration - startClipTrim - endClipTrim;
        }





        public boolean getIsLockedForTemplate()
        {
            return isLockedForTemplate;
        }
        public void setIsLockedForTemplate(boolean value)
        {
            isLockedForTemplate = value;
            templateLockViewRef.setVisibility(value ? View.VISIBLE : View.GONE);
        }

        public String getClipName()
        {
            return clipName;
        }
        public void setClipName(String clipName, MainAreaScreen.ProjectData data)
        {
            File file = new File(getAbsolutePath(data));
            if(file.renameTo(new File(getAbsolutePath(data).replace(this.clipName, clipName))))
                this.clipName = clipName;
            else
                ; // Operation failed
        }

    }




    public enum ClipType {
        VIDEO,
        AUDIO,
        IMAGE,
        TEXT,
        TRANSITION,
        EFFECT
    }
    public static class EffectTemplate implements Serializable {
        @Expose
        public String type; // "transition", "overlay", etc
        @Expose
        public String style; // "fade", "zoom", "glitch"
        @Expose
        public double duration;
        @Expose
        public double offset;
        @Expose
        public Map<String, Object> params;

        public EffectTemplate(String style, double duration, double offset)
        {
            this.style = style;
            this.duration = duration;
            this.offset = offset;
        }

    }


    static class DragContext {
        View ghost;
        Track currentTrack;
        Clip clip;
    }

    public static class TransitionClip implements Serializable {
        @Expose
        public int trackIndex;
        @Expose
        public float startTime;
        @Expose
        public float duration;

        @Expose
        public EffectTemplate effect; // e.g. xfade, zoom, etc.

        @Expose
        public TransitionMode mode;


        public enum TransitionMode {
            END_FIRST,
            OVERLAP,
            BEGIN_SECOND
        }
    }



    public static class VideoSettings implements Serializable {
        int videoWidth;
        int videoHeight;
        int frameRate;
        int crf;
        int clipCap;
        String preset;
        String tune;
        public VideoSettings(int videoWidth, int videoHeight, int frameRate, int crf, int clipCap, String preset, String tune)
        {
            this.videoWidth = videoWidth;
            this.videoHeight = videoHeight;
            this.frameRate = frameRate;
            this.crf = crf;
            this.clipCap = clipCap;
            this.preset = preset;
            this.tune = tune;
        }

        public int getVideoWidth() {
            return videoWidth;
        }
        public int getVideoHeight() {
            return videoHeight;
        }
        public int getFrameRate() {
            return frameRate;
        }
        public int getCRF() {
            return crf;
        }
        public int getClipCap() {
            return clipCap;
        }
        public String getPreset() {
            return preset;
        }
        public String getTune() {
            return tune;
        }

        public void saveSettings(Context context, MainAreaScreen.ProjectData data) {
            IOHelper.writeToFile(context, IOHelper.CombinePath(data.getProjectPath(), Constants.DEFAULT_VIDEO_SETTINGS_FILENAME), new Gson().toJson(this));
        }
        public void loadSettingsFromProject(Context context, MainAreaScreen.ProjectData data)
        {
            VideoSettings loadSettings = loadSettings(context, data);
            this.videoWidth = loadSettings.videoWidth;
            this.videoHeight = loadSettings.videoHeight;
            this.frameRate = loadSettings.frameRate;
            this.crf = loadSettings.crf;
            this.clipCap = loadSettings.clipCap;
            this.preset = loadSettings.preset;
            this.tune = loadSettings.tune;
        }
        public static VideoSettings loadSettings(Context context, MainAreaScreen.ProjectData data) {
            return new Gson().fromJson(IOHelper.readFromFile(context, IOHelper.CombinePath(data.getProjectPath(), Constants.DEFAULT_VIDEO_SETTINGS_FILENAME)), VideoSettings.class);
        }

        /*
        // Low
videoWidth = 640;
videoHeight = 360;
frameRate = 24;

// Medium
videoWidth = 1280;
videoHeight = 720;
frameRate = 30;

// High
videoWidth = 1920;
videoHeight = 1080;
frameRate = 60;

         */


        public class FfmpegPreset
        {
            public static final String PLACEBO = "placebo";
            public static final String VERYSLOW = "veryslow";
            public static final String SLOWER = "slower";
            public static final String SLOW = "slow";
            public static final String MEDIUM = "medium";
            public static final String FAST = "fast";
            public static final String FASTER = "faster";
            public static final String VERYFAST = "veryfast";
            public static final String SUPERFAST = "superfast";
            public static final String ULTRAFAST = "ultrafast";
        }

        public class FfmpegTune
        {
            public static final String FILM = "film";
            public static final String ANIMATION = "animation";
            public static final String GRAIN = "grain";
            public static final String STILLIMAGE = "stillimage";
            public static final String FASTDECODE = "fastdecode";
            public static final String ZEROLATENCY = "zerolatency";
        }

    }
    public static class VideoProperties implements Serializable {
        @Expose
        public float valuePosX;
        @Expose
        public float valuePosY;
        @Expose
        public float valueRot;
        @Expose
        public float valueScaleX;
        @Expose
        public float valueScaleY;
        @Expose
        public float valueOpacity;
        @Expose
        public float valueSpeed;

        public VideoProperties()
        {
            this.valuePosX = 0;
            this.valuePosY = 0;
            this.valueRot = 0;
            this.valueScaleX = 1;
            this.valueScaleY = 1;
            this.valueOpacity = 1;
            this.valueSpeed = 1;
        }
        public VideoProperties(float valuePosX, float valuePosY,
                               float valueRot,
                               float valueScaleX, float valueScaleY,
                               float valueOpacity, float valueSpeed)
        {
            this.valuePosX = valuePosX;
            this.valuePosY = valuePosY;
            this.valueRot = valueRot;
            this.valueScaleX = valueScaleX;
            this.valueScaleY = valueScaleY;
            this.valueOpacity = valueOpacity;
            this.valueSpeed = valueSpeed;
        }

        public VideoProperties(VideoProperties properties)
        {
            this.valuePosX = properties.valuePosX;
            this.valuePosY = properties.valuePosY;
            this.valueRot = properties.valueRot;
            this.valueScaleX = properties.valueScaleX;
            this.valueScaleY = properties.valueScaleY;
            this.valueOpacity = properties.valueOpacity;
            this.valueSpeed = properties.valueSpeed;
        }

        public float getValue(ValueType valueType) {

            switch (valueType)
            {
                case PosX:
                    return valuePosX;
                case PosY:
                    return valuePosY;
                case Rot:
                    return valueRot;
                case RotInRadians:
                    return (float) Math.toRadians(valueRot);
                case ScaleX:
                    return valueScaleX;
                case ScaleY:
                    return valueScaleY;
                case Opacity:
                    return valueOpacity;
                case Speed:
                    return valueSpeed;
                default:
                    return 1;

            }
        }

        public void setValue(float v, ValueType valueType) {

            switch (valueType)
            {
                case PosX:
                    valuePosX = v;
                    break;
                case PosY:
                    valuePosY = v;
                    break;
                case Rot:
                    valueRot = v;
                    break;
                case ScaleX:
                    valueScaleX = v;
                    break;
                case ScaleY:
                    valueScaleY = v;
                    break;
                case Opacity:
                    valueOpacity = v;
                    break;
                case Speed:
                    valueSpeed = v;
                    break;

            }
        }

        public enum ValueType {
            PosX, PosY, Rot, RotInRadians, ScaleX, ScaleY, Opacity, Speed
        }
    }
    public static class Keyframe implements Serializable {
        @Expose
        private float time; // seconds, in local clip time
        @Expose
        public VideoProperties value;
        @Expose
        public EasingType easing = EasingType.LINEAR;


        public Keyframe(float time, VideoProperties value, EasingType easing)
        {
            this.time = time;
            this.value = value;
            this.easing = easing;
        }

        public float getLocalTime()
        {
            return time;
        }
        public float getGlobalTime(Clip clip)
        {
            return time + clip.startTime;
        }
    }
    public static class AnimatedProperty implements Serializable {

        @Expose
        public List<Keyframe> keyframes = new ArrayList<>();

        public float getValueAtTime(Clip clip, float playheadTime, VideoProperties.ValueType valueType) {
            if (keyframes.isEmpty()) return -1;

            // preprocessing the global time to match the local time of the clip
            playheadTime -= clip.startTime;

            Keyframe prev = keyframes.get(0);
            for (Keyframe next : keyframes) {
                if (playheadTime < next.getLocalTime()) {
                    float t = (playheadTime - prev.getLocalTime()) / (next.getLocalTime() - prev.getLocalTime());
                    t = Math.max(0f, Math.min(1f, t));

                    float prevValue = prev.value.getValue(valueType);
                    float nextValue = next.value.getValue(valueType);


                    return lerp(prevValue, nextValue, ease(t, prev.easing)); // linear interpolation
                }
                prev = next;

//                if (playheadTime <= keyframes.get(0).time) return keyframes.get(0).value;
//                if (playheadTime >= keyframes.get(keyframes.size() - 1).time) return keyframes.get(keyframes.size() - 1).value;

            }
            return keyframes.get(keyframes.size() - 1).value.getValue(valueType);
        }
        public float getValueAtPoint(int keyframeIndex, VideoProperties.ValueType valueType) {
            if (keyframes.isEmpty()) return 0f;

            return keyframes.get(keyframeIndex).value.getValue(valueType);
        }

        private float lerp(float a, float b, float t) {
            return a + (b - a) * t;
        }

        private float ease(float t, EasingType type) {
            switch (type) {
                case LINEAR: return t;
                case EASE_IN: return t * t;
                case EASE_OUT: return 1 - (1 - t) * (1 - t);
                case EXPONENTIAL: return (float)Math.pow(2, 10 * (t - 1));
                case EASE_IN_OUT:
                    return t < 0.5f ? 2 * t * t : 1 - (float)Math.pow(-2 * t + 2, 2) / 2;
                case QUADRATIC:
                    return t * t; // same as EASE_IN, but you can customize later
                case SPRING:
                    MathHelper.spring(t, 1, 12, 0.5f);
//                    SpringProperty spring = new SpringProperty();
//                    return spring.getValueAt(t);
                // Add more...
                default: return t;
            }
        }

    }
//    public static class SpringProperty implements Serializable {
//        public float mass = 1f;
//        public float stiffness = 12f;
//        public float damping = 0.5f;
//
//        public float getValueAt(float t) {
//            // Critically damped spring motion
//            return 1 - (float)Math.exp(-damping * t) * (1 + damping * t);
//        }
//    }

    public enum EasingType {
        LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT, EXPONENTIAL, SPRING, QUADRATIC
    }


    @UnstableApi
    public static class ClipRenderer {
        public final Clip clip;

        private ExoPlayer exoPlayer; // ExoPlayer handles both video and audio
        private TextureView textureView;
        private Context context;
        private EditingActivity editingActivity;
        private FrameLayout previewViewGroupRef;
        private MainAreaScreen.ProjectData projectData;

        public boolean isPlaying;
        private boolean isExoPlayerPrepared = false; // Lazy loading flag
        private Player.Listener exoPlayerListener; // Store listener to remove it later



        private Matrix matrix = new Matrix();
        private float scaleX = 1, scaleY = 1;
        private float rot = 0;
        private float posX = 0, posY = 0;


        private float scaleMatrixX = 1, scaleMatrixY = 1;
        private float rotMatrix = 0;
        private float posMatrixX = 0, posMatrixY = 0;

        private float initialPosX = 0, initialPosY = 0;
        private float startTouchX = 0, startTouchY = 0;

        private View dragBorderView;
        private android.graphics.drawable.GradientDrawable dragBorderDrawable;

        private long lastSeekPositionMs = -1;
        private float lastPlayheadTime = -1f;


        public ClipRenderer(Context context, Clip clip, MainAreaScreen.ProjectData data, VideoSettings settings, EditingActivity editingActivity, FrameLayout previewViewGroup, TextView textCanvasControllerInfo) {
            this.context = context;
            this.clip = clip;
            this.editingActivity = editingActivity;
            this.previewViewGroupRef = previewViewGroup;
            this.projectData = data;

            try
            {

                switch (clip.type)
                {
                    case VIDEO:
                    {

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
                            // Each ExoPlayer will use a unique audio session ID
                            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                    .setContentType(C.CONTENT_TYPE_MOVIE)
                                    .setUsage(C.USAGE_MEDIA)
                                    .build();
                            exoPlayer.setAudioAttributes(audioAttributes, false); // false = don't handle audio focus, allow multiple streams
                            exoPlayer.setVolume(1.0f); // Ensure volume is at maximum

                            // Don't prepare ExoPlayer immediately - lazy load when clip becomes visible
                            // This prevents ANR when importing multiple files
                            // ExoPlayer will be prepared in ensureExoPlayerPrepared() when clip is first visible
                            String originalPath = clip.getAbsolutePath(data);
                            Log.d("ClipRenderer", "Initializing ExoPlayer for clip: " + clip.clipName + ", path: " + originalPath);
                            
                            if (originalPath == null || originalPath.isEmpty()) {
                                Log.e("ClipRenderer", "Invalid path (null or empty) for clip: " + clip.clipName);
                                exoPlayer.release();
                                exoPlayer = null;
                                break;
                            }
                            
                            File file = new File(originalPath);
                            if (!file.exists()) {
                                Log.e("ClipRenderer", "File does not exist: " + originalPath + " for clip: " + clip.clipName + ". File size: " + file.length() + ", isDirectory: " + file.isDirectory());
                                exoPlayer.release();
                                exoPlayer = null;
                                break;
                            }
                            
                            Log.d("ClipRenderer", "File exists, size: " + file.length() + " bytes for clip: " + clip.clipName);
                            
                            MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(file));
                            exoPlayer.setMediaItem(mediaItem);
                            // Don't call prepare() here - will be called lazily
                            
                            Log.d("ClipRenderer", "ExoPlayer initialized successfully for clip: " + clip.clipName);
                            
                            // Set initial transformations from clip.videoProperties
                            float renderScaleX = clip.videoProperties.getValue(VideoProperties.ValueType.ScaleX);
                            float renderScaleY = clip.videoProperties.getValue(VideoProperties.ValueType.ScaleY);
                            posX = (EditingActivity.renderToPreviewConversionX(clip.videoProperties.getValue(VideoProperties.ValueType.PosX), settings.videoWidth));
                            posY = (EditingActivity.renderToPreviewConversionY(clip.videoProperties.getValue(VideoProperties.ValueType.PosY), settings.videoHeight));
                            scaleX = (EditingActivity.renderToPreviewConversionScalingX(renderScaleX, settings.videoWidth, settings.videoHeight));
                            scaleY = (EditingActivity.renderToPreviewConversionScalingY(renderScaleY, settings.videoWidth, settings.videoHeight));
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
                    case IMAGE:
                    {
                        textureView = new TextureView(context);
                        RelativeLayout.LayoutParams textureViewLayoutParams =
                                new RelativeLayout.LayoutParams(clip.width, clip.height);
                        previewViewGroup.addView(textureView, textureViewLayoutParams);

                        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                            @Override
                            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                                // Create a Surface from the TextureView

                                // For IMAGE rendering
                                // TODO: WTF sample size 1? Yes for rendering. We don't know how to forcefully extend the 8 old sampleSize
                                Bitmap image = IOImageHelper.LoadFileAsPNGImage(context, clip.getAbsolutePreviewPath(data), 1);

                                // Resize TextureView to match bitmap size
                                ViewGroup.LayoutParams params = textureView.getLayoutParams();
                                params.width = clip.width;
                                params.height = clip.height;
                                textureView.setLayoutParams(params);

                                // Draw the bitmap onto the TextureView’s canvas
                                Canvas canvas = textureView.lockCanvas();
                                if (canvas != null) {
                                    //canvas.drawColor(Color.BLACK); // optional background
                                    canvas.drawBitmap(image, 0, 0, null); // draw at top-left
                                    textureView.unlockCanvasAndPost(canvas);
                                }


                                surfaceTexture.setDefaultBufferSize(clip.width, clip.height); // or your target resolution

                                posX = (EditingActivity.renderToPreviewConversionX(clip.videoProperties.getValue(VideoProperties.ValueType.PosX), settings.videoWidth));
                                posY = (EditingActivity.renderToPreviewConversionY(clip.videoProperties.getValue(VideoProperties.ValueType.PosY), settings.videoHeight));
                                scaleX = (EditingActivity.renderToPreviewConversionScalingX(clip.videoProperties.getValue(VideoProperties.ValueType.ScaleX), settings.videoWidth, settings.videoHeight));
                                scaleY = (EditingActivity.renderToPreviewConversionScalingY(clip.videoProperties.getValue(VideoProperties.ValueType.ScaleY), settings.videoWidth, settings.videoHeight));

                                float clipRatio = (float) clip.width / clip.height;
                                float resolutionRatio = (float) settings.videoWidth / settings.videoHeight;
                                float previewAvailableRatio = previewAvailableWidth > 0 && previewAvailableHeight > 0 ? (float) previewAvailableWidth / previewAvailableHeight : 0;
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
                                Log.e("ClipRatio", "PreviewAvailable: " + previewAvailableWidth + "x" + previewAvailableHeight);
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
                            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}

                            @Override
                            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                                return true;
                            }

                            @Override
                            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
                        });


                        break;
                    }
                    case AUDIO:
                    {
                        // Audio-only clips: Use ExoPlayer for audio playback
                        exoPlayer = new ExoPlayer.Builder(context).build();
                        
                        // Set audio attributes to ensure audio playback
                        // Set handleAudioFocus to false to allow multiple audio streams to play simultaneously
                        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                .setContentType(C.CONTENT_TYPE_MUSIC)
                                .setUsage(C.USAGE_MEDIA)
                                .build();
                        exoPlayer.setAudioAttributes(audioAttributes, false); // false = don't handle audio focus, allow multiple streams
                        exoPlayer.setVolume(1.0f); // Ensure volume is at maximum

                        // Load audio media item from original file (not preview)
                        // Don't prepare immediately - lazy load
                        String originalPath = clip.getAbsolutePath(data);
                        MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(new File(originalPath)));
                        exoPlayer.setMediaItem(mediaItem);
                        // Don't call prepare() here - will be called lazily

                        break;
                    }
                }


                if(textureView != null)
                {
                    setPivot();
                    attachGestureControls(textureView, clip, settings, editingActivity, textCanvasControllerInfo);
                }


            }
            catch (Exception e)
            {
                Log.e("ClipRenderer", "Exception in ClipRenderer constructor for clip: " + (clip != null ? clip.clipName : "unknown") + ", type: " + (clip != null ? clip.type : "unknown"), e);
                e.printStackTrace();
                LoggingManager.LogExceptionToNoteOverlay(context, e);
                // Don't set exoPlayer = null here as it might have been set in inner try-catch
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
//            if(isPlaying && !isSeekingOnly) return;

            // Lazy load ExoPlayer - prepare only when clip becomes visible
            ensureExoPlayerPrepared();
            
            // Check if exoPlayer is initialized before calling startPlayingAt
            // For VIDEO and AUDIO clips, exoPlayer must be initialized
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
                        
                        // Set media item
                        String originalPath = clip.getAbsolutePath(projectData);
                        if (originalPath != null && !originalPath.isEmpty()) {
                            File file = new File(originalPath);
                            if (file.exists()) {
                                MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(file));
                                exoPlayer.setMediaItem(mediaItem);
                                Log.d("ClipRenderer", "ExoPlayer recreated successfully for clip: " + clip.clipName);
                            } else {
                                Log.e("ClipRenderer", "File does not exist when recreating ExoPlayer: " + originalPath);
                                exoPlayer.release();
                                exoPlayer = null;
                                return;
                            }
                        } else {
                            Log.e("ClipRenderer", "Invalid path when recreating ExoPlayer: " + originalPath);
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
                    
                    // Prepare ExoPlayer on main thread (ExoPlayer handles async internally)
                    // Using Handler to ensure it's on main thread
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

        private void pumpDecoderVideoSeek(float playheadTime) {
            // This method is no longer used when playing
            // It's kept for backward compatibility but logic moved to startPlayingAt
        }
        
        private void pumpDecoderAudioSeek(float playheadTime) {
            // Audio is handled by ExoPlayer automatically
        }





        public void startPlayingAt(float playheadTime, boolean isSeekingOnly) {
            if (!isVisible(playheadTime)) {
//                Canvas canvas = surfaceHolder.lockCanvas();
//                if (canvas != null) {
//                    canvas.drawColor(Color.BLACK); // Fill canvas with black
//                    surfaceHolder.unlockCanvasAndPost(canvas);
//                }
                return;
            }


            try {

                if(textureView != null)
                {
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
                    //applyPostMatrixTransformation();
                }

                switch (clip.type)
                {
                    case VIDEO:
                    {
                        // Check if exoPlayer is initialized
                        if (exoPlayer == null) {
                            Log.w("ClipRenderer", "exoPlayer is null for VIDEO clip: " + clip.clipName);
                            break;
                        }
                        
                        // Control playback state first
                        if (!isSeekingOnly) {
                            // When playing, let ExoPlayer run continuously
                            if (!isPlaying) {
                                // Initial seek to start position
                                float clipTime = playheadTime - clip.startTime + clip.startClipTrim;
                                if (clipTime < 0) clipTime = 0;
                                long positionMs = (long)(clipTime * 1000);
                                exoPlayer.seekTo(positionMs);
                                lastSeekPositionMs = positionMs;
                                
                                exoPlayer.setPlayWhenReady(true);
                                exoPlayer.setPlaybackParameters(
                                    new PlaybackParameters(editingActivity.playbackSpeed)
                                );
                                isPlaying = true;
                            }
                            // When already playing, do NOT seek - let ExoPlayer run freely
                            // This prevents stuttering and frame drops
                        } else {
                            // When seeking (scrubbing), pause and seek to exact position
                            if (isPlaying) {
                                exoPlayer.setPlayWhenReady(false);
                                isPlaying = false;
                            }
                            // Always seek when scrubbing to show exact frame
                            float clipTime = playheadTime - clip.startTime + clip.startClipTrim;
                            if (clipTime < 0) clipTime = 0;
                            long positionMs = (long)(clipTime * 1000);
                            exoPlayer.seekTo(positionMs);
                            lastSeekPositionMs = positionMs;
                        }
                        break;
                    }
                    case AUDIO:
                    {
                        // Audio-only clips: Control playback with ExoPlayer
                        if (exoPlayer == null) break;
                        
                        if (!isSeekingOnly) {
                            // When playing, let ExoPlayer run continuously
                            if (!isPlaying) {
                                // Initial seek to start position
                                float clipTime = playheadTime - clip.startTime + clip.startClipTrim;
                                if (clipTime < 0) clipTime = 0;
                                long positionMs = (long)(clipTime * 1000);
                                exoPlayer.seekTo(positionMs);
                                lastSeekPositionMs = positionMs;
                                
                                exoPlayer.setPlayWhenReady(true);
                                exoPlayer.setPlaybackParameters(
                                    new PlaybackParameters(editingActivity.playbackSpeed)
                                );
                                isPlaying = true;
                            }
                            // When already playing, do NOT seek - let ExoPlayer run freely
                        } else {
                            // When seeking (scrubbing), pause and seek to exact position
                            if (isPlaying) {
                                exoPlayer.setPlayWhenReady(false);
                                isPlaying = false;
                            }
                            // Always seek when scrubbing to show exact frame
                            float clipTime = playheadTime - clip.startTime + clip.startClipTrim;
                            if (clipTime < 0) clipTime = 0;
                            long positionMs = (long)(clipTime * 1000);
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
                // Not affecting the translation pos when scaling
                textureView.setPivotX(0);
                textureView.setPivotY(0);
            });

        }


        EditMode currentMode = EditMode.NONE;
        
        // Use raw coordinates for stable dragging (not affected by view position changes)
        private float lastRawX = 0f;
        private float lastRawY = 0f;


        private void attachGestureControls(TextureView tv, Clip clip, VideoSettings settings, EditingActivity editingActivity, TextView textCanvasControllerInfo) {
            final GestureDetector tapDrag = new GestureDetector(tv.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override public boolean onDown(MotionEvent e) {
                    initialPosX = posX;
                    initialPosY = posY;
                    startTouchX = e.getX();
                    startTouchY = e.getY();
                    lastRawX = e.getRawX();
                    lastRawY = e.getRawY();
                    return true;
                }
                @Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
                    // Use raw coordinates to calculate delta (stable, not affected by view movement)
                    float rawX = e2.getRawX();
                    float rawY = e2.getRawY();
                    float deltaX = rawX - lastRawX;
                    float deltaY = rawY - lastRawY;
                    lastRawX = rawX;
                    lastRawY = rawY;
                    
                    if (EditingActivity.selectedClip != clip) {
                        editingActivity.selectingClip(clip);
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
                                (int) (1 * editingActivity.getResources().getDisplayMetrics().density),
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
                    float previewWidth = previewViewGroupRef != null ? previewViewGroupRef.getWidth() : EditingActivity.previewAvailableWidth;
                    float previewHeight = previewViewGroupRef != null ? previewViewGroupRef.getHeight() : EditingActivity.previewAvailableHeight;

                    if (previewWidth > 0 && previewHeight > 0) {
                        newPosX = Math.max(0f, Math.min(previewWidth - clipDisplayWidth, newPosX));
                        newPosY = Math.max(0f, Math.min(previewHeight - clipDisplayHeight, newPosY));
                    }

                    posX = newPosX;
                    posY = newPosY;
                    
                    applyPostTransformation();

                    // Sync model
                    clip.videoProperties.setValue(EditingActivity.previewToRenderConversionX(posX, settings.videoWidth), VideoProperties.ValueType.PosX);
                    clip.videoProperties.setValue(EditingActivity.previewToRenderConversionY(posY, settings.videoHeight), VideoProperties.ValueType.PosY);

                    textCanvasControllerInfo.setText("Pos X: " + clip.videoProperties.getValue(VideoProperties.ValueType.PosX) + " | Pos Y: " + clip.videoProperties.getValue(VideoProperties.ValueType.PosY));
                    return true;
                }
                @Override public boolean onSingleTapUp(MotionEvent e) {
                    editingActivity.selectingClip(clip);
                    return true;
                }
            });

            // Manual scale and rotation tracking
            final float[] lastSpanManual = { -1f };
            final float[] lastAngle = { Float.NaN };
            
            tv.setOnTouchListener((v, event) -> {
                // Handle 2-finger gestures (scale + rotate)
                        if (event.getPointerCount() >= 2) {
                    float x0 = event.getX(0);
                    float y0 = event.getY(0);
                    float x1 = event.getX(1);
                    float y1 = event.getY(1);
                    
                    // Calculate span (distance between 2 fingers) for scale
                    float currentSpan = (float) Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
                    
                    // Calculate angle for rotation
                    float angle = (float) Math.toDegrees(Math.atan2(y1 - y0, x1 - x0));
                    
                    if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                        // Start of 2-finger gesture
                        lastSpanManual[0] = currentSpan;
                                lastAngle[0] = angle;
                    currentMode = EditMode.SCALE;
                        if (EditingActivity.selectedClip != clip) {
                            editingActivity.selectingClip(clip);
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

                            // Snap to nearest 90 degrees
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
                    float renderScaleX = EditingActivity.previewToRenderConversionScalingX(scaleX, settings.videoWidth);
                    float renderScaleY = EditingActivity.previewToRenderConversionScalingY(scaleY, settings.videoHeight);
                    clip.videoProperties.setValue(renderScaleX, VideoProperties.ValueType.ScaleX);
                    clip.videoProperties.setValue(renderScaleY, VideoProperties.ValueType.ScaleY);
                    clip.videoProperties.setValue(rot, VideoProperties.ValueType.Rot);
                    
                    Log.d("ClipZoomDebug", "Scale sync | clip=" + clip.clipName 
                            + " | previewScale=(" + scaleX + "," + scaleY + ")"
                            + " | renderScale=(" + renderScaleX + "," + renderScaleY + ")");

                                textCanvasControllerInfo.setText(
                                "Scale: " + String.format("%.2f", scaleX) +
                                " | Rot: " + String.format("%.1f", rot) + "°"
                        );
                    } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
                        // End of 2-finger gesture
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
        private void applyPostTransformation() {
            textureView.post(() -> {
                // Sử dụng View properties (setScaleX/Y, setRotation, setX/Y) thay vì matrix transform
                // để hit-area của view luôn trùng với vùng hiển thị
                
                // Pivot ở góc trên trái (0,0) để scale/rotate từ góc
                textureView.setPivotX(0f);
                textureView.setPivotY(0f);
                
                // Áp dụng scale và rotation qua View properties
                // Điều này đảm bảo hit-area được scale theo
                textureView.setScaleX(scaleX);
                textureView.setScaleY(scaleY);
                textureView.setRotation(rot);
                
                // Đặt vị trí
                textureView.setX(posX);
                textureView.setY(posY);
                
            textureView.invalidate();

                // Tính bounding box thực tế sau transform
                float actualDisplayWidth = textureView.getWidth() * scaleX;
                float actualDisplayHeight = textureView.getHeight() * scaleY;
                float actualDisplayRatio = actualDisplayHeight > 0 ? actualDisplayWidth / actualDisplayHeight : 0;

                // Bounding box trong parent coordinates
                android.graphics.RectF mapped = new android.graphics.RectF(
                        posX,
                        posY,
                        posX + actualDisplayWidth,
                        posY + actualDisplayHeight
                );

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
            // Map to [-180, 180] to avoid jump
            while (a > 180f) a -= 360f;
            while (a < -180f) a += 360f;
            return a;
        }












        public enum EditMode {
            MOVE, SCALE, ROTATE, NONE
        }


        public void release() {
            // Remove listener before release to prevent memory leaks
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


    public static class TimelineRenderer {
        private final Context context;
        private List<List<ClipRenderer>> trackLayers = new ArrayList<>();
        private final Object lock = new Object(); // Lock for synchronization

        public TimelineRenderer(Context context) {
            this.context = context;
        }

        public void buildTimeline(Timeline timeline, MainAreaScreen.ProjectData properties, VideoSettings settings, EditingActivity editingActivity, FrameLayout previewViewGroup, TextView textCanvasControllerInfo)
        {
            // Build new timeline structure and migrate existing ClipRenderers if clips still exist
            // Use synchronized to avoid race condition with updateTime()
            synchronized (lock) {
                Log.d("TimelineRenderer", "buildTimeline called, current trackLayers size: " + trackLayers.size());
                
                // Store old trackLayers reference before creating new one
                List<List<ClipRenderer>> oldTrackLayers = trackLayers;
                
                // Create a map of existing ClipRenderers by clip name for quick lookup
                // Also store by clip object reference in case updateTime() created new ClipRenderers
                java.util.Map<String, ClipRenderer> existingRenderersByName = new java.util.HashMap<>();
                java.util.Set<ClipRenderer> allExistingRenderers = new java.util.HashSet<>();
                
                for (List<ClipRenderer> trackRenderer : oldTrackLayers) {
                for (ClipRenderer clipRenderer : trackRenderer) {
                        if (clipRenderer != null && clipRenderer.clip != null) {
                            // Store existing renderer by clip name for potential reuse
                            existingRenderersByName.put(clipRenderer.clip.clipName, clipRenderer);
                            allExistingRenderers.add(clipRenderer);
                        }
                    }
                }
                Log.d("TimelineRenderer", "Found " + allExistingRenderers.size() + " existing ClipRenderers to potentially migrate");

                // Note: UI operations (removeAllViews, addView) are handled in regeneratingTimelineRenderer() on main thread
                // Don't manipulate views here as this method runs on background thread

                // Create new trackLayers list
                List<List<ClipRenderer>> newTrackLayers = new ArrayList<>();
                java.util.Set<ClipRenderer> migratedRenderers = new java.util.HashSet<>();

                // Build new timeline structure and migrate existing ClipRenderers
            for (Track track : timeline.tracks) {
                List<ClipRenderer> renderers = new ArrayList<>();
                for (Clip clip : track.clips) {
                    switch (clip.type)
                    {
                        case VIDEO:
                        case AUDIO:
                        case IMAGE:
                                // Check if we can reuse an existing ClipRenderer for this clip
                                ClipRenderer existingRenderer = existingRenderersByName.get(clip.clipName);
                                if (existingRenderer != null && existingRenderer.clip.clipName.equals(clip.clipName)) {
                                    // Migrate ClipRenderer even if exoPlayer is null - exoPlayer will be created/recreated when needed
                                    // This preserves scale, position, and other state
                                    // Note: clip is final in ClipRenderer, so we can't update it, but that's fine
                                    // since we've already verified clip names match
                                    renderers.add(existingRenderer);
                                    migratedRenderers.add(existingRenderer);
                                    
                                    Log.d("TimelineRenderer", "Migrating existing ClipRenderer for clip: " + clip.clipName 
                                            + " | exoPlayer: " + (existingRenderer.exoPlayer != null ? "not null" : "null (will be created when needed)")
                                            + " | scaleX=" + existingRenderer.scaleX + " | scaleY=" + existingRenderer.scaleY
                                            + " | clip.videoProperties.ScaleX=" + clip.videoProperties.getValue(VideoProperties.ValueType.ScaleX)
                                            + " | clip.videoProperties.ScaleY=" + clip.videoProperties.getValue(VideoProperties.ValueType.ScaleY));
                                } else {
                                    // New clip or clip changed - create null placeholder
                                    // ClipRenderer will be created lazily in updateTime() when clip becomes visible
                                    renderers.add(null);
                                }
                            break;
                    }
                }
                    newTrackLayers.add(renderers);
                }
                
                // Release ClipRenderers that are no longer in the new timeline
                // Note: We don't release ClipRenderers just because exoPlayer is null - exoPlayer will be created when needed
                int releasedCount = 0;
                for (ClipRenderer clipRenderer : allExistingRenderers) {
                    if (!migratedRenderers.contains(clipRenderer)) {
                        // Clip no longer exists in timeline - release it
                        releasedCount++;
                        Log.d("TimelineRenderer", "Releasing ClipRenderer for clip that no longer exists: " + (clipRenderer.clip != null ? clipRenderer.clip.clipName : "unknown"));
                        clipRenderer.release();
                    }
                }
                Log.d("TimelineRenderer", "Released " + releasedCount + " ClipRenderers, migrated " + migratedRenderers.size() + " ClipRenderers");
                
                // Store migrated renderers for re-adding their views later
                this.lastMigratedRenderers = new java.util.HashSet<>(migratedRenderers);
                
                // Only update trackLayers after creating new structure to avoid race condition
                trackLayers = newTrackLayers;
                
                // Store references for lazy creation
                this.timeline = timeline;
                this.properties = properties;
                this.settings = settings;
                this.editingActivity = editingActivity;
                this.previewViewGroup = previewViewGroup;
                this.textCanvasControllerInfo = textCanvasControllerInfo;
            }
        }
        
        private Timeline timeline;
        private MainAreaScreen.ProjectData properties;
        private VideoSettings settings;
        private EditingActivity editingActivity;
        private FrameLayout previewViewGroup;
        private TextView textCanvasControllerInfo;
        private java.util.Set<ClipRenderer> lastMigratedRenderers = new java.util.HashSet<>();
        
        // Re-add textureViews of migrated ClipRenderers to previewViewGroup
        public void reAddMigratedViews(FrameLayout previewViewGroup) {
            synchronized (lock) {
                for (ClipRenderer cr : lastMigratedRenderers) {
                    if (cr != null && cr.textureView != null) {
                        // Update previewViewGroupRef to new previewViewGroup
                        cr.previewViewGroupRef = previewViewGroup;
                        
                        // Remove from parent if already attached
                        if (cr.textureView.getParent() != null) {
                            ((android.view.ViewGroup) cr.textureView.getParent()).removeView(cr.textureView);
                        }
                        // Add to previewViewGroup with FrameLayout.LayoutParams (not RelativeLayout)
                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(cr.clip.width, cr.clip.height);
                        previewViewGroup.addView(cr.textureView, lp);
                        
                        // Also re-add dragBorderView if exists
                        if (cr.dragBorderView != null) {
                            if (cr.dragBorderView.getParent() != null) {
                                ((android.view.ViewGroup) cr.dragBorderView.getParent()).removeView(cr.dragBorderView);
                            }
                            FrameLayout.LayoutParams borderLp = new FrameLayout.LayoutParams(
                                    cr.textureView.getWidth(), cr.textureView.getHeight());
                            previewViewGroup.addView(cr.dragBorderView, borderLp);
                        }
                        
                        // Don't sync scale/position from clip.videoProperties when migrating
                        // ClipRenderer instance already has correct preview coordinates
                        // Syncing would cause conversion errors due to non-symmetric conversion functions
                        // (previewToRenderConversionX uses 1:1 mapping, but renderToPreviewConversionX uses ratio)
                        
                        // Just re-apply current transformations to ensure view is correctly positioned
                        // The ClipRenderer instance already has the correct scale, position, and rotation
                        Log.d("TimelineRenderer", "Re-add clip: " + cr.clip.clipName 
                                + " | keeping current previewPos=(" + cr.posX + "," + cr.posY + ")"
                                + " | keeping current previewScale=(" + cr.scaleX + "," + cr.scaleY + ")"
                                + " | rot=" + cr.rot);
                        
                        // Re-apply transformations (no sync needed, just re-apply to view)
                        cr.applyPostTransformation();
                    }
                }
            }
        }
        
        public void updateTime(float time, boolean isSeekingOnly)
        {
            // Lazy create ClipRenderer when clip becomes visible
            // Use synchronized to avoid race condition with buildTimeline()
            synchronized (lock) {
                Log.d("TimelineRenderer", "updateTime called with time: " + time + ", isSeekingOnly: " + isSeekingOnly + ", trackLayers size: " + trackLayers.size());
                int trackIndex = 0;
            for (List<ClipRenderer> trackRenderer : trackLayers) {
                    if (trackIndex >= timeline.tracks.size()) break;
                    Track track = timeline.tracks.get(trackIndex);
                    
                    // Use index-based loop to avoid issues with concurrent modification
                    for (int clipIndex = 0; clipIndex < trackRenderer.size() && clipIndex < track.clips.size(); clipIndex++) {
                        Clip clip = track.clips.get(clipIndex);
                        ClipRenderer clipRenderer = trackRenderer.get(clipIndex);
                        
                        // Check if clip is visible
                        boolean isVisible = time >= clip.startTime && time <= clip.startTime + clip.duration;
                        
                        if (clipRenderer == null && isVisible) {
                            // Lazy create ClipRenderer when clip becomes visible
                            // Double check after acquiring lock to avoid creating multiple instances
                            if (clipIndex < trackRenderer.size() && trackRenderer.get(clipIndex) == null) {
                                try {
                                    Log.d("TimelineRenderer", "Creating ClipRenderer in updateTime for clip: " + clip.clipName 
                                            + " | type: " + clip.type
                                            + " | clip.videoProperties.ScaleX=" + clip.videoProperties.getValue(VideoProperties.ValueType.ScaleX)
                                            + " | clip.videoProperties.ScaleY=" + clip.videoProperties.getValue(VideoProperties.ValueType.ScaleY));
                                    clipRenderer = new ClipRenderer(context, clip, properties, settings, editingActivity, previewViewGroup, textCanvasControllerInfo);
                                    trackRenderer.set(clipIndex, clipRenderer);
                                    Log.d("TimelineRenderer", "ClipRenderer created in updateTime for clip: " + clip.clipName 
                                            + " | exoPlayer: " + (clipRenderer.exoPlayer != null ? "not null" : "null")
                                            + " | scaleX=" + clipRenderer.scaleX + " | scaleY=" + clipRenderer.scaleY
                                            + " | hashCode: " + clipRenderer.hashCode());
                                } catch (Exception e) {
                                    Log.e("TimelineRenderer", "Error creating ClipRenderer in updateTime for clip: " + clip.clipName + ": " + e.getMessage(), e);
                                    e.printStackTrace();
                                }
                            } else {
                                // Another thread already created it, get the existing one
                                clipRenderer = clipIndex < trackRenderer.size() ? trackRenderer.get(clipIndex) : null;
                                if (clipRenderer != null) {
                                    Log.d("TimelineRenderer", "Using existing ClipRenderer for clip: " + clip.clipName + ", exoPlayer: " + (clipRenderer.exoPlayer != null ? "not null" : "null") + ", hashCode: " + clipRenderer.hashCode());
                                }
                            }
                        }
                        
                        if (clipRenderer != null) {
                            if (clipRenderer.isVisible(time)) {
                                if (clipRenderer.textureView != null)
                                clipRenderer.textureView.setVisibility(View.VISIBLE);
                                // Log before renderFrame to check exoPlayer state
                                if (clip.type == ClipType.VIDEO && clipRenderer.exoPlayer == null) {
                                    Log.w("TimelineRenderer", "exoPlayer is null before renderFrame for clip: " + clip.clipName + ", ClipRenderer hashCode: " + clipRenderer.hashCode());
                        }
                                clipRenderer.renderFrame(time, isSeekingOnly);
                            } else {
                                if (clipRenderer.textureView != null)
                                clipRenderer.textureView.setVisibility(View.GONE);
                            clipRenderer.isPlaying = false;
                        }
                    }
                    }
                    trackIndex++;
                }
            }
        }


        public void startPlayAt(float playheadTime) {
            boolean renderedAny = false;

            // Lazy create ClipRenderer when clip becomes visible
            int trackIndex = 0;
            for (List<ClipRenderer> trackRenderer : trackLayers) {
                if (trackIndex >= timeline.tracks.size()) break;
                Track track = timeline.tracks.get(trackIndex);
                int clipIndex = 0;
                for (ClipRenderer clipRenderer : trackRenderer) {
                    if (clipIndex >= track.clips.size()) break;
                    Clip clip = track.clips.get(clipIndex);
                    
                    // Check if clip is visible
                    boolean isVisible = playheadTime >= clip.startTime && playheadTime <= clip.startTime + clip.duration;
                    
                    if (clipRenderer == null && isVisible) {
                        // Lazy create ClipRenderer when clip becomes visible
                        try {
                            Log.d("TimelineRenderer", "Creating ClipRenderer in startPlayAt for clip: " + clip.clipName + ", type: " + clip.type);
                            clipRenderer = new ClipRenderer(context, clip, properties, settings, editingActivity, previewViewGroup, textCanvasControllerInfo);
                            trackRenderer.set(clipIndex, clipRenderer);
                            Log.d("TimelineRenderer", "ClipRenderer created in startPlayAt for clip: " + clip.clipName + ", exoPlayer: " + (clipRenderer.exoPlayer != null ? "not null" : "null"));
                        } catch (Exception e) {
                            Log.e("TimelineRenderer", "Error creating ClipRenderer in startPlayAt for clip: " + clip.clipName + ": " + e.getMessage(), e);
                            e.printStackTrace();
                        }
                    }
                    
                    if (clipRenderer != null && clipRenderer.isVisible(playheadTime)) {
                        // Log before renderFrame to check exoPlayer state
                        if (clip.type == ClipType.VIDEO && clipRenderer.exoPlayer == null) {
                            Log.w("TimelineRenderer", "exoPlayer is null before renderFrame in startPlayAt for clip: " + clip.clipName);
                        }
                        clipRenderer.renderFrame(playheadTime, false);
                        renderedAny = true;
                    }
                    clipIndex++;
                }
                trackIndex++;
            }

            if (!renderedAny) {
                renderSolidBlack();
            }
        }

        private void renderSolidBlack() {
//            GLES20.glClearColor(0f, 0f, 0f, 1f);
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }

        public void release() {
            for (List<ClipRenderer> track : trackLayers) {
                for (ClipRenderer cr : track) {
                    if (cr != null) {
                        cr.release();
                    }
                }
            }
        }
    }



}



