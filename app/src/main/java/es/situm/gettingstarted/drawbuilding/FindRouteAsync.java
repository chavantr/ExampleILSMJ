package es.situm.gettingstarted.drawbuilding;

import android.os.AsyncTask;
import android.util.Xml;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Tatyabhau on 3/11/2018.
 */

public class FindRouteAsync extends AsyncTask<String, Void, String> {


    private HttpConnectionUtil httpConnectionUtil = new HttpConnectionUtil();
    private OnResultListener onResultListener;


    @Override
    protected String doInBackground(String... params) {
        try {
            String encodedUrl = URLEncoder.encode(params[0], "UTF-8");
            return httpConnectionUtil.requestGet(IndoorConstants.URL + IndoorConstants.GET_ROUTES + "?" + params[0]);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        onResultListener.onSuccess(s);
    }

    public void setOnResultListener(OnResultListener onResultListener, String url) {
        this.onResultListener = onResultListener;
        super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }


}
