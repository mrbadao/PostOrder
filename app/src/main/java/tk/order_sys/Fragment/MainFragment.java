package tk.order_sys.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import tk.order_sys.Adapter.OrdersAdapter;
import tk.order_sys.HttpRequest.DeliveryGetOrdersHttpRequest;
import tk.order_sys.HttpRequestInterface.OrdersInterface;
import tk.order_sys.Postorder.MainActivity;
import tk.order_sys.Postorder.OrderDetailActivity;
import tk.order_sys.Postorder.R;
import tk.order_sys.XListView.view.XListView;
import tk.order_sys.models.ContentOrder;

/**
 * Created by HieuNguyen on 4/22/2015.
 */
public class MainFragment extends Fragment implements XListView.IXListViewListener, OrdersInterface {
    public static final String PREFS_ORDER_TAG = "tk.order_sys.postorder.order";

    private static final String LIST_ORDERS_TAG = "listOrders";
    private static final int ORDERS_DETAIL_ACTIVITY_CODE = 101;
    private static final int LOAD_MORE_ITEMS = 5;

    private View rootView;
    private XListView xListViewOrders;
    private OrdersAdapter mAdapter;
    private ArrayList<ContentOrder> listOrders = new ArrayList<ContentOrder>();
    private Handler mHandler;

    private int page = 1;
    private int pages = 0;

    private String staff_id;
    private String token;
    private boolean isFirstLoad;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).onShowHideActionBar(true);

        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        staff_id = token = null;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (sharedPreferences.contains(LoginFragment.PREF_STAFF_TOKEN_TAG)) {
            token = sharedPreferences.getString(LoginFragment.PREF_STAFF_TOKEN_TAG, null);
            Log.i("token", token);
        }

        if (sharedPreferences.contains(LoginFragment.PREF_STAFF_ID_TAG)) {
            staff_id = sharedPreferences.getString(LoginFragment.PREF_STAFF_ID_TAG, null);
            Log.i("staff_id", staff_id);
        }

        isFirstLoad = true;
        getOrders();

        xListViewOrders = (XListView) rootView.findViewById(R.id.xListViewOrders);
        xListViewOrders.setPullLoadEnable(true);

        xListViewOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String orderId = listOrders.get(position - 1).id;
                String phoneNumber = listOrders.get(position - 1).order_phone;
                String orderName = listOrders.get(position - 1).name;
                if (orderId != null && !orderId.isEmpty() && phoneNumber != null && !phoneNumber.isEmpty() && orderName != null) {
                    Intent intentOrderDetail = new Intent(getActivity().getApplicationContext(), OrderDetailActivity.class);
                    intentOrderDetail.putExtra("orderId", orderId);
                    intentOrderDetail.putExtra("phoneNumber", phoneNumber);
                    intentOrderDetail.putExtra("orderName", orderName);
                    startActivityForResult(intentOrderDetail, ORDERS_DETAIL_ACTIVITY_CODE);
                }
            }
        });

        mHandler = new Handler();
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK && (requestCode == ORDERS_DETAIL_ACTIVITY_CODE)) {
            if (data.hasExtra(OrderDetailInfoFragment.CALL_BACK_ORDER_COMPLETED_FLAG)) {
                boolean flag = data.getBooleanExtra(OrderDetailInfoFragment.CALL_BACK_ORDER_COMPLETED_FLAG, false);
                if (flag) {
                    isFirstLoad = true;
                    xListViewOrders.setAdapter(null);
                    mAdapter = null;
                    page = 1;
                    pages = 0;
                    listOrders.clear();
                    getOrders();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void getOrders() {
        if (token != null && staff_id != null) {
            JSONObject params = new JSONObject();
            try {
                params.put("token", token);
                params.put("staff_id", staff_id);
                params.put("limit", LOAD_MORE_ITEMS);
                params.put("offset", (page - 1) * LOAD_MORE_ITEMS);
                new DeliveryGetOrdersHttpRequest(getActivity(), null, this).execute(params);
            } catch (JSONException e) {
                e.printStackTrace();
            }

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
                page = 1;
                pages = 0;
                listOrders.clear();
                getOrders();
                isFirstLoad = true;
                onLoad();
            }
        }, 2000);
    }

    @Override
    public void onLoadMore() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                page++;
                if (page <= pages && pages != 0) {
                    getOrders();
                    onLoad();
                } else {
                    xListViewOrders.stopLoadMore();
                    xListViewOrders.stopRefresh();
                }
            }
        }, 2000);
    }

    @Override
    public void onGetOrders(JSONObject jsonObject) {
        if (jsonObject != null) {
            Log.i("Orders", jsonObject.toString());
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = sharedPreferences.edit();

            try {
                if (!jsonObject.isNull("count")) {
                    pages = (int) Math.ceil(jsonObject.getDouble("count") / (double) LOAD_MORE_ITEMS);
                    Log.i("paging", String.valueOf(pages));

                    if (pages <= page) {
                        xListViewOrders.setPullLoadEnable(false);
                    }
                }

                if (!jsonObject.isNull("orders")) {
                    JSONArray jsonOrders = jsonObject.getJSONArray("orders");
                    JSONObject jsonOrder = null;
                    Log.i("Count", String.valueOf(jsonOrders.length()));

                    ContentOrder item = null;
                    String PrefsTag = null;

                    for (int i = 0; i < jsonOrders.length(); i++) {
                        jsonOrder = jsonOrders.getJSONObject(i);

                        item = new ContentOrder(
                                jsonOrder.getString("id"),
                                jsonOrder.getString("name"),
                                jsonOrder.getString("customer_name"),
                                jsonOrder.getString("order_phone"),
                                jsonOrder.getString("coordinate_lat"),
                                jsonOrder.getString("coordinate_long"),
                                jsonOrder.getString("delivery_id"),
                                jsonOrder.getString("status"),
                                jsonOrder.getString("created"),
                                jsonOrder.getString("completed"),
                                jsonOrder.getString("customer_address")
                        );
                        Log.i("Item:", item.toString());
                        Log.i("address:", jsonOrder.getString("customer_address"));

                        if (!sharedPreferences.contains(PREFS_ORDER_TAG + "." + item.id)) {
                            PrefsTag = PREFS_ORDER_TAG + "." + item.id + ".";
                            Log.i("Item SP:", PrefsTag);

                            editor.putString(PrefsTag + "name", item.name);
                            editor.putString(PrefsTag + "customer_name", item.customer_name);
                            editor.putString(PrefsTag + "order_phone", item.order_phone);
                            editor.putString(PrefsTag + "coordinate_lat", item.coordinate_lat);
                            editor.putString(PrefsTag + "coordinate_long", item.coordinate_long);
                            editor.putString(PrefsTag + "delivery_id", item.delivery_id);
                            editor.putString(PrefsTag + "status", item.status);
                            editor.putString(PrefsTag + "created", item.created);
                            editor.putString(PrefsTag + "address", item.address);

                        }

                        listOrders.add(item);
                    }

                    editor.commit();

                    if (listOrders.size() > 0) {
                        if (isFirstLoad) {
                            mAdapter = null;
                            mAdapter = new OrdersAdapter(getActivity(), R.layout.orders_list_item, listOrders);
                            xListViewOrders.setAdapter(mAdapter);
                            xListViewOrders.setXListViewListener(this);
                            isFirstLoad = false;
                        } else {
                            mAdapter.notifyDataSetChanged();
                            onLoad();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public interface LogoutInterface {
        void onLogoutSuccess();
    }
}