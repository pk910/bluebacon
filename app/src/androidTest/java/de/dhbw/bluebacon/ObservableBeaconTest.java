package de.dhbw.bluebacon;

import android.test.InstrumentationTestCase;

import org.altbeacon.beacon.AltBeacon;
import org.altbeacon.beacon.Beacon;

import de.dhbw.bluebacon.model.ObservableBeacon;

/**
 * Created by David on 28.07.2015.
 */
public class ObservableBeaconTest extends InstrumentationTestCase {
    Beacon beacon1;
    ObservableBeacon observableBeacon1;

    public ObservableBeaconTest(){
        beacon1= new AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                .setId2("1").setId3("1").setRssi(-55).setTxPower(-55).build();
        observableBeacon1= new ObservableBeacon(beacon1, true);
    }

    public void testGetUUID() {
        assertEquals("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A-1-1", observableBeacon1.getFullUUID().toUpperCase());
    }
    public void testGetRSSI(){
        assertEquals(-55, observableBeacon1.getRSSI());
    }
    public void testGetDistance(){
        assertEquals(beacon1.getDistance(), observableBeacon1.getDistance());
    }
    public void testCommpareTo(){
        Beacon beaconA = new AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                .setId2("1").setId3("1").setRssi(-40).setTxPower(-55).build();
        Beacon beaconB = new AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                .setId2("1").setId3("1").setRssi(-50).setTxPower(-55).build();
        Beacon beaconC = new AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                .setId2("1").setId3("1").setRssi(-50).setTxPower(-55).build();
        ObservableBeacon observableBeaconA = new ObservableBeacon(beaconA, true);
        ObservableBeacon observableBeaconB = new ObservableBeacon(beaconB, true);
        ObservableBeacon observableBeaconC = new ObservableBeacon(beaconC, true);

        assertEquals(-10,observableBeaconA.compareTo(observableBeaconB));   //(-40 - (-50)) * -1 = -10
        assertEquals(10, observableBeaconB.compareTo(observableBeaconA));   //(-50 - (-40))*-1 = 10
        assertEquals(0, observableBeaconC.compareTo(observableBeaconB));    //(-50 - (-50))*-1 = 0
    }

    public void testcollectData(){
        Beacon beacon3= new AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997B")
                .setId2("1").setId3("1").setRssi(-20).setTxPower(-55).build();
        observableBeacon1.setBeacon(beacon3);
        observableBeacon1.setBeacon(beacon3);
        observableBeacon1.setBeacon(beacon3);
        observableBeacon1.setBeacon(beacon3);
        assertEquals(observableBeacon1.getRSSI(true),-27);
    }

}