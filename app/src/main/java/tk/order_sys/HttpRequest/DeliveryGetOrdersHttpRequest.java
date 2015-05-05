package tk.order_sys.HttpRequest;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import tk.order_sys.API.API;
import tk.order_sys.HttpRequestInterface.OrdersInterface;

/**
 * Created by mrbadao on 13/04/2015.
 */
public class DeliveryGetOrdersHttpRequest extends AsyncTask<JSONObject, String, JSONObject> {
    private ProgressDialog pdia;
    public OrdersInterface delegate;
    private Context context;
    private JSONArray jsonCookieStore;

    public DeliveryGetOrdersHttpRequest(Context context, JSONArray jsonCookieStore, OrdersInterface delegate) {
        this.context = context;
        this.jsonCookieStore = jsonCookieStore;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        pdia = new ProgressDialog(context);
        pdia.setMessage("Loading...");
        pdia.show();
    }

    @Override
    protected JSONObject doInBackground(JSONObject... params) {
        return API.getDeliveryOrders(params[0], jsonCookieStore);
    }

    protected void onPostExecute(JSONObject jsonObject) {
        delegate.onGetOrders(jsonObject);
        pdia.dismiss();
    }
}