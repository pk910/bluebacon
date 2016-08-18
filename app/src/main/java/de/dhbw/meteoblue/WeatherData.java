package de.dhbw.meteoblue;

import android.content.Context;
import android.content.res.Resources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by pk910 on 18.08.2016.
 */
public class WeatherData {
    private static Context AppContext;

    public static void SetAppContext(Context context) {
        AppContext = context;
    }

    private class WeatherDataCode {
        private int code;
        private String desc;
        private String dayPic;
        private String nightPic;
    }

    private WeatherData nextDay = null;

    private String dataDate;
    private int dataCode;
    private String dataCodeDesc;
    private long dataCodeDayPicId, dataCodeNightPicId;
    private int dataUVIndex;
    private double dataTempMax, dataTempMin, dataTempAvg;
    private double dataFeltTempMax, dataFeltTempMin;
    private double dataRainValue, dataSnowValue;
    private double dataWindMin, dataWindMax, dataWindAvg, dataHumidityMin, dataHumidityMax, dataHumidityAvg;
    private int dataWindDir;
    private int dataRainPossibility, dataAccuracy;

    public WeatherData(JSONObject json) {
        parseJsonData(json);
    }

    private WeatherData() {
        // internal constructor
    }

    public WeatherData getNextDay() {
        return nextDay;
    }

    private void parseJsonData(JSONObject json) {
        try {
            JSONObject daydata = json.getJSONObject("data_day");

            JSONArray timeData = daydata.getJSONArray("time");
            int dayCount = timeData.length();
            WeatherData weatherDays[] = new WeatherData[dayCount];
            weatherDays[0] = this;
            for(int i = 1; i < dayCount; i++) {
                weatherDays[i] = new WeatherData();
                weatherDays[i-1].nextDay = weatherDays[i];
            }

            for (Iterator<String> keys = daydata.keys(); keys.hasNext();){
                String key = keys.next();
                JSONArray values = daydata.getJSONArray(key);
                for(int i = 0; i < dayCount; i++) {
                    WeatherData data = weatherDays[i];

                    try {
                        if (key.equalsIgnoreCase("time"))
                            data.dataDate = values.getString(i);
                        else if (key.equalsIgnoreCase("pictocode")) {
                            int pictocode = values.getInt(i);
                            data.dataCode = pictocode;

                            String dataCodeDayPic = String.format("meteoblue_%02d_day", pictocode);
                            data.dataCodeDayPicId = AppContext.getResources().getIdentifier(dataCodeDayPic, "drawable", AppContext.getPackageName());

                            String dataCodeNightPic = String.format("meteoblue_%02d_night", pictocode);
                            data.dataCodeNightPicId = AppContext.getResources().getIdentifier(dataCodeNightPic, "drawable", AppContext.getPackageName());

                            String dataCodeDescRes = String.format("meteoblue_code%02d", pictocode);
                            int dataCodeDescResId = AppContext.getResources().getIdentifier(dataCodeNightPic, "string", AppContext.getPackageName());
                            data.dataCodeDesc = AppContext.getString(dataCodeDescResId);
                        } else if (key .equalsIgnoreCase("uvindex"))
                            data.dataUVIndex = values.getInt(i);
                        else if (key.equalsIgnoreCase("temperature_max"))
                            data.dataTempMax = values.getDouble(i);
                        else if (key.equalsIgnoreCase("temperature_min"))
                            data.dataTempMin = values.getDouble(i);
                        else if (key.equalsIgnoreCase("temperature_mean"))
                            data.dataTempAvg = values.getDouble(i);
                        else if (key.equalsIgnoreCase("felttemperature_max"))
                            data.dataFeltTempMax = values.getDouble(i);
                        else if (key.equalsIgnoreCase("felttemperature_min"))
                            data.dataFeltTempMin = values.getDouble(i);
                        else if (key.equalsIgnoreCase("winddirection"))
                            data.dataWindDir = values.getInt(i);
                        else if (key.equalsIgnoreCase("precipitation_probability"))
                            data.dataRainPossibility = values.getInt(i);
                        else if (key.equalsIgnoreCase("predictability"))
                            data.dataAccuracy = values.getInt(i);
                        else if (key.equalsIgnoreCase("precipitation"))
                            data.dataRainValue = values.getDouble(i);
                        else if (key.equalsIgnoreCase("snowfraction"))
                            data.dataSnowValue = values.getDouble(i);
                        else if (key.equalsIgnoreCase("windspeed_max"))
                            data.dataWindMax = values.getDouble(i);
                        else if (key.equalsIgnoreCase("windspeed_min"))
                            data.dataWindMin = values.getDouble(i);
                        else if (key.equalsIgnoreCase("windspeed_mean"))
                            data.dataWindAvg = values.getDouble(i);
                        else if (key.equalsIgnoreCase("relativehumidity_max"))
                            data.dataHumidityMax = values.getDouble(i);
                        else if (key.equalsIgnoreCase("relativehumidity_min"))
                            data.dataHumidityMin = values.getDouble(i);
                        else if (key.equalsIgnoreCase("relativehumidity_mean"))
                            data.dataHumidityAvg = values.getDouble(i);
                    } catch(JSONException e){}
                }
            }
        } catch (JSONException e) {}
    }

    public String getDate() {
        return dataDate;
    }

    public int getCode() {
        return dataCode;
    }

    public String getCodeDesc() {
        return dataCodeDesc;
    }

    public long getCodeDayPic() {
        return dataCodeDayPicId;
    }

    public long getCodeNightPic() {
        return dataCodeNightPicId;
    }

    public int getUVIndex() {
        return dataUVIndex;
    }

    public double getTempMax() {
        return dataTempMax;
    }

    public double getTempAvg() {
        return dataTempAvg;
    }

    public double getTempMin() {
        return dataTempMin;
    }

    public double getFeltTempMax() {
        return dataFeltTempMax;
    }

    public double getFeltTempMin() {
        return dataFeltTempMin;
    }

    public double getRainValue() {
        return dataRainValue;
    }

    public double getSnowValue() {
        return dataSnowValue;
    }

    public double getWindMin() {
        return dataWindMin;
    }

    public double getWindMax() {
        return dataWindMax;
    }

    public double getWindAvg() {
        return dataWindAvg;
    }

    public double getHumidityMin() {
        return dataHumidityMin;
    }

    public double getHumidityMax() {
        return dataHumidityMax;
    }

    public double getHumidityAvg() {
        return dataHumidityAvg;
    }

    public int getRainPossibility() {
        return dataRainPossibility;
    }

    public int getWindDir() {
        return dataWindDir;
    }

    public int getAccuracy() {
        return dataAccuracy;
    }

}
