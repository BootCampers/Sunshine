package org.sunlightmap.backgroundlibrary;

import android.app.IntentService;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.sunlightmap.org.backgroundlibrary.R;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DieIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "android.sunlightmap.org.backgroundlibrary.action.FOO";
    private static final String ACTION_BAZ = "android.sunlightmap.org.backgroundlibrary.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "android.sunlightmap.org.backgroundlibrary.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "android.sunlightmap.org.backgroundlibrary.extra.PARAM2";

    public DieIntentService() {
        super("DieIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, DieIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, DieIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
        int selection = sharedPreferences.getInt(Constants.SELECTION, 0);
        String url = getResources().getStringArray(R.array.urls)[selection];

        Log.i(Constants.TAG, "Service to get image triggered! " + selection);
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        WakefulBroadcastReceiver.completeWakefulIntent(intent);

        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
            //doc = Jsoup.connect("https://dl.dropboxusercontent.com/u/257493884/le").get();
        } catch (IOException e) {
            Log.e(Constants.TAG, e.getMessage());
            e.printStackTrace();
        }
        //If using dropbox link
            /*Element link = doc.body();
            String absHref = link.toString();
            absHref = absHref.replaceAll("(\\r|\\n|\\t|\\s)", "");
            absHref = absHref.replaceAll("<body>", "");
            absHref = absHref.replaceAll("</body>", "");*/

        if(doc != null){
            Elements media = doc.select("[src]");
            for (Element src : media) {
                if (src.tagName().equals("img")) {
                    if(src.attr("abs:src").endsWith("jpg")){

                        String ua = "Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19";
                        String urldisplay = src.attr("abs:src");
                        Bitmap wallpaperBitmap = null;
                        try {
                            System.setProperty("http.agent", ua);
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
                            BitmapDrawable drawable = new BitmapDrawable(getResources(), scaledBitmap);

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

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
