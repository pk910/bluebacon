package de.dhbw.bluebacon.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.dhbw.bluebacon.R;
import de.dhbw.bluebacon.model.ObservableBeacon;

/**
 * Beacon Radar class
 */
public class BeaconRadar extends Fragment {
    ListView listView;
    BeaconAdapter beaconAdapter;
    List<ObservableBeacon> beacons;

    /**
     * Create Fragment
     * @param savedInstanceState saved instance state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beacons = new ArrayList<ObservableBeacon>();
        beaconAdapter = new BeaconAdapter(getActivity(),beacons);

    }

    /**
     * Create Fragment view
     * @param inflater inflater
     * @param parent parent
     * @param savedInstanceState saved instance state
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        //inflate fragment
        View beaconRadar = inflater.inflate(R.layout.radar_frag,parent,false);
        listView = (ListView) beaconRadar.findViewById(R.id.radarList);
        listView.setAdapter(beaconAdapter);
        listView.setEmptyView(beaconRadar.findViewById(R.id.empty));
        return beaconRadar;
    }

    /**
     * Refresh  data in list view
     * @param newBeacons List
     */
    public void refreshAllData(List<ObservableBeacon> newBeacons) {
        beacons.clear();
        beacons.addAll(newBeacons);
        beaconAdapter.notifyDataSetChanged();
    }
}
