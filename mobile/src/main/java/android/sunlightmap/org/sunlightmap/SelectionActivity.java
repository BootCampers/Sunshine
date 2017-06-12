package android.sunlightmap.org.sunlightmap;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.sunlightmap.org.backgroundlibrary.Constants;
import android.sunlightmap.org.backgroundlibrary.Scheduler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class SelectionActivity extends AppCompatActivity {

     /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private BroadcastReceiver imageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(Constants.TAG, "Image recieved by app for wallpaper!");
            int resultCode = intent.getIntExtra("resultCode", RESULT_CANCELED);

            if (resultCode == RESULT_OK) {
                WallpaperManager wm = WallpaperManager.getInstance(getApplicationContext());
                int h = wm.getDesiredMinimumHeight();
                int w = wm.getDesiredMinimumWidth();

                byte[] byteArray = intent.getByteArrayExtra(Constants.WALL);
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
                    Toast.makeText(getApplicationContext(), "Please check network connectivity",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Scheduler scheduler = new Scheduler(this);
        scheduler.scheduleAlarm();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(Constants.LOCAL_BROADCAST_STRING);
        LocalBroadcastManager.getInstance(this).registerReceiver(imageReceiver, filter);
        // or `registerReceiver(testReceiver, filter)` for a normal broadcast
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_selection, container, false);
            int currentIndex = getArguments().getInt(ARG_SECTION_NUMBER)-1 ;
            TextView tvDesc = (TextView) rootView.findViewById(R.id.tvDesc);
            tvDesc.setText(getResources()
                    .getStringArray(R.array.sunshine_description)[currentIndex]);

            TypedArray drawablesArray = getResources().obtainTypedArray(R.array.wallpaper_drawables);

            ImageView ivSampleWall = (ImageView) rootView.findViewById(R.id.ivSource);
            ivSampleWall.setImageResource(drawablesArray.getResourceId(currentIndex, -1));
            drawablesArray.recycle();

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 6 total pages.
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getResources().getStringArray(R.array.sunshine_modes)[position];
        }
    }
}
