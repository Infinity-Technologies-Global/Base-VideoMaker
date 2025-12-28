package com.vanvatcorporation.doubleclips.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.vanvatcorporation.doubleclips.manager.LoggingManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ProgressCompressionHelper {

    public static void zipFolder(Context context, String srcFolder, ContentResolver resolver, Uri destUri, ZipProgressListener listener) {
        try (OutputStream fos = resolver.openOutputStream(destUri); ZipOutputStream zip = new ZipOutputStream(fos)) {
            long totalBytes = calculateTotalSize(new File(srcFolder));
            long[] written = {0}; // use array to mutate inside lambda
            addFolderToZip(context, "", srcFolder, zip, listener, written, totalBytes);
            zip.flush();
            listener.onCompleted();
        } catch (Exception e) {
            if (listener != null) listener.onError(e);
            LoggingManager.LogExceptionToNoteOverlay(context, e);
        }
    }

    public static void unzipFolder(Context context, ContentResolver resolver, Uri zipUri, String destDir, UnzipProgressListener listener) {
        try {
            File dir = new File(destDir);
            if (!dir.exists()) dir.mkdirs();


            // First pass: calculate total size
            long totalBytes = calculateTotalZipSize(context, resolver, zipUri);
            long[] extracted = {0};

            ZipInputStream zis = new ZipInputStream(resolver.openInputStream(zipUri));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(Objects.requireNonNull(newFile.getParent())).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                            extracted[0] += len;
                            if (listener != null) listener.onProgress(extracted[0], totalBytes, entry.getName());
                        }
                    }
                }
                zis.closeEntry();
            }

            if (listener != null) listener.onCompleted();
        } catch (Exception e) {
            if (listener != null) listener.onError(e);
            LoggingManager.LogExceptionToNoteOverlay(context, e);
        }
    }









    private static void addFileToZip(Context context, String path, String srcFile, ZipOutputStream zip, ZipProgressListener listener, long[] written, long totalBytes) throws IOException {
        File file = new File(srcFile);
        if (file.isDirectory()) {
            addFolderToZip(context, path, srcFile, zip, listener, written, totalBytes);
        } else {
            byte[] buf = new byte[1024];
            int len;
            try (FileInputStream in = new FileInputStream(srcFile)) {
                zip.putNextEntry(new ZipEntry(path + "/" + file.getName()));
                while ((len = in.read(buf)) > 0) {
                    zip.write(buf, 0, len);
                    written[0] += len;
                    if (listener != null) listener.onProgress(written[0], totalBytes, file.getName());
                }
            }
        }
    }

    private static void addFolderToZip(Context context, String path, String srcFolder, ZipOutputStream zip, ZipProgressListener listener, long[] written, long totalBytes) throws IOException {
        File folder = new File(srcFolder);
        for (String fileName : Objects.requireNonNull(folder.list())) {
            String newPath = path.isEmpty() ? folder.getName() : path + "/" + folder.getName();
            addFileToZip(context, newPath, srcFolder + "/" + fileName, zip, listener, written, totalBytes);
        }
    }


    private static long calculateTotalSize(File folder) {
        if (folder.isFile()) return folder.length();
        long size = 0;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                size += calculateTotalSize(f);
            }
        }
        return size;
    }
    private static long calculateTotalZipSize(Context context, ContentResolver resolver, Uri uri) throws IOException {
        long total = 0;


        InputStream in = resolver.openInputStream(uri);
        File tempFile = File.createTempFile("tempZip", ".zip", context.getCacheDir());
        try (OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        in.close();

        ZipFile zipfile = new ZipFile(tempFile);
        Enumeration<? extends ZipEntry> zipEnum = zipfile.entries();
        while (zipEnum.hasMoreElements())
        {
            ZipEntry entry = zipEnum.nextElement();
            if (! entry.isDirectory ())
            {
                total += entry.getSize();
                // entry.getName()
                // entry.getSize ()
                // entry.getCompressedSize ()
            }
        }

//        try (ZipInputStream zis = new ZipInputStream(resolver.openInputStream(uri))) {
//            ZipEntry entry;
//            while ((entry = zis.getNextEntry()) != null) {
//                if (!entry.isDirectory()) {
//                    total += (entry.getSize() > 0 ? entry.getSize() : entry.getCompressedSize());
//                }
//                zis.closeEntry();
//            }
//        }
        tempFile.delete();
        if(total <= 0) total = 1;
        return total;
    }




    public interface ZipProgressListener {
        void onProgress(long bytesWritten, long totalBytes, String name);
        void onCompleted();
        void onError(Exception e);
    }
    public interface UnzipProgressListener {
        void onProgress(long bytesExtracted, long totalBytes, String name);
        void onCompleted();
        void onError(Exception e);
    }


}
