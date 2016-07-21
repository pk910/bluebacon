package de.dhbw.bluebacon;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import org.altbeacon.beacon.BeaconConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.dhbw.bluebacon.model.*;
import de.dhbw.bluebacon.view.BeaconRadar;
import de.dhbw.bluebacon.view.MachineRadar;
import de.dhbw.bluebacon.view.TabPageAdapter;


/**
 * Main Activity class
 */
public class MainActivity extends FragmentActivity implements IObserver, BeaconConsumer {

    protected BlueBaconManager blueBaconManager;
    protected List<ObservableBeacon> beacons;
    protected List<Machine> machines;
    ViewPager Tab;
    TabPageAdapter TabAdapter;
    ActionBar actionBar;

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

        this.beacons = new ArrayList<ObservableBeacon>();
        this.machines = new ArrayList<Machine>();
        this.blueBaconManager = new BlueBaconManager(this);
        this.blueBaconManager.subscribe(this);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_main);
        actionBar = getActionBar();
        //actionBar.hide();

        TabAdapter = new TabPageAdapter(getSupportFragmentManager());
        Tab = (ViewPager)findViewById(R.id.pager);
        Tab.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                }
        );
        Tab.setAdapter(TabAdapter);
        //Enable Tabs on Action Bar
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

            }

            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                Tab.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

            }
        };

        //Add New Tab
        actionBar.addTab(actionBar.newTab().setText("Beacon Radar").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Machine Radar").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Machine Setup").setTabListener(tabListener));

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

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

        for(ObservableBeacon beacon : this.blueBaconManager.GetBeacons().values()) {
            this.beacons.add(beacon);
        }

        Collections.sort(this.beacons);

        for(Machine machine : this.blueBaconManager.GetMachines().values()) {
            this.machines.add(machine);
        }

        Collections.sort(this.machines);

        refreshViews();

        Log.d("DHBW MainActivity", "Sort by RSSI");

        for(ObservableBeacon beacon : this.beacons) {
            Log.d("DHBW MainActivity", beacon.GetFullUUID() + " : " + beacon.GetRSSI() + " : " + beacon.GetDistance());
        }
    }

    /**
     * Destroy Activity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.blueBaconManager.Destroy();
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
        this.blueBaconManager.Resume();
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        this.blueBaconManager.Pause();
    }

    /**
     * Triggered when BeaconManager service connects
     */
    @Override
    public void onBeaconServiceConnect() {
        this.blueBaconManager.Start();
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
