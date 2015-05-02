package tk.order_sys.Postorder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import tk.order_sys.Fragment.LoginFragment;
import tk.order_sys.Fragment.MainFragment;


public class MainActivity extends ActionBarActivity implements LoginFragment.LoginInterface, MainFragment.LogoutInterface {
    FragmentManager fragmentManager;
    SharedPreferences mSharedPreferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.actionbar_logo);

        fragmentManager = getSupportFragmentManager();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (savedInstanceState == null) {
            Fragment fragment = null;

            if(mSharedPreferences != null && mSharedPreferences.contains(LoginFragment.PREF_STAFF_ID_TAG) && mSharedPreferences.contains(LoginFragment.PREF_STAFF_ID_TAG)) {
                fragment = new MainFragment();
            }else {
                fragment = new LoginFragment();
            }

            fragmentManager.beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_map:
                Intent intentOrdersMap = new Intent(getApplicationContext(), OrdersMapActivity.class);
                startActivity(intentOrdersMap);
                return true;

            case R.id.action_settings:
                Intent intentSettings =  new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intentSettings);
                return true;
            case R.id.action_logout:
                onLogoutSuccess();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoginSuccess() {
        fragmentManager.beginTransaction()
                .replace(R.id.container, new MainFragment())
                .commit();
    }

    public void onShowHideActionBar(boolean flag){
        if (flag) getSupportActionBar().show();
        else {
            getSupportActionBar().hide();
        }
    }

    @Override
    public void onLogoutSuccess() {
        fragmentManager.beginTransaction()
                .replace(R.id.container, new LoginFragment())
                .commit();
    }
}
