package de.dhbw.bluebacon.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import de.dhbw.bluebacon.MainActivity;
import de.dhbw.bluebacon.R;
import de.dhbw.bluebacon.model.BlueBaconManager;

/**
 * Machine Setup class
 */
public class MachineSetup extends Fragment implements CompoundButton.OnCheckedChangeListener {

    MainActivity mainActivity;
    BlueBaconManager blueBaconManager;
    Switch swUseCleanedValues;
    Switch swUseSimpleMode;

    /**
     * Attach MainActivity to Fragment
     * @param activity MainActivity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
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
        swUseCleanedValues.setChecked(blueBaconManager.GetValueCleaning());
        // attach a listener to check for changes in state
        swUseCleanedValues.setOnCheckedChangeListener(this);

        // find switch for simple mode
        swUseSimpleMode = (Switch) machineSetup.findViewById(R.id.swUseSimpleMode);
        // set value from bacon manager
        swUseSimpleMode.setChecked(blueBaconManager.GetSimpleMode());
        // attach a listener to check for changes in state
        swUseSimpleMode.setOnCheckedChangeListener(this);

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
                blueBaconManager.SetValueCleaning(isChecked);
                Log.d("DHBW BlueBaconManager", "use cleaned value is set to "+blueBaconManager.GetValueCleaning());
                break;

            case R.id.swUseSimpleMode:
                blueBaconManager.SetSimpleMode(isChecked);
                Log.d("DHBW BlueBaconManager", "use simple mode is set to "+blueBaconManager.GetSimpleMode());
                break;

            default:
                break;
        }
    }
}
