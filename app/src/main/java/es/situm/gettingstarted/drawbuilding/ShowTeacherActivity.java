package es.situm.gettingstarted.drawbuilding;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import es.situm.gettingstarted.R;
import es.situm.gettingstarted.drawbuilding.models.UserInfoHolder;

public class ShowTeacherActivity extends AppCompatActivity implements OnGetTeacherListener, OnGetTeacherLocationListener, OnTeacherLocationSelectedListener {

    private RecyclerView lstTeachers;
    private ProgressDialogUtil progressDialogUtil;
    private TeacherAdapter teacherAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_teacher);
        lstTeachers = (RecyclerView) findViewById(R.id.lstTeachers);
        lstTeachers.setLayoutManager(new LinearLayoutManager(this));
        progressDialogUtil = new ProgressDialogUtil(this);
        init();
    }

    private void init() {
        progressDialogUtil.show();
        GetTeachersAsync getTeachersAsync = new GetTeachersAsync();
        getTeachersAsync.setOnGetTeacherListener(this);
    }

    private void initL(Teacher teacher) throws JSONException {
        progressDialogUtil.show();
        Calendar calendar = Calendar.getInstance();
        GetTeacherLocationAsync getTeacherLocationAsync = new GetTeacherLocationAsync();
        JSONObject jRequest = new JSONObject();
        JSONObject param = new JSONObject();
        param.put("From", calendar.get(Calendar.HOUR_OF_DAY));
        param.put("To", calendar.get(Calendar.HOUR_OF_DAY) + 1);
        param.put("Id", teacher.getId());
        param.put("Day", Calendar.DAY_OF_WEEK);
        jRequest.put("request", param);
        getTeacherLocationAsync.setOnGetTeacherLocationListener(this, jRequest);
    }

    @Override
    public void onGetTeacherSuccess(List<Teacher> result) {
        progressDialogUtil.hide();
        teacherAdapter = new TeacherAdapter(result);
        teacherAdapter.setOnTeacherLocationSelectedListener(this);
        lstTeachers.setAdapter(teacherAdapter);
    }

    @Override
    public void onSuccessLocation(LatLng location) {
        progressDialogUtil.hide();
        if (null != location) {
            UserInfoHolder.getInstance().setTeacherLocation(location);
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onTeacherLocation(Teacher teacher) {
        try {
            if (null != teacher) {
                initL(teacher);
                UserInfoHolder.getInstance().setSelectedTeacher(teacher);
            } else
                Toast.makeText(this, "Issue with connection in proxy", Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
