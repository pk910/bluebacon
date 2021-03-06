package de.dhbw.bluebacon.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Machine class
 */
public class Machine implements IObserver, Comparable<Machine> {

    protected Map<String, ObservableBeacon> machineBeacons;
    protected Map<String, Tuple<Double, Double>> beaconPositions;
    protected Boolean useCleanedValues = true;
    protected Boolean useSimpleMode = false;
    protected Double simpleModeDistance = 2.;
    protected int id;
    protected String name;
    protected String description;
    protected String maintenanceState;
    protected String productionState;
    protected Double distance = 999.;

    /**
     * Constructor
     * @param name Name of the machine
     * @param description Description of the machine
     * @param maintenanceState Current maintenance state of the machine
     * @param productionState Current production state of the machine
     */
    public Machine(int id, String name, String description, String maintenanceState, String productionState) {
        this.machineBeacons = new HashMap<>();
        this.beaconPositions = new HashMap<>();
        this.id = id;
        this.name = name;
        this.description = description;
        this.maintenanceState = maintenanceState;
        this.productionState = productionState;
    }

    /**
     * Sets if value cleaning will be used or not
     * @param state Boolean true/false
     */
    public void setValueCleaning(Boolean state) {
        this.useCleanedValues = state;
    }

    /**
     * Returns if value cleaning will be used or not
     * @return Boolean true/false
     */
    public Boolean getValueCleaning() {
        return this.useCleanedValues;
    }

    /**
     * Getter for machine id
     * @return int Machine id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Getter for machine name
     * @return String Machine name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter for machine description
     * @return String Machine description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Getter for machine maintenance state
     * @return String Machine maintenance State
     */
    public String getMaintenanceState() {
        return this.maintenanceState;
    }

    /**
     * Getter for machine production state
     * @return String Machine production state
     */
    public String getProductionState() {
        return this.productionState;
    }

    /**
     * Getter for mapped beacons of the machine
     * @return List of mapped beacons
     */
    public Map<String, ObservableBeacon> getMappedBeacons() {
        List<ObservableBeacon> availableBeacons = new ArrayList<>();
        List<String> unavailableBeacons = new ArrayList<>();
        Map<String, ObservableBeacon> returnMap = new LinkedHashMap<>();

        for(Map.Entry<String, ObservableBeacon> entry : this.machineBeacons.entrySet()){
            if(entry.getValue() == null) {
                unavailableBeacons.add(entry.getKey());
            }else{
                availableBeacons.add(entry.getValue());
            }
        }

        Collections.sort(availableBeacons);

        for(ObservableBeacon beacon : availableBeacons) {
            returnMap.put(beacon.getFullUUID(), beacon);
        }

        for(String uuid : unavailableBeacons) {
            returnMap.put(uuid, null);
        }

        return returnMap;
    }

    /**
     * Registers beacon to machine (mapping)
     * @param uuid Full UUID of beacon
     */
    public void registerBeacon(String uuid) {
        this.registerBeacon(uuid, 0., 0.);
    }

    /**
     * Registers beacon to machine with coordinates (mapping)
     * @param uuid Full UUID of beacon
     * @param posX X position of coordinate
     * @param posY Y position of coordinate
     */
    public void registerBeacon(String uuid, Double posX, Double posY) {
        this.machineBeacons.put(uuid, null);
        this.beaconPositions.put(uuid, new Tuple<>(posX, posY));
    }

    /**
     * Updates a beacon object of the machine
     * @param uuid Full UUID of beacon
     * @param beacon ObservableBeacon object
     */
    public void setBeacon(String uuid, ObservableBeacon beacon) {
        this.machineBeacons.remove(uuid);
        this.machineBeacons.put(uuid, beacon);
    }

    /**
     * Getter for all available beacons of the machine sorted by rssi
     * @return List of available beacons
     */
    public List<ObservableBeacon> getTopBeacons() {
        List<ObservableBeacon> topBeacons = new ArrayList<>();

        for(Map.Entry<String, ObservableBeacon> entry : this.machineBeacons.entrySet()){
            if(entry.getValue() != null) {
                topBeacons.add(entry.getValue());
            }
        }

        Collections.sort(topBeacons);

        return topBeacons;
    }

    /**
     * Getter for top n available beacons of the machine sorted by rssi
     * @param count n
     * @return List of available beacons
     */
    public List<ObservableBeacon> getTopBeacons(Integer count) {
        List<ObservableBeacon> allTopBeacons = this.getTopBeacons();

        if(allTopBeacons.size() <= count) {
            return allTopBeacons;
        }else{
            List<ObservableBeacon> reducedTopBeacons = new ArrayList<>();

            for(Integer i = 0; i < count; i++) {
                reducedTopBeacons.add(allTopBeacons.get(i));
            }

            return reducedTopBeacons;
        }
    }

    /**
     * Recalculates distance of machine
     */
    protected void recalculate() {
        List<ObservableBeacon> availableBeacons = new ArrayList<>();

        for(Map.Entry<String, ObservableBeacon> entry : this.machineBeacons.entrySet()) {
            if (entry.getValue() != null) {
                availableBeacons.add(entry.getValue());
            }
        }

        Collections.sort(availableBeacons);

        switch(availableBeacons.size()) {
            case 0:
                this.distance = 999.;
                break;
            case 1:
                this.distance = availableBeacons.get(0).getDistance();
                break;
            default:
                this.distance = this.distanceCalculation(availableBeacons);
                break;
        }
    }

    /**
     * Perform distance calculation algorithm
     * @param availableBeacons List of available beacons
     * @return Distance in meters
     */
    protected double distanceCalculation(List<ObservableBeacon> availableBeacons) {
        double a;

        if(this.useSimpleMode) {
            a = this.simpleModeDistance;
        }else{
            Tuple<Double, Double> beacon1 = this.beaconPositions.get(availableBeacons.get(0).getFullUUID());
            Tuple<Double, Double> beacon2 = this.beaconPositions.get(availableBeacons.get(1).getFullUUID());
            a = Math.sqrt(Math.pow(beacon1.x - beacon2.x, 2.) + Math.pow(beacon1.y - beacon2.y, 2.));
        }

        double b = availableBeacons.get(0).getDistance();
        double c = availableBeacons.get(1).getDistance();
        double calcDistance = (b + c) * (1 - Math.pow(a / (b + c), 2.));

        if(calcDistance < 0) {
            return 0.;
        }else{
            return calcDistance;
        }
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
    }

    /**
     * Check if simple mode (2 beacon mode) is enabled
     * @return State of simple mode as boolean (true = enabled ; false = disabled)
     */
    public Boolean setSimpleMode() {
        return this.useSimpleMode;
    }

    /**
     * Getter for simple mode distance
     * @return Simple mode distance
     */
    public Double getSimpleModeDistance() {
        return this.simpleModeDistance;
    }

    /**
     * Getter for machine distance
     * @return Machine distance
     */
    public double getDistance() {
        return Math.round(this.distance * 100) / 100.;
    }

    /**
     * Getter for average rssi of machines beacons
     * @return Average rssi of machines beacons
     */
    public int getRSSI() {
        int avg = 0;
        int count = 0;

        for(Map.Entry<String, ObservableBeacon> entry : this.machineBeacons.entrySet()){
            if(entry.getValue() != null) {
                avg += entry.getValue().getRSSI();
                count++;
            }
        }

        if(count < 0) {
            return Math.round((float) avg / count);
        } else {
            return 0;
        }
    }

    /**
     * notify this object about changes in observed object
     * @param observable Observed object
     */
    @Override
    public void notify(IObservable observable) {
        ObservableBeacon beacon = (ObservableBeacon)observable;

        if(this.machineBeacons.get(beacon.getFullUUID()) == null) {
            this.machineBeacons.put(beacon.getFullUUID(), beacon);
        }

        this.recalculate();
    }

    /**
     * Compares this object to the specified object to determine their relative
     * order.
     *
     * @param another the object to compare to this instance.
     * @return a negative integer if this instance is less than {@code another};
     * a positive integer if this instance is greater than
     * {@code another}; 0 if this instance has the same order as
     * {@code another}.
     */
    @Override
    public int compareTo(@NonNull Machine another) {
        if(this.useSimpleMode) {
            return this.getRSSI() - another.getRSSI();
        } else {
            return (int) (this.getDistance() * 100) - (int) (another.getDistance() * 100);
        }
    }

    /**
     * Checks if this object is equal to the specified object.
     *
     * @param obj the object to compare to this instance.
     * @return boolean {@code obj};
     * @throws ClassCastException if {@code another} cannot be converted into something
     *                            comparable to {@code this} instance.
     */
    @Override
    public boolean equals(Object obj) throws ClassCastException {
        return obj != null && getClass() == obj.getClass() && this.compareTo((Machine) obj) == 0;
    }

    @Override
    public int hashCode() {
        // We can do this as long as Machine is never used as a key for a
        // Map object such as HashSet, LinkedHashSet, HashMap, Hashtable, or WeakHashMap,
        // or in any other situation where hashCode() will be called. The theoretic definition
        // is that whenever a.equals(b), then a.hashCode() must be same as b.hashCode().
        // We provide our own implementation of equals() though, so we shouldn't use Object.hashCode().
        throw new UnsupportedOperationException("hashCode() not supported.");
    }

}
