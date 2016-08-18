package de.dhbw.bluebacon;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;

import org.altbeacon.beacon.BeaconConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.dhbw.bluebacon.model.BeaconDB;
import de.dhbw.bluebacon.model.BlueBaconManager;
import de.dhbw.bluebacon.model.IObservable;
import de.dhbw.bluebacon.model.IObserver;
import de.dhbw.bluebacon.model.Machine;
import de.dhbw.bluebacon.model.ObservableBeacon;
import de.dhbw.bluebacon.view.BeaconRadar;
import de.dhbw.bluebacon.view.MachineRadar;
import de.dhbw.bluebacon.view.TabPageAdapter;
import de.dhbw.meteoblue.LocationResolver;
import de.dhbw.meteoblue.WeatherData;


/**
 * Main Activity class
 */
public class MainActivity extends AppCompatActivity implements IObserver, BeaconConsumer {

    public static final String LOG_TAG = "DHBW MainActivity";

    public static final int PERMISSIONS_REQUEST_LOCATION_RESOLVER = 1;

    public SharedPreferences prefs;
    public enum PrefKeys {
        SERVER_LOCATION_PRIORITY("SERVER_LOCATION_PRIORITY"),
        SERVER_ADDR("SERVER_ADDR"),
        DB_SCHEMA_VERSION("DB_SCHEMA_VERSION");

        private final String text;

        PrefKeys(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private ProgressDialog progress;
    protected BeaconDB beaconDB;
    protected BlueBaconManager blueBaconManager;
    protected List<ObservableBeacon> beacons;
    protected List<Machine> machines;
    private LocationResolver locationResolver;

    public List<ObservableBeacon> getBeacons() {
        return beacons;
    }

    /**
     * Create MainActivity
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);
        setSupportActionBar(toolbar);

        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setProgress(0);
        progress.setCancelable(false);

        prefs = this.getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);
        beacons = new ArrayList<>();
        machines = new ArrayList<>();
        beaconDB = new BeaconDB(this);
        blueBaconManager = new BlueBaconManager(this);
        blueBaconManager.subscribe(this);

        locationResolver = new LocationResolver(this);
        WeatherData.SetAppContext(this);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter adapter = new TabPageAdapter(getSupportFragmentManager(), getApplicationContext());
        viewPager.setAdapter(adapter);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);

    }

    /**
     * Create Main Menu / Action Bar
     * @param menu Menu
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Gets triggered by BlueBaconManager if beacons or machines have updated data
     * @param observable Observed object
     */
    @Override
    public void notify(IObservable observable) {
        this.beacons.clear();
        this.machines.clear();

        for(ObservableBeacon beacon : this.blueBaconManager.getBeacons().values()) {
            this.beacons.add(beacon);
        }

        Collections.sort(this.beacons);

        for(int i = 0; i < this.blueBaconManager.getMachines().size(); i++){
            this.machines.add(this.blueBaconManager.getMachines().get(i));
        }

        Collections.sort(this.machines);

        refreshViews();

        Log.d(LOG_TAG, "Sort by RSSI");

        if(BuildConfig.DEBUG){
            for(ObservableBeacon beacon : this.beacons) {
                Log.d(LOG_TAG, beacon.getFullUUID() + " : " + beacon.getRSSI() + " : " + beacon.getDistance());
            }
        }

    }

    /**
     * Destroy Activity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.blueBaconManager.destroy();
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        this.blueBaconManager.resume();
        locationResolver.startLocationListener();
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        this.blueBaconManager.pause();
        locationResolver.stopLocationListener();
    }

    /**
     * Triggered when BeaconManager service connects
     */
    @Override
    public void onBeaconServiceConnect() {
        this.blueBaconManager.start();
    }

    private void refreshViews() {
        //identify the current fragment tab
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        Fragment currentFragment = null;
        for (Fragment fragment : fragments) {
            if(fragment != null && fragment.getUserVisibleHint()) {
                currentFragment = fragment;
            }
        }
        // refresh Beacon fragment
        if(currentFragment instanceof BeaconRadar ){
            Log.d(LOG_TAG, "Found Fragment");
            final Fragment finalCurrentFragment = currentFragment;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((BeaconRadar) finalCurrentFragment).refreshAllData(beacons);
                }
            });

        }
        // refresh Machine fragment
        if(currentFragment instanceof MachineRadar){
            Log.d(LOG_TAG, "Found Fragment");
            final Fragment finalCurrentFragment = currentFragment;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((MachineRadar) finalCurrentFragment).refreshAllData(machines);
                }
            });

        }
    }

    /**
     * Get BlueBaconManager object (e.g. from Fragments)
     * @return BlueBaconManager
     */
    public BlueBaconManager getBlueBaconManager() {
        return this.blueBaconManager;
    }

    /**
     * Get BeaconDB object
     * @return BeaconDB
     */
    public BeaconDB getBeaconDB() {
        return this.beaconDB;
    }

    /**
     * Check if Network is available
     * @return boolean
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public void progressShow(String progressText){
        progress.setMessage(progressText);
        progress.show();
    }

    public void progressHide(){
        progress.hide();
    }

    /**
     * Android 5 permission management
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION_RESOLVER: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    locationResolver.startLocationListener();
                } else {
                    // permission denied
                }
                return;
            }
            // other requests?
        }
    }
}
