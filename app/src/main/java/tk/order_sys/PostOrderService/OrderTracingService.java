package tk.order_sys.PostOrderService;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import tk.order_sys.Postorder.OrdersMapActivity;

/**
 * Created by mrbadao on 30/04/2015.
 */
public class OrderTracingService extends Service implements LocationListener, RoutingListener {
    public static final String BROADCAST_ACTION = "OrderTracingService.broadcast";
    public static final String DATA_LOCATION = "OrderTracingService.data.myLocation";
    public static final String DATA_ROUTING = "OrderTracingService.data.route";
    public static final String DATA_POLY_OPTIONS = "OrderTracingService.data.mPolyOptions";
    public static final String DATA_LAST_ORDER_LOCATION = "OrderTracingService.data.mLastOrderLocation";
    public static final String DATA_ROUTING_FAILED = "OrderTracingService.data.mRoutingFailed";

    private static String CURRENT_LOCATION_TAG = "mCurrentLocation";

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 0;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10 * 1;

    private LocationManager locationManager;
    private String mProvider;
    private LatLng mCurrentLocation;
    private LatLng mLastOrderLocation;
    private Routing.TravelMode mTravelMode;

    @Override
    public void onCreate() {
        super.onCreate();

        mCurrentLocation = null;
        mLastOrderLocation = null;
        mTravelMode = Routing.TravelMode.DRIVING;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        mProvider = locationManager.getBestProvider(criteria, true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String intentAction = intent.getAction();

//      Get Lastorder location
        if(intent.hasExtra(OrdersMapActivity.ORDER_TRACING_SERVICE_PARAM_LAST_ORDER_LOCATION)){
            Gson gson = new Gson();
            mLastOrderLocation = gson.fromJson(intent.getStringExtra(OrdersMapActivity.ORDER_TRACING_SERVICE_PARAM_LAST_ORDER_LOCATION), LatLng.class);
        }
//        Toast.makeText(getApplicationContext(), intentAction, Toast.LENGTH_SHORT).show();
        // Setup locationManager
        locationManager.requestLocationUpdates(mProvider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
        Location location = locationManager.getLastKnownLocation(mProvider);

        //get Current Location
        if(location != null){
            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        }else {
            mCurrentLocation = loadSavedLocation();
        }

        if (mCurrentLocation != null){
            if (mLastOrderLocation != null) {
                getRouting(mCurrentLocation, mLastOrderLocation, mTravelMode);
            } else {
                reportLocation();
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if (mLastOrderLocation != null) {
            getRouting(mCurrentLocation, mLastOrderLocation, mTravelMode);
        } else {
            reportLocation();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Criteria criteria = new Criteria();
        mProvider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(mProvider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Criteria criteria = new Criteria();
        mProvider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(mProvider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
    }

    private void reportLocation(){
        Gson gson = new Gson();
        if(mCurrentLocation != null){
            String jsonCurrentLocation = gson.toJson(mCurrentLocation);
            Intent localIntent = new Intent(BROADCAST_ACTION).putExtra(DATA_LOCATION, jsonCurrentLocation);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }

    private LatLng loadSavedLocation(){
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        LatLng latLng = null;

        if (mSharedPreferences.contains(CURRENT_LOCATION_TAG)) {
            String jsonCurrentLocation = mSharedPreferences.getString(CURRENT_LOCATION_TAG, null);
            latLng = gson.fromJson(jsonCurrentLocation, LatLng.class);
        }
        return latLng;
    }

    @Override
    public void onRoutingFailure() {
        Intent localIntent = new Intent(BROADCAST_ACTION).putExtra(DATA_ROUTING_FAILED, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
        Gson gson = new Gson();

        if(mCurrentLocation != null)
        {
            String jsonCurrentLocation = gson.toJson(mCurrentLocation);
            String jsonRoute = gson.toJson(route);
            String jsonPolyOptions = gson.toJson(mPolyOptions);
            String jsonLastOrderLocation = gson.toJson(mLastOrderLocation);

            Intent localIntent = new Intent(BROADCAST_ACTION);

            localIntent.putExtra(DATA_LOCATION, jsonCurrentLocation);
            localIntent.putExtra(DATA_POLY_OPTIONS, jsonPolyOptions);
            localIntent.putExtra(DATA_ROUTING, jsonRoute);
            localIntent.putExtra(DATA_LAST_ORDER_LOCATION, jsonLastOrderLocation);

            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
//        Toast.makeText(getApplicationContext(), "Routing ok", Toast.LENGTH_SHORT).show();
    }

    private void getRouting(LatLng fromLatLng, LatLng toLatLng, Routing.TravelMode travelMode) {
        if (fromLatLng != null && toLatLng != null) {
            Routing mRouting = new Routing(travelMode);
            mRouting.registerListener(this);
            mRouting.execute(fromLatLng, toLatLng);
        }
    }
}
