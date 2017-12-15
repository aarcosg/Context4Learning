package es.us.context4learning.backend;


import com.google.gson.annotations.SerializedName;

public class MoodleStudentProgress {

    @SerializedName("user")
    public String user;
    @SerializedName("avancealumno")
    public Double progress;
    @SerializedName("media")
    public Double avg;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public Double getAvg() {
        return avg;
    }

    public void setAvg(Double avg) {
        this.avg = avg;
    }

    @Override
    public String toString() {
        return "StudentProgress{" +
                "user='" + user + '\'' +
                ", progress=" + progress +
                ", avg=" + avg +
                '}';
    }
}
