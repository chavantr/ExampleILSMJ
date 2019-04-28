package es.situm.gettingstarted.drawbuilding;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetTeachersAsync extends AsyncTask<Void, Void, List<Teacher>> {

    private OnGetTeacherListener onGetTeacherListener;

    private HttpConnectionUtil httpConnectionUtil = new HttpConnectionUtil();

    @Override
    protected List<Teacher> doInBackground(Void... params) {
        String response = httpConnectionUtil.requestGet(IndoorConstants.URL + IndoorConstants.GET_TEACHERS);
        if (response.isEmpty()) return null;
        else {
            try {
                JSONArray jTeacher = new JSONArray(response);
                List<Teacher> lstTeacher = new ArrayList<>();
                if (jTeacher.length() > 0) {
                    for (int i = 0; i < jTeacher.length(); i++) {
                        JSONObject jNode = jTeacher.getJSONObject(i);
                        Teacher teacher = new Teacher();
                        teacher.setId(jNode.getInt("Id"));
                        teacher.setName(jNode.getString("Name"));
                        teacher.setUserName(jNode.getString("Username"));
                        teacher.setPassword(jNode.getString("Password"));
                        teacher.setQualification(jNode.getString("Qualification"));
                        teacher.setDesignation(jNode.getString("Designation"));
                        teacher.setLat(jNode.getString("Lat"));
                        teacher.setLng(jNode.getString("Lng"));
                        teacher.setPhoneNumber(jNode.getString("PNumber"));
                        lstTeacher.add(teacher);
                    }
                    return lstTeacher;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Teacher> teachers) {
        super.onPostExecute(teachers);
        onGetTeacherListener.onGetTeacherSuccess(teachers);
    }

    public void setOnGetTeacherListener(OnGetTeacherListener onGetTeacherListener) {
        this.onGetTeacherListener = onGetTeacherListener;
        super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
