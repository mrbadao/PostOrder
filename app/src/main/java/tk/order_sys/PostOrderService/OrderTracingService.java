package tk.order_sys.PostOrderService;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import tk.order_sys.config.appConfig;

/**
 * Created by mrbadao on 30/04/2015.
 */
public class OrderTracingService extends Service implements LocationListener{
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 0;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10 * 1;

    private LocationManager locationManager;
    private String mProvider;
    Location mCurrentLocation;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        mProvider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(mProvider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mCurrentLocation = locationManager.getLastKnownLocation(mProvider);
        if (mCurrentLocation != null){
            reportLocation();
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
        mCurrentLocation = location;
        reportLocation();
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
        Log.i("Provider", mProvider);
    }

    private void reportLocation(){
        Gson gson = new Gson();
        if(mCurrentLocation != null){
            String jsonCurrentLocation = gson.toJson(mCurrentLocation);
            Intent localIntent = new Intent(appConfig.BROADCAST_LOCATION_ACTION).putExtra(appConfig.DATA_LOCATION_STATUS, jsonCurrentLocation);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }
}
