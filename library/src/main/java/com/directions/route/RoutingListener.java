package com.directions.route;

import com.google.android.gms.maps.model.PolylineOptions;

public interface RoutingListener {
    void onRoutingFailure();

    void onRoutingStart();

    void onRoutingSuccess(PolylineOptions mPolyOptions, Route route);
}
