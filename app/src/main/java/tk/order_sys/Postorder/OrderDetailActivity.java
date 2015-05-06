package tk.order_sys.Postorder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import tk.order_sys.Adapter.OrderDetailSectionsPagerAdapter;
import tk.order_sys.Fragment.MainFragment;


public class OrderDetailActivity extends ActionBarActivity implements ActionBar.TabListener {
    private static final int ORDERS_MAPS_ACTIVITY_CODE = 102;
    OrderDetailSectionsPagerAdapter mSectionsPagerAdapter;

    private String orderId;
    private String phoneNumber;
    private String orderName;

    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        orderId = null;
        phoneNumber = null;
        orderName = null;

        Intent intent = getIntent();

        if (intent.hasExtra("orderId")) {
            orderId = intent.getStringExtra("orderId");
        }

        if(intent.hasExtra("phoneNumber")){
            phoneNumber = intent.getStringExtra("phoneNumber");
        }

        if(intent.hasExtra("orderName")){
            orderName = intent.getStringExtra("orderName");
        }

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.actionbar_logo);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        mSectionsPagerAdapter = new OrderDetailSectionsPagerAdapter(getSupportFragmentManager(), getApplicationContext());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_order_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_map:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                Gson gson = new Gson();
                double lat = 0, lng = 0;
                String mPrefsTag = MainFragment.PREFS_ORDER_TAG + "." + orderId + ".";

                if (sharedPreferences.contains(mPrefsTag + "coordinate_lat") && sharedPreferences.contains(mPrefsTag + "coordinate_long")) {
                    lat = Double.parseDouble(sharedPreferences.getString(mPrefsTag + "coordinate_lat", "0"));
                    lng = Double.parseDouble(sharedPreferences.getString(mPrefsTag + "coordinate_long", "0"));
                }

                Intent intentOrdersMap = new Intent(OrderDetailActivity.this, OrdersMapActivity.class);

                if (lat != 0 & lng != 0) {
                    LatLng latLng = new LatLng(lat, lng);
                    intentOrdersMap.putExtra(OrdersMapActivity.PREF_LAST_ORDER_LOCATION_TAG, gson.toJson(latLng).toString());
                }

                if(orderName != null){
                    intentOrdersMap.putExtra(OrdersMapActivity.PREF_CURRENT_ORDER_NAME_TAG, orderName);
                }

                if(phoneNumber != null){
                    intentOrdersMap.putExtra(OrdersMapActivity.PREF_CURRENT_PHONE_NUMBER_TAG, phoneNumber);
                }

                startActivityForResult(intentOrdersMap, ORDERS_MAPS_ACTIVITY_CODE);

                return true;

            case R.id.action_settings:
                Intent intentSettings = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intentSettings);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public String getOrderId() {
        return orderId;
    }
}
