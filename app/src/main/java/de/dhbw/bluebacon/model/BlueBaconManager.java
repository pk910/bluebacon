package de.dhbw.bluebacon.model;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RangedBeacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.dhbw.bluebacon.BuildConfig;
import de.dhbw.bluebacon.MainActivity;

/**
 * BlueBaconManager class
 * manager registers activity as observers on machines or observableBeacons
 */
public class BlueBaconManager implements IObservable {

    protected static final Integer FOREGROUNDSCANPERIOD = 150;
    protected static final Integer DELETEBEACONAFTER = 10000;
    protected static final String LOG_TAG = "DHBW BlueBaconManager";

    protected BeaconConsumer boundConsumer;
    protected BeaconManager beaconManager;
    protected ArrayList<Machine> machines;
    protected Map<String, Machine> beaconUuidMachineMapping;
    protected Map<String, ObservableBeacon> observableBeacons;
    protected Region region = new Region("region", null, null, null);
    protected List<IObserver> observers;
    protected BlueBaconManager self = this;
    protected Boolean useCleanedValues = true;
    protected Boolean useSimpleMode = false;
    protected Double simpleModeDistance = 2.;

    /**
     * RangeNotifier is triggered if beacons are discovered
     */
    protected RangeNotifier rangeNotifier = new BlueBaconRangeNotifier();

    /**
     * Implementation of AltBeacon RangeNotifier
     */
    private class BlueBaconRangeNotifier implements RangeNotifier {
        /**
         * Triggered every 150ms
         * @param beacons Beacons in range
         * @param region Ranged region
         */
        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
            Log.d(LOG_TAG, "--- RangeNotifier triggered ---");

            Date minTime = new Date();
            minTime.setTime(minTime.getTime() - DELETEBEACONAFTER); //erst wenn Beacon 2 Sekunden vom Radar verschwunden ist löschen

            for(Beacon beacon : beacons) {
                String uuid = beacon.getId1() + "-" + beacon.getId2() + "-" + beacon.getId3();
                ObservableBeacon observableBeacon;

                if(observableBeacons.containsKey(uuid)) {
                    observableBeacon = observableBeacons.get(uuid);
                    observableBeacon.setBeacon(beacon);
                }else{
                    Log.d(LOG_TAG, "Previosly unknown beacon '" + uuid + "' now in Range");
                    observableBeacon = new ObservableBeacon(beacon, useCleanedValues);
                    observableBeacons.put(uuid, observableBeacon);
                }

                if(beaconUuidMachineMapping.containsKey(uuid)) {
                    Machine machine = beaconUuidMachineMapping.get(uuid);

                    if(!observableBeacon.isSubscribed(machine)) {
                        Log.d(LOG_TAG, "Beacon '" + uuid + "' subscribed to machine '" + machine.getName() + "'");
                        observableBeacon.subscribe(machine);
                    }
                }
            }

            List<String> deleteList = new ArrayList<>();

            for(ObservableBeacon beacon : observableBeacons.values()) {
                if(beacon.getLastUpdate().before(minTime)) {
                    Log.d(LOG_TAG, "Beacon '" + beacon.getFullUUID() + "' out of Range");
                    deleteList.add(beacon.getFullUUID());
                }
            }

            for(String uuid : deleteList) {
                observableBeacons.remove(uuid);

                if(beaconUuidMachineMapping.containsKey(uuid)) {
                    beaconUuidMachineMapping.get(uuid).setBeacon(uuid, null);
                }
            }

            for(IObserver observer : observers) {
                observer.notify(self);
            }
        }
    }

    /**
     * Async task for loading machine data
     */
    private class GetMachines extends AsyncTask<Void, Void, Void> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog((Activity)boundConsumer);
            pDialog.setMessage("Loading machines...");
            pDialog.setCancelable(false);
            pDialog.show();
            machines.clear();
            beaconUuidMachineMapping.clear();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // this now only accesses the local SQLite DB
            ArrayList<BeaconData> beaconData = new ArrayList<>();
            beaconData.addAll(((MainActivity)boundConsumer).getBeaconDB().getBeacons());

            ArrayList<Machine> machineData = new ArrayList<>();
            machineData.addAll(((MainActivity)boundConsumer).getBeaconDB().getMachines());


            for(Machine machine : machineData) {
                machines.add(machine);
                for(BeaconData beacon : beaconData){
                    if(beacon.machineid == machine.getId()){
                        machine.registerBeacon(beacon.uuid, beacon.posX, beacon.posY);
                        beaconUuidMachineMapping.put(beacon.uuid, machine);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }

    }

    /**
     * Constructor
     * @param consumer Consumer object (Activity implementing BeaconConsumer)
     */
    public BlueBaconManager(BeaconConsumer consumer) {
        this.boundConsumer = consumer;
        this.beaconUuidMachineMapping = new HashMap<>();
        this.machines = new ArrayList<>();
        this.observableBeacons = new HashMap<>();
        this.observers = new ArrayList<>();

        this.loadMachines();

        RangedBeacon.setSampleExpirationMilliseconds(5000);
        this.beaconManager = BeaconManager.getInstanceForApplication((Activity)this.boundConsumer);
        this.beaconManager.setForegroundScanPeriod(FOREGROUNDSCANPERIOD);
        this.beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        this.beaconManager.bind(this.boundConsumer);
        this.beaconManager.setRangeNotifier(this.rangeNotifier);
    }

    /**
     * Enables or disables value cleaning (Messschleife)
     * @param state Boolean
     */
    public void setValueCleaning(Boolean state) {
        this.useCleanedValues = state;

        for(ObservableBeacon beacon : this.observableBeacons.values()) {
            beacon.setValueCleaning(state);
        }

        for(int i = 0; i < this.machines.size(); i++){
            this.machines.get(i).setValueCleaning(state);
        }
    }

    /**
     * Checks if value cleaning mdoe enabled
     * @return State of value cleaning mode
     */
    public Boolean getValueCleaning() {
        return this.useCleanedValues;
    }

    /**
     * Enable simple mode (2 beacon mode) with default distance (2.0m) or simply disable it
     * @param state true = enable ; false = disable
     */
    public void setSimpleMode(Boolean state) {
        this.setSimpleMode(state, 2.);
    }

    /**
     * Enable simple mode (2 beacon mode) with custom distance or disable it
     * @param state true = enable ; false = disable
     * @param distance distance between 2 beacons
     */
    public void setSimpleMode(Boolean state, double distance) {
        this.useSimpleMode = state;
        this.simpleModeDistance = distance;

        for(int i = 0; i < this.machines.size(); i++){
            this.machines.get(i).setSimpleMode(state, distance);
        }
    }

    /**
     * Check if simple mode (2 beacon mode) is enabled
     * @return State of simple mode as boolean (true = enabled ; false = disabled)
     */
    public Boolean getSimpleMode() {
        return this.useSimpleMode;
    }

    /**
     * Get the configured beacon distance for the simple mode (2 beacon mode)
     * @return double
     */
    public double getSimpleModeDistance() {
        return this.simpleModeDistance;
    }

    /**
     * Unbind consumer
     */
    public void destroy() {
        this.beaconManager.unbind(this.boundConsumer);
    }

    /**
     * Set beacon manager to background mode
     */
    public void pause() {
        if (beaconManager.isBound(this.boundConsumer)) {
            beaconManager.setBackgroundMode(true);
        }
    }

    /**
     * Set beacon manager to foreground mode
     */
    public void resume() {
        if (beaconManager.isBound(this.boundConsumer)) {
            beaconManager.setBackgroundMode(false);
        }
    }

    /**
     * Start ranging beacons
     */
    public void start() {
        try {
            this.beaconManager.startRangingBeaconsInRegion(this.region);
        }catch(Exception ex) {
            if(BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "--- Error while starting ranging ---");
                Log.d(LOG_TAG, ex.getMessage());
            }
        }
    }

    /**
     * Stop ranging beacons
     */
    public void stop() {
        try {
            this.beaconManager.stopRangingBeaconsInRegion(this.region);
        }catch(Exception ex) {
            if(BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "--- Error while stopping ranging ---");
                Log.d(LOG_TAG, ex.getMessage());
            }
        }
    }

    /**
     * Load machines from backend
     */
    public final void loadMachines() {
        new GetMachines().execute();
    }

    /**
     * Return beacons in range
     * @return Beacons in range
     */
    public Map<String, ObservableBeacon> getBeacons() {
        return this.observableBeacons;
    }

    /**
     * Return machines
     */
    public ArrayList<Machine> getMachines() {
        return this.machines;
    }

    /**
     * subscribe observing object for changes of this object
     * @param observer Observing object
     */
    @Override
    public void subscribe(IObserver observer) {
        this.observers.add(observer);
    }

    /**
     * unsubscribe observing object for changes of this object
     * @param observer Observing object
     */
    @Override
    public void unsubscribe(IObserver observer) {
        this.observers.remove(observer);
    }

    /**
     * Checks if the observer is already subscribed
     *
     * @param observer IObserver
     * @return True if observer is already subscribed
     */
    @Override
    public Boolean isSubscribed(IObserver observer) {
        //Not used
        return false;
    }

}
