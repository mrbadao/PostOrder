package tk.order_sys.Fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import tk.order_sys.Postorder.OrderDetailActivity;
import tk.order_sys.Postorder.R;

/**
 * Created by mrbadao on 30/04/2015.
 */
public class OrderDetailInfoFragment extends Fragment {
    View rootView;
    TextView txtOrderDetailInfoCustomerName, txtOrderDetailInfoPhone, txtOrderDetailInfoCreated, txtOrderDetailInfoOrderName, txtOrderDetailInfoAddress;
    private String orderId;

    public OrderDetailInfoFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_order_detail_info, container, false);

        txtOrderDetailInfoCustomerName = (TextView) rootView.findViewById(R.id.txtOrderDetailInfoCustomerName);
        txtOrderDetailInfoPhone = (TextView) rootView.findViewById(R.id.txtOrderDetailInfoPhone);
        txtOrderDetailInfoCreated = (TextView) rootView.findViewById(R.id.txtOrderDetailInfoCreated);
        txtOrderDetailInfoOrderName = (TextView) rootView.findViewById(R.id.txtOrderDetailInfoOrderName);
        txtOrderDetailInfoAddress = (TextView) rootView.findViewById(R.id.txtOrderDetailInfoAddress);

        orderId = null;
        orderId = ((OrderDetailActivity)getActivity()).getOrderId();

        return rootView;
    }

    private void render(){
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String PrefsTag = MainFragment.PREFS_ORDER_TAG + "." + orderId + ".";

        if(sharedPreferences.contains(PrefsTag + "name")){
            txtOrderDetailInfoOrderName.setText("MS: " +  sharedPreferences.getString(PrefsTag + "name", null));
        }

        if(sharedPreferences.contains(PrefsTag + "customer_name")){
            Toast.makeText(getActivity().getApplicationContext(),sharedPreferences.getString(PrefsTag + "customer_name", null),Toast.LENGTH_SHORT).show();
            txtOrderDetailInfoCustomerName.setText(sharedPreferences.getString(PrefsTag + "customer_name", null));
        }

        if(sharedPreferences.contains(PrefsTag + "created")){
            txtOrderDetailInfoCreated.setText(sharedPreferences.getString(PrefsTag + "created", null));
        }

        if(sharedPreferences.contains(PrefsTag + "order_phone")){
            txtOrderDetailInfoCreated.setText(sharedPreferences.getString(PrefsTag + "order_phone", null));
        }

//        if(sharedPreferences.contains(PrefsTag + "order_address")){
//            txtOrderDetailInfoOrderName.setText("MS: " +  sharedPreferences.getString(PrefsTag + "order_address", null));
//        }
    }
}
