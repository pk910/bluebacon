package de.dhbw.bluebacon.extensions;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

public class ToastExtension implements ExtensionInterface {

    private Context context;

    @Override
    public void connect(Context context) {
        this.context=context;
    }

    @Override
    public void disconnect() {
        // Nothing to do for now.
    }

    @Override
    public void sendMessage(final String message) {
        ((Activity)context).runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
