package de.dhbw.bluebacon.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.dhbw.bluebacon.R;
import de.dhbw.bluebacon.model.ObservableBeacon;

/**
 * Beacon Adapter class
 */
public class BeaconAdapter extends BaseAdapter{

    List<ObservableBeacon> beacons = new ArrayList<>();
    LayoutInflater inflater;
    Context context;

    /**
     * Constructor
     * @param context Activity Context
     * @param beacons Beacon List
     */
    public BeaconAdapter(Context context, List<ObservableBeacon> beacons) {
        this.context = context;
        this.beacons = beacons;
        inflater = LayoutInflater.from(this.context);
    }

    /**
     * Get number of Beacon objects
     * @return int
     */
    @Override
    public int getCount() {
        return beacons.size();
    }

    /**
     * Get Beacon object in array
     * @param i position
     * @return Beacon
     */
    @Override
    public Object getItem(int i) {
        return beacons.get(i);
    }

    /**
     * Get item id, not used
     * @param i position
     * @return 0
     */
    @Override
    public long getItemId(int i) {
        return 0;
    }

    /**
     * Get List View with Beacons
     * @param i position
     * @param view View
     * @param parent parent
     * @return View
     */
    @Override
    public View getView(int i, View view, ViewGroup parent) {
        MyViewHolder myViewHolder;
        View newView;

        if(view == null){
            newView = inflater.inflate(R.layout.radar_frag_item,parent,false);
            myViewHolder = new MyViewHolder(newView);
            newView.setTag(myViewHolder);
        } else {
            newView = view;
            myViewHolder = (MyViewHolder) newView.getTag();
        }

        ObservableBeacon currentBeacon = (ObservableBeacon) getItem(i);
        myViewHolder.tvUUID.setText(currentBeacon.getFullUUID());
        myViewHolder.tvRSSI.setText(String.format(Locale.getDefault(), "%d", currentBeacon.getRSSI()));
        myViewHolder.tvDistance.setText(String.format(Locale.getDefault(), "%f", currentBeacon.getDistance()));
        myViewHolder.ivIcon.setImageResource(R.drawable.blukii);

        myViewHolder.tvMajor.setText(currentBeacon.getMajor());
        myViewHolder.tvMinor.setText(currentBeacon.getMinor());

        return newView;
    }

    private static class MyViewHolder{
        TextView tvUUID, tvRSSI, tvDistance,tvMinor,tvMajor;
        ImageView ivIcon;

        public MyViewHolder(View item) {
            tvUUID = (TextView) item.findViewById(R.id.tvUUID);
            tvRSSI = (TextView) item.findViewById(R.id.tvRSSI);
            tvDistance = (TextView) item.findViewById(R.id.tvDistance);
            ivIcon = (ImageView) item.findViewById(R.id.ivIcon);
            tvMinor = (TextView) item.findViewById(R.id.tvMINOR);
            tvMajor = (TextView) item.findViewById(R.id.tvMAJOR);
        }
    }
}
