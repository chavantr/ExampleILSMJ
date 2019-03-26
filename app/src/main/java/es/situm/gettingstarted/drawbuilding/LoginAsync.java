package es.situm.gettingstarted.drawbuilding;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginAsync extends AsyncTask<JSONObject, Void, Teacher> {

    private OnLoginListener onLoginListener;

    @Override
    protected Teacher doInBackground(JSONObject... params) {
        String response = new HttpConnectionUtil().requestPost(IndoorConstants.URL + IndoorConstants.LOGIN, params[0]);
        if (response.isEmpty()) return null;
        else {

            JSONObject jNode = null;
            try {
                jNode = new JSONObject(response);
                if (null != jNode) {
                    Teacher teacher = new Teacher();
                    teacher.setId(jNode.getInt("Id"));
                    teacher.setName(jNode.getString("Name"));
                    teacher.setUserName(jNode.getString("Username"));
                    teacher.setPassword(jNode.getString("Password"));
                    teacher.setQualification(jNode.getString("Qualification"));
                    teacher.setDesignation(jNode.getString("Designation"));
                    teacher.setLat(jNode.getString("Lat"));
                    teacher.setLng(jNode.getString("Lng"));
                    return teacher;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return null;
    }

    @Override
    protected void onPostExecute(Teacher teacher) {
        super.onPostExecute(teacher);
        onLoginListener.onLoginSuccess(teacher);
    }

    public void setOnLoginListener(OnLoginListener onLoginListener, JSONObject request) {
        this.onLoginListener = onLoginListener;
        super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, request);
    }
}
