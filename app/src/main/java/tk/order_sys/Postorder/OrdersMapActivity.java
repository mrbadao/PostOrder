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
import android.util.Log;
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

import tk.order_sys.PostOrderBroadcastReceiver.LocationReceiver;
import tk.order_sys.PostOrderInterface.LocationReceiverInterface;
import tk.order_sys.PostOrderService.OrderTracingService;

public class OrdersMapActivity extends FragmentActivity implements LocationReceiverInterface, GoogleMap.OnMarkerClickListener {    private static String LAST_ORDER_LOCATION_TAG = "mLastOrderLocation";
    public static String ORDER_TRACING_SERVICE_ACTION_GET_ROUTING = "OrderTracingService.action.getRouting";
    public static String ORDER_TRACING_SERVICE_PARAM_LAST_ORDER_LOCATION = "OrderTracingService.param.mLastOrderLocation";

    private static String PREF_CURRENT_LOCATION_TAG = "mCurrentLocation";

    private static int MAP_ZOOM_DEFAULT = 12;
    private static int MAP_MY_LOCATION_ZOOM_DEFAULT = 15;


    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private TextView txtStatus;
    private ArrayList<Marker> mOrderMarkersArrayList;
    private String mCurrenOrederMarkerIndex;

    private Routing.TravelMode mTravelMode;

    SharedPreferences mSharedPreferences = null;
    LatLng mCurrentLocation;
    LatLng mLastOrderLocation;
    Polyline mRoutingLastPolyLine;
    LocationReceiver mCurrentLocationReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_map);

        txtStatus = (TextView) findViewById(R.id.txtOrderStatus);

        mCurrentLocation = null;
        mRoutingLastPolyLine = null;
        mLastOrderLocation = null;
        mCurrenOrederMarkerIndex = null;
        mTravelMode = Routing.TravelMode.DRIVING;
        mOrderMarkersArrayList = new ArrayList<Marker>();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mCurrentLocationReceiver = new LocationReceiver(this);

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

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            mMap.setPadding(0, 50, 0, 0);

            if (mMap != null) {
                loadSavedData();

                Intent mOrderTracingService = new Intent(OrdersMapActivity.this, OrderTracingService.class);
                mOrderTracingService.setAction(ORDER_TRACING_SERVICE_ACTION_GET_ROUTING);

                if (mSharedPreferences != null) {
                    Gson gson = new Gson();
                    if (mSharedPreferences.contains(LAST_ORDER_LOCATION_TAG)) {
                        String jsonLastOrderLocation = mSharedPreferences.getString(LAST_ORDER_LOCATION_TAG, null);
                        mOrderTracingService.putExtra(ORDER_TRACING_SERVICE_PARAM_LAST_ORDER_LOCATION, jsonLastOrderLocation);
                    }
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
            if (mLastOrderLocation == null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_MY_LOCATION_ZOOM_DEFAULT));
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mCurrentLocation != null) {
            mCurrenOrederMarkerIndex = marker.getId();
            mLastOrderLocation = marker.getPosition();

            if (mSharedPreferences != null && mLastOrderLocation !=null)
            {
                Intent mOrderTracingService = new Intent(OrdersMapActivity.this, OrderTracingService.class);
                mOrderTracingService.setAction(ORDER_TRACING_SERVICE_ACTION_GET_ROUTING);

                Gson gson = new Gson();

                String jsonLastOrderLocation = gson.toJson(mLastOrderLocation);
                mOrderTracingService.putExtra(ORDER_TRACING_SERVICE_PARAM_LAST_ORDER_LOCATION, jsonLastOrderLocation);

                startService(mOrderTracingService);
            }
        }

        return false;
    }

    private void addOrderMarkers() {

        if (mOrderMarkersArrayList.size() > 0) {
            Log.i("Maker", "restore");
            return;
        }

        Log.i("Maker", "new");

        mOrderMarkersArrayList.add(mMap.addMarker(
                new MarkerOptions().position(
                        new LatLng(10.8000952, 106.61643240000001))
                        .title("AEONMALL Tan Phu Celadon")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        ));

        mOrderMarkersArrayList.add(mMap.addMarker(new MarkerOptions().position(
                        new LatLng(10.8124513, 106.67860859999996))
                        .title("Big C Gò Vấp")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        ));

        mOrderMarkersArrayList.add(mMap.addMarker(new MarkerOptions().position(
                        new LatLng(10.801811572755648, 106.64007067680359))
                        .title("Etown")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        ));

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

        editor.commit();
    }

    public void loadSavedData() {
        if (mSharedPreferences == null) return;

        Gson gson = new Gson();

        if (mSharedPreferences.contains(LAST_ORDER_LOCATION_TAG)) {
            String jsonLastOrderLocation = mSharedPreferences.getString(LAST_ORDER_LOCATION_TAG, null);
            mLastOrderLocation = gson.fromJson(jsonLastOrderLocation, LatLng.class);
        }
    }


    @Override
    public void onCurrentLocationReceived(Context context, Intent intent) {

        if (intent.hasExtra(OrderTracingService.DATA_ROUTING_FAILED)){
            mCurrenOrederMarkerIndex = null;
            mLastOrderLocation = null;
            return;
        }

        Route route = null;
        PolylineOptions mPolyOptions = null;

        Gson gson = new Gson();

        if(intent.hasExtra(OrderTracingService.DATA_LOCATION)) {
            mCurrentLocation = gson.fromJson(intent.getStringExtra(OrderTracingService.DATA_LOCATION), LatLng.class);
        }

        if(intent.hasExtra(OrderTracingService.DATA_ROUTING)){
            route = gson.fromJson(intent.getStringExtra(OrderTracingService.DATA_ROUTING), Route.class);
        }

        if(intent.hasExtra(OrderTracingService.DATA_POLY_OPTIONS)){
            mPolyOptions = gson.fromJson(intent.getStringExtra(OrderTracingService.DATA_POLY_OPTIONS), PolylineOptions.class);
        }

        if(intent.hasExtra(OrderTracingService.DATA_LOCATION)){
            mLastOrderLocation =  gson.fromJson(intent.getStringExtra(OrderTracingService.DATA_LAST_ORDER_LOCATION), LatLng.class);
        }

        if(route != null && mPolyOptions != null) {
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

            txtStatus.setText("Khoảng cách: " + route.getDistanceText() + "\n" + "Thời gian dự tính: " + route.getDurationText());
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLocation));
    }
}
