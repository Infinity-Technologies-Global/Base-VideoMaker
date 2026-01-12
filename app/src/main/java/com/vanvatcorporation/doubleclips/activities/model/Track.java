package com.vanvatcorporation.doubleclips.activities.model;

import android.view.ViewGroup;
import com.google.gson.annotations.Expose;
import com.vanvatcorporation.doubleclips.R;
import com.vanvatcorporation.doubleclips.activities.EditingActivity;
import com.vanvatcorporation.doubleclips.impl.TrackFrameLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Track implements Serializable {
    @Expose
    public int trackIndex;
    @Expose
    public List<Clip> clips = new ArrayList<>();
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

    public void sortClips() {
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
        if (viewRef != null) {
            viewRef.setBackgroundColor(0xFFAAAAAA);
        }
    }

    public void deselect() {
        if (viewRef != null) {
            viewRef.setBackgroundColor(0xFF222222);
        }
    }

    public void clearTransition() {
        for (Clip clip : clips) {
            clip.endTransition = null;
        }
    }

    public void disableTransitions() {
        for (Clip clip : clips) {
            clip.endTransitionEnabled = false;
        }
    }

    public void removeTransitionUi() {
        if (viewRef == null) return;
        for (int i = 0; i < viewRef.getChildCount(); i++) {
            ViewGroup targetView = (ViewGroup) viewRef.getChildAt(i);
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
        if (viewRef != null) {
            trackContainer.removeView(viewRef);
        }
        // Since the track #n is following the pattern, we just need to delete the last track # text and it does the job
        // -1 for the count to index, like count is 4 but index should be 3
        // -1 for the index for the button
        if (trackInfo.getChildCount() > 1) {
            trackInfo.removeView(trackInfo.getChildAt(trackInfo.getChildCount() - 2));
        }
        // Decrease the trackIndex from the global scope
        activity.trackCount--;
    }
}

