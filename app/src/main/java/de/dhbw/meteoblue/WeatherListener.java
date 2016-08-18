package de.dhbw.meteoblue;

/**
 * Created by pk910 on 18.08.2016.
 */
public interface WeatherListener {
    public void OnWeatherReceived(WeatherData weather);
}
