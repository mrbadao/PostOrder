package tk.order_sys.postorder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import tk.order_sys.Fragment.LoginFragment;
import tk.order_sys.Fragment.MainFragment;


public class MainActivity extends ActionBarActivity implements LoginFragment.LoginInterface {
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        check login here
//        getSupportActionBar().hide();

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            Fragment fragment = new LoginFragment();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoginSuccess() {
        fragmentManager.beginTransaction()
                .replace(R.id.container, new MainFragment())
                .commit();
        getSupportActionBar().show();
    }

    public void onShowHideActionBar(boolean flag){
        if (flag) getSupportActionBar().show();
        else getSupportActionBar().hide();
    }
}
