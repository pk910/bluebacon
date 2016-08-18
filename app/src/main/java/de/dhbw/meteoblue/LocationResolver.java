package de.dhbw.meteoblue;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;

import de.dhbw.bluebacon.MainActivity;

/**
 * Created by pk910 on 18.08.2016.
 */
public class LocationResolver implements LocationListener {
    private Activity appActivity;
    private LocationManager locationManager;
    private ArrayList<LocationListener> locationListeners = new ArrayList<LocationListener>();
    private Location currentBestLocation;
    private boolean isRunning = false;

    public LocationResolver(Activity activity) {
        appActivity = activity;
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        startLocationListener();
    }

    public void addLocationListener(LocationListener listener) {
        locationListeners.add(listener);
        if(currentBestLocation != null)
            listener.onLocationChanged(currentBestLocation);
    }

    public void delLocationListener(LocationListener listener) {
        locationListeners.remove(listener);
    }

    public void startLocationListener() {
        if (ActivityCompat.checkSelfPermission(appActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(appActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(appActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MainActivity.PERMISSIONS_REQUEST_LOCATION_RESOLVER);
        } else if(!isRunning) {
            isRunning = true;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 20, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 50, this);
        }
    }

    public void stopLocationListener() {
        if(isRunning) try {
            locationManager.removeUpdates(this);
            isRunning = false;
        } catch(SecurityException e) {
        }
    }

    /** Determines whether one location reading is better than the current location fix
     * @param location  The new location that you want to evaluate
     * @param currentBestLocation  The current location fix, to which you want to compare the new one.
     */
    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > 60*2*1000;
        boolean isSignificantlyOlder = timeDelta < -60*2*1000;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location,
        // because the user has likely moved.
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse.
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (currentBestLocation == null || isBetterLocation(location, currentBestLocation)) {
            currentBestLocation = location;
            for(LocationListener listener : locationListeners)
                listener.onLocationChanged(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        for(LocationListener listener : locationListeners)
            listener.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(String provider) {
        for(LocationListener listener : locationListeners)
            listener.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        for(LocationListener listener : locationListeners)
            listener.onProviderDisabled(provider);
    }
}
