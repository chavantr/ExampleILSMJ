package es.situm.gettingstarted.drawbuilding.models;

import es.situm.gettingstarted.drawbuilding.Teacher;

public class UserInfoHolder {


    private User user;

    private Teacher teacher;


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


    private static class UserInfoHolderHelper {
        final static UserInfoHolder INSTANCE = new UserInfoHolder();
    }

}
