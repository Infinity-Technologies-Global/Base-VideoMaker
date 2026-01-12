package com.vanvatcorporation.doubleclips.activities.command;

import com.vanvatcorporation.doubleclips.activities.EditingActivity;
import com.vanvatcorporation.doubleclips.activities.model.Clip;
import com.vanvatcorporation.doubleclips.activities.model.ClipHelper;
import com.vanvatcorporation.doubleclips.activities.model.Timeline;
import com.vanvatcorporation.doubleclips.activities.model.Track;

public class SplitClipCommand implements Command {
    private final Clip originalClip;
    private final Timeline timeline;
    private final float splitTime;
    private Clip secondaryClip;
    private float originalEndClipTrim;
    private float originalStartClipTrim;
    private float originalDuration;

    public SplitClipCommand(Clip clip, Timeline timeline, float currentGlobalTime) {
        this.originalClip = clip;
        this.timeline = timeline;
        this.splitTime = currentGlobalTime;
        this.originalEndClipTrim = clip.endClipTrim;
        this.originalStartClipTrim = clip.startClipTrim;
        this.originalDuration = clip.duration;
    }

    @Override
    public void execute(EditingActivity activity) {
        ClipHelper.splitClip(originalClip, activity, timeline, splitTime);
        Track track = timeline.tracks.get(originalClip.trackIndex);
        secondaryClip = track.clips.get(track.clips.size() - 1);
    }

    @Override
    public void undo(EditingActivity activity) {
        if (secondaryClip != null) {
            ClipHelper.deleteClip(secondaryClip, timeline, activity);
        }
        originalClip.endClipTrim = originalEndClipTrim;
        originalClip.startClipTrim = originalStartClipTrim;
        originalClip.duration = originalDuration;
        activity.revalidationClipView(originalClip);
        activity.updateClipLayouts();
        activity.updateCurrentClipEnd();
    }

    @Override
    public String getDescription() {
        return "Split clip: " + originalClip.clipName;
    }
}

