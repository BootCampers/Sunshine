package org.sunlightmap.mobileapp;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SelectionActivity extends AppCompatActivity {

  /**
   * The {@link PagerAdapter} that will provide fragments for each of the sections. We use a {@link
   * FragmentPagerAdapter} derivative, which will keep every loaded fragment in memory. If this
   * becomes too memory intensive, it may be best to switch to a {@link FragmentStatePagerAdapter}.
   */
  private SectionsPagerAdapter mSectionsPagerAdapter;

  /** The {@link ViewPager} that will host the section contents. */
  private ViewPager mViewPager;

  private BroadcastReceiver imageReceiver =
      new BroadcastReceiver() {
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
              Toast.makeText(
                      getApplicationContext(),
                      "Please check network connectivity",
                      Toast.LENGTH_LONG)
                  .show();
              e.printStackTrace();
            }
          }
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_selection);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    // Create the adapter that will return a fragment for each of the three
    // primary sections of the activity.
    mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

    // Set up the ViewPager with the sections adapter.
    mViewPager = findViewById(R.id.container);
    mViewPager.setAdapter(mSectionsPagerAdapter);

    TabLayout tabLayout = findViewById(R.id.tabs);
    tabLayout.setupWithViewPager(mViewPager);
  }

  @Override
  protected void onResume() {
    super.onResume();
    // Register for the particular broadcast based on ACTION string
    IntentFilter filter = new IntentFilter(Constants.LOCAL_BROADCAST_STRING);
    LocalBroadcastManager.getInstance(this).registerReceiver(imageReceiver, filter);
    // or `registerReceiver(testReceiver, filter)` for a normal broadcast
  }

  /** A placeholder fragment containing a simple view. */
  public static class PlaceholderFragment extends Fragment {
    /** The fragment argument representing the section number for this fragment. */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public PlaceholderFragment() {}

    /** Returns a new instance of this fragment for the given section number. */
    public static PlaceholderFragment newInstance(int sectionNumber) {
      PlaceholderFragment fragment = new PlaceholderFragment();
      Bundle args = new Bundle();
      args.putInt(ARG_SECTION_NUMBER, sectionNumber);
      fragment.setArguments(args);
      return fragment;
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_selection, container, false);
      final int currentIndex = getArguments().getInt(ARG_SECTION_NUMBER) - 1;
      TextView tvDesc = rootView.findViewById(R.id.tvDesc);
      tvDesc.setText(getResources().getStringArray(R.array.sunshine_description)[currentIndex]);

      TypedArray drawablesArray = getResources().obtainTypedArray(R.array.wallpaper_drawables);

      ImageView ivSampleWall = rootView.findViewById(R.id.ivSource);
      ivSampleWall.setImageResource(drawablesArray.getResourceId(currentIndex, -1));
      drawablesArray.recycle();

      Button btnSelection = rootView.findViewById(R.id.btnSet);
      btnSelection.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              SharedPreferences sharedPreferences =
                  getContext().getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);

              SharedPreferences.Editor editor = sharedPreferences.edit();
              editor.putInt(Constants.SELECTION, currentIndex);
              editor.putBoolean(Constants.APP_ON, true);
              editor.apply();

              PeriodicWorkRequest periodicWorkRequest =
                  new PeriodicWorkRequest.Builder(UpdateImageWorker.class, 2, TimeUnit.MINUTES)
                      .build();
              WorkManager.getInstance(getContext()).enqueue(periodicWorkRequest);
              Toast.makeText(getActivity(), "All set for updates!", Toast.LENGTH_SHORT).show();
            }
          });

      return rootView;
    }
  }

  /**
   * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the
   * sections/tabs/pages.
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_selection, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_turn_off) {
      new AlertDialog.Builder(this)
          .setTitle("Are you sure?")
          .setMessage("Turn off wallpaper updates! Hit back to cancel!")
          .setPositiveButton( "Okay",
              new AlertDialog.OnClickListener() {
                  @Override
                public void onClick(DialogInterface sDialog, int which) {
                  sDialog.dismiss();
                  SharedPreferences sharedPreferences =
                      getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);
                  SharedPreferences.Editor editor = sharedPreferences.edit();
                  editor.putBoolean(Constants.APP_ON, false);
                  editor.commit();
                  Toast.makeText(getApplicationContext(), R.string.msg_turn_off, Toast.LENGTH_SHORT)
                      .show();
                }
              })
          .setNegativeButton("Cancel",
              new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface sDialog, int which) {
                  sDialog.dismiss();
                }
              })
          .show();
    } else if (id == R.id.action_info) {
      new AlertDialog.Builder(this)
          .setTitle("About")
          .setMessage(getResources().getString(R.string.msg_about))
          .setPositiveButton("Close",
              new AlertDialog.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                      dialog.dismiss();
                  }
              })
          .show();
    } else if (id == R.id.action_settings) {
      startActivity(new Intent(SelectionActivity.this, SettingsActivity.class));
    }
    return super.onOptionsItemSelected(item);
  }
}
