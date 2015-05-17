package tk.order_sys.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by HieuNguyen on 4/7/2015.
 */
public class ContentOrder implements Parcelable {
    public String id;
    public String name;
    public String customer_name;
    public String order_phone;
    public String coordinate_lat;
    public String coordinate_long;
    public String delivery_id;
    public String status;
    public String created;
    public String completed;
    public String address;

    public ContentOrder() {
        id = name = customer_name = order_phone = coordinate_lat = coordinate_long = delivery_id = status = completed = created = address= null;
    }

    public ContentOrder(String id, String name, String customer_name, String order_phone, String coordinate_lat,
                        String coordinate_long, String delivery_id, String status, String created, String completed, String address) {
        this.id = id;
        this.name = name;
        this.customer_name = customer_name;
        this.order_phone = order_phone;
        this.coordinate_lat = coordinate_lat;
        this.coordinate_long = coordinate_long;
        this.delivery_id = delivery_id;
        this.status = status;
        this.completed = completed;
        this.created = created;
        this.address = address;
    }

    @Override
    public String toString() {
        return "id: " + id + "; name: " + name + "; customer_name: " + customer_name +
                "; order_phone: " + order_phone + "; coordinate_lat: " + coordinate_lat + "; coordinate_long: " + coordinate_long +
                "; delivery_id: " + delivery_id + "; status: " + status + "; created: " + created + "; completed: " + completed + "; address: " + address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
