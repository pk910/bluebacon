package de.dhbw.bluebacon.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.dhbw.bluebacon.R;
import de.dhbw.bluebacon.model.BlueBaconManager;
import de.dhbw.bluebacon.model.Machine;
import de.dhbw.bluebacon.model.ObservableBeacon;

/**
 * Machine Adapter class
 */
public class MachineAdapter extends BaseExpandableListAdapter {

    List<Machine> machines = new ArrayList<>();
    List<ObservableBeacon> topBeacons;
    Map<String, ObservableBeacon> mappedBeacons;
    LayoutInflater inflater;
    Context context;
    BlueBaconManager blueBaconManager;

    /**
     * Constructor
     * @param context Activity Context
     * @param machines Array of Machine objects
     * @param blueBaconManager BlueBaconManager object from MainActivity
     */
    public MachineAdapter(Context context, List<Machine> machines, BlueBaconManager blueBaconManager) {
        this.context = context;
        this.machines = machines;
        this.blueBaconManager = blueBaconManager;
        this.inflater = LayoutInflater.from(this.context);
    }

    /**
     * Get number of machine objects
     * @return int
     */
    @Override
    public int getGroupCount() {
        return machines.size();
    }

    /**
     * Get number of machine detail items (fix 1)
     * @param groupPosition machine object position in array
     * @return int
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    /**
     * Get machine object by position in array
     * @param groupPosition machine object position in array
     * @return Machine
     */
    @Override
    public Object getGroup(int groupPosition) {
        return machines.get(groupPosition);
    }

    /**
     * Not implemented, no child objects used, force override
     * @param groupPosition group position
     * @param childPosition child position
     * @return null
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    /**
     * Get machine id, not implemented, force override
     * @param groupPosition machine object position in array
     * @return int
     */
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    /**
     * Get child id, not implemented, force override
     * @param groupPosition group position
     * @param childPosition child position
     * @return int
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /**
     * Check for stable ids, not used
     * @return boolean
     */
    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * Create group list item for each machine
     * @param groupPosition machine object position in array
     * @param isExpanded state expanded/collapsed
     * @param convertView view to put content in
     * @param parent parent item
     * @return View
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View newConvertView;

        if(convertView == null){
            newConvertView = this.inflater.inflate(R.layout.machine_frag_item,parent,false);
        } else {
            newConvertView = convertView;
        }

        // define text views and linear layout for distance
        TextView tvName, tvMaintenance, tvBeacons;
        LinearLayout llDistance;

        // find text views and linear layout for distance in xml
        tvName = (TextView) newConvertView.findViewById(R.id.tvName);
        tvMaintenance = (TextView) newConvertView.findViewById(R.id.tvMaintenance);
        tvBeacons = (TextView) newConvertView.findViewById(R.id.tvBeacons);
        llDistance = (LinearLayout) newConvertView.findViewById(R.id.llDistance);

        // get current machine object and set values
        Machine currentMachine = (Machine) getGroup(groupPosition);
        // get top beacons mapped to machine
        this.topBeacons = currentMachine.getTopBeacons();
        // get all beacons mapped to machine
        this.mappedBeacons = currentMachine.getMappedBeacons();

        // set text data in text views
        tvName.setText(currentMachine.getName());
        tvMaintenance.setText(currentMachine.getMaintenanceState());
        tvBeacons.setText(String.format(this.context.getResources().getString(R.string.beacons_placeholder), this.topBeacons.size(), this.mappedBeacons.size()));

        // on refresh remove all programmatically added text views from linear layout
        llDistance.removeAllViews();

        // set distance/rssi values according to simple mode value (true/false)
        if(this.blueBaconManager.getSimpleMode()) {
            // add text view for rssi title to linear layout
            llDistance.addView(createTextView(this.context.getString(R.string.rssi_title)));
            // iterate through mapped beacons list
            for(ObservableBeacon beacon : this.topBeacons) {
                // add text view with rssi data for each beacon to linear layout
                llDistance.addView(createTextView(Integer.toString(beacon.getRSSI())));
            }
        } else {
            // add text view for distance title to linear layout
            llDistance.addView(createTextView(this.context.getString(R.string.distance_title)));
            // add text view with distance data from algorithm to linear layout
            llDistance.addView(createTextView(Double.toString(currentMachine.getDistance())));
        }

        return newConvertView;
    }

    /**
     * Create expandable/collapsible child with detail information for each machine
     * @param groupPosition machine object position in array
     * @param childPosition machine detail object position in array
     * @param isLastChild state last child
     * @param convertView view to put content in
     * @param parent parent item
     * @return View
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View newConvertView;

        if(convertView == null){
            newConvertView = this.inflater.inflate(R.layout.machine_frag_detail,parent,false);
        } else {
            newConvertView = convertView;
        }

        // define text views
        TextView tvProduction, tvDescription;
        LinearLayout llBeacons_Wrapper;

        // find text views in xml
        tvProduction = (TextView) newConvertView.findViewById(R.id.tvProduction);
        tvDescription = (TextView) newConvertView.findViewById(R.id.tvDescription);
        llBeacons_Wrapper = (LinearLayout) newConvertView.findViewById(R.id.llBeacons_Wrapper);

        // get current machine object and set values
        Machine currentMachine = (Machine) getGroup(groupPosition);
        // get top beacons mapped to machine
        this.topBeacons = currentMachine.getTopBeacons();
        // get all beacons mapped to machine
        this.mappedBeacons = currentMachine.getMappedBeacons();

        // set text data in text views
        tvProduction.setText(currentMachine.getProductionState());
        tvDescription.setText(currentMachine.getDescription());

        // on refresh remove all programmatically added text views from linear layout
        llBeacons_Wrapper.removeAllViews();

        // iterate through all mapped beacons and add text view
        for(Map.Entry<String, ObservableBeacon> entry : this.mappedBeacons.entrySet()){
            ObservableBeacon beacon = entry.getValue();

            // outer wrapper for a single beacon info & icon
            LinearLayout llBeacon_Wrapper = new LinearLayout(this.context);
            llBeacon_Wrapper.setOrientation(LinearLayout.HORIZONTAL);

            // blukii icon
            ImageView blukii = new ImageView(this.context);
            Drawable icon = ResourcesCompat.getDrawable(this.context.getResources(), R.drawable.blukii, null);
            icon = resize(icon);
            blukii.setPadding(5,5,5,5);
            blukii.setImageDrawable(icon);

            // inner wrapper for single beacon information
            LinearLayout llBeacons = new LinearLayout(this.context);
            llBeacons.setOrientation(LinearLayout.VERTICAL);

            // wrapper for uuid
            LinearLayout llUUID = new LinearLayout(this.context);
            llUUID.setOrientation(LinearLayout.HORIZONTAL);

            // wrapper for major / minor
            LinearLayout llMajor_Minor = new LinearLayout(this.context);
            llMajor_Minor.setOrientation(LinearLayout.HORIZONTAL);

            // wrapper for rssi
            LinearLayout llRSSI = new LinearLayout(this.context);
            llRSSI.setOrientation(LinearLayout.HORIZONTAL);

            // add uuid info
            llUUID.addView(createTextView(this.context.getString(R.string.uuid_title)));
            llUUID.addView(createTextView(entry.getKey()));

            // add major / minor & rssi if object not null (active)
            if(beacon == null) {
                llMajor_Minor.addView(createTextView(this.context.getString(R.string.not_active)));
            } else {
                // add major / minor text views
                llMajor_Minor.addView(createTextView(this.context.getString(R.string.major_id_title)));
                llMajor_Minor.addView(createTextView(beacon.getMajor()));
                llMajor_Minor.addView(createTextView(this.context.getString(R.string.minor_id_title)));
                llMajor_Minor.addView(createTextView(beacon.getMinor()));

                // add rssi text views
                llRSSI.addView(createTextView(this.context.getString(R.string.rssi_title)));
                llRSSI.addView(createTextView(Integer.toString(beacon.getRSSI())));
            }

            // add beacon info to inner wrapper
            llBeacons.addView(llUUID);
            llBeacons.addView(llMajor_Minor);
            llBeacons.addView(llRSSI);

            // add icon & beacon info to outer wrapper
            llBeacon_Wrapper.addView(blukii);
            llBeacon_Wrapper.addView(llBeacons);

            // add all beacons to wrapper in xml
            llBeacons_Wrapper.addView(llBeacon_Wrapper);
        }

        return newConvertView;
    }

    /**
     * Check is child selectable, not used
     * @param groupPosition group position
     * @param childPosition child position
     * @return boolean
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    /**
     * Create a new text view programmatically
     * @param string (default) text for text view
     * @return TextView
     */
    public TextView createTextView(String string) {
        // get context density
        final float scale = this.context.getResources().getDisplayMetrics().density;
        // calculate padding in dp format
        int padding_5dp = (int) (5 * scale + 0.5f);

        // create new text view in context
        TextView textView = new TextView(this.context);
        // set padding in dp format
        textView.setPadding(padding_5dp,padding_5dp,padding_5dp,padding_5dp);
        // set text from passed string parameter
        textView.setText(string);

        return textView;
    }

    /**
     * Resize drawable
     * @param image Drawable object
     * @return BitmapDrawable
     */
    private Drawable resize(Drawable image) {
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 100, 100, false);
        return new BitmapDrawable(this.context.getResources(), bitmapResized);
    }
}
