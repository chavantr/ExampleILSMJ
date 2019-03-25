package es.situm.gettingstarted.drawbuilding;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import es.situm.gettingstarted.R;

public class ShowTeacherActivity extends AppCompatActivity implements OnGetTeacherListener {

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

    @Override
    public void onGetTeacherSuccess(List<Teacher> result) {
        progressDialogUtil.hide();
        teacherAdapter = new TeacherAdapter(result);
        lstTeachers.setAdapter(teacherAdapter);
    }
}
