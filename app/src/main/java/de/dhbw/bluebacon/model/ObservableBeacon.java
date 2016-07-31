package de.dhbw.bluebacon.model;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ObservableBeacon class
 */
public class ObservableBeacon implements IObservable, Comparable<ObservableBeacon> {

    protected static final Integer AVGCOUNT = 5;

    protected String uuid;
    protected List<IObserver> observer;
    protected Beacon altBeacon;
    protected Date lastUpdate;
    protected Map<Integer, Integer> collectedRSSIs;
    protected Map<Integer, Double> collectedDistances;
    protected Integer cleanedRSSI = 0;
    protected Double cleanedDistance = 0.;
    protected Boolean useCleanedValues = true;

    /**
     * Constructor
     * @param altBeacon Beacon object of AltBeacon library
     * @param useCleanedValues Should values be cleaned?
     */
    public ObservableBeacon(Beacon altBeacon, Boolean useCleanedValues) {
        this.observer = new ArrayList<>();
        this.collectedRSSIs = new HashMap<>();
        this.collectedDistances = new HashMap<>();
        this.useCleanedValues = useCleanedValues;
        this.uuid = altBeacon.getId1() + "-" + altBeacon.getId2() + "-" + altBeacon.getId3();
        this.setBeacon(altBeacon);
    }

    /**
     * Enables or disables value cleaning (Messschleife)
     * @param state Boolean
     */
    public void setValueCleaning(Boolean state) {
        this.useCleanedValues = state;
    }

    /**
     * Checks if value cleaning mdoe enabled
     * @return State of value cleaning mode
     */
    public Boolean getValueCleaning() {
        return this.useCleanedValues;
    }

    /**
     * Update beacon object of AltBeacon library
     * @param altBeacon Beacon object of AltBeacon library
     */
    public void setBeacon(Beacon altBeacon) {
        this.lastUpdate = new Date();
        Beacon prevBeacon = this.altBeacon;
        this.altBeacon = altBeacon;

        if(prevBeacon == null) {
            this.cleanedRSSI = altBeacon.getRssi();
            this.cleanedDistance = altBeacon.getDistance();
        }

        this.collectData();

        for(IObserver currentObserver : this.observer) {
            currentObserver.notify(this);
        }
    }

    /**
     * Führt Messwertkorrektur durch (Messschleife)
     */
    protected void collectData() {
        this.collectedRSSIs.put(this.collectedRSSIs.size(), this.altBeacon.getRssi());
        this.collectedDistances.put(this.collectedDistances.size(), this.altBeacon.getDistance());

        if(this.collectedRSSIs.size() >= AVGCOUNT) {
            Integer rssiSum = 0;

            for(Integer value : this.collectedRSSIs.values()) {
                rssiSum += value;
            }

            this.cleanedRSSI = Math.round((float)rssiSum / this.collectedRSSIs.size());
            this.collectedRSSIs.clear();
        }

        if(this.collectedDistances.size() >= AVGCOUNT) {
            Double distanceSum = 0.;

            for(Double value : this.collectedDistances.values()) {
                distanceSum += value;
            }

            this.cleanedDistance = distanceSum / this.collectedDistances.size();
            this.collectedDistances.clear();
        }
    }

    /**
     * Getter for last update datetime of ObservableBeacon
     * @return Datetime of last update
     */
    public Date getLastUpdate() {
        return this.lastUpdate;
    }

    /**
     * Getter for full UUID of beacon
     * @return Full UUID of beacon
     */
    public String getFullUUID() {
        return this.uuid;
    }

    /**
     * Getter for beacon rssi
     * @return Beacon rssi
     */
    public int getRSSI() {
        return this.getRSSI(this.useCleanedValues);
    }

    /**
     * Getter for beacon rssi
     * @param cleaned Should value be cleaned?
     * @return Beacon rssi
     */
    public int getRSSI(Boolean cleaned) {
        if(cleaned) {
            return this.cleanedRSSI;
        }else{
            return this.altBeacon.getRssi();
        }
    }

    /**
     * Getter for beacon distance
     * @return Beacon distance
     */
    public double getDistance() {
        return this.getDistance(this.useCleanedValues);
    }

    /**
     * Getter for beacon distance
     * @param cleaned Should value be cleaned?
     * @return Beacon distance
     */
    public double getDistance(Boolean cleaned) {
        if(cleaned) {
            return Math.round(this.cleanedDistance * 100) / 100.;
        }else{
            return Math.round(this.altBeacon.getDistance() * 100) / 100.;
        }
    }

    /**
     * Getter for beacons major ID
     * @return Beacon major ID
     */
    public String getMajor() {
        return this.altBeacon.getId2().toString();
    }

    /**
     * Getter for beacons minor ID
     * @return Beacon minor ID
     */
    public String getMinor() {
        return this.altBeacon.getId3().toString();
    }

    /**
     * subscribe observing object for changes of this ObservableBeacon
     * @param observer Observing object
     */
    @Override
    public void subscribe(IObserver observer) {
        this.observer.add(observer);
        observer.notify(this);
    }

    /**
     * unsubscribe observing object for changes of this ObservableBeacon
     * @param observer Observing object
     */
    @Override
    public void unsubscribe(IObserver observer) {
        this.observer.remove(observer);
    }

    /**
     * Checks if the observer is already subscribed
     *
     * @param observer
     * @return True if observer is already subscribed
     */
    @Override
    public Boolean isSubscribed(IObserver observer) {
        return this.observer.contains(observer);
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
    public int compareTo(ObservableBeacon another) {
        return (this.getRSSI() - another.getRSSI()) * -1;
    }

}
