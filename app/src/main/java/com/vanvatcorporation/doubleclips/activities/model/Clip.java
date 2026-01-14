package com.vanvatcorporation.doubleclips.activities.model;

import android.view.View;
import com.google.gson.annotations.Expose;
import com.vanvatcorporation.doubleclips.activities.main.MainAreaScreen;
import com.vanvatcorporation.doubleclips.constants.Constants;
import com.vanvatcorporation.doubleclips.helper.IOHelper;
import com.vanvatcorporation.doubleclips.impl.ImageGroupView;
import java.io.Serializable;
import java.io.File;
import java.util.ArrayList;

public class Clip implements Serializable {
    @Expose
    public ClipType type;
    @Expose
    public String clipName;
    @Expose
    public float startTime;
    @Expose
    public float duration;
    @Expose
    public float startClipTrim;
    @Expose
    public float endClipTrim;
    @Expose
    public float originalDuration;
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
    @Expose
    public EffectTemplate effect;
    @Expose
    public String textContent;
    @Expose
    public float fontSize;
    @Expose
    public TransitionClip endTransition = null;
    @Expose
    public boolean endTransitionEnabled = false;
    @Expose
    public boolean isVideoHasAudio;
    @Expose
    public boolean isMute;
    @Expose
    public boolean isLockedForTemplate;
    public transient View leftHandle, rightHandle;
    public transient ImageGroupView viewRef;
    public transient android.widget.ImageView templateLockViewRef;

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
        if (clip.type == ClipType.TEXT) {
            this.textContent = clip.textContent;
            this.fontSize = clip.fontSize;
        }
        if (clip.type == ClipType.EFFECT) {
            this.effect = clip.effect;
        }
    }

    public void filterNullAfterLoad() {
        if (type == null) type = ClipType.VIDEO;
        if (videoProperties == null) videoProperties = new VideoProperties();
        if (keyframes == null) keyframes = new AnimatedProperty();
        if (keyframes.keyframes == null) keyframes.keyframes = new ArrayList<>();
    }

    public void restate() {
        videoProperties = new VideoProperties(0, 0, 0, 1, 1, 1, 1);
    }

    public void mergingVideoPropertiesFromSingleKeyframe() {
        if (hasOnlyOneAnimatedProperties()) {
            VideoProperties videoProperties = keyframes.keyframes.get(0).value;
            applyPropertiesToClip(videoProperties);
        }
    }

    public void applyPropertiesToClip(VideoProperties properties) {
        this.videoProperties = new VideoProperties(properties);
    }

    public boolean isClipTransitionAvailable() {
        return endTransitionEnabled && endTransition != null;
    }

    public boolean hasAnimatedProperties() {
        return keyframes.keyframes.size() > 1;
    }

    public boolean hasOnlyOneAnimatedProperties() {
        return keyframes.keyframes.size() == 1;
    }

    public String getAbsolutePath(MainAreaScreen.ProjectData properties) {
        return getAbsolutePath(properties.getProjectPath());
    }

    public String getAbsolutePath(String projectPath) {
        return IOHelper.CombinePath(projectPath, Constants.DEFAULT_CLIP_DIRECTORY, getClipName());
    }

    public String getAbsolutePreviewPath(MainAreaScreen.ProjectData properties) {
        return getAbsolutePreviewPath(properties.getProjectPath());
    }

    public String getAbsolutePreviewPath(String projectPath) {
        String path = IOHelper.CombinePath(projectPath, Constants.DEFAULT_PREVIEW_CLIP_DIRECTORY, getClipName());
        if (!IOHelper.isFileExist(path)) {
            path = getAbsolutePath(projectPath);
        }
        return path;
    }

    public String getAbsolutePreviewPath(MainAreaScreen.ProjectData properties, String previewExtension) {
        return getAbsolutePreviewPath(properties.getProjectPath(), previewExtension);
    }

    public String getAbsolutePreviewPath(String projectPath, String previewExtension) {
        String path = IOHelper.CombinePath(projectPath, Constants.DEFAULT_PREVIEW_CLIP_DIRECTORY, getClipName().substring(0, getClipName().lastIndexOf('.')) + previewExtension);
        if (!IOHelper.isFileExist(path)) {
            path = getAbsolutePath(projectPath);
        }
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

    public boolean getIsLockedForTemplate() {
        return isLockedForTemplate;
    }

    public void setIsLockedForTemplate(boolean value) {
        isLockedForTemplate = value;
        if (templateLockViewRef != null) {
            templateLockViewRef.setVisibility(value ? View.VISIBLE : View.GONE);
        }
    }

    public String getClipName() {
        return clipName;
    }

    public void setClipName(String clipName, MainAreaScreen.ProjectData data) {
        File file = new File(getAbsolutePath(data));
        if (file.renameTo(new File(getAbsolutePath(data).replace(this.clipName, clipName)))) {
            this.clipName = clipName;
        }
    }
}



