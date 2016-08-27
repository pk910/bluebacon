package de.dhbw.meteoblue;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import cz.msebera.android.httpclient.Header;

/**
 * Created by pk910 on 09.03.2016.
 */
public abstract class JsonRequestHelper {

    protected void requestJsonFromWeb(String url) {
        AsyncHttpClient client = new AsyncHttpClient();

        Log.i("HTTPClient", "Request: " + url);
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String recvStr = new String(response, StandardCharsets.UTF_8);
                JSONObject jsonObj = null;
                JSONArray jsonArr = null;
                try {
                    jsonObj = new JSONObject(recvStr);
                } catch (JSONException e) {
                }
                try {
                    jsonArr = new JSONArray(recvStr);
                } catch (JSONException e) {
                }
                if(jsonObj != null)
                    onJsonReceived(jsonObj);
                else if(jsonArr != null)
                    onJsonReceived(jsonArr);
                else
                    onTextReceived(recvStr);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                String error = null;
                try {
                    error = new String(errorResponse, StandardCharsets.UTF_8);
                } catch (Exception e1) {
                }
                Log.i("HTTPClient", "  Error: " + statusCode + " - " + error);
                onJsonRequestFail(statusCode, error);
            }

            @Override
            public void onRetry(int retryNo) {
            }
        });
    }

    protected abstract void onJsonReceived(JSONObject json);
    protected abstract void onJsonReceived(JSONArray json);
    protected abstract void onTextReceived(String response);
    protected abstract void onJsonRequestFail(int statusCode, String errorMessage);

}
