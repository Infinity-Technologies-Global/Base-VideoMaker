package com.vanvatcorporation.doubleclips.activities.command;

import com.vanvatcorporation.doubleclips.activities.EditingActivity;
import com.vanvatcorporation.doubleclips.activities.model.Clip;
import com.vanvatcorporation.doubleclips.activities.model.Timeline;
import com.vanvatcorporation.doubleclips.activities.model.Track;

public class AddClipCommand implements Command {
    private final Clip clip;
    private final Timeline timeline;
    private final int trackIndex;

    public AddClipCommand(Clip clip, Timeline timeline, int trackIndex) {
        this.clip = clip;
        this.timeline = timeline;
        this.trackIndex = trackIndex;
    }

    @Override
    public void execute(EditingActivity activity) {
        Track track = timeline.tracks.get(trackIndex);
        activity.addClipToTrack(track, clip);
        activity.updateCurrentClipEnd();
    }

    @Override
    public void undo(EditingActivity activity) {
        Track track = timeline.tracks.get(trackIndex);
        track.removeClip(clip);
        if (track.viewRef != null && clip.viewRef != null) {
            track.viewRef.removeView(clip.viewRef);
        }
        activity.updateClipLayouts();
        activity.updateCurrentClipEnd();
    }

    @Override
    public String getDescription() {
        return "Add clip: " + clip.clipName;
    }
}

