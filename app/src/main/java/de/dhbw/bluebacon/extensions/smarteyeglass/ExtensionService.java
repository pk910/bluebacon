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

import android.content.Intent;
import android.util.Log;

import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.registration.DeviceInfo;
import com.sonyericsson.extras.liveware.extension.util.registration.DisplayInfo;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationAdapter;
import com.sonyericsson.extras.liveware.extension.util.widget.WidgetExtension;

import java.util.List;

/**
 * The Hello World Extension Service handles registration and keeps track of
 * all controls on all accessories.
 */
public final class ExtensionService extends com.sonyericsson.extras.liveware.extension.util.ExtensionService {

    /** */
    public static Control SmartEyeglassControl;
    /** */
    public static ExtensionService Object;

    /** */
    private static String Message = null;

    /** Creates a new instance. */
    public ExtensionService() {
        super(de.dhbw.bluebacon.extensions.smarteyeglass.Constants.EXTENSION_KEY);
        Object = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(de.dhbw.bluebacon.extensions.smarteyeglass.Constants.LOG_TAG, "onCreate: ExtensionService");
    }

    @Override
    protected com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation getRegistrationInformation() {
        return new de.dhbw.bluebacon.extensions.smarteyeglass.BlueBaconRegistrationInformation(this);
    }

    @Override
    protected boolean keepRunningWhenConnected() {
        return false;
    }

    /**
     * Sets message to be shown when the SmartEyeglass app is ready.
     * Starts the app if it has not already started.
     */
    public void sendMessageToExtension(final String message) {
        Message = message;
        if (SmartEyeglassControl == null) {
            startSmartEyeglassExtension();
        } else {
            SmartEyeglassControl.requestExtensionStart();
        }
    }

    /**
     * You can use this method to kickstart your extension on SmartEyeglass
     * Host App.
     * This is useful if the user has not started your extension
     * since the SmartEyeglass was turned on.
     */
    public void startSmartEyeglassExtension() {
        Intent intent = new Intent(com.sonyericsson.extras.liveware.aef.control.Control.Intents
                .CONTROL_START_REQUEST_INTENT);
        ExtensionUtils.sendToHostApp(getApplicationContext(),
                "com.sony.smarteyeglass", intent);
    }

    /**
     * Creates ControlExtension object for the accessory.
     * This creates the Control object after verifying
     * that the connected accessory is a SmartEyeglass.
     */
    @Override
    public ControlExtension createControlExtension(
            final String hostAppPackageName) {
        de.dhbw.bluebacon.extensions.smarteyeglass.ScreenSize size = new de.dhbw.bluebacon.extensions.smarteyeglass.ScreenSize(this);
        final int width = size.getWidth();
        final int height = size.getHeight();
        List<DeviceInfo> list = RegistrationAdapter.getHostApplication(
                this, hostAppPackageName).getDevices();
        for (DeviceInfo device : list) {
            for (DisplayInfo display : device.getDisplays()) {
                if (display.sizeEquals(width, height)) {
                    return new Control(this,
                            hostAppPackageName, Message);
                }
            }
        }
        throw new IllegalArgumentException("No control for: "
                + hostAppPackageName);
    }

    @Override
    public WidgetExtension createWidgetExtension(String hostAppPackageName) {
        return new HelloWidget(this, hostAppPackageName);
    }

}
