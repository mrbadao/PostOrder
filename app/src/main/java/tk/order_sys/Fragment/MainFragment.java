package tk.order_sys.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.os.Handler;

import java.util.ArrayList;

import tk.order_sys.XListView.view.XListView;
import tk.order_sys.postorder.R;

/**
 * Created by HieuNguyen on 4/22/2015.
 */
public class MainFragment extends Fragment implements XListView.IXListViewListener {
    private View rootView;
    private XListView xListViewOrders;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> listOrders = new ArrayList<String>();
    private Handler mHandler;

    private int start = 0;

    private static int refreshCnt = 0;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        getOrders();

        xListViewOrders = (XListView) rootView.findViewById(R.id.xListViewOrders);
        xListViewOrders.setPullLoadEnable(true);
        xListViewOrders.setXListViewListener(this);
        mAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item, listOrders);
        xListViewOrders.setAdapter(mAdapter);

        mHandler = new Handler();
        return rootView;
    }

    private void getOrders() {
        for (int i = 0; i != 20; ++i) {
            listOrders.add("Order_" + (++start));
        }
    }

    private void onLoad() {
        xListViewOrders.stopRefresh();
        xListViewOrders.stopLoadMore();
        xListViewOrders.setRefreshTime("NOW");
    }

    @Override
    public void onRefresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                start = ++refreshCnt;
                listOrders.clear();
                getOrders();
                // mAdapter.notifyDataSetChanged();
                mAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item, listOrders);
                xListViewOrders.setAdapter(mAdapter);
                onLoad();
            }
        }, 2000);
    }

    @Override
    public void onLoadMore() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getOrders();
                mAdapter.notifyDataSetChanged();
                onLoad();
            }
        }, 2000);
    }
}