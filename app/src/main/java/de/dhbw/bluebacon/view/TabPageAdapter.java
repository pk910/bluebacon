package de.dhbw.bluebacon.view;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import de.dhbw.bluebacon.R;

/**
 * TabPageAdapter class
 */
public class TabPageAdapter extends FragmentStatePagerAdapter {

    Context ctx;

    public TabPageAdapter(FragmentManager fm, Context ctx) {
        super(fm);
        this.ctx = ctx;
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

    /**
     * Get Fragment title
     * @return CharSequence
     */
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return this.ctx.getString(R.string.action_beacons);
            case 1:
                return this.ctx.getString(R.string.action_machines);
            case 2:
                return this.ctx.getString(R.string.action_settings);
            default:
                break;
        }
        return null;
    }
}
