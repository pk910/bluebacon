package de.dhbw.bluebacon.model;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import de.dhbw.bluebacon.BuildConfig;

public class URLRequest {

    public static final String LOG_TAG = "URLRequest";
    public static final int HTTP_OK = 200;

    private String urlString;
    private int http_status;
    private String result;

    public URLRequest(String urlString){
        this.urlString = urlString;
        this.http_status = 0;
    }

    public void exec() throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection;
        StringBuilder sb = new StringBuilder();

        if(BuildConfig.DEBUG) {
            Log.v(LOG_TAG, "Query URL: " + url.toString());
        }
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setRequestMethod("GET");

        http_status = connection.getResponseCode();
        String line;
        InputStreamReader isr = new InputStreamReader(connection.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        while ((line = reader.readLine()) != null)
        {
            sb.append(line);
            sb.append("\n");
        }

        isr.close();
        reader.close();
        connection.disconnect();

        this.result = sb.toString();
    }

    public String getResult(){
        if(result == null)
            throw new RuntimeException("Must call exec() before fetching result!");
        return result;
    }

    public int getHTTPStatus(){
        if(http_status == 0)
            throw new RuntimeException("Must call exec() before fetching HTTP status!");
        return http_status;
    }
}
