package de.dhbw.bluebacon;

import android.test.InstrumentationTestCase;

import org.altbeacon.beacon.BeaconConsumer;

import java.util.ArrayList;

import de.dhbw.bluebacon.model.IObserver;
import de.dhbw.bluebacon.model.Machine;

/**
 * Created by manchnei on 28.07.2015.
 */
public class MachineTest extends InstrumentationTestCase{
     private Machine machine = new Machine(1, "Name", "Beschreibung", "-", "-");
     private BeaconConsumer boundComsumer = new MainActivity();
     private IObserver observer = (IObserver)boundComsumer;
     private ArrayList<IObserver> ArrayObserver = new ArrayList<IObserver>();



}
