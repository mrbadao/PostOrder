package tk.order_sys.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tk.order_sys.Postorder.R;

/**
 * Created by mrbadao on 30/04/2015.
 */
public class OrderDetailProductsFragment extends Fragment {
    View rootView;

    public OrderDetailProductsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_order_detail_products, container, false);
        return rootView;
    }
}
