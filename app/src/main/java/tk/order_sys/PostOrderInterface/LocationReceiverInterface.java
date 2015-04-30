package tk.order_sys.PostOrderInterface;

import android.content.Context;
import android.content.Intent;

/**
 * Created by mrbadao on 30/04/2015.
 */
public interface LocationReceiverInterface {
    void onCurrentLocationReceived(Context context, Intent intent);
}
