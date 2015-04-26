package tk.order_sys.postorder;

import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import tk.order_sys.GPSTracer.GpsTracer;

public class OrdersMapActivity extends FragmentActivity implements LocationListener{
    private static int MAP_ZOOM_DEFAULT =15;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 0;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    LocationManager locationManager;
    String mProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_map);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        mProvider = locationManager.getBestProvider(criteria,true);

        locationManager.requestLocationUpdates(mProvider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATE, this);

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
                setUpMap();
            }
        }
    }


    private void setUpMap() {
        mMap.setMyLocationEnabled(true);

        Location mylocation = locationManager.getLastKnownLocation(mProvider);

        if(mylocation!=null){
            onLocationChanged(mylocation);
        }

        mMap.setMyLocationEnabled(true);
        mMap.addMarker(new MarkerOptions().position(new LatLng(10.8000952, 106.61643240000001)).title("Poster"));
    }

    @Override
    public void onLocationChanged(Location location) {

        // Getting latitude of the current location
        double latitude = location.getLatitude();

        // Getting longitude of the current location
        double longitude = location.getLongitude();

        // Creating a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);

        // Showing the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        Polyline line = mMap.addPolyline(new PolylineOptions().
                add(new LatLng(latitude,longitude),
                        new LatLng(10.8000952,106.61643240000001))
                .width(5).color(Color.RED));

        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Criteria criteria = new Criteria();
        mProvider = locationManager.getBestProvider(criteria,true);
        locationManager.requestLocationUpdates(mProvider, MIN_TIME_BW_UPDATES, 0, this);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Criteria criteria = new Criteria();
        mProvider = locationManager.getBestProvider(criteria,true);
        locationManager.requestLocationUpdates(mProvider, MIN_TIME_BW_UPDATES, 0, this);
    }
}
