package de.dhbw.bluebacon.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import de.dhbw.bluebacon.MainActivity;
import de.dhbw.bluebacon.R;
import de.dhbw.bluebacon.model.BlueBaconManager;
import de.dhbw.bluebacon.model.DiscoveryBroadcaster;
import de.dhbw.bluebacon.model.DiscoveryListener;
import de.dhbw.bluebacon.model.JSONLoader;
import de.dhbw.meteoblue.WeatherData;
import de.dhbw.meteoblue.WeatherListener;
import de.dhbw.meteoblue.WeatherRequest;

/**
 * Created by pk910 on 18.08.2016.
 */
public class WeatherTest extends Fragment implements WeatherListener {
    MainActivity mainActivity;
    BlueBaconManager blueBaconManager;
    View currentView;
    WeatherRequest weather;

    public static final String LOG_TAG = "DHBW MachineSetup";

    /**
     * Attach MainActivity to Fragment
     * @param context Context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a;

        if (context instanceof Activity){
            a=(Activity) context;
            mainActivity = (MainActivity) a;
        }
    }

    /**
     * Create Fragment
     * @param savedInstanceState saved instance state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blueBaconManager = mainActivity.getBlueBaconManager();

        weather = new WeatherRequest();
        weather.addWeatherListener(this);
        mainActivity.getLocationResolver().addLocationListener(weather);
    }

    /**
     * Create view for Fragment
     * @param inflater inflater
     * @param container container
     * @param savedInstanceState saved instance state
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //inflate fragment
        currentView = inflater.inflate(R.layout.weather_frag, container, false);
        weather.requestWeather(false);

        Location loc = mainActivity.getLocationResolver().getLastKnownLocation();
        if(loc != null && weather.getWeatherLocation() == null)
            weather.onLocationChanged(loc);

        return currentView;
    }

    @Override
    public void OnWeatherReceived(WeatherData weather) {
        TextView weatherView = (TextView) currentView.findViewById(R.id.weatherData);

        StringBuilder strb = new StringBuilder();
        strb.append(weather.getDate());
        strb.append("\n");
        strb.append(weather.getCodeDesc());
        strb.append("\n");
        strb.append("Temp: ");
        strb.append(weather.getTempAvg());
        strb.append(" (Min: ");
        strb.append(weather.getTempMin());
        strb.append(", Max: ");
        strb.append(weather.getTempMax());
        strb.append(")\n");
        strb.append("Wind: ");
        strb.append(weather.getWindAvg());
        strb.append(" ");
        strb.append(weather.getWindDir());
        strb.append(" (Min: ");
        strb.append(weather.getWindMin());
        strb.append(", Max: ");
        strb.append(weather.getWindMax());
        strb.append(")\n");
        strb.append("Precipitation Probability: ");
        strb.append(weather.getRainPossibility());
        strb.append("%\n");
        strb.append("Rel. Humidity: ");
        strb.append(weather.getHumidityAvg());
        strb.append(" (Min: ");
        strb.append(weather.getHumidityMin());
        strb.append(", Max: ");
        strb.append(weather.getHumidityMax());
        strb.append(")\n");

        ImageView weatherImageView = (ImageView) currentView.findViewById(R.id.weatherImage);
        weatherImageView.setImageResource(weather.getCodeDayPic());

        weatherView.setText(strb.toString());


        weather = weather.getNextDay();
        if(weather == null)
            return;

        weatherView = (TextView) currentView.findViewById(R.id.weatherData2);

        strb = new StringBuilder();
        strb.append(weather.getDate());
        strb.append("\n");
        strb.append(weather.getCodeDesc());
        strb.append("\n");
        strb.append("Temp: ");
        strb.append(weather.getTempAvg());
        strb.append(" (Min: ");
        strb.append(weather.getTempMin());
        strb.append(", Max: ");
        strb.append(weather.getTempMax());
        strb.append(")\n");
        strb.append("Wind: ");
        strb.append(weather.getWindAvg());
        strb.append(" ");
        strb.append(weather.getWindDir());
        strb.append(" (Min: ");
        strb.append(weather.getWindMin());
        strb.append(", Max: ");
        strb.append(weather.getWindMax());
        strb.append(")\n");
        strb.append("Precipitation Probability: ");
        strb.append(weather.getRainPossibility());
        strb.append("%\n");
        strb.append("Rel. Humidity: ");
        strb.append(weather.getHumidityAvg());
        strb.append(" (Min: ");
        strb.append(weather.getHumidityMin());
        strb.append(", Max: ");
        strb.append(weather.getHumidityMax());
        strb.append(")\n");

        weatherImageView = (ImageView) currentView.findViewById(R.id.weatherImage2);
        weatherImageView.setImageResource(weather.getCodeDayPic());

        weatherView.setText(strb.toString());

        weather = weather.getNextDay();
        if(weather == null)
            return;

        weatherView = (TextView) currentView.findViewById(R.id.weatherData3);

        strb = new StringBuilder();
        strb.append(weather.getDate());
        strb.append("\n");
        strb.append(weather.getCodeDesc());
        strb.append("\n");
        strb.append("Temp: ");
        strb.append(weather.getTempAvg());
        strb.append(" (Min: ");
        strb.append(weather.getTempMin());
        strb.append(", Max: ");
        strb.append(weather.getTempMax());
        strb.append(")\n");
        strb.append("Wind: ");
        strb.append(weather.getWindAvg());
        strb.append(" ");
        strb.append(weather.getWindDir());
        strb.append(" (Min: ");
        strb.append(weather.getWindMin());
        strb.append(", Max: ");
        strb.append(weather.getWindMax());
        strb.append(")\n");
        strb.append("Precipitation Probability: ");
        strb.append(weather.getRainPossibility());
        strb.append("%\n");
        strb.append("Rel. Humidity: ");
        strb.append(weather.getHumidityAvg());
        strb.append(" (Min: ");
        strb.append(weather.getHumidityMin());
        strb.append(", Max: ");
        strb.append(weather.getHumidityMax());
        strb.append(")\n");


        weatherImageView = (ImageView) currentView.findViewById(R.id.weatherImage3);
        weatherImageView.setImageResource(weather.getCodeDayPic());

        weatherView.setText(strb.toString());
    }
}
