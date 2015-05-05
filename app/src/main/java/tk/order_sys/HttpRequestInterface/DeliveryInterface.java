package tk.order_sys.HttpRequestInterface;

import org.json.JSONObject;

/**
 * Created by mrbadao on 03/05/2015.
 */
public interface DeliveryInterface {
    void onUserLogin(JSONObject jsonObject);

    void onCheckToken(JSONObject jsonObject);
}
