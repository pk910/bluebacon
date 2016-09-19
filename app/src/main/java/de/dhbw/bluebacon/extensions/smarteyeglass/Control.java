/*
Copyright (c) 2011, Sony Mobile Communications Inc.
Copyright (c) 2014, Sony Corporation

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 * Neither the name of the Sony Mobile Communications Inc.
 nor the names of its contributors may be used to endorse or promote
 products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.dhbw.bluebacon.extensions.smarteyeglass;

import android.content.Context;
import android.util.Log;

import com.sony.smarteyeglass.SmartEyeglassControl;
import com.sony.smarteyeglass.extension.util.SmartEyeglassControlUtils;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;

import de.dhbw.bluebacon.R;

/**
 * Demonstrates how to communicate between an Android activity and its
 * corresponding SmartEyeglass app.
 *
 */
public final class Control extends ControlExtension {

    /** Instance of the SmartEyeglass Control Utility class. */
    private final SmartEyeglassControlUtils utils;
    String message;
    public static Control cont;

    /** The SmartEyeglass API version that this app uses */
    private static final int SMARTEYEGLASS_API_VERSION = 1;

    /**
     * Shows a simple layout on the SmartEyeglass display and sets
     * the text content dynamically at startup.
     * Tap on the device controller touch pad to start the Android activity
     * for this app on the phone.
     * Tap the Android activity button to run the SmartEyeglass app.
     *
     * @param context            The context.
     * @param hostAppPackageName Package name of SmartEyeglass host application.
     */
    public Control(final Context context,
                   final String hostAppPackageName, final String message) {
        super(context, hostAppPackageName);
        utils = new SmartEyeglassControlUtils(hostAppPackageName, null);
        utils.setRequiredApiVersion(SMARTEYEGLASS_API_VERSION);
        utils.activate(context);
        this.message = "";
        cont = null;

        /*
         * Set reference back to this Control object
         * in ExtensionService class to allow access to SmartEyeglass Control
         */
        ExtensionService.Object.SmartEyeglassControl = this;

        /*
         * Show the message that was set if any) when this Control started
         */
        if (message == null) {
            updateLayout();
        } else {
            showToast(message);
        }
    }

    /**
     * Provides a public method for ExtensionService and Activity to call in
     * order to request start.
     */
    public void requestExtensionStart() {
        startRequest();
    }

    // Update the SmartEyeglass display when app becomes visible
    @Override
    public void onResume() {
        updateLayout();
        super.onResume();
    }

    // Clean up data structures on termination.
    @Override
    public void onDestroy() {
        Log.d(Constants.LOG_TAG, "onDestroy: Control");
        cont = null;
        utils.deactivate();
    }

    /**
     *  Update the display with the dynamic message text.
     */
    private void updateLayout() {
        showLayout(R.layout.smarteyeglass_layout, null);
        sendText(R.id.btn_update_this, this.message);
        cont=this;
    }

    public void updateLayout(String message) {
        sendText(R.id.btn_update_this, message);
    }

    /**
     * Timeout dialog messages are similar to Toast messages on
     * Android Activities
     * This shows a timeout dialog with the specified message.
     */
    public void showToast(final String message) {
        this.message = message;
        Log.d(Constants.LOG_TAG, "Timeout Dialog : Control");
        utils.showDialogMessage(message,
                SmartEyeglassControl.Intents.DIALOG_MODE_TIMEOUT);
    }
}
