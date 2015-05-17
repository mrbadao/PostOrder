package tk.order_sys.Fragment;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import tk.order_sys.HttpRequest.DeliveryCompleteOrderHttpRequest;
import tk.order_sys.HttpRequestInterface.OrderActionInterface;
import tk.order_sys.PostOrderService.OrderTracingService;
import tk.order_sys.Postorder.OrderDetailActivity;
import tk.order_sys.Postorder.OrdersMapActivity;
import tk.order_sys.Postorder.R;

/**
 * Created by mrbadao on 30/04/2015.
 */
public class OrderDetailInfoFragment extends Fragment implements View.OnClickListener, OrderActionInterface {
    public static final String CALL_BACK_ORDER_COMPLETED_FLAG = "oderCompleted";

    View rootView;
    TextView txtOrderDetailInfoCustomerName, txtOrderDetailInfoPhone, txtOrderDetailInfoCreated, txtOrderDetailInfoOrderName, txtOrderDetailInfoAddress;
    Button btnComplete;
    Button btnDelay;

    private String orderId;
    private String mPrefsTag;
    private String mToken;
    private String mStaffID;
    private String mPhoneNumber;

    public OrderDetailInfoFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPrefsTag = null;
        mToken = null;
        mStaffID = null;
        mPhoneNumber = null;

        rootView = inflater.inflate(R.layout.fragment_order_detail_info, container, false);

        txtOrderDetailInfoCustomerName = (TextView) rootView.findViewById(R.id.txtOrderDetailInfoCustomerName);
        txtOrderDetailInfoPhone = (TextView) rootView.findViewById(R.id.txtOrderDetailInfoPhone);
        txtOrderDetailInfoCreated = (TextView) rootView.findViewById(R.id.txtOrderDetailInfoCreated);
        txtOrderDetailInfoOrderName = (TextView) rootView.findViewById(R.id.txtOrderDetailInfoOrderName);
        txtOrderDetailInfoAddress = (TextView) rootView.findViewById(R.id.txtOrderDetailInfoAddress);

        btnComplete = (Button) rootView.findViewById(R.id.btnCompleted);
        btnDelay = (Button) rootView.findViewById(R.id.btnDelay);

        btnComplete.setOnClickListener(this);
        btnDelay.setOnClickListener(this);

        orderId = null;
        orderId = ((OrderDetailActivity) getActivity()).getOrderId();

        render();

        return rootView;
    }

    private void render() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mPrefsTag = MainFragment.PREFS_ORDER_TAG + "." + orderId + ".";
        Log.i("INFO", mPrefsTag);

        if (sharedPreferences.contains(LoginFragment.PREF_STAFF_TOKEN_TAG)) {
            mToken = sharedPreferences.getString(LoginFragment.PREF_STAFF_TOKEN_TAG, null);
        }

        if (sharedPreferences.contains(LoginFragment.PREF_STAFF_ID_TAG)) {
            mStaffID = sharedPreferences.getString(LoginFragment.PREF_STAFF_ID_TAG, null);
        }

        if (sharedPreferences.contains(mPrefsTag + "name")) {
            txtOrderDetailInfoOrderName.setText("MS: " + sharedPreferences.getString(mPrefsTag + "name", null));
        }

        if (sharedPreferences.contains(mPrefsTag + "customer_name")) {
            txtOrderDetailInfoCustomerName.setText(sharedPreferences.getString(mPrefsTag + "customer_name", null));
        }

        if (sharedPreferences.contains(mPrefsTag + "created")) {
            txtOrderDetailInfoCreated.setText(sharedPreferences.getString(mPrefsTag + "created", null));
        }

        if (sharedPreferences.contains(mPrefsTag + "order_phone")) {
            mPhoneNumber = sharedPreferences.getString(mPrefsTag + "order_phone", null);
            txtOrderDetailInfoPhone.setText(mPhoneNumber);
        }

        if(sharedPreferences.contains(mPrefsTag + "address")){
            txtOrderDetailInfoAddress.setText("Địa chỉ: " +  sharedPreferences.getString(mPrefsTag + "address", null));
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnCompleted:
                if (mToken != null && mStaffID != null) {
                    JSONObject params = new JSONObject();

                    try {
                        params.put("token", mToken);
                        params.put("staff_id", mStaffID);
                        params.put("order_id", orderId);

                        new DeliveryCompleteOrderHttpRequest(getActivity(), null, this).execute(params);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                break;

            case R.id.btnDelay:
                if(mPhoneNumber != null){
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", mPhoneNumber, null)));
                }
                break;
        }
    }

    @Override
    public void onCompleteOrder(JSONObject jsonObject) {
        if (jsonObject != null) {
            try {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sharedPreferences.edit();

                if (!jsonObject.isNull("error")) {
                    int errorCode = jsonObject.getJSONObject("error").getInt("error_code");

                    if (errorCode == 1015) {

                    }
                }

                if (!jsonObject.isNull("status")) {
                    int statusCode = jsonObject.getJSONObject("status").getInt("status_code");

                    if (statusCode == 1016) {
                        if (sharedPreferences.contains(mPrefsTag + "name")) {
                            editor.remove(mPrefsTag + "name");
                        }

                        if (sharedPreferences.contains(mPrefsTag + "customer_name")) {
                            editor.remove(mPrefsTag + "customer_name");
                        }

                        if (sharedPreferences.contains(mPrefsTag + "created")) {
                            editor.remove(mPrefsTag + "created");
                        }

                        if (sharedPreferences.contains(mPrefsTag + "order_phone")) {
                            editor.remove(mPrefsTag + "order_phone");
                        }

                        if (sharedPreferences.contains(mPrefsTag + "detail")) {
                            editor.remove(mPrefsTag + "detail");
                        }

                        String lat = null;

                        if (sharedPreferences.contains(mPrefsTag + "coordinate_lat")) {
                            lat = sharedPreferences.getString(mPrefsTag + "coordinate_lat", null);
                            editor.remove(mPrefsTag + "coordinate_lat");
                        }

                        String lng = null;
                        if (sharedPreferences.contains(mPrefsTag + "coordinate_long")) {
                            editor.remove(mPrefsTag + "coordinate_long");
                            lng = sharedPreferences.getString(mPrefsTag + "coordinate_long", null);
                        }

                        if (sharedPreferences.contains(mPrefsTag + "delivery_id")) {
                            editor.remove(mPrefsTag + "delivery_id");
                        }

                        if (sharedPreferences.contains(mPrefsTag + "status")) {
                            editor.remove(mPrefsTag + "status");
                        }

                        if (sharedPreferences.contains(mPrefsTag + "created")) {
                            editor.remove(mPrefsTag + "created");
                        }

                        if (sharedPreferences.contains(mPrefsTag + "address")) {
                            editor.remove(mPrefsTag + "address");
                        }

                        if (sharedPreferences.contains(OrdersMapActivity.LAST_ORDER_LOCATION_TAG)) {
                            Gson gson = new Gson();
                            LatLng latLng = gson.fromJson(sharedPreferences.getString(OrdersMapActivity.LAST_ORDER_LOCATION_TAG, null), LatLng.class);
                            if (String.valueOf(latLng.latitude).equals(lat) && String.valueOf(latLng.longitude).equals(lng)) {
                                editor.remove(OrdersMapActivity.LAST_ORDER_LOCATION_TAG);
                                editor.remove(OrdersMapActivity.PREF_CURRENT_PHONE_NUMBER_TAG);
                                editor.remove(OrdersMapActivity.PREF_CURRENT_ORDER_NAME_TAG);

                                Intent mOrderTracingService = new Intent(getActivity(), OrderTracingService.class);
                                mOrderTracingService.setAction(OrdersMapActivity.ORDER_TRACING_SERVICE_ACTION_CLOSE);
                                getActivity().startService(mOrderTracingService);
                            }
                        }


                        editor.commit();

                        Toast.makeText(getActivity(), "Đã giao thành công đơn hàng:\n" + txtOrderDetailInfoOrderName.getText(), Toast.LENGTH_SHORT).show();

                        Intent intentCallBackData = new Intent();
                        intentCallBackData.putExtra(CALL_BACK_ORDER_COMPLETED_FLAG, true);
                        getActivity().setResult(Activity.RESULT_OK, intentCallBackData);

                        getActivity().finish();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
