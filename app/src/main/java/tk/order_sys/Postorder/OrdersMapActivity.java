package tk.order_sys.Postorder;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

import com.directions.route.Route;
import com.directions.route.Routing;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

import tk.order_sys.Dialogs.PostOrderDialog;
import tk.order_sys.PostOrderBroadcastReceiver.LocationReceiver;
import tk.order_sys.PostOrderInterface.LocationReceiverInterface;
import tk.order_sys.PostOrderService.OrderTracingService;

public class OrdersMapActivity extends FragmentActivity implements LocationReceiverInterface, GoogleMap.OnMarkerClickListener {
    public static String LAST_ORDER_LOCATION_TAG = "mLastOrderLocation";
    public static String ORDER_TRACING_SERVICE_ACTION_GET_ROUTING = "OrderTracingService.action.getRouting";
    public static String ORDER_TRACING_SERVICE_ACTION_CLOSE = "OrderTracingService.action.close";
    public static String ORDER_TRACING_SERVICE_PARAM_LAST_ORDER_LOCATION = "OrderTracingService.param.mLastOrderLocation";
    public static String ORDER_TRACING_SERVICE_PARAM_ORDER_NAME = "OrderTracingService.param.mOrderName";
    public static String ORDER_TRACING_SERVICE_PARAM_PHONE_NUMBER = "OrderTracingService.param.mPhoneNumber";

    public static String PREF_CURRENT_LOCATION_TAG = "mCurrentLocation";
    public static String PREF_CURRENT_PHONE_NUMBER_TAG = "mPhoneNumber";
    public static String PREF_CURRENT_ORDER_NAME_TAG = "mOrderName";
    public static String PREF_LAST_ORDER_LOCATION_TAG = "mLastOrderLocation";

    private static int MAP_ZOOM_DEFAULT = 12;
    private static int MAP_MY_LOCATION_ZOOM_DEFAULT = 15;


    private GoogleMap mMap;
    private TextView txtStatus;
    private ArrayList<Marker> mOrderMarkersArrayList;
    private String mCurrenOrederMarkerIndex;
    Polyline mRoutingLastPolyLine;

    private Routing.TravelMode mTravelMode;

    SharedPreferences mSharedPreferences = null;
    LocationReceiver mCurrentLocationReceiver;

    LatLng mCurrentLocation;
    LatLng mLastOrderLocation;
    String mPhoneNumber;
    String mOrderName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_map);

        txtStatus = (TextView) findViewById(R.id.txtOrderStatus);

        mCurrentLocation = null;
        mRoutingLastPolyLine = null;
        mLastOrderLocation = null;
        mPhoneNumber= null;
        mOrderName = null;
        mCurrenOrederMarkerIndex = null;
        mTravelMode = Routing.TravelMode.DRIVING;
        mOrderMarkersArrayList = new ArrayList<Marker>();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mCurrentLocationReceiver = new LocationReceiver(this);

        Intent intent = getIntent();

        if (intent.hasExtra(PREF_LAST_ORDER_LOCATION_TAG)) {
            String jsonLastOrderLocation = intent.getStringExtra(PREF_LAST_ORDER_LOCATION_TAG);
            if (!jsonLastOrderLocation.isEmpty()) {
                Gson gson = new Gson();
                mLastOrderLocation = gson.fromJson(jsonLastOrderLocation, LatLng.class);
            }
        }

        if(intent.hasExtra(PREF_CURRENT_ORDER_NAME_TAG)){
            mOrderName = intent.getStringExtra(PREF_CURRENT_ORDER_NAME_TAG);
        }

        if(intent.hasExtra(PREF_CURRENT_PHONE_NUMBER_TAG)){
            mPhoneNumber = intent.getStringExtra(PREF_CURRENT_PHONE_NUMBER_TAG);
        }

        IntentFilter mLocationIntentFilter = new IntentFilter(OrderTracingService.BROADCAST_ACTION);

        LocalBroadcastManager.getInstance(this).registerReceiver(mCurrentLocationReceiver, mLocationIntentFilter);

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onPause() {
        saveData();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        saveData();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        saveData();
        super.onStop();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            mMap.setPadding(0, 60, 0, 0);

            if (mMap != null) {
                loadSavedData();

                Intent mOrderTracingService = new Intent(OrdersMapActivity.this, OrderTracingService.class);
                mOrderTracingService.setAction(ORDER_TRACING_SERVICE_ACTION_GET_ROUTING);

                if (mLastOrderLocation != null) {
                    Gson gson = new Gson();
                    String jsonLastOrderLocation = gson.toJson(mLastOrderLocation);
                    mOrderTracingService.putExtra(ORDER_TRACING_SERVICE_PARAM_LAST_ORDER_LOCATION, jsonLastOrderLocation);
                }

                if(mOrderName != null){
                    mOrderTracingService.putExtra(ORDER_TRACING_SERVICE_PARAM_ORDER_NAME, mOrderName);
                }

                if(mPhoneNumber != null){
                    mOrderTracingService.putExtra(ORDER_TRACING_SERVICE_PARAM_PHONE_NUMBER, mPhoneNumber);
                }

                startService(mOrderTracingService);

                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM_DEFAULT));

        addOrderMarkers();

        if (mCurrentLocation != null) {
//            if (mLastOrderLocation == null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_MY_LOCATION_ZOOM_DEFAULT));
//            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
//        if (mCurrentLocation != null) {
//        mCurrenOrederMarkerIndex = marker.getId();
//        mLastOrderLocation = marker.getPosition();
//
//        Intent mOrderTracingService = new Intent(OrdersMapActivity.this, OrderTracingService.class);
//        mOrderTracingService.setAction(ORDER_TRACING_SERVICE_ACTION_GET_ROUTING);
//
//        if (mLastOrderLocation != null) {
//            Gson gson = new Gson();
//            String jsonLastOrderLocation = gson.toJson(mLastOrderLocation);
//            mOrderTracingService.putExtra(ORDER_TRACING_SERVICE_PARAM_LAST_ORDER_LOCATION, jsonLastOrderLocation);
//
//
//        }
//
//        if(mOrderName != null){
//            mOrderTracingService.putExtra(ORDER_TRACING_SERVICE_PARAM_ORDER_NAME, mOrderName);
//        }
//
//        if(mPhoneNumber != null){
//            mOrderTracingService.putExtra(ORDER_TRACING_SERVICE_PARAM_PHONE_NUMBER, mPhoneNumber);
//        }
//
//        startService(mOrderTracingService);

//        }

        return false;
    }

    private void addOrderMarkers() {
        String key = null;
        String tilte = null;
        mOrderMarkersArrayList.clear();

        Map<String, ?> mapOrders = mSharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : mapOrders.entrySet()) {
            key = entry.getKey();
            if (key.matches("tk.order_sys.postorder.order.[0-9]*.name")) {
                key = key.substring(0, entry.getKey().length() - 4);

                tilte = entry.getValue().toString();

                if (mapOrders.containsKey(key + "coordinate_lat") && mapOrders.containsKey(key + "coordinate_long")) {
                    Double lat = Double.parseDouble(mapOrders.get(key + "coordinate_lat").toString());
                    Double lng = Double.parseDouble(mapOrders.get(key + "coordinate_long").toString());

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(lat, lng));
                    markerOptions.title(tilte);

                    if (mLastOrderLocation != null && mLastOrderLocation.latitude == lat && mLastOrderLocation.longitude == lng) {
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    } else {
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    }

                    mOrderMarkersArrayList.add(mMap.addMarker(markerOptions));
                }
            }
        }

        mMap.setOnMarkerClickListener(this);
    }


    public void saveData() {
        if (mSharedPreferences == null) return;

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        Gson gson = new Gson();

        if (mLastOrderLocation != null) {
            String jsonLastOrderLocation = gson.toJson(mLastOrderLocation);
            editor.putString(LAST_ORDER_LOCATION_TAG, jsonLastOrderLocation);
        }

        if (mCurrentLocation != null) {
            String jsonCurrentLocation = gson.toJson(mCurrentLocation);
            editor.putString(PREF_CURRENT_LOCATION_TAG, jsonCurrentLocation);
        }

        if(mOrderName != null){
            editor.putString(PREF_CURRENT_ORDER_NAME_TAG, mOrderName);
        }

        if(mPhoneNumber != null){
            editor.putString(PREF_CURRENT_PHONE_NUMBER_TAG, mPhoneNumber);
        }

        editor.commit();
    }

    public void loadSavedData() {
        if (mSharedPreferences == null) return;

        Gson gson = new Gson();
        if (mSharedPreferences.contains(PREF_LAST_ORDER_LOCATION_TAG) && mLastOrderLocation == null) {
            String jsonLastOrderLocation = mSharedPreferences.getString(PREF_LAST_ORDER_LOCATION_TAG, null);
            mLastOrderLocation = gson.fromJson(jsonLastOrderLocation, LatLng.class);
        }

        if (mSharedPreferences.contains(PREF_CURRENT_ORDER_NAME_TAG) && mOrderName == null) {
            mOrderName = mSharedPreferences.getString(PREF_CURRENT_ORDER_NAME_TAG, null);
        }
        if (mSharedPreferences.contains(PREF_CURRENT_PHONE_NUMBER_TAG) && mPhoneNumber == null) {
            mPhoneNumber = mSharedPreferences.getString(PREF_CURRENT_PHONE_NUMBER_TAG, null);
        }
    }


    @Override
    public void onCurrentLocationReceived(Context context, Intent intent) {

        if (intent.hasExtra(OrderTracingService.SERVICE_START_FAILED)) {
            PostOrderDialog.showAlertDialog(this, "Cảnh báo", " Ứng dụng cầnđược truy cập mạng và vị trí của bạn. Hãy kiểm tra thiết bị và thữ lại");
            return;
        }

        if (intent.hasExtra(OrderTracingService.DATA_ROUTING_FAILED)) {
            mCurrenOrederMarkerIndex = null;
            mLastOrderLocation = null;
            return;
        }

        Route route = null;
        PolylineOptions mPolyOptions = null;

        Gson gson = new Gson();

        if (intent.hasExtra(OrderTracingService.DATA_LOCATION)) {
            mCurrentLocation = gson.fromJson(intent.getStringExtra(OrderTracingService.DATA_LOCATION), LatLng.class);
        }

        if (intent.hasExtra(OrderTracingService.DATA_ROUTING)) {
            route = gson.fromJson(intent.getStringExtra(OrderTracingService.DATA_ROUTING), Route.class);
        }

        if (intent.hasExtra(OrderTracingService.DATA_POLY_OPTIONS)) {
            mPolyOptions = gson.fromJson(intent.getStringExtra(OrderTracingService.DATA_POLY_OPTIONS), PolylineOptions.class);
        }

        if (intent.hasExtra(OrderTracingService.DATA_LAST_ORDER_LOCATION)) {
            mLastOrderLocation = gson.fromJson(intent.getStringExtra(OrderTracingService.DATA_LAST_ORDER_LOCATION), LatLng.class);
        }

        if (intent.hasExtra(OrderTracingService.DATA_LAST_ORDER_NAME)) {
            mOrderName = intent.getStringExtra(OrderTracingService.DATA_LAST_ORDER_NAME);
        }

        if (intent.hasExtra(OrderTracingService.DATA_LAST_PHONE_NUMBER)) {
            mPhoneNumber = intent.getStringExtra(OrderTracingService.DATA_LAST_PHONE_NUMBER);
        }

        if (route != null && mPolyOptions != null) {
            if (mRoutingLastPolyLine != null) {
                mRoutingLastPolyLine.remove();
            }

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(Color.RED);
            polyOptions.width(5);
            polyOptions.addAll(mPolyOptions.getPoints());

            mRoutingLastPolyLine = mMap.addPolyline(polyOptions);

            if (mCurrenOrederMarkerIndex != null) {
                for (int i = 0; i < mOrderMarkersArrayList.size(); i++) {
                    mOrderMarkersArrayList.get(i).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    if (mOrderMarkersArrayList.get(i).getId().equals(mCurrenOrederMarkerIndex)) {
                        mOrderMarkersArrayList.get(i);
                        mOrderMarkersArrayList.get(i).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }
                }
            }

            txtStatus.setText(mOrderName + "\nKhoảng cách: " + route.getDistanceText() + "\n" + "Thời gian dự tính: " + route.getDurationText());
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLocation));
    }
}
