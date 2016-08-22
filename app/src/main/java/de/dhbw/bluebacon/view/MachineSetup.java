package de.dhbw.bluebacon.view;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;

import de.dhbw.bluebacon.MainActivity;
import de.dhbw.bluebacon.R;
import de.dhbw.bluebacon.model.BlueBaconManager;
import de.dhbw.bluebacon.model.DiscoveryBroadcaster;
import de.dhbw.bluebacon.model.DiscoveryListener;
import de.dhbw.bluebacon.model.JSONLoader;

/**
 * Machine Setup class
 */
public class MachineSetup extends Fragment implements CompoundButton.OnCheckedChangeListener {

    MainActivity mainActivity;
    BlueBaconManager blueBaconManager;
    Switch swUseCleanedValues;
    Switch swUseSimpleMode;
    RadioButton rdRemoteServer;
    RadioButton rdLocalServer;

    public static final String LOG_TAG = "DHBW MachineSetup";

    /**
     * Attach MainActivity to Fragment
     * @param context Context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a;

        if (context instanceof Activity){
            a=(Activity) context;
            mainActivity = (MainActivity) a;
        }
    }

    /**
     * Create Fragment
     * @param savedInstanceState saved instance state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blueBaconManager = mainActivity.getBlueBaconManager();
    }

    /**
     * Create view for Fragment
     * @param inflater inflater
     * @param container container
     * @param savedInstanceState saved instance state
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //inflate fragment
        View machineSetup = inflater.inflate(R.layout.setup_frag,container,false);

        // find switch for cleaned values
        swUseCleanedValues = (Switch) machineSetup.findViewById(R.id.swUseCleanedValues);
        // set value from bacon manager
        swUseCleanedValues.setChecked(blueBaconManager.getValueCleaning());
        // attach a listener to check for changes in state
        swUseCleanedValues.setOnCheckedChangeListener(this);

        // find switch for simple mode
        swUseSimpleMode = (Switch) machineSetup.findViewById(R.id.swUseSimpleMode);
        // set value from bacon manager
        swUseSimpleMode.setChecked(blueBaconManager.getSimpleMode());
        // attach a listener to check for changes in state
        swUseSimpleMode.setOnCheckedChangeListener(this);

        // find radio buttons for server location preference
        rdRemoteServer = (RadioButton) machineSetup.findViewById(R.id.rdRemoteServer);
        rdLocalServer = (RadioButton) machineSetup.findViewById(R.id.rdLocalServer);
        // get value from shared prefs and change the radio buttons to the correct state
        boolean preferRemoteServer = mainActivity.prefs.getBoolean(MainActivity.PrefKeys.SERVER_LOCATION_PRIORITY.toString(), true);
        rdRemoteServer.setChecked(preferRemoteServer);
        rdLocalServer.setChecked(!preferRemoteServer);
        // attach listeners to check for changes in state
        View.OnClickListener rdListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // we don't need to check whether the radio button is checked (;)) because when the user clicked on it, it IS checked.

                // update preference value
                mainActivity.prefs.edit().putBoolean(MainActivity.PrefKeys.SERVER_LOCATION_PRIORITY.toString(), v.getId() == R.id.rdRemoteServer).commit();
            }
        };
        rdRemoteServer.setOnClickListener(rdListener);
        rdLocalServer.setOnClickListener(rdListener);

        final Button btUpdateData = (Button) machineSetup.findViewById(R.id.btUpdateData);
        btUpdateData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean preferRemoteServer = mainActivity.prefs.getBoolean(MainActivity.PrefKeys.SERVER_LOCATION_PRIORITY.toString(), true);
                if(preferRemoteServer){
                    // try hard-coded hostname first
                    mainActivity.progressShow(getString(R.string.contacting_server));
                    new JSONLoader(mainActivity).execute();
                } else {
                    // try a discovery via UDP broadcast first
                    mainActivity.progressShow(getString(R.string.discovering_server));
                    new DiscoveryListener(mainActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    new DiscoveryBroadcaster(mainActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });

        return machineSetup;
    }

    /**
     * Listener for switches state change
     * @param buttonView button view
     * @param isChecked check state
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {
            case R.id.swUseCleanedValues:
                blueBaconManager.setValueCleaning(isChecked);
                Log.d(LOG_TAG, "use cleaned value is set to "+blueBaconManager.getValueCleaning());
                break;

            case R.id.swUseSimpleMode:
                blueBaconManager.setSimpleMode(isChecked);
                Log.d(LOG_TAG, "use simple mode is set to "+blueBaconManager.getSimpleMode());
                break;

            default:
                break;
        }
    }
}
