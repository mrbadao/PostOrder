package tk.order_sys.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;

import tk.order_sys.Postorder.R;
import tk.order_sys.models.ContentOrder;

/**
 * Created by HieuNguyen on 4/17/2015.
 */
public class OrdersAdapter extends ArrayAdapter {
    private Context context;

    private JSONArray jsonCookieStore;
    private int layoutRes;
    private ArrayList<ContentOrder> Orders;

    public OrdersAdapter(Context context, int resource, ArrayList<ContentOrder> objects) {
        super(context, resource, objects);
        this.context = context;
        this.layoutRes = resource;
        Orders = objects;
    }

    @Override
    public int getCount() {
        return Orders.size();
    }

    @Override
    public Object getItem(int position) {
        return Orders.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            view = mInflater.inflate(layoutRes, parent, false);
        } else {
            view = convertView;
        }

        ContentOrder item = (ContentOrder) getItem(position);
        TextView txtOrderName = (TextView) view.findViewById(R.id.list_item_textview);

        txtOrderName.setText(item.name);
        return view;
    }
}
