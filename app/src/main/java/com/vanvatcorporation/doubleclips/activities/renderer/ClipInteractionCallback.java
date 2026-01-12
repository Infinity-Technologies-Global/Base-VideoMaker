package com.vanvatcorporation.doubleclips.activities.renderer;

import com.vanvatcorporation.doubleclips.activities.model.Clip;

/**
 * Callback interface for clip interaction events.
 * Allows ClipRenderer to interact with EditingActivity without direct dependency.
 */
public interface ClipInteractionCallback {
    /**
     * Called when a clip is selected by user interaction.
     * @param clip The clip that was selected
     */
    void onClipSelected(Clip clip);

    /**
     * Get the current playback speed multiplier.
     * @return Playback speed (1.0 = normal speed, 2.0 = 2x speed, etc.)
     */
    float getPlaybackSpeed();

    /**
     * Check if a clip is currently selected.
     * @param clip The clip to check
     * @return true if the clip is currently selected
     */
    boolean isClipSelected(Clip clip);

    /**
     * Get the display density for converting dp to px.
     * @return Display density (e.g., 2.0 for mdpi, 3.0 for xhdpi)
     */
    float getDisplayDensity();
}

