package com.vanvatcorporation.doubleclips.activities.model;


import static com.vanvatcorporation.doubleclips.activities.EditingActivity.MIN_CLIP_DURATION;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.vanvatcorporation.doubleclips.R;
import com.vanvatcorporation.doubleclips.activities.EditingActivity;
import com.vanvatcorporation.doubleclips.impl.ImageGroupView;

public class ClipHelper {
    public static void registerClipHandle(Clip clip, ImageGroupView clipView, EditingActivity activity, HorizontalScrollView timelineScroll) {
        clip.viewRef = clipView;

        clip.templateLockViewRef = new ImageView(activity);
        ImageGroupView.LayoutParams templateLockLayoutParams = new ImageGroupView.LayoutParams(30, 30);
        templateLockLayoutParams.setMargins(5, 5, 0, 0);
        clip.templateLockViewRef.setLayoutParams(templateLockLayoutParams);
        clip.templateLockViewRef.setImageResource(R.drawable.baseline_lock_24);
        clipView.addView(clip.templateLockViewRef);
        clip.templateLockViewRef.setVisibility(clip.isLockedForTemplate ? View.VISIBLE : View.GONE);

        clip.leftHandle = new View(clipView.getContext());
        clip.leftHandle.setBackgroundColor(Color.WHITE);
        RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(35, ViewGroup.LayoutParams.MATCH_PARENT);
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        clip.leftHandle.setLayoutParams(leftParams);

        clip.rightHandle = new View(clipView.getContext());
        clip.rightHandle.setBackgroundColor(Color.WHITE);
        RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(35, ViewGroup.LayoutParams.MATCH_PARENT);
        rightParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        clip.rightHandle.setLayoutParams(rightParams);

        clip.leftHandle.setOnTouchListener(new View.OnTouchListener() {
            float dX;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Clip clip = (Clip) clipView.getTag();
                int minWidth = (int) (MIN_CLIP_DURATION * EditingActivity.pixelsPerSecond);
                int maxWidth = (int) (clip.originalDuration * EditingActivity.pixelsPerSecond);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = event.getRawX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        timelineScroll.requestDisallowInterceptTouchEvent(true);
                        float deltaX = event.getRawX() - dX;
                        dX = event.getRawX();
                        if (clip.type == ClipType.VIDEO || clip.type == ClipType.AUDIO) {
                            deltaX = (Math.min(-deltaX, clip.startClipTrim * EditingActivity.pixelsPerSecond));
                            deltaX = -deltaX;
                            int newWidth = clipView.getWidth() - (int) deltaX;
                            if (newWidth < minWidth) return true;
                            newWidth = Math.max(minWidth, Math.min(newWidth, maxWidth));
                            clipView.getLayoutParams().width = newWidth;
                            clipView.setX(clipView.getX() + deltaX);
                            clipView.requestLayout();
                            clip.startTime = (clipView.getX() - EditingActivity.centerOffset) / EditingActivity.pixelsPerSecond;
                            clip.startClipTrim += (deltaX) / EditingActivity.pixelsPerSecond;
                            clip.duration = clip.originalDuration - clip.endClipTrim - clip.startClipTrim;
                        } else {
                            int newWidth = clipView.getWidth() - (int) deltaX;
                            clipView.getLayoutParams().width = newWidth;
                            clipView.setX(clipView.getX() + deltaX);
                            clipView.requestLayout();
                            clip.startTime = (clipView.getX() - EditingActivity.centerOffset) / EditingActivity.pixelsPerSecond;
                            clip.startClipTrim += (deltaX) / EditingActivity.pixelsPerSecond;
                            clip.duration = clip.originalDuration - clip.endClipTrim - clip.startClipTrim;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        timelineScroll.requestDisallowInterceptTouchEvent(false);
                        if (clip.startTime < 0) {
                            clipView.setX(activity.getTimeInX(0));
                            clip.startTime = 0;
                        }
                        activity.updateCurrentClipEnd();
                        break;
                }
                return true;
            }
        });

        clip.rightHandle.setOnTouchListener(new View.OnTouchListener() {
            float dX;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Clip clip = (Clip) clipView.getTag();
                int minWidth = (int) (MIN_CLIP_DURATION * EditingActivity.pixelsPerSecond);
                int maxWidth = (int) (clip.originalDuration * EditingActivity.pixelsPerSecond);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = event.getRawX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        timelineScroll.requestDisallowInterceptTouchEvent(true);
                        float deltaX = event.getRawX() - dX;
                        dX = event.getRawX();
                        if (clip.type == ClipType.VIDEO || clip.type == ClipType.AUDIO) {
                            deltaX = Math.min(deltaX, clip.endClipTrim * EditingActivity.pixelsPerSecond);
                            int newWidth = clipView.getWidth() + (int) deltaX;
                            if (newWidth < minWidth) return true;
                            newWidth = Math.max(minWidth, Math.min(newWidth, maxWidth));
                            clipView.getLayoutParams().width = newWidth;
                            clipView.requestLayout();
                            clip.endClipTrim -= (deltaX) / EditingActivity.pixelsPerSecond;
                            clip.duration = clip.originalDuration - clip.endClipTrim - clip.startClipTrim;
                        } else {
                            int newWidth = clipView.getWidth() + (int) deltaX;
                            clipView.getLayoutParams().width = newWidth;
                            clipView.requestLayout();
                            clip.endClipTrim -= (deltaX) / EditingActivity.pixelsPerSecond;
                            clip.duration = clip.originalDuration - clip.endClipTrim - clip.startClipTrim;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        timelineScroll.requestDisallowInterceptTouchEvent(false);
                        activity.updateCurrentClipEnd();
                        break;
                }
                return true;
            }
        });

        clipView.addView(clip.leftHandle);
        clipView.addView(clip.rightHandle);
        deselect(clip);
    }

    public static void select(Clip clip) {
        if (clip.viewRef != null) {
            clip.viewRef.getFilledImageView().setColorFilter(0x77AAAAAA);
            if (clip.leftHandle != null) clip.leftHandle.setVisibility(View.VISIBLE);
            if (clip.rightHandle != null) clip.rightHandle.setVisibility(View.VISIBLE);
        }
    }

    public static void deselect(Clip clip) {
        if (clip.viewRef != null) {
            clip.viewRef.getFilledImageView().setColorFilter(0x00000000);
            if (clip.leftHandle != null) clip.leftHandle.setVisibility(View.GONE);
            if (clip.rightHandle != null) clip.rightHandle.setVisibility(View.GONE);
        }
    }

    public static void deleteClip(Clip clip, Timeline timeline, EditingActivity activity) {
        activity.deselectingClip();
        Track currentTrack = timeline.tracks.get(clip.trackIndex);
        currentTrack.removeClip(clip);
        if (currentTrack.viewRef != null && clip.viewRef != null) {
            currentTrack.viewRef.removeView(clip.viewRef);
        }
    }

    public static void splitClip(Clip clip, EditingActivity activity, Timeline timeline, float currentGlobalTime) {
        Track currentTrack = timeline.tracks.get(clip.trackIndex);
        float translatedLocalCurrentTime = clip.getLocalClipTime(currentGlobalTime);
        Clip secondaryClip = new Clip(clip);
        secondaryClip.startTime = currentGlobalTime;
        float oldEndClipTrim = clip.endClipTrim;
        float oldStartClipTrim = clip.startClipTrim;
        clip.endClipTrim = clip.originalDuration - (translatedLocalCurrentTime + oldStartClipTrim);
        clip.duration = clip.originalDuration - clip.endClipTrim - clip.startClipTrim;
        secondaryClip.startClipTrim = translatedLocalCurrentTime + oldStartClipTrim;
        secondaryClip.endClipTrim = oldEndClipTrim;
        secondaryClip.duration = secondaryClip.originalDuration - secondaryClip.endClipTrim - secondaryClip.startClipTrim;
        activity.addClipToTrack(currentTrack, secondaryClip);
        activity.revalidationClipView(clip);
    }

    public static void restate(Clip clip) {
        clip.videoProperties = new VideoProperties(0, 0, 0, 1, 1, 1, 1);
    }
}

