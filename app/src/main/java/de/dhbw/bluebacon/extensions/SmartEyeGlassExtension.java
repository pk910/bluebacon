package de.dhbw.bluebacon.extensions;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sonyericsson.extras.liveware.aef.registration.Registration;

import de.dhbw.bluebacon.extensions.smarteyeglass.Control;
import de.dhbw.bluebacon.extensions.smarteyeglass.ExtensionService;


public class SmartEyeGlassExtension implements ExtensionInterface {

    @Override
    public void connect(Context context) {

        /*
         * Make sure ExtensionService of your SmartEyeglass app has already
         * started.
         * This is normally started automatically when user enters your app
         * on SmartEyeglass, although you can initialize it early using
         * request intent.
         */
        if (ExtensionService.Object == null) {
            Intent intent = new Intent(Registration.Intents
                    .EXTENSION_REGISTER_REQUEST_INTENT);
            intent.setClass(context, ExtensionService.class);
            context.startService(intent);
        }
    }

    @Override
    public void disconnect() {
        // Nothing to do for now.
    }

    @Override
    public void sendMessage(String message) {
        try {
            if(Control.cont==null)
            {
                if (ExtensionService.Object != null) {
                    ExtensionService.Object.sendMessageToExtension(message);
                }
            }
            else
            {
                Control.cont.updateLayout(message);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("SmartEyeGlass","Conection problem");
        }
    }
}
