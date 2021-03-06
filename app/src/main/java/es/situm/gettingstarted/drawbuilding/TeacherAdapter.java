package es.situm.gettingstarted.drawbuilding;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import es.situm.gettingstarted.R;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder> {

    private List<Teacher> lstTeachers;
    private OnTeacherLocationSelectedListener onTeacherLocationSelectedListener;

    public TeacherAdapter(List<Teacher> teachers) {
        this.lstTeachers = teachers;
    }

    @Override
    public TeacherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TeacherViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_teacher_row, parent, false));
    }

    @Override
    public void onBindViewHolder(TeacherViewHolder viewHolder, final int position) {
        viewHolder.lblName.setText(lstTeachers.get(position).getName());
        viewHolder.lblName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTeacherLocationSelectedListener.onTeacherLocation(lstTeachers.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return lstTeachers.size();
    }

    public void setOnTeacherLocationSelectedListener(OnTeacherLocationSelectedListener onTeacherLocationSelectedListener) {
        this.onTeacherLocationSelectedListener = onTeacherLocationSelectedListener;
    }

    class TeacherViewHolder extends RecyclerView.ViewHolder {

        TextView lblName;

        public TeacherViewHolder(View itemView) {
            super(itemView);
            lblName = (TextView) itemView.findViewById(R.id.lblName);
        }
    }

}
