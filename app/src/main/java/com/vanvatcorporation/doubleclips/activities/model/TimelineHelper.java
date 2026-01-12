package com.vanvatcorporation.doubleclips.activities.model;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vanvatcorporation.doubleclips.activities.EditingActivity;
import com.vanvatcorporation.doubleclips.activities.main.MainAreaScreen;
import com.vanvatcorporation.doubleclips.constants.Constants;
import com.vanvatcorporation.doubleclips.helper.IOHelper;
import com.vanvatcorporation.doubleclips.helper.IOImageHelper;
import java.util.Date;

public class TimelineHelper {
    public static void saveTimeline(Context context, Timeline timeline, MainAreaScreen.ProjectData data, VideoSettings settings) {
        float max = 0;
        for (Track trackCpn : timeline.tracks) {
            float endTime = trackCpn.getTrackEndTime();
            if (endTime > max) max = endTime;
            trackCpn.sortClips();
        }
        timeline.duration = max;

        Clip nearestClip = null;
        for (Track trackCpn : timeline.tracks) {
            for (Clip c : trackCpn.clips) {
                nearestClip = c;
                break;
            }
            if (nearestClip != null) break;
        }
        if (nearestClip != null) {
            java.util.List<android.graphics.Bitmap> thumbnails = EditingActivity.extractThumbnail(context, nearestClip.getAbsolutePreviewPath(data), nearestClip, 1);
            if (thumbnails != null && !thumbnails.isEmpty()) {
                IOImageHelper.SaveFileAsPNGImage(context, IOHelper.CombinePath(data.getProjectPath(), "preview.png"), thumbnails.get(0), 25);
            }
        }

        data.setProjectTimestamp(new Date().getTime());
        data.setProjectDuration((long) (timeline.duration * 1000));
        data.setProjectSize(IOHelper.getFileSize(context, data.getProjectPath()));

        String jsonTimeline = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(timeline);

        data.savePropertiesAtProject(context);
        settings.saveSettings(context, data);
        IOHelper.writeToFile(context, IOHelper.CombinePath(data.getProjectPath(), Constants.DEFAULT_TIMELINE_FILENAME), jsonTimeline);
    }

    public static Timeline loadRawTimeline(Context context, MainAreaScreen.ProjectData data) {
        String json = IOHelper.readFromFile(context, IOHelper.CombinePath(data.getProjectPath(), Constants.DEFAULT_TIMELINE_FILENAME));
        return new Gson().fromJson(json, Timeline.class);
    }

    public static Timeline loadTimeline(Context context, EditingActivity instance, MainAreaScreen.ProjectData data) {
        return loadTimeline(context, instance, loadRawTimeline(context, data));
    }

    public static Timeline loadTimeline(Context context, EditingActivity instance, Timeline timeline) {
        if (timeline == null) return new Timeline();
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
}

