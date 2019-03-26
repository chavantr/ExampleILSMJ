package es.situm.gettingstarted.drawbuilding;

import com.google.android.gms.maps.model.LatLng;

public interface OnGetCurrentLocationListener {
    void onGetCurrentLocationSuccess(LatLng location);
}
