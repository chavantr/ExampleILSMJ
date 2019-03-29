package es.situm.gettingstarted.drawbuilding;

import android.os.AsyncTask;



public class DrawPointsAsync extends AsyncTask<String, Void, String> {

    private HttpConnectionUtil httpConnectionUtil = new HttpConnectionUtil();
    private OnResultListener onResultListener;


    @Override
    protected String doInBackground(String... params) {
        return httpConnectionUtil.requestGet(params[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        //super.onPostExecute(s);
        onResultListener.onSuccess(s);
    }

    public void setOnResultListener(OnResultListener onResultListener, String params) {
        this.onResultListener = onResultListener;
        super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }


}
