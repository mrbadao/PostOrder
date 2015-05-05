package tk.order_sys.Fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tk.order_sys.HttpRequest.DeliveryGetOrderDetailHttpRequest;
import tk.order_sys.HttpRequestInterface.OrderDetailInterface;
import tk.order_sys.Postorder.OrderDetailActivity;
import tk.order_sys.Postorder.R;

/**
 * Created by mrbadao on 30/04/2015.
 */
public class OrderDetailProductsFragment extends Fragment implements OrderDetailInterface {
    View rootView;
    String orderID;
    String mPrefsTag;
    TableLayout tableLayoutProducts;

    public OrderDetailProductsFragment() {
        orderID = null;
        mPrefsTag = null;
        tableLayoutProducts = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_order_detail_products, container, false);
        tableLayoutProducts = (TableLayout) rootView.findViewById(R.id.tableLayoutOrderDetailInfo);

        orderID = ((OrderDetailActivity) getActivity()).getOrderId();
        mPrefsTag = MainFragment.PREFS_ORDER_TAG + "." + orderID + ".detail";

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (sharedPreferences.contains(mPrefsTag)) {
            JSONObject jsonOrderDetail = null;

            String strOderDetail = sharedPreferences.getString(mPrefsTag, null);

            if (strOderDetail != null) {
                try {
                    jsonOrderDetail = new JSONObject(strOderDetail);
                    onGetOrderDetail(jsonOrderDetail);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            String Token = sharedPreferences.contains(LoginFragment.PREF_STAFF_TOKEN_TAG) ? sharedPreferences.getString(LoginFragment.PREF_STAFF_TOKEN_TAG, null) : null;
            String StaffID = sharedPreferences.contains(LoginFragment.PREF_STAFF_ID_TAG) ? sharedPreferences.getString(LoginFragment.PREF_STAFF_ID_TAG, null) : null;

            if (Token != null && StaffID != null && orderID != null) {
                JSONObject params = new JSONObject();

                try {
                    params.put("token", Token);
                    params.put("staff_id", StaffID);
                    params.put("order_id", orderID);
                    new DeliveryGetOrderDetailHttpRequest(getActivity(), null, this).execute(params);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }

        return rootView;
    }

    @Override
    public void onGetOrderDetail(JSONObject jsonObject) {
        if (jsonObject != null) {
            Long cost, total = Long.valueOf(0);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Log.i("JSON", jsonObject.toString());
            try {
                if (!jsonObject.isNull("order")) {
                    JSONArray jsonOrderArray = jsonObject.getJSONArray("order");
                    JSONObject jsonItem = null;
                    TableRow tableRow = null;

                    for (int i = 0; i < jsonOrderArray.length(); i++) {
                        jsonItem = jsonOrderArray.getJSONObject(i);
                        tableRow = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_order_detail_products_table_row, null);
                        TextView txtOrderDetailProductName = (TextView) tableRow.findViewById(R.id.txtOrderDetailProductName);
                        TextView txtOrderDetailProductQty = (TextView) tableRow.findViewById(R.id.txtOrderDetailProductQty);
                        TextView txtOrderDetailProductCost = (TextView) tableRow.findViewById(R.id.txtOrderDetailProductCost);

                        cost = Long.valueOf(jsonItem.getString("price")) * Long.valueOf(jsonItem.getString("qty"));
                        total += cost;

                        txtOrderDetailProductName.setText(jsonItem.getString("name"));
                        txtOrderDetailProductQty.setText(jsonItem.getString("qty"));
                        txtOrderDetailProductCost.setText(String.format("%,d", cost));

                        tableLayoutProducts.addView(tableRow);
                    }

                    tableRow = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.fragment_order_detail_products_table_result_row, null);
                    TextView txtOrderDetailProductTotal = (TextView) tableRow.findViewById(R.id.txtOrderDetailProductTotal);
                    txtOrderDetailProductTotal.setText(String.format("%,d", total) + " đồng");

                    tableLayoutProducts.addView(tableRow);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!sharedPreferences.contains(mPrefsTag)) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(mPrefsTag, jsonObject.toString());
                editor.commit();
            }
        }
    }

}
