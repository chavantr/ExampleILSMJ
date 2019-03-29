package es.situm.gettingstarted.drawbuilding;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetTeacherLocationAsync extends AsyncTask<JSONObject, Void, LatLng> {

    private final HttpConnectionUtil httpConnectionUtil = new HttpConnectionUtil();
    private OnGetTeacherLocationListener onGetTeacherLocationListener;

    @Override
    protected LatLng doInBackground(JSONObject... param) {
        String response = httpConnectionUtil.requestPost(IndoorConstants.URL + IndoorConstants.GET_TEACHER_CURRENT_LOCATION, param[0]);
        if (null == response && response.isEmpty()) return null;
        else {
            try {
                JSONObject jLocation = new JSONObject(response);
                LatLng latLng = new LatLng(Double.parseDouble(jLocation.getString("Latitude")), Double.parseDouble(jLocation.getString("Longitude")));
                return latLng;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setOnGetTeacherLocationListener(OnGetTeacherLocationListener onGetTeacherLocationListener, JSONObject request) {
        this.onGetTeacherLocationListener = onGetTeacherLocationListener;
        super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, request);
    }
}
