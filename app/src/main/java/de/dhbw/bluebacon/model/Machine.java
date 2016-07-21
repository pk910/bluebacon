package de.dhbw.bluebacon.model;

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

    /**
     * Tupel class for coordinates
     * @param <X> Datatype of X coordinate
     * @param <Y> Datatype of Y coordinate
     */
    public class Tuple<X, Y> {
        protected X x;
        protected Y y;

        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }

        public X getX() {
            return this.x;
        }

        public Y getY() {
            return this.y;
        }
    }

    protected List<IObserver> observer;
    protected Map<String, ObservableBeacon> machineBeacons;
    protected Map<String, Tuple<Double, Double>> beaconPositions;
    protected Boolean useCleanedValues = true;
    protected Boolean useSimpleMode = false;
    protected Double simpleModeDistance = 2.;
    protected Integer id;
    protected String name;
    protected String description;
    protected String maintenanceState;
    protected String productionState;
    protected Double distance = 999.;

    /**
     * Constructor
     * @param id ID of the machine
     * @param name Name of the machine
     * @param description Description of the machine
     * @param maintenanceState Current maintenance state of the machine
     * @param productionState Current production state of the machine
     */
    public Machine(Integer id, String name, String description, String maintenanceState, String productionState) {
        this.observer = new ArrayList<IObserver>();
        this.machineBeacons = new HashMap<String, ObservableBeacon>();
        this.beaconPositions = new HashMap<String, Tuple<Double, Double>>();
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
    public void SetValueCleaning(Boolean state) {
        this.useCleanedValues = state;
    }

    /**
     * Returns if value cleaning will be used or not
     * @return Boolean true/false
     */
    public Boolean GetValueCleaning() {
        return this.useCleanedValues;
    }

    /**
     * Getter for machine name
     * @return String Machine name
     */
    public String GetName() {
        return this.name;
    }

    /**
     * Getter for machine description
     * @return String Machine description
     */
    public String GetDescription() {
        return this.description;
    }

    /**
     * Getter for machine maintenance state
     * @return String Machine maintenance State
     */
    public String GetMaintenanceState() {
        return this.maintenanceState;
    }

    /**
     * Getter for machine production state
     * @return String Machine production state
     */
    public String GetProductionState() {
        return this.productionState;
    }

    /**
     * Getter for mapped beacons of the machine
     * @return List of mapped beacons
     */
    public Map<String, ObservableBeacon> GetMappedBeacons() {
        List<ObservableBeacon> availableBeacons = new ArrayList<ObservableBeacon>();
        List<String> unavailableBeacons = new ArrayList<String>();
        Map<String, ObservableBeacon> returnMap = new LinkedHashMap<String, ObservableBeacon>();

        for(String uuid : this.machineBeacons.keySet()) {
            if(this.machineBeacons.get(uuid) != null) {
                availableBeacons.add(this.machineBeacons.get(uuid));
            }else{
                unavailableBeacons.add(uuid);
            }
        }

        Collections.sort(availableBeacons);

        for(ObservableBeacon beacon : availableBeacons) {
            returnMap.put(beacon.GetFullUUID(), beacon);
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
    public void RegisterBeacon(String uuid) {
        this.RegisterBeacon(uuid, 0., 0.);
    }

    /**
     * Registers beacon to machine with coordinates (mapping)
     * @param uuid Full UUID of beacon
     * @param posX X position of coordinate
     * @param posY Y position of coordinate
     */
    public void RegisterBeacon(String uuid, Double posX, Double posY) {
        this.machineBeacons.put(uuid, null);
        this.beaconPositions.put(uuid, new Tuple<Double, Double>(posX, posY));
    }

    /**
     * Updates a beacon object of the machine
     * @param uuid Full UUID of beacon
     * @param beacon ObservableBeacon object
     */
    public void SetBeacon(String uuid, ObservableBeacon beacon) {
        this.machineBeacons.remove(uuid);
        this.machineBeacons.put(uuid, beacon);
    }

    /**
     * Getter for all available beacons of the machine sorted by rssi
     * @return List of available beacons
     */
    public List<ObservableBeacon> GetTopBeacons() {
        List<ObservableBeacon> topBeacons = new ArrayList<ObservableBeacon>();

        for(String uuid : this.machineBeacons.keySet()) {
            if(this.machineBeacons.get(uuid) != null) {
                topBeacons.add(this.machineBeacons.get(uuid));
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
    public List<ObservableBeacon> GetTopBeacons(Integer count) {
        List<ObservableBeacon> allTopBeacons = this.GetTopBeacons();

        if(allTopBeacons.size() <= count) {
            return allTopBeacons;
        }else{
            List<ObservableBeacon> reducedTopBeacons = new ArrayList<ObservableBeacon>();

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
        List<ObservableBeacon> availableBeacons = new ArrayList<ObservableBeacon>();

        for(String uuid : this.machineBeacons.keySet()) {
            if(this.machineBeacons.get(uuid) != null) {
                availableBeacons.add(this.machineBeacons.get(uuid));
            }
        }

        Collections.sort(availableBeacons);

        switch(availableBeacons.size()) {
            case 0:
                this.distance = 999.;
                break;
            case 1:
                this.distance = availableBeacons.get(0).GetDistance();
                break;
            default:
                this.distance = this.distanceCalculation(availableBeacons);
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
            Tuple<Double, Double> beacon1 = this.beaconPositions.get(availableBeacons.get(0).GetFullUUID());
            Tuple<Double, Double> beacon2 = this.beaconPositions.get(availableBeacons.get(1).GetFullUUID());
            a = Math.sqrt(Math.pow(beacon1.x - beacon2.x, 2.) + Math.pow(beacon1.y - beacon2.y, 2.));
        }

        double b = availableBeacons.get(0).GetDistance();
        double c = availableBeacons.get(1).GetDistance();
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
    public void SetSimpleMode(Boolean state) {
        this.SetSimpleMode(state, 2.);
    }

    /**
     * Enable simple mode (2 beacon mode) with custom distance or disable it
     * @param state true = enable ; false = disable
     * @param distance distance between 2 beacons
     */
    public void SetSimpleMode(Boolean state, double distance) {
        this.useSimpleMode = state;
        this.simpleModeDistance = distance;
    }

    /**
     * Check if simple mode (2 beacon mode) is enabled
     * @return State of simple mode as boolean (true = enabled ; false = disabled)
     */
    public Boolean GetSimpleMode() {
        return this.useSimpleMode;
    }

    /**
     * Getter for simple mode distance
     * @return Simple mode distance
     */
    public Double GetSimpleModeDistance() {
        return this.simpleModeDistance;
    }

    /**
     * Getter for machine distance
     * @return Machine distance
     */
    public double GetDistance() {
        return Math.round(this.distance * 100) / 100.;
    }

    /**
     * Getter for average rssi of machines beacons
     * @return Average rssi of machines beacons
     */
    public int GetRSSI() {
        int avg = 0;
        int count = 0;

        for(String uuid : this.machineBeacons.keySet()) {
            if(this.machineBeacons.get(uuid) != null) {
                avg += this.machineBeacons.get(uuid).GetRSSI();
                count++;
            }
        }

        if(count < 0)
            return Math.round((float)avg / count);
        else
            return 0;
    }

    /**
     * notify this object about changes in observed object
     * @param observable Observed object
     */
    @Override
    public void notify(IObservable observable) {
        ObservableBeacon beacon = (ObservableBeacon)observable;

        if(this.machineBeacons.get(beacon.GetFullUUID()) == null) {
            this.machineBeacons.put(beacon.GetFullUUID(), beacon);
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
     * @throws ClassCastException if {@code another} cannot be converted into something
     *                            comparable to {@code this} instance.
     */
    @Override
    public int compareTo(Machine another) {
        if(!this.useSimpleMode)
            return (int) (this.GetDistance() * 100) - (int) (another.GetDistance() * 100);
        else
            return this.GetRSSI() - another.GetRSSI();
    }

}
