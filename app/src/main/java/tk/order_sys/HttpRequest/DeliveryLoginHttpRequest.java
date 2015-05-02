package tk.order_sys.HttpRequest;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import tk.order_sys.API.API;
import tk.order_sys.HttpRequestInterface.DeliveryInterface;

/**
 * Created by mrbadao on 13/04/2015.
 */
public class DeliveryLoginHttpRequest extends AsyncTask<JSONObject, String, JSONObject> {
    public DeliveryInterface delegate;
    private Context context;
    private JSONArray jsonCookieStore;

    public DeliveryLoginHttpRequest(Context context, JSONArray jsonCookieStore, DeliveryInterface delegate) {
        this.context = context;
        this.jsonCookieStore = jsonCookieStore;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected JSONObject doInBackground(JSONObject... params) {
        return API.DeliveryLogin(params[0], jsonCookieStore);
    }

    protected void onPostExecute(JSONObject jsonObject) {
        delegate.onUserLogin(jsonObject);
    }
}