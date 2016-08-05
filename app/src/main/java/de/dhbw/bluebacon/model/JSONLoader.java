package de.dhbw.bluebacon.model;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.client.utils.URLEncodedUtils;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.util.EntityUtils;

/**
 * Loader for data loading
 */
public class JSONLoader {

    static String response = null;
    public static final int GET = 1;
    public static final int POST = 2;

    /**
     * Constructor
     */
    public JSONLoader() {

    }

    /**
     * Get JSON without params
     * @param url Address to load
     * @param method HTTP Method (GET / POST)
     * @return JSON String
     * @throws IOException
     */
//    public String getJSON(String url, int method) throws IOException {
//        return this.getJSON(url, method, null);
//    }

    /**
     * Get JSON with params
     * @param url Address to load
     * @param method HTTP Method (GET / POST)
     * @param params Parameter
     * @return JSON String
     * @throws IOException
     */
//    public String getJSON(String url, int method,
//                                  List<NameValuePair> params) throws IOException {
//        // http client
//        DefaultHttpClient httpClient = new DefaultHttpClient();
//        HttpEntity httpEntity = null;
//        HttpResponse httpResponse = null;
//        String address = url;
//
//        // Checking http request method type
//        if (method == POST) {
//            HttpPost httpPost = new HttpPost(address);
//            httpPost.setHeader("Accept", "application/json");
//            // adding post params
//            if (params != null) {
//                httpPost.setEntity(new UrlEncodedFormEntity(params));
//            }
//
//            httpResponse = httpClient.execute(httpPost);
//
//        } else if (method == GET) {
//            // appending params to url
//            if (params != null) {
//                String paramString = URLEncodedUtils
//                        .format(params, "utf-8");
//                address += "?" + paramString;
//            }
//            HttpGet httpGet = new HttpGet(address);
//            httpGet.setHeader("Accept", "application/json");
//
//            httpResponse = httpClient.execute(httpGet);
//
//        }
//        httpEntity = httpResponse.getEntity();
//        response = EntityUtils.toString(httpEntity);
//
//        return response;
//    }

    /**
     * Saves received date locally
     * @param json Received JSON String
     */
    public void saveLocalMachineData(String json, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("cache.txt", Context.MODE_PRIVATE), StandardCharsets.UTF_8);
            outputStreamWriter.write(json);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    /**
     * Loads locally saved machine data
     * @return JSON String
     */
    public String loadLocalMachineData(Context context) {
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("cache.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                bufferedReader.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

}
