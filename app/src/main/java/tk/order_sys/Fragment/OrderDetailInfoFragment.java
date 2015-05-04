package tk.order_sys.Fragment;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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

import org.json.JSONException;
import org.json.JSONObject;

import tk.order_sys.HttpRequest.DeliveryCompleteOrderHttpRequest;
import tk.order_sys.HttpRequestInterface.OrderActionInterface;
import tk.order_sys.Postorder.OrderDetailActivity;
import tk.order_sys.Postorder.R;

/**
 * Created by mrbadao on 30/04/2015.
 */
public class OrderDetailInfoFragment extends Fragment implements View.OnClickListener, OrderActionInterface{
    public static final String CALL_BACK_ORDER_COMPLETED_FLAG = "oderCompleted";

    View rootView;
    TextView txtOrderDetailInfoCustomerName, txtOrderDetailInfoPhone, txtOrderDetailInfoCreated, txtOrderDetailInfoOrderName, txtOrderDetailInfoAddress;
    Button btnComplete;

    private String orderId;
    private String mPrefsTag;
    private String mToken;
    private String mStaffID;

    public OrderDetailInfoFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPrefsTag = null;
        mToken = null;
        mStaffID = null;

        rootView = inflater.inflate(R.layout.fragment_order_detail_info, container, false);

        txtOrderDetailInfoCustomerName = (TextView) rootView.findViewById(R.id.txtOrderDetailInfoCustomerName);
        txtOrderDetailInfoPhone = (TextView) rootView.findViewById(R.id.txtOrderDetailInfoPhone);
        txtOrderDetailInfoCreated = (TextView) rootView.findViewById(R.id.txtOrderDetailInfoCreated);
        txtOrderDetailInfoOrderName = (TextView) rootView.findViewById(R.id.txtOrderDetailInfoOrderName);
        txtOrderDetailInfoAddress = (TextView) rootView.findViewById(R.id.txtOrderDetailInfoAddress);

        btnComplete = (Button) rootView.findViewById(R.id.btnCompleted);
        btnComplete.setOnClickListener(this);

        orderId = null;
        orderId = ((OrderDetailActivity)getActivity()).getOrderId();

        render();

        return rootView;
    }

    private void render(){
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getActivity());

        mPrefsTag = MainFragment.PREFS_ORDER_TAG + "." + orderId + ".";
        Log.i("INFO", mPrefsTag);

        if(sharedPreferences.contains(LoginFragment.PREF_STAFF_TOKEN_TAG)){
            mToken = sharedPreferences.getString(LoginFragment.PREF_STAFF_TOKEN_TAG, null);
        }

        if(sharedPreferences.contains(LoginFragment.PREF_STAFF_ID_TAG)){
            mStaffID = sharedPreferences.getString(LoginFragment.PREF_STAFF_ID_TAG, null);
        }

        if(sharedPreferences.contains(mPrefsTag + "name")){
            txtOrderDetailInfoOrderName.setText("MS: " +  sharedPreferences.getString(mPrefsTag + "name", null));
        }

        if(sharedPreferences.contains(mPrefsTag + "customer_name")){
            txtOrderDetailInfoCustomerName.setText(sharedPreferences.getString(mPrefsTag + "customer_name", null));
        }

        if(sharedPreferences.contains(mPrefsTag + "created")){
            txtOrderDetailInfoCreated.setText(sharedPreferences.getString(mPrefsTag + "created", null));
        }

        if(sharedPreferences.contains(mPrefsTag + "order_phone")){
            txtOrderDetailInfoPhone.setText(sharedPreferences.getString(mPrefsTag + "order_phone", null));
        }

//        if(sharedPreferences.contains(PrefsTag + "order_address")){
//            txtOrderDetailInfoOrderName.setText("MS: " +  sharedPreferences.getString(PrefsTag + "order_address", null));
//        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.btnCompleted:
                if(mToken != null && mStaffID != null){
                    JSONObject params = new JSONObject();

                    try {
                        params.put("token",mToken);
                        params.put("staff_id",mStaffID);
                        params.put("order_id",orderId);

                        new DeliveryCompleteOrderHttpRequest(getActivity(), null, this).execute(params);

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }

                break;
        }
    }

    @Override
    public void onCompleteOrder(JSONObject jsonObject) {
        if(jsonObject !=null) {
            try {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sharedPreferences.edit();

                if (!jsonObject.isNull("error")) {
                    int errorCode = jsonObject.getJSONObject("error").getInt("error_code");

                    if(errorCode == 1015){

                    }
                }

                if(!jsonObject.isNull("status")){
                    int statusCode = jsonObject.getJSONObject("status").getInt("status_code");

                    if(statusCode == 1016){
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

                        editor.commit();

                        Toast.makeText(getActivity(), "Đã giao thành công đơn hàng:\n" + txtOrderDetailInfoOrderName.getText(), Toast.LENGTH_SHORT).show();

                        Intent intentCallBackData = new Intent();
                        intentCallBackData.putExtra(CALL_BACK_ORDER_COMPLETED_FLAG, true);
                        getActivity().setResult(Activity.RESULT_OK, intentCallBackData);

                        getActivity().finish();
                    }
                }
            } catch (JSONException e) { e.printStackTrace(); }
        }
    }
}
