package tk.order_sys.PostOrderBroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tk.order_sys.PostOrderInterface.LocationReceiverInterface;

/**
 * Created by mrbadao on 30/04/2015.
 */
public class LocationReceiver extends BroadcastReceiver {
    LocationReceiverInterface locationReceiverInterface;

    public LocationReceiver(LocationReceiverInterface locationReceiverInterface) {
        this.locationReceiverInterface = locationReceiverInterface;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        locationReceiverInterface.onCurrentLocationReceived(context, intent);
    }
}
