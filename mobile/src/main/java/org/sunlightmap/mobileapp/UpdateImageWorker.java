package org.sunlightmap.mobileapp;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UpdateImageWorker extends Worker {

    private Context mContext;

    public UpdateImageWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        int selection = sharedPreferences.getInt(Constants.SELECTION, 0);
        boolean isAppOn = sharedPreferences.getBoolean(Constants.APP_ON, false);

        if (!isAppOn) {
            return null;
        }

        String url = mContext.getResources().getStringArray(R.array.urls)[selection];
        url = url.replace("http", "https");

        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
            //doc = Jsoup.connect("https://dl.dropboxusercontent.com/u/257493884/le").get();
        } catch (IOException e) {
            Log.e(Constants.TAG, e.getMessage());
            e.printStackTrace();
        }
        //If using dropbox link
            Element link = doc.body();
            String absHref = link.toString();
            absHref = absHref.replaceAll("(\\r|\\n|\\t|\\s)", "");
            absHref = absHref.replaceAll("<body>", "");
            absHref = absHref.replaceAll("</body>", "");

        if(doc != null){
            Elements media = doc.select("[src]");
            for (Element src : media) {
                if (src.tagName().equals("img")) {
                    if(src.attr("abs:src").endsWith("jpg")){

                        String ua = "Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19";
                        String urldisplay = src.attr("abs:src");
                        Bitmap wallpaperBitmap = null;
                        try {
                            //System.setProperty("http.agent", ua);
                            InputStream in = new java.net.URL(urldisplay).openStream();

                            wallpaperBitmap = BitmapFactory.decodeStream(in);

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            wallpaperBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();

                            /*Intent intentBackToActivity = new Intent(Constants.LOCAL_BROADCAST_STRING);
                            intentBackToActivity.putExtra(Constants.WALL, byteArray);
                            intentBackToActivity.putExtra("resultCode", Activity.RESULT_OK);

                            LocalBroadcastManager.getInstance(this).sendBroadcast(intentBackToActivity);*/

                            WallpaperManager wm = WallpaperManager.getInstance(getApplicationContext());
                            int h = wm.getDesiredMinimumHeight();
                            int w = wm.getDesiredMinimumWidth();

                            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

                            Log.i(Constants.TAG, "Size is " + byteArray.length);

                            int rw = bmp.getWidth();
                            int rh = bmp.getHeight();

                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp, w, h, false);
                            BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(), scaledBitmap);

                            try {
                                wm.setBitmap(scaledBitmap);
                            } catch (IOException e) {
                                Log.e(Constants.TAG, "Error in setting bitmap " + e.getMessage());
                                e.printStackTrace();
                            }

                        } catch (Exception e) {
                            Log.e(Constants.TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
        return Result.success();
    }
}
