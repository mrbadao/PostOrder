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
import com.directions.route.RoutingListener;
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
import tk.order_sys.config.appConfig;

public class OrdersMapActivity extends FragmentActivity implements LocationReceiverInterface, RoutingListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMarkerClickListener {
    private static String APP_TAG = "PostOrder";
    private static String LAST_ORDER_LOCATION_TAG = "mLastOrderLocation";
    private static String CURRENT_LOCATION_TAG = "mCurrentLocation";
    private static int MAP_ZOOM_DEFAULT = 12;
    private static int MAP_MY_LOCATION_ZOOM_DEFAULT = 15;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 0;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10 * 1;


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

        Intent mOrderTracingService = new Intent(OrdersMapActivity.this, OrderTracingService.class);
        mOrderTracingService.setAction("tk.order_sys.postorder.startforeground");
        startService(mOrderTracingService);

        mCurrentLocation = null;
        mRoutingLastPolyLine = null;
        mLastOrderLocation = null;
        mCurrenOrederMarkerIndex = null;
        mTravelMode = Routing.TravelMode.DRIVING;
        mOrderMarkersArrayList = new ArrayList<Marker>();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mCurrentLocationReceiver = new LocationReceiver(this);

        IntentFilter mLocationIntentFilter = new IntentFilter(appConfig.BROADCAST_LOCATION_ACTION);

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

            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.setPadding(0, 50, 0, 0);

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                loadSavedData();
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM_DEFAULT));
        addOrderMarkers();

        onMyLocationButtonClick();

        if (mCurrentLocation != null) {
//            LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

            if (mLastOrderLocation != null) {
                getRouting(mCurrentLocation, mLastOrderLocation, mTravelMode);

            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_MY_LOCATION_ZOOM_DEFAULT));
            }
        }
    }

    @Override
    public void onRoutingFailure() {
        mCurrenOrederMarkerIndex = null;
        mLastOrderLocation = null;
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {

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

        mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLocation));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mCurrentLocation != null) {

            mCurrenOrederMarkerIndex = marker.getId();
            mLastOrderLocation = marker.getPosition();

            getRouting(mCurrentLocation, mLastOrderLocation, mTravelMode);

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

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
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
            editor.putString(CURRENT_LOCATION_TAG, jsonCurrentLocation);
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

        if (mSharedPreferences.contains(CURRENT_LOCATION_TAG) && mCurrentLocation == null) {
            String jsonCurrentLocation = mSharedPreferences.getString(CURRENT_LOCATION_TAG, null);
//            mCurrentLocation = gson.fromJson(jsonCurrentLocation, Location.class);
        }
    }

    private void getRouting(LatLng fromLatLng, LatLng toLatLng, Routing.TravelMode travelMode) {
        if (fromLatLng != null && toLatLng != null) {
            Routing mRouting = new Routing(travelMode);
            mRouting.registerListener(this);
            mRouting.execute(fromLatLng, toLatLng);
        }
    }

    @Override
    public void onCurrentLocationReceived(Context context, Intent intent) {
        String jsonCurrentLocation = intent.getStringExtra(appConfig.DATA_LOCATION_STATUS);

        if (jsonCurrentLocation != null) {
            Gson gson = new Gson();
            mCurrentLocation = gson.fromJson(jsonCurrentLocation, LatLng.class);

            if (mLastOrderLocation != null) {
                getRouting(mCurrentLocation, mLastOrderLocation, mTravelMode);
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_MY_LOCATION_ZOOM_DEFAULT));
            }
        }
    }
}
