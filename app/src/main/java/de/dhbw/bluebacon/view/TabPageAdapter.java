package de.dhbw.bluebacon.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * TabPageAdapter class
 */
public class TabPageAdapter extends FragmentStatePagerAdapter {

    public TabPageAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * Get Fragment items
     * @param position position
     * @return null
     */
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                //Fragment for beacon radar
                return new BeaconRadar();
            case 1:
                //Fragment for machine radar
                return new MachineRadar();
            case 2:
                //Fragment for setup page
                return new MachineSetup();
            default:
                break;
        }
        return null;
    }

    /**
     * Get Fragment count
     * @return int
     */
    @Override
    public int getCount() {
        //Number of Tabs
        return 3;
    }
}
