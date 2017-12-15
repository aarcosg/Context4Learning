package es.us.context4learning.data.api.moodle.entity;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Course implements Parcelable {

    @SerializedName("id_curso")
    public Long id;
    @SerializedName("nombre_curso")
    public String name;
    @SerializedName("descripcion_breve")
    public String description;
    @SerializedName("avancealumno")
    public Double progress;
    @SerializedName("media")
    public Double avg;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeValue(this.progress);
        dest.writeValue(this.avg);
    }

    public Course() {
    }

    private Course(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.name = in.readString();
        this.description = in.readString();
        this.progress = (Double) in.readValue(Double.class.getClassLoader());
        this.avg = (Double) in.readValue(Double.class.getClassLoader());
    }

    public static final Creator<Course> CREATOR = new Creator<Course>() {
        public Course createFromParcel(Parcel source) {
            return new Course(source);
        }

        public Course[] newArray(int size) {
            return new Course[size];
        }
    };
}
