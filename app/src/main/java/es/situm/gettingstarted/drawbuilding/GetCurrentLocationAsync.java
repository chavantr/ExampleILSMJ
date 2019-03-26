package es.situm.gettingstarted.drawbuilding;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class GetCurrentLocationAsync extends AsyncTask<Void, Void, LatLng> {

    private OnGetCurrentLocationListener onGetCurrentLocationListener;

    @Override
    protected LatLng doInBackground(Void... params) {
        String response = new HttpConnectionUtil().requestGet(IndoorConstants.URL + IndoorConstants.GET_CU_LOCATION);
        if (response.isEmpty()) return null;
        else {
            JSONObject jResponse = null;
            try {
                jResponse = new JSONObject(response);
                LatLng latLng = new LatLng(Double.parseDouble(jResponse.getString("Latitude")), Double.parseDouble(jResponse.getString("Longitude")));
                return latLng;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onPostExecute(LatLng latLng) {
        super.onPostExecute(latLng);
        onGetCurrentLocationListener.onGetCurrentLocationSuccess(latLng);
    }

    public void setOnGetCurrentLocationListener(OnGetCurrentLocationListener onGetCurrentLocationListener) {
        this.onGetCurrentLocationListener = onGetCurrentLocationListener;
        super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
