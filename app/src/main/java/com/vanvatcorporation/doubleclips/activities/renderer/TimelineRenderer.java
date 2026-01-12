package com.vanvatcorporation.doubleclips.activities.renderer;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vanvatcorporation.doubleclips.activities.model.Clip;
import com.vanvatcorporation.doubleclips.activities.model.ClipType;
import com.vanvatcorporation.doubleclips.activities.model.Timeline;
import com.vanvatcorporation.doubleclips.activities.model.Track;
import com.vanvatcorporation.doubleclips.activities.model.VideoSettings;
import com.vanvatcorporation.doubleclips.activities.main.MainAreaScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Renders the timeline by managing ClipRenderer instances for all clips.
 * Handles lazy loading, migration, and synchronization of clip renderers.
 */
public class TimelineRenderer {
    private final Context context;
    public List<List<ClipRenderer>> trackLayers = new ArrayList<>();
    private final Object lock = new Object(); // Lock for synchronization

    private Timeline timeline;
    private MainAreaScreen.ProjectData properties;
    private VideoSettings settings;
    private ClipInteractionCallback callback;
    private FrameLayout previewViewGroup;
    private TextView textCanvasControllerInfo;
    private Set<ClipRenderer> lastMigratedRenderers = new HashSet<>();

    public TimelineRenderer(Context context) {
        this.context = context;
    }

    public void buildTimeline(Timeline timeline, MainAreaScreen.ProjectData properties, VideoSettings settings, ClipInteractionCallback callback, FrameLayout previewViewGroup, TextView textCanvasControllerInfo) {
        // Build new timeline structure and migrate existing ClipRenderers if clips still exist
        synchronized (lock) {
            Log.d("TimelineRenderer", "buildTimeline called, current trackLayers size: " + trackLayers.size());

            // Store old trackLayers reference before creating new one
            List<List<ClipRenderer>> oldTrackLayers = trackLayers;

            // Create a map of existing ClipRenderers by clip name for quick lookup
            Map<String, ClipRenderer> existingRenderersByName = new HashMap<>();
            Set<ClipRenderer> allExistingRenderers = new HashSet<>();

            for (List<ClipRenderer> trackRenderer : oldTrackLayers) {
                for (ClipRenderer clipRenderer : trackRenderer) {
                    if (clipRenderer != null && clipRenderer.clip != null) {
                        existingRenderersByName.put(clipRenderer.clip.clipName, clipRenderer);
                        allExistingRenderers.add(clipRenderer);
                    }
                }
            }
            Log.d("TimelineRenderer", "Found " + allExistingRenderers.size() + " existing ClipRenderers to potentially migrate");

            // Create new trackLayers list
            List<List<ClipRenderer>> newTrackLayers = new ArrayList<>();
            Set<ClipRenderer> migratedRenderers = new HashSet<>();

            // Build new timeline structure and migrate existing ClipRenderers
            for (Track track : timeline.tracks) {
                List<ClipRenderer> renderers = new ArrayList<>();
                for (Clip clip : track.clips) {
                    switch (clip.type) {
                        case VIDEO:
                        case AUDIO:
                        case IMAGE:
                            // Check if we can reuse an existing ClipRenderer for this clip
                            ClipRenderer existingRenderer = existingRenderersByName.get(clip.clipName);
                            if (existingRenderer != null && existingRenderer.clip.clipName.equals(clip.clipName)) {
                                // Migrate ClipRenderer even if exoPlayer is null - exoPlayer will be created/recreated when needed
                                renderers.add(existingRenderer);
                                migratedRenderers.add(existingRenderer);

                                Log.d("TimelineRenderer", "Migrating existing ClipRenderer for clip: " + clip.clipName
                                        + " | exoPlayer: " + (existingRenderer.exoPlayer != null ? "not null" : "null (will be created when needed)")
                                        + " | scaleX=" + existingRenderer.scaleX + " | scaleY=" + existingRenderer.scaleY
                                        + " | clip.videoProperties.ScaleX=" + clip.videoProperties.getValue(com.vanvatcorporation.doubleclips.activities.model.VideoProperties.ValueType.ScaleX)
                                        + " | clip.videoProperties.ScaleY=" + clip.videoProperties.getValue(com.vanvatcorporation.doubleclips.activities.model.VideoProperties.ValueType.ScaleY));
                            } else {
                                // New clip or clip changed - create null placeholder
                                renderers.add(null);
                            }
                            break;
                    }
                }
                newTrackLayers.add(renderers);
            }

            // Release ClipRenderers that are no longer in the new timeline
            int releasedCount = 0;
            for (ClipRenderer clipRenderer : allExistingRenderers) {
                if (!migratedRenderers.contains(clipRenderer)) {
                    releasedCount++;
                    Log.d("TimelineRenderer", "Releasing ClipRenderer for clip that no longer exists: " + (clipRenderer.clip != null ? clipRenderer.clip.clipName : "unknown"));
                    clipRenderer.release();
                }
            }
            Log.d("TimelineRenderer", "Released " + releasedCount + " ClipRenderers, migrated " + migratedRenderers.size() + " ClipRenderers");

            // Store migrated renderers for re-adding their views later
            this.lastMigratedRenderers = new HashSet<>(migratedRenderers);

            // Only update trackLayers after creating new structure to avoid race condition
            trackLayers = newTrackLayers;

            // Store references for lazy creation
            this.timeline = timeline;
            this.properties = properties;
            this.settings = settings;
            this.callback = callback;
            this.previewViewGroup = previewViewGroup;
            this.textCanvasControllerInfo = textCanvasControllerInfo;
        }
    }

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
                    // Add to previewViewGroup with FrameLayout.LayoutParams
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

                    Log.d("TimelineRenderer", "Re-add clip: " + cr.clip.clipName
                            + " | keeping current previewPos=(" + cr.posX + "," + cr.posY + ")"
                            + " | keeping current previewScale=(" + cr.scaleX + "," + cr.scaleY + ")"
                            + " | rot=" + cr.rot);

                    // Re-apply transformations
                    cr.applyPostTransformation();
                }
            }
        }
    }

    public void updateTime(float time, boolean isSeekingOnly) {
        // Lazy create ClipRenderer when clip becomes visible
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
                        if (clipIndex < trackRenderer.size() && trackRenderer.get(clipIndex) == null) {
                            try {
                                Log.d("TimelineRenderer", "Creating ClipRenderer in updateTime for clip: " + clip.clipName
                                        + " | type: " + clip.type
                                        + " | clip.videoProperties.ScaleX=" + clip.videoProperties.getValue(com.vanvatcorporation.doubleclips.activities.model.VideoProperties.ValueType.ScaleX)
                                        + " | clip.videoProperties.ScaleY=" + clip.videoProperties.getValue(com.vanvatcorporation.doubleclips.activities.model.VideoProperties.ValueType.ScaleY));
                                clipRenderer = new ClipRenderer(context, clip, properties, settings, callback, previewViewGroup, textCanvasControllerInfo);
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
                        clipRenderer = new ClipRenderer(context, clip, properties, settings, callback, previewViewGroup, textCanvasControllerInfo);
                        trackRenderer.set(clipIndex, clipRenderer);
                        Log.d("TimelineRenderer", "ClipRenderer created in startPlayAt for clip: " + clip.clipName + ", exoPlayer: " + (clipRenderer.exoPlayer != null ? "not null" : "null"));
                    } catch (Exception e) {
                        Log.e("TimelineRenderer", "Error creating ClipRenderer in startPlayAt for clip: " + clip.clipName + ": " + e.getMessage(), e);
                        e.printStackTrace();
                    }
                }

                if (clipRenderer != null && clipRenderer.isVisible(playheadTime)) {
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
        // Placeholder for solid black rendering
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

