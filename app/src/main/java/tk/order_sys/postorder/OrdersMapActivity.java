package tk.order_sys.postorder;

import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

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

import java.util.ArrayList;

public class OrdersMapActivity extends FragmentActivity implements LocationListener, RoutingListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMarkerClickListener {
    private static int MAP_ZOOM_DEFAULT = 15;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 0;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10 * 1;


    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ArrayList<Marker> mOrderMarkersArrayList;
    private String mCurrenOrederMarkerIndex;
    LocationManager locationManager;
    String mProvider;
    Location mCurrentLocation;
    Polyline mRoutingLastPolyLine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_map);

        mCurrentLocation = null;
        mRoutingLastPolyLine = null;
        mCurrenOrederMarkerIndex = null;
        mOrderMarkersArrayList = new ArrayList<Marker>();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        mProvider = locationManager.getBestProvider(criteria, true);

        Log.i("Provider", mProvider);

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                locationManager.requestLocationUpdates(mProvider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
                mCurrentLocation = locationManager.getLastKnownLocation(mProvider);
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

        if (mCurrentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(MAP_ZOOM_DEFAULT));
        }

        addOrderMarkers();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Criteria criteria = new Criteria();
        mProvider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(mProvider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
        Log.i("Provider", mProvider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Criteria criteria = new Criteria();
        mProvider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(mProvider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
        Log.i("Provider", mProvider);
    }

    @Override
    public void onRoutingFailure() {
        mCurrenOrederMarkerIndex = null;
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

        if(mCurrenOrederMarkerIndex != null){
            for (int i=0; i < mOrderMarkersArrayList.size(); i++){
                mOrderMarkersArrayList.get(i).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                if (mOrderMarkersArrayList.get(i).getId().equals(mCurrenOrederMarkerIndex)){
                    mOrderMarkersArrayList.get(i);
                    mOrderMarkersArrayList.get(i).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }
            }
        }

        Toast.makeText(
                getApplicationContext(),
                "Khoảng cách: " + route.getDistanceText() + "\n" + "Thời gian dự tính: " + route.getDurationText(),
                Toast.LENGTH_SHORT
        ).show();



        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Routing mRouting = new Routing(Routing.TravelMode.DRIVING);
        mRouting.registerListener(this);
        mRouting.execute(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), marker.getPosition());
        mCurrenOrederMarkerIndex = marker.getId();
        Log.i("MarkerIdx",mCurrenOrederMarkerIndex);
        return false;
    }

    private void addOrderMarkers() {
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
        if (mCurrentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())));
        }
        return false;
    }
}
