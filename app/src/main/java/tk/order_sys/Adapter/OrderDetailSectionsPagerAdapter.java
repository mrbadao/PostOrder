package tk.order_sys.Adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.Locale;

import tk.order_sys.Fragment.OrderDetailInfoFragment;
import tk.order_sys.Fragment.OrderDetailProductsFragment;
import tk.order_sys.Postorder.R;

/**
 * Created by mrbadao on 01/05/2015.
 */
public class OrderDetailSectionsPagerAdapter extends FragmentPagerAdapter {
    private Context context;

    public OrderDetailSectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment sectionFragment = null;

        switch (position) {
            case 1:
                sectionFragment = new OrderDetailProductsFragment();
                break;
            default:
                sectionFragment = new OrderDetailInfoFragment();
                break;
        }

        return sectionFragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return context.getString(R.string.title_order_info).toUpperCase(l);
            case 1:
                return context.getString(R.string.title_order_products).toUpperCase(l);
        }
        return null;
    }
}
