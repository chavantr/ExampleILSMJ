package es.situm.gettingstarted.drawbuilding.models;

import com.google.android.gms.maps.model.LatLng;

import es.situm.gettingstarted.drawbuilding.Teacher;

public class UserInfoHolder {


    private User user;

    private Teacher teacher;

    private LatLng teacherLocation;

    private Teacher selectedTeacher;

    public static UserInfoHolder getInstance() {
        return UserInfoHolderHelper.INSTANCE;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public LatLng getTeacherLocation() {
        return teacherLocation;
    }

    public void setTeacherLocation(LatLng teacherLocation) {
        this.teacherLocation = teacherLocation;
    }

    public Teacher getSelectedTeacher() {
        return selectedTeacher;
    }

    public void setSelectedTeacher(Teacher selectedTeacher) {
        this.selectedTeacher = selectedTeacher;
    }


    private static class UserInfoHolderHelper {
        final static UserInfoHolder INSTANCE = new UserInfoHolder();
    }

}
