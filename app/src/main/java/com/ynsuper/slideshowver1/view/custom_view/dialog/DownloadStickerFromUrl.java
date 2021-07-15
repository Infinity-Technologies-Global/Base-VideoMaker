package com.ynsuper.slideshowver1.view.custom_view.dialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;


import com.ynsuper.slideshowver1.callback.IUnzipFile;
import com.ynsuper.slideshowver1.util.Constants;
import com.ynsuper.slideshowver1.util.UnzipUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadStickerFromUrl extends AsyncTask<String, String, String> {
    private final Context context;
    private final IUnzipFile iUnZipFile;
    private ProgressDialog pDialog;


    public DownloadStickerFromUrl(Context context, IUnzipFile iUnzipFile) {
        this.iUnZipFile = iUnzipFile;
        this.context = context;
    }

    /**
     * Before starting background thread
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        System.out.println("Starting download");

        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Loading... Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    /**
     * Downloading file in background thread
     */
    @Override
    protected String doInBackground(String... f_url) {
        int count;
        try {
            String root = Environment.getExternalStorageDirectory().toString();

            System.out.println("Downloading");
            URL url = new URL(f_url[0]);

            URLConnection conection = url.openConnection();
            conection.connect();
            // getting file length
            int lenghtOfFile = conection.getContentLength();

            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            // Output stream to write file
            String PATH = Constants.PATH_DOWNLOAD_STICKER_FROM_CLOUD;
            String nameFile = url.toString().substring(url.toString().lastIndexOf("/")+1);
            Log.v("LOG_TAG", "PATH: " +
                    PATH + "/" + nameFile);

            File filePath = new File(PATH);
            filePath.mkdirs();

            File mFile = new File(filePath, nameFile);
            OutputStream output = new FileOutputStream(mFile);
            byte data[] = new byte[1024];

            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;

                // writing data to file
                output.write(data, 0, count);

            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();
            return mFile.getAbsolutePath();
        } catch (Exception e) {
//            Toast.makeText(context, R.string.text_no_internet, Toast.LENGTH_SHORT).show();
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "No Internet connection", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            Log.e("Error: ", e.getMessage());
        }

        return null;
    }


    /**
     * After completing background task
     **/
    @Override
    protected void onPostExecute(String url) {
//        Toast.makeText(context, "Downloaded filter", Toast.LENGTH_SHORT).show();
        //  System.out.println("Downloaded");
        try {
            String PATH = Constants.PATH_DOWNLOAD_STICKER_FROM_CLOUD;
            String nameFile = url.substring(url.lastIndexOf("/") + 1);
            File filePath = new File(PATH);
            File mFile = new File(filePath, nameFile);

            new UnzipUtil(mFile.getAbsolutePath(), PATH + "/", iUnZipFile).unzip();

        } catch (Exception e) {
            // Toast.makeText(context, "Download: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            pDialog.dismiss();
            e.printStackTrace();
        }

        pDialog.dismiss();
    }

}