package com.vanvatcorporation.doubleclips.activities.command;

import com.vanvatcorporation.doubleclips.activities.EditingActivity;
import com.vanvatcorporation.doubleclips.activities.model.Clip;
import com.vanvatcorporation.doubleclips.activities.model.ClipHelper;
import com.vanvatcorporation.doubleclips.activities.model.Timeline;
import com.vanvatcorporation.doubleclips.activities.model.Track;

public class DeleteClipCommand implements Command {
    private final Clip clip;
    private final Timeline timeline;
    private final int trackIndex;
    private final int clipIndexInTrack;

    public DeleteClipCommand(Clip clip, Timeline timeline) {
        this.clip = clip;
        this.timeline = timeline;
        Track track = timeline.tracks.get(clip.trackIndex);
        this.trackIndex = clip.trackIndex;
        this.clipIndexInTrack = track.clips.indexOf(clip);
    }

    @Override
    public void execute(EditingActivity activity) {
        ClipHelper.deleteClip(clip, timeline, activity);
        activity.updateCurrentClipEnd();
    }

    @Override
    public void undo(EditingActivity activity) {
        Track track = timeline.tracks.get(trackIndex);
        track.clips.add(clipIndexInTrack, clip);
        activity.addClipToTrackUi(track.viewRef, clip);
        activity.updateClipLayouts();
        activity.updateCurrentClipEnd();
    }

    @Override
    public String getDescription() {
        return "Delete clip: " + clip.clipName;
    }
}

