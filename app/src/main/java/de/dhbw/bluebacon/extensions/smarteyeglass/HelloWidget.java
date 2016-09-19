package de.dhbw.bluebacon.extensions.smarteyeglass;

import android.content.Context;

import com.sonyericsson.extras.liveware.extension.util.widget.WidgetExtension;

import de.dhbw.bluebacon.R;

class HelloWidget extends WidgetExtension {

    // Create a widget extension.
    public HelloWidget(Context context, String hostAppPackageName) {
        super(context, hostAppPackageName);
    }

    @Override
    public void onStartRefresh() {
        // Send a UI layout when the widget is visible.
        showLayout(R.layout.smarteyeglass_layout);
        sendText(R.id.btn_update_this, "TEST");
    }

    @Override
    public void onStopRefresh() {
        // Send a UI layout when the widget is visible.
        //showLayout(R.layout.layout_widget);
    }
}
