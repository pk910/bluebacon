package de.dhbw.bluebacon;

import android.content.Context;
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
import android.view.MenuItem;

import org.altbeacon.beacon.BeaconConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.dhbw.bluebacon.model.BlueBaconManager;
import de.dhbw.bluebacon.model.IObservable;
import de.dhbw.bluebacon.model.IObserver;
import de.dhbw.bluebacon.model.Machine;
import de.dhbw.bluebacon.model.ObservableBeacon;
import de.dhbw.bluebacon.view.BeaconRadar;
import de.dhbw.bluebacon.view.MachineRadar;
import de.dhbw.bluebacon.view.TabPageAdapter;


/**
 * Main Activity class
 */
public class MainActivity extends AppCompatActivity implements IObserver, BeaconConsumer {

    protected BlueBaconManager blueBaconManager;
    protected List<ObservableBeacon> beacons;
    protected List<Machine> machines;

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

        this.beacons = new ArrayList<>();
        this.machines = new ArrayList<>();
        this.blueBaconManager = new BlueBaconManager(this);
        this.blueBaconManager.subscribe(this);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new TabPageAdapter(getSupportFragmentManager(), getApplicationContext());
        viewPager.setAdapter(adapter);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position)
            {
            }
            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }
        });
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
     * Handle Action Bar Items
     * @param item MenuItem
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
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

        Log.d("DHBW MainActivity", "Sort by RSSI");

        if(BuildConfig.DEBUG){
            for(ObservableBeacon beacon : this.beacons) {
                Log.d("DHBW MainActivity", beacon.getFullUUID() + " : " + beacon.getRSSI() + " : " + beacon.getDistance());
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
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        this.blueBaconManager.pause();
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
        if(currentFragment != null && currentFragment instanceof BeaconRadar ){
            Log.d("DHBW FragmentActivity", "Found Fragment");
            final Fragment finalCurrentFragment = currentFragment;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((BeaconRadar) finalCurrentFragment).refreshAllData(beacons);
                }
            });

        }
        // refresh Machine fragment
        if(currentFragment != null && currentFragment instanceof MachineRadar){
            Log.d("DHBW FragmentActivity", "Found Fragment");
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
     * Check if Network is available
     * @return boolean
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

}
