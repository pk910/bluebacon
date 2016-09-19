package de.dhbw.bluebacon.model;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import de.dhbw.bluebacon.BuildConfig;

public class URLRequest {

    public static final String LOG_TAG = "URLRequest";
    public static final int HTTP_OK = 200;
    public static final int CONNECT_TIMEOUT_MILLIS_DEFAULT = 2000;

    private final String urlString;
    private final int connectTimeoutMillis;
    private int http_status;
    private String result;

    public URLRequest(String urlString){
        this.urlString = urlString;
        this.http_status = 0;
        this.connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS_DEFAULT;
    }

    public URLRequest(String urlString, int connectTimeoutMillis){
        this.urlString = urlString;
        this.http_status = 0;
        this.connectTimeoutMillis = connectTimeoutMillis;
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
        connection.setConnectTimeout(this.connectTimeoutMillis);

        http_status = connection.getResponseCode();
        String line;
        InputStreamReader isr = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(isr);
        while ((line = reader.readLine()) != null)
        {
            sb.append(line);
            sb.append(System.lineSeparator());
        }

        reader.close();
        isr.close();
        connection.disconnect();

        this.result = sb.toString();
    }

    public String getResult(){
        if(result == null) {
            throw new RuntimeException("Must call exec() before fetching result!");
        }
        return result;
    }

    public int getHTTPStatus(){
        if(http_status == 0) {
            throw new RuntimeException("Must call exec() before fetching HTTP status!");
        }
        return http_status;
    }
}
