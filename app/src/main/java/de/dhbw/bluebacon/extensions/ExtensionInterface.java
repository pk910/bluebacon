package de.dhbw.bluebacon.extensions;

import android.content.Context;

/**
 * Extension Interface class
 */
public interface ExtensionInterface {

    void connect(Context context);
    void disconnect();
    void sendMessage(String message);

}
