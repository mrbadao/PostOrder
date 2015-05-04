package tk.order_sys.HttpRequest;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import tk.order_sys.API.API;
import tk.order_sys.HttpRequestInterface.OrderDetailInterface;

/**
 * Created by mrbadao on 13/04/2015.
 */
public class DeliveryGetOrderDetailHttpRequest extends AsyncTask<JSONObject, String, JSONObject> {
    public OrderDetailInterface delegate;
    private Context context;
    private JSONArray jsonCookieStore;

    public DeliveryGetOrderDetailHttpRequest(Context context, JSONArray jsonCookieStore, OrderDetailInterface delegate) {
        this.context = context;
        this.jsonCookieStore = jsonCookieStore;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected JSONObject doInBackground(JSONObject... params) {
        return API.getDeliveryOrderDetail(params[0], jsonCookieStore);
    }

    protected void onPostExecute(JSONObject jsonObject) {
        delegate.onGetOrderDetail(jsonObject);
    }
}