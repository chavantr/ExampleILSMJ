package es.situm.gettingstarted.drawbuilding;

import android.os.AsyncTask;

import org.json.JSONObject;

public class RegistrationAsync extends AsyncTask<JSONObject, Void, String> {

    private OnRegistrationListener onRegistrationListener;

    @Override
    protected String doInBackground(JSONObject... params) {
        return new HttpConnectionUtil().requestPost(IndoorConstants.URL + IndoorConstants.REGISTRATION, params[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        onRegistrationListener.onRegistrationSuccess(result);
    }

    public void setOnRegistrationListener(OnRegistrationListener onRegistrationListener, JSONObject request) {
        this.onRegistrationListener = onRegistrationListener;
        super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, request);
    }
}
