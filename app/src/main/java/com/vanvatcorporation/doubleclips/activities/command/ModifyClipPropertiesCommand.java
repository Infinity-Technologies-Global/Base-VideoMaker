package com.vanvatcorporation.doubleclips.activities.command;

import com.vanvatcorporation.doubleclips.activities.EditingActivity;
import com.vanvatcorporation.doubleclips.activities.model.Clip;
import com.vanvatcorporation.doubleclips.activities.model.VideoProperties;

public class ModifyClipPropertiesCommand implements Command {
    private final Clip clip;
    private final VideoProperties oldProperties;
    private final VideoProperties newProperties;
    private final String oldClipName;
    private final String newClipName;
    private final float oldDuration;
    private final float newDuration;
    private final boolean oldIsMute;
    private final boolean newIsMute;

    public ModifyClipPropertiesCommand(Clip clip, VideoProperties newProperties, String newClipName, float newDuration, boolean newIsMute) {
        this.clip = clip;
        this.oldProperties = new VideoProperties(clip.videoProperties);
        this.newProperties = newProperties;
        this.oldClipName = clip.clipName;
        this.newClipName = newClipName;
        this.oldDuration = clip.duration;
        this.newDuration = newDuration;
        this.oldIsMute = clip.isMute;
        this.newIsMute = newIsMute;
    }

    @Override
    public void execute(EditingActivity activity) {
        clip.videoProperties = new VideoProperties(newProperties);
        clip.clipName = newClipName;
        clip.duration = newDuration;
        clip.isMute = newIsMute;
        activity.updateClipLayouts();
        activity.updateCurrentClipEnd();
        activity.regeneratingTimelineRenderer();
    }

    @Override
    public void undo(EditingActivity activity) {
        clip.videoProperties = new VideoProperties(oldProperties);
        clip.clipName = oldClipName;
        clip.duration = oldDuration;
        clip.isMute = oldIsMute;
        activity.updateClipLayouts();
        activity.updateCurrentClipEnd();
        activity.regeneratingTimelineRenderer();
    }

    @Override
    public String getDescription() {
        return "Modify clip: " + clip.clipName;
    }
}

