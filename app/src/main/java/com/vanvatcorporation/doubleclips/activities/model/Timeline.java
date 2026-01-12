package com.vanvatcorporation.doubleclips.activities.model;

import com.google.gson.annotations.Expose;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Timeline implements Serializable {
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
        return clips;
    }

    public Track getTrackFromClip(Clip selectedClip) {
        for (Track track : tracks) {
            if (track.clips.contains(selectedClip)) {
                return track;
            }
        }
        return null;
    }

    public int getAllClipCount() {
        int clipCount = 0;
        for (Track track : tracks) {
            clipCount += track.clips.size();
        }
        return clipCount;
    }
}

