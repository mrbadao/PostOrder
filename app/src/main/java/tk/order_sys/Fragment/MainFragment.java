package tk.order_sys.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import tk.order_sys.Adapter.OrdersAdapter;
import tk.order_sys.Postorder.MainActivity;
import tk.order_sys.Postorder.OrderDetailActivity;
import tk.order_sys.Postorder.R;
import tk.order_sys.XListView.view.XListView;
import tk.order_sys.config.appConfig;
import tk.order_sys.models.ContentOrder;

/**
 * Created by HieuNguyen on 4/22/2015.
 */
public class MainFragment extends Fragment implements XListView.IXListViewListener {
    private static final String LIST_ORDERS_TAG = "listOrders";
    private static final int ORDERS_DETAIL_ACTIVITY_CODE = 101;

    private View rootView;
    private SharedPreferences sharedPreferences;
    private XListView xListViewOrders;
    private OrdersAdapter mAdapter;
    private ArrayList<ContentOrder> listOrders = new ArrayList<ContentOrder>();
    private Handler mHandler;

    private int start = 0;

    private static int refreshCnt = 0;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).onShowHideActionBar(true);

        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        sharedPreferences = getActivity().getSharedPreferences(appConfig.getSharePreferenceTag(), Context.MODE_PRIVATE);

        if(sharedPreferences.contains(appConfig.getSharePreferenceTag(LIST_ORDERS_TAG))){
        }

        getOrders();

        mAdapter = new OrdersAdapter(getActivity().getApplicationContext(), R.layout.orders_list_item,listOrders);

        xListViewOrders = (XListView) rootView.findViewById(R.id.xListViewOrders);
        xListViewOrders.setPullLoadEnable(true);
        xListViewOrders.setXListViewListener(this);
        xListViewOrders.setAdapter(mAdapter);
        xListViewOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intentOrderDetail = new Intent(getActivity().getApplicationContext(), OrderDetailActivity.class);
                getActivity().startActivityForResult(intentOrderDetail,ORDERS_DETAIL_ACTIVITY_CODE);
            }
        });

        mHandler = new Handler();
        return rootView;
    }

    private void getOrders() {
        for (int i = 0; i != 20; ++i) {
            listOrders.add(new ContentOrder("1","Order_" + String.valueOf(++start), "1","1","1","1","1","1","1","1"));
        }
    }

    private void onLoad() {
        xListViewOrders.stopRefresh();
        xListViewOrders.stopLoadMore();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        Date date = new Date();
        xListViewOrders.setRefreshTime(dateFormat.format(date));
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
                mAdapter = new OrdersAdapter(getActivity(), R.layout.orders_list_item, listOrders);
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

    public static interface LogoutInterface{
        public void onLogoutSuccess();
    }
}