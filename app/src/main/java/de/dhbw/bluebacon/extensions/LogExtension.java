package de.dhbw.bluebacon.extensions;


import android.content.Context;
import android.util.Log;

public class LogExtension implements ExtensionInterface {

    private static final String TAG = "LogExtension";

    @Override
    public void connect(Context context) {
        // Nothing to do for now.
    }

    @Override
    public void disconnect() {
        // Nothing to do for now.
    }

    @Override
    public void sendMessage(String message) {
        Log.d(TAG, message);
    }
}
