package de.dhbw.bluebacon.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;

import de.dhbw.bluebacon.MainActivity;
import de.dhbw.bluebacon.R;
import de.dhbw.bluebacon.model.BlueBaconManager;
import de.dhbw.bluebacon.model.Machine;

/**
 * Machine Radar class
 */
public class MachineRadar extends Fragment {
    MainActivity mainActivity;
    BlueBaconManager blueBaconManager;
    ExpandableListView listView;
    MachineAdapter machineAdapter;
    List<Machine> machines;

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
     * Create Fragment with machine objects
     * @param savedInstanceState saved instance state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blueBaconManager = mainActivity.getBlueBaconManager();
        machines = new ArrayList<Machine>();
        machineAdapter = new MachineAdapter(getActivity(), machines, blueBaconManager);
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
        // inflate fragment
        View machineRadar = inflater.inflate(R.layout.machine_frag, container, false);
        listView = (ExpandableListView) machineRadar.findViewById(R.id.machineList);
        listView.setAdapter(machineAdapter);
        listView.setEmptyView(machineRadar.findViewById(R.id.empty));

        return machineRadar;
    }

    /**
     * Refresh data in list view
     * @param newMachines List
     */
    public void refreshAllData(List<Machine> newMachines) {
        machines.clear();
        machines.addAll(newMachines);
        machineAdapter.notifyDataSetChanged();
    }
}
