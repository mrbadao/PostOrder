package tk.order_sys.PostOrderService;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.gsm.SmsManager;

import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import tk.order_sys.Postorder.OrdersMapActivity;
import tk.order_sys.Postorder.R;
import tk.order_sys.config.Constants;

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

    private static String PREF_CURRENT_LOCATION_TAG = "mCurrentLocation";

    private LocationManager locationManager;
    private String mProvider;
    private LatLng mCurrentLocation;
    private LatLng mLastOrderLocation;
    private Routing.TravelMode mTravelMode;

    private long mGpsUpdateMinDistance;
    private long mGpsUpdateMinTime;

    private boolean isSendNoticeSms;
    private int distanceSendNoticeSms;

    private int mNotifyId = 104;
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
        loadAppSetting();

        String intentAction = intent.getAction();

        if(intentAction.equals(OrdersMapActivity.ORDER_TRACING_SERVICE_ACTION_GET_ROUTING)) {
//          Get Lastorder location
            if (intent.hasExtra(OrdersMapActivity.ORDER_TRACING_SERVICE_PARAM_LAST_ORDER_LOCATION)) {
                Gson gson = new Gson();
                mLastOrderLocation = gson.fromJson(intent.getStringExtra(OrdersMapActivity.ORDER_TRACING_SERVICE_PARAM_LAST_ORDER_LOCATION), LatLng.class);
            }
//          Toast.makeText(getApplicationContext(), intentAction, Toast.LENGTH_SHORT).show();
            // Setup locationManager
            locationManager.requestLocationUpdates(mProvider, mGpsUpdateMinTime, mGpsUpdateMinDistance, this);
            Location location = locationManager.getLastKnownLocation(mProvider);

            //get Current Location
            if (location != null) {
                mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            } else {
                mCurrentLocation = loadSavedLocation();
            }

            if (mCurrentLocation != null) {
                if (mLastOrderLocation != null) {
                    getRouting(mCurrentLocation, mLastOrderLocation, mTravelMode);
                } else {
                    reportLocation();
                }
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
        locationManager.requestLocationUpdates(mProvider, mGpsUpdateMinTime, mGpsUpdateMinDistance, this);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Criteria criteria = new Criteria();
        mProvider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(mProvider, mGpsUpdateMinTime, mGpsUpdateMinDistance, this);
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

        if (mSharedPreferences.contains(PREF_CURRENT_LOCATION_TAG)) {
            String jsonCurrentLocation = mSharedPreferences.getString(PREF_CURRENT_LOCATION_TAG, null);
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

        if(isSendNoticeSms && distanceSendNoticeSms >= route.getLength()){
            sendNoticeSms();
        }
    }

    private void getRouting(LatLng fromLatLng, LatLng toLatLng, Routing.TravelMode travelMode) {
        if (fromLatLng != null && toLatLng != null) {
            Routing mRouting = new Routing(travelMode);
            mRouting.registerListener(this);
            mRouting.execute(fromLatLng, toLatLng);
        }
    }

    private void loadAppSetting(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (sharedPreferences.contains(Constants.SETTING_GPS_UPDATE_DISTANCE)){
            mGpsUpdateMinDistance = Long.parseLong(sharedPreferences.getString(Constants.SETTING_GPS_UPDATE_DISTANCE, "0"));
        }

        if (sharedPreferences.contains(Constants.SETTING_GPS_UPDATE_MIN_TIME)){
            mGpsUpdateMinTime = 1000 * 10 * Long.parseLong(sharedPreferences.getString(Constants.SETTING_GPS_UPDATE_MIN_TIME, "1"));
        }

        if (sharedPreferences.contains(Constants.SETTING_SMS_SEND_FLAG)){
            isSendNoticeSms = sharedPreferences.getBoolean(Constants.SETTING_SMS_SEND_FLAG, true);
        }

        if (sharedPreferences.contains(Constants.SETTING_SMS_SEND_DISTANCE)){
            distanceSendNoticeSms = Integer.parseInt(sharedPreferences.getString(Constants.SETTING_SMS_SEND_DISTANCE, "1000"));
        }
    }

    private void sendNotification(String msg){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_actionbar_ico)
                        .setContentTitle("Delivery man")
                        .setContentText(msg);

        Intent resultIntent = new Intent(this, OrdersMapActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addParentStack(OrdersMapActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(mNotifyId, mBuilder.build());
    }

    private void sendNoticeSms(){
        String message = "Sắp tới";

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, OrderTracingService.class), 0);

        SmsManager sms = null;
        try {
            sms = SmsManager.getDefault();
            sms.sendTextMessage("0929028027", null, message, pendingIntent, null);
            sendNotification("Gỡi tin nhắn nhắc nhỡ.");
        } catch (Exception e) {
            sendNotification("Có lỗi trong quá trình gỡi sms.");
            e.printStackTrace();
        }
    }
}
