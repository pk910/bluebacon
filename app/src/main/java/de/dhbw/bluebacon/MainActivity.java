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
import android.widget.Toast;

import org.altbeacon.beacon.BeaconConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.dhbw.bluebacon.extensions.ExtensionInterface;
import de.dhbw.bluebacon.extensions.SmartEyeGlassExtension;
import de.dhbw.bluebacon.model.BeaconDB;
import de.dhbw.bluebacon.model.BlueBaconManager;
import de.dhbw.bluebacon.model.IObservable;
import de.dhbw.bluebacon.model.IObserver;
import de.dhbw.bluebacon.model.JSONLoader;
import de.dhbw.bluebacon.model.Machine;
import de.dhbw.bluebacon.model.ObservableBeacon;
import de.dhbw.bluebacon.view.BeaconRadar;
import de.dhbw.bluebacon.view.MachineRadar;
import de.dhbw.bluebacon.view.MachineSetup;
import de.dhbw.bluebacon.view.TabPageAdapter;
import de.dhbw.meteoblue.LocationResolver;
import de.dhbw.meteoblue.WeatherData;


/**
 * Main Activity class
 */
public class MainActivity extends AppCompatActivity implements IObserver, BeaconConsumer {

    public static final String LOG_TAG = "DHBW MainActivity";
    public static final String SERVER_URL_TEMPLATE = "http://%s/json.php";

    public static final int PERMISSIONS_REQUEST_LOCATION_RESOLVER = 1;

    public SharedPreferences prefs;
    public enum PrefKeys {
        SERVER_LOCATION_PRIORITY("SERVER_LOCATION_PRIORITY"),
        SERVER_ADDR("SERVER_ADDR"),
        LAST_UPDATE_TIMESTAMP("LAST_UPDATE_TIMESTAMP"),
        LAST_UPDATE_SERVER_TYPE("LAST_UPDATE_SERVER_TYPE"),
        LAST_UPDATE_SUCCESS("LAST_UPDATE_SUCCESS");

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
    private ExtensionInterface extension;
    private Thread glassAdpaterThread;

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
        //beaconDB.clearBeacons();
        //beaconDB.clearMachines();

        blueBaconManager = new BlueBaconManager(this);
        blueBaconManager.subscribe(this);

        locationResolver = new LocationResolver(this);
        WeatherData.setAppContext(this);

        extension = new SmartEyeGlassExtension();
        extension.connect(this);

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

        startSonySmartEyeglassNotify();

    }

    //TODO: more elegant solution using e.g. Handler instead?
    //TODO: continue measuring while app is in background
    private void startSonySmartEyeglassNotify(){
        if(glassAdpaterThread == null || !glassAdpaterThread.isAlive()) {
            glassAdpaterThread = new Thread(new Runnable() {
                public void run() {
                    ArrayList<Machine> machines;
                    while (!Thread.currentThread().isInterrupted()) {
                        machines = blueBaconManager.getMachines();
                        if(machines.size() > 0){
                            String msg = String.valueOf(machines.get(0).getDistance());
                            extension.sendMessage(msg);
                        }
                        try {
                            Thread.sleep(5000);
                            if (Thread.currentThread().isInterrupted()) {
                                break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                }
            });
            glassAdpaterThread.start();
        }
    }

    /**
     * Destroy Activity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.blueBaconManager.destroy();
        glassAdpaterThread.interrupt();
        extension.disconnect();
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

    public void refreshSettingsUi(){
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (final Fragment fragment : fragments) {
            if(fragment instanceof MachineSetup) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((MachineSetup) fragment).refreshLastUpdateUi();
                    }
                });
            }
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

    public LocationResolver getLocationResolver() {
        return locationResolver;
    }

    public void updateLastUpdateInfo(boolean success, String serverType){
        long unixTimeMillis = System.currentTimeMillis();
        prefs.edit().putLong(PrefKeys.LAST_UPDATE_TIMESTAMP.toString(), unixTimeMillis).apply();
        prefs.edit().putBoolean(PrefKeys.LAST_UPDATE_SUCCESS.toString(), success).apply();
        prefs.edit().putString(PrefKeys.LAST_UPDATE_SERVER_TYPE.toString(), serverType).apply();
    }

    public void onServiceDiscoveryStatusUpdate(String localIpAddr){
        if(localIpAddr == null){
            Log.i(LOG_TAG, "UDP discovery: we got no answer");
            boolean preferRemoteServer = prefs.getBoolean(MainActivity.PrefKeys.SERVER_LOCATION_PRIORITY.toString(), true);
            if(preferRemoteServer){
                Log.e(LOG_TAG, "No local and/or remote servers could be reached.");
                progressHide();
                Toast.makeText(this, getString(R.string.no_server_found), Toast.LENGTH_LONG).show();
                updateLastUpdateInfo(false, "-");
                refreshSettingsUi();
            } else {
                // use JSONLoader within new thread
                Log.i(LOG_TAG, "No local server found, trying remote server...");
                progressShow(getString(R.string.contacting_server));
                new JSONLoader(this).execute();
            }

        } else {
            Log.i(LOG_TAG, "UDP discovery: got answer from: " + localIpAddr);
            prefs.edit().putString(MainActivity.PrefKeys.SERVER_ADDR.toString(), localIpAddr).apply();
            // we have found our server and can contact it via JSONLoader now
            progressShow(getString(R.string.contacting_server));
            new JSONLoader(this, false).execute(String.format(SERVER_URL_TEMPLATE, localIpAddr));
        }
    }

}
