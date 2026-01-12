package com.vanvatcorporation.doubleclips.activities.helper;

import android.util.Log;
import com.vanvatcorporation.doubleclips.activities.model.Clip;
import com.vanvatcorporation.doubleclips.activities.model.ClipType;
import com.vanvatcorporation.doubleclips.activities.main.MainAreaScreen;
import com.vanvatcorporation.doubleclips.helper.IOHelper;
import java.io.File;

public class PreviewFileChecker {
    public static class PreviewStatus {
        public final boolean previewExists;
        public final boolean originalExists;
        public final String previewPath;
        public final String originalPath;
        public final String usingPath;
        public final boolean isUsingPreview;

        public PreviewStatus(boolean previewExists, boolean originalExists, 
                           String previewPath, String originalPath, 
                           String usingPath, boolean isUsingPreview) {
            this.previewExists = previewExists;
            this.originalExists = originalExists;
            this.previewPath = previewPath;
            this.originalPath = originalPath;
            this.usingPath = usingPath;
            this.isUsingPreview = isUsingPreview;
        }
    }

    public static PreviewStatus checkPreviewFile(Clip clip, MainAreaScreen.ProjectData data) {
        String previewPath = clip.getAbsolutePreviewPath(data);
        String originalPath = clip.getAbsolutePath(data.getProjectPath());
        
        boolean previewExists = previewPath != null && !previewPath.isEmpty() && IOHelper.isFileExist(previewPath);
        boolean originalExists = originalPath != null && !originalPath.isEmpty() && IOHelper.isFileExist(originalPath);
        
        String usingPath = previewExists ? previewPath : originalPath;
        boolean isUsingPreview = previewExists;
        
        if (!previewExists && clip.type == ClipType.VIDEO && originalExists) {
            Log.w("PreviewFileChecker", "Preview file not found for clip: " + clip.clipName + 
                  ". Preview seeking may be slower. Preview path: " + previewPath);
        }
        
        return new PreviewStatus(previewExists, originalExists, previewPath, originalPath, usingPath, isUsingPreview);
    }

    public static boolean hasPreviewFile(Clip clip, MainAreaScreen.ProjectData data) {
        String previewPath = clip.getAbsolutePreviewPath(data);
        return previewPath != null && !previewPath.isEmpty() && IOHelper.isFileExist(previewPath);
    }
}

