package es.us.context4learning.backend;

import com.google.gson.annotations.SerializedName;

public class MoodleCourse {

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
}
