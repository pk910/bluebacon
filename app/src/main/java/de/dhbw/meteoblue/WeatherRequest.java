package de.dhbw.meteoblue;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by pk910 on 18.08.2016.
 */
public class WeatherRequest extends JsonRequestHelper implements LocationListener {
    private static final String METEOBLUE_APIKEY = "f0daa1b8a7a1";

    private boolean requestRunning;
    private boolean requestPending;
    private Location currentLocation, weatherLocation;
    private WeatherData weatherData;
    private ArrayList<WeatherListener> weatherListener = new ArrayList<>();

    public void addWeatherListener(WeatherListener listener) {
        weatherListener.add(listener);
    }

    public void delWeatherListener(WeatherListener listener) {
        weatherListener.remove(listener);
    }

    private void requestWeather() {
        if(requestRunning)
            return;
        requestPending = false;
        requestRunning = true;
        weatherLocation = currentLocation;
        weatherData = null;

        String lon = Double.toString(currentLocation.getLongitude());
        String lat = Double.toString(currentLocation.getLatitude());
        String asl = Integer.toString((int)currentLocation.getAltitude());

        StringBuilder url = new StringBuilder("http://my.meteoblue.com/packages/basic-day");
        url.append("?lat="+lat);
        url.append("&lon="+lon);
        url.append("&asl="+asl);
        url.append("&tz=Europe%2FBerlin");
        url.append("&apikey="+METEOBLUE_APIKEY);
        url.append("&temperature=C");
        url.append("&windspeed=ms-1");
        url.append("&winddirection=degree");
        url.append("&precipitationamount=mm");
        url.append("&timeformat=iso8601");
        url.append("&format=json");

        requestJsonFromWeb(url.toString());
    }

    public void requestWeather(boolean refresh) {
        if(currentLocation == null) {
            requestPending = true;
        }
        else if(weatherLocation != null && weatherLocation.distanceTo(currentLocation) < 1000 && !refresh) {
            for (WeatherListener listener : weatherListener) {
                listener.OnWeatherReceived(weatherData);
            }
        }
        else {
            requestWeather();
        }
    }

    @Override
    protected void onJsonReceived(JSONObject json) {
        requestRunning = false;

        // parse weather data
        weatherData = new WeatherData(json);

        for (WeatherListener listener : weatherListener) {
            listener.OnWeatherReceived(weatherData);
        }
    }

    @Override
    protected void onJsonReceived(JSONArray json) {
    }

    @Override
    protected void onTextReceived(String response) {

    }

    @Override
    protected void onJsonRequestFail(int statusCode, String errorMessage) {
        requestRunning = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        if(requestPending) {
            requestWeather();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
