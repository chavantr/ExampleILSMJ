package es.situm.gettingstarted.drawbuilding;


import android.os.AsyncTask;

public class GetRoutePointsAsync extends AsyncTask<String, Void, String> {

    private OnPointsListener onPointsListener;
    private HttpConnectionUtil httpConnectionUtil = new HttpConnectionUtil();

    @Override
    protected String doInBackground(String... params) {
        return httpConnectionUtil.requestGet(params[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        onPointsListener.onSuccessPoint(s);
    }

    public void setOnResultListener(OnPointsListener onResultListener,String url) {
        this.onPointsListener = onResultListener;
        super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url);
    }

}
