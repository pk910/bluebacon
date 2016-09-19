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

import android.content.ContentValues;
import android.content.Context;

import com.sony.smarteyeglass.extension.util.SmartEyeglassWidgetImage;
import com.sonyericsson.extras.liveware.aef.registration.Registration.ExtensionColumns;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;

import de.dhbw.bluebacon.MainActivity;
import de.dhbw.bluebacon.R;

/**
 * Provides information needed during extension registration.
 */
public final class BlueBaconRegistrationInformation
        extends com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation {

    /** The application context. */
    private final Context context;

    /** The control API version in use. */
    private static final int CONTROL_API_VERSION = 4;

    /**
     * Creates a control registration object.
     *
     * @param context The context.
     */
    public BlueBaconRegistrationInformation(final Context context) {
        this.context = context;
    }

    @Override
    public int getRequiredControlApiVersion() {
        // This extension supports all accessories from Control API level 1 and
        // up.
        return CONTROL_API_VERSION;
    }

    @Override
    public int getTargetControlApiVersion() {
        return CONTROL_API_VERSION;
    }

    @Override
    public int getRequiredSensorApiVersion() {
        return API_NOT_REQUIRED;
    }

    @Override
    public int getRequiredNotificationApiVersion() {
        return API_NOT_REQUIRED;
    }

    @Override
    public int getRequiredWidgetApiVersion() {
        return API_NOT_REQUIRED;
    }

    @Override
    public ContentValues getExtensionRegistrationConfiguration() {
        String iconHostapp = getUriString(R.mipmap.ic_launcher);
        String iconExtension = getUriString(R.mipmap.ic_launcher);
        String iconExtension48 = getUriString(R.mipmap.ic_launcher);

        ContentValues values = new ContentValues();
        values.put(ExtensionColumns.CONFIGURATION_ACTIVITY,
                MainActivity.class.getName());
        values.put(ExtensionColumns.CONFIGURATION_TEXT,
                context.getString(R.string.configuration_text));
        values.put(ExtensionColumns.NAME,
                context.getString(R.string.extension_name));
        values.put(ExtensionColumns.EXTENSION_KEY, Constants.EXTENSION_KEY);
        values.put(ExtensionColumns.HOST_APP_ICON_URI, iconHostapp);
        values.put(ExtensionColumns.EXTENSION_ICON_URI, iconExtension);
        values.put(ExtensionColumns.EXTENSION_48PX_ICON_URI, iconExtension48);
        values.put(ExtensionColumns.NOTIFICATION_API_VERSION,
                getRequiredNotificationApiVersion());
        values.put(ExtensionColumns.PACKAGE_NAME, context.getPackageName());
        return values;
    }

    @Override
    public boolean isDisplaySizeSupported(final int width, final int height) {
        de.dhbw.bluebacon.extensions.smarteyeglass.ScreenSize size = new de.dhbw.bluebacon.extensions.smarteyeglass.ScreenSize(context);
        return size.equals(width, height);
    }

    @Override
    public boolean isWidgetSizeSupported(final int width, final int height) {
        return (height == SmartEyeglassWidgetImage.getSupportedWidgetHeight(this.context) &&
                width == SmartEyeglassWidgetImage.getSupportedWidgetWidth(this.context));
    }

    /**
     * Returns the URI string corresponding to the specified resource ID.
     *
     * @param id
     *            The resource ID.
     * @return The URI string.
     */
    private String getUriString(final int id) {
        return ExtensionUtils.getUriString(context, id);
    }
}
