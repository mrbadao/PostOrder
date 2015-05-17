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

import org.json.JSONException;
import org.json.JSONObject;

import tk.order_sys.Dialogs.PostOrderDialog;
import tk.order_sys.Fragment.LoginFragment;
import tk.order_sys.Fragment.MainFragment;
import tk.order_sys.HttpRequest.DeliveryCheckTokenHttpRequest;
import tk.order_sys.HttpRequestInterface.DeliveryInterface;
import tk.order_sys.config.appConfig;


public class MainActivity extends ActionBarActivity implements LoginFragment.LoginInterface, MainFragment.LogoutInterface, DeliveryInterface {
    FragmentManager fragmentManager;
    SharedPreferences mSharedPreferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.actionbar_logo);
        getSupportActionBar().hide();

        fragmentManager = getSupportFragmentManager();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(savedInstanceState == null){
            Init();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void Init() {
        if (!appConfig.isNetworkAvailable(this)) {
            PostOrderDialog.showNetworkAlertDialog(this);
        } else {
            if (mSharedPreferences != null && mSharedPreferences.contains(LoginFragment.PREF_STAFF_TOKEN_TAG) && mSharedPreferences.contains(LoginFragment.PREF_STAFF_ID_TAG)) {
                String Token = mSharedPreferences.getString(LoginFragment.PREF_STAFF_TOKEN_TAG, null);
                String StaffID = mSharedPreferences.getString(LoginFragment.PREF_STAFF_ID_TAG, null);

                JSONObject params = new JSONObject();
                try {
                    params.put("token", Token);
                    params.put("staff_id", StaffID);

                    new DeliveryCheckTokenHttpRequest(getApplicationContext(), null, this).execute(params);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                onCheckToken(null);
            }
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

        switch (id) {
            case R.id.action_map:
                Intent intentOrdersMap = new Intent(getApplicationContext(), OrdersMapActivity.class);
                startActivity(intentOrdersMap);
                return true;

            case R.id.action_settings:
                Intent intentSettings = new Intent(getApplicationContext(), SettingsActivity.class);
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

    public void onShowHideActionBar(boolean flag) {
        if (flag) getSupportActionBar().show();
        else {
            getSupportActionBar().hide();
        }
    }

    @Override
    public void onLogoutSuccess() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.commit();

        fragmentManager.beginTransaction()
                .replace(R.id.container, new LoginFragment())
                .commit();
    }

    @Override
    public void onUserLogin(JSONObject jsonObject) {
        return;
    }

    @Override
    public void onCheckToken(JSONObject jsonObject) {
        Fragment fragment = null;

        if (jsonObject != null) {
            if (!jsonObject.isNull("status")) {
                int statusCode = 0;

                try {
                    statusCode = jsonObject.getJSONObject("status").getInt("status_code");
                    if (statusCode == 1017) {
                        fragment = new MainFragment();
                    } else fragment = new LoginFragment();
                } catch (JSONException e) {
                    fragment = new LoginFragment();
                    e.printStackTrace();
                }

            } else {
                fragment = new LoginFragment();
            }
        } else {
            fragment = new LoginFragment();
        }

        for(int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
            fragmentManager.popBackStack();
        }

        fragmentManager.beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }
}
