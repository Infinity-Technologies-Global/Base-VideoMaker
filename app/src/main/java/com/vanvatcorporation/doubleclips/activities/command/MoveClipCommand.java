package com.vanvatcorporation.doubleclips.activities.command;

import android.view.ViewGroup;

import com.vanvatcorporation.doubleclips.activities.EditingActivity;
import com.vanvatcorporation.doubleclips.activities.model.Clip;
import com.vanvatcorporation.doubleclips.activities.model.Timeline;
import com.vanvatcorporation.doubleclips.activities.model.Track;

public class MoveClipCommand implements Command {
    private final Clip clip;
    private final Timeline timeline;
    private final int oldTrackIndex;
    private final int newTrackIndex;
    private final float oldStartTime;
    private final float newStartTime;

    public MoveClipCommand(Clip clip, Timeline timeline, int oldTrackIndex, int newTrackIndex, float oldStartTime, float newStartTime) {
        this.clip = clip;
        this.timeline = timeline;
        this.oldTrackIndex = oldTrackIndex;
        this.newTrackIndex = newTrackIndex;
        this.oldStartTime = oldStartTime;
        this.newStartTime = newStartTime;
    }

    @Override
    public void execute(EditingActivity activity) {
        Track oldTrack = timeline.tracks.get(oldTrackIndex);
        Track newTrack = timeline.tracks.get(newTrackIndex);
        if (oldTrack.clips.contains(clip)) {
            oldTrack.removeClip(clip);
        }
        clip.trackIndex = newTrackIndex;
        clip.startTime = newStartTime;
        if (!newTrack.clips.contains(clip)) {
            newTrack.addClip(clip);
        }
        if (clip.viewRef != null) {
            clip.viewRef.setX(activity.getTimeInX(newStartTime));
            ViewGroup parent = (ViewGroup) clip.viewRef.getParent();
            if (parent != null && parent != newTrack.viewRef) {
                parent.removeView(clip.viewRef);
            }
            if (newTrack.viewRef != null && clip.viewRef.getParent() != newTrack.viewRef) {
                newTrack.viewRef.addView(clip.viewRef);
            }
        }
        newTrack.sortClips();
        activity.updateClipLayouts();
        activity.updateCurrentClipEnd();
    }

    @Override
    public void undo(EditingActivity activity) {
        Track newTrack = timeline.tracks.get(newTrackIndex);
        Track oldTrack = timeline.tracks.get(oldTrackIndex);
        newTrack.removeClip(clip);
        clip.trackIndex = oldTrackIndex;
        clip.startTime = oldStartTime;
        oldTrack.addClip(clip);
        if (clip.viewRef != null) {
            clip.viewRef.setX(activity.getTimeInX(oldStartTime));
            ViewGroup parent = (ViewGroup) clip.viewRef.getParent();
            if (parent != null && parent != oldTrack.viewRef) {
                parent.removeView(clip.viewRef);
            }
            if (oldTrack.viewRef != null && clip.viewRef.getParent() != oldTrack.viewRef) {
                oldTrack.viewRef.addView(clip.viewRef);
            }
        }
        oldTrack.sortClips();
        activity.updateClipLayouts();
        activity.updateCurrentClipEnd();
    }

    @Override
    public String getDescription() {
        return "Move clip: " + clip.clipName;
    }
}

