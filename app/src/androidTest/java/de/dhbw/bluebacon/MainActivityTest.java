package de.dhbw.bluebacon;

import android.test.ActivityInstrumentationTestCase2;

import org.altbeacon.beacon.AltBeacon;
import org.altbeacon.beacon.Beacon;

import de.dhbw.bluebacon.model.ObservableBeacon;

/**
 * Created by manchnei on 31.07.2015.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    MainActivity activity;
    Beacon beacon1 = new AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-222222222222")
            .setId2("1").setId3("1").setRssi(-55).setTxPower(-55).build();
    Beacon beacon2 = new AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-111111111111")
            .setId2("1").setId3("1").setRssi(-30).setTxPower(-55).build();

    ObservableBeacon observableBeacon1 = new ObservableBeacon(beacon1, true);
    ObservableBeacon observableBeacon2 = new ObservableBeacon(beacon2, true);

    public MainActivityTest(){
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception{
        super.setUp();
        activity = getActivity();
    }




}
