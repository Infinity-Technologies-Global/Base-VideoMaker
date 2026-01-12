package com.vanvatcorporation.doubleclips.activities.renderer;

import android.util.Log;

/**
 * Helper class for converting between preview coordinates and render coordinates.
 * Preview coordinates are in pixels (screen space).
 * Render coordinates are in the video's resolution space.
 */
public class PreviewConversionHelper {
    // These are set by EditingActivity when preview size changes
    public static float previewAvailableWidth = 0;
    public static float previewAvailableHeight = 0;

    /**
     * Convert preview position to render position.
     * Uses the same uniform ratio as scale conversion to maintain consistency.
     */
    public static float previewToRenderConversionX(float previewX, float renderResolutionX, float renderResolutionY) {
        float ratio = getUniformRatio(renderResolutionX, renderResolutionY);
        float result = ratio == 0 ? previewX : previewX / ratio;
        Log.d("Conversion", "previewToRenderConversionX: previewX=" + previewX 
                + ", ratio=" + ratio + ", result=" + result);
        return result;
    }

    public static float previewToRenderConversionY(float previewY, float renderResolutionX, float renderResolutionY) {
        float ratio = getUniformRatio(renderResolutionX, renderResolutionY);
        float result = ratio == 0 ? previewY : previewY / ratio;
        Log.d("Conversion", "previewToRenderConversionY: previewY=" + previewY 
                + ", ratio=" + ratio + ", result=" + result);
        return result;
    }

    /**
     * Convert render position to preview position.
     * Uses the same uniform ratio as scale conversion to maintain consistency.
     */
    public static float renderToPreviewConversionX(float renderX, float renderResolutionX, float renderResolutionY) {
        float ratio = getUniformRatio(renderResolutionX, renderResolutionY);
        float result = renderX * ratio;
        Log.d("Conversion", "renderToPreviewConversionX: renderX=" + renderX 
                + ", ratio=" + ratio + ", result=" + result);
        return result;
    }

    public static float renderToPreviewConversionY(float renderY, float renderResolutionX, float renderResolutionY) {
        float ratio = getUniformRatio(renderResolutionX, renderResolutionY);
        float result = renderY * ratio;
        Log.d("Conversion", "renderToPreviewConversionY: renderY=" + renderY 
                + ", ratio=" + ratio + ", result=" + result);
        return result;
    }

    // TODO: Using the same ratio system like below because multiplication and division is in the same order, no plus and subtract
    //  the matrix of the preview clip are not using the previewAvailable ratio system yet, so 1366 width
    //  in the 1080px screen the movement will be jittered

    /**
     * Convert preview scale to render scale.
     * IMPORTANT: Both X and Y must use the SAME ratio to maintain aspect ratio!
     */
    public static float previewToRenderConversionScalingX(float clipScaleX, float renderResolutionX, float renderResolutionY) {
        float ratio = getUniformRatio(renderResolutionX, renderResolutionY);
        Log.d("Conversion", "previewToRenderConversionScalingX: clipScaleX=" + clipScaleX 
                + ", renderRes=" + renderResolutionX + "x" + renderResolutionY 
                + ", previewAvailable=" + previewAvailableWidth + "x" + previewAvailableHeight
                + ", ratio=" + ratio + ", result=" + (ratio == 0 ? clipScaleX : clipScaleX / ratio));
        return ratio == 0 ? clipScaleX : clipScaleX / ratio;
    }

    public static float previewToRenderConversionScalingY(float clipScaleY, float renderResolutionX, float renderResolutionY) {
        float ratio = getUniformRatio(renderResolutionX, renderResolutionY);
        Log.d("Conversion", "previewToRenderConversionScalingY: clipScaleY=" + clipScaleY 
                + ", renderRes=" + renderResolutionX + "x" + renderResolutionY 
                + ", previewAvailable=" + previewAvailableWidth + "x" + previewAvailableHeight
                + ", ratio=" + ratio + ", result=" + (ratio == 0 ? clipScaleY : clipScaleY / ratio));
        return ratio == 0 ? clipScaleY : clipScaleY / ratio;
    }

    /**
     * Get uniform ratio for both X and Y to maintain aspect ratio.
     * Uses the same calculation for both dimensions.
     */
    public static float getUniformRatio(float renderResolutionX, float renderResolutionY) {
        float ratioX = getRenderRatio(previewAvailableWidth, renderResolutionX);
        float ratioY = getRenderRatio(previewAvailableHeight, renderResolutionY);
        return Math.min(ratioX, ratioY);
    }

    public static float renderToPreviewConversionScalingX(float clipScaleX, float renderResolutionX, float renderResolutionY) {
        float ratioX = getRenderRatio(previewAvailableWidth, renderResolutionX);
        float ratioY = getRenderRatio(previewAvailableHeight, renderResolutionY);
        float ratio = Math.min(ratioX, ratioY);
        float result = clipScaleX * ratio;
        Log.d("Conversion", "renderToPreviewConversionScalingX: clipScaleX=" + clipScaleX + ", renderResolutionX=" + renderResolutionX + ", renderResolutionY=" + renderResolutionY + ", previewAvailableWidth=" + previewAvailableWidth + ", previewAvailableHeight=" + previewAvailableHeight + ", ratioX=" + ratioX + ", ratioY=" + ratioY + ", minRatio=" + ratio + ", result=" + result);
        return result;
    }

    public static float renderToPreviewConversionScalingY(float clipScaleY, float renderResolutionX, float renderResolutionY) {
        float ratioX = getRenderRatio(previewAvailableWidth, renderResolutionX);
        float ratioY = getRenderRatio(previewAvailableHeight, renderResolutionY);
        float ratio = Math.min(ratioX, ratioY);
        float result = clipScaleY * ratio;
        Log.d("Conversion", "renderToPreviewConversionScalingY: clipScaleY=" + clipScaleY + ", renderResolutionX=" + renderResolutionX + ", renderResolutionY=" + renderResolutionY + ", previewAvailableWidth=" + previewAvailableWidth + ", previewAvailableHeight=" + previewAvailableHeight + ", ratioX=" + ratioX + ", ratioY=" + ratioY + ", minRatio=" + ratio + ", result=" + result);
        return result;
    }

    public static float getRenderRatio(float previewAvailable, float renderResolution) {
        float ratio = Math.min(previewAvailable, renderResolution) / renderResolution;
        Log.d("Conversion", "getRenderRatio: previewAvailable=" + previewAvailable + ", renderResolution=" + renderResolution + ", ratio=" + ratio);
        return ratio;
    }
}

