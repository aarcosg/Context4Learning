package es.us.context4learning.data.api.moodle.entity;


import com.google.gson.annotations.SerializedName;

public class Task {

    @SerializedName("id_recurso")
    public Long id;
    @SerializedName("idcurso")
    public Long courseId;
    @SerializedName("nombre_recurso")
    public String name;
    @SerializedName("tipo_recurso")
    public String sourceType;
    @SerializedName("objetivo")
    public String objective;
    @SerializedName("duracion")
    public Integer duration;
    @SerializedName("url_acceso")
    public String url;
    @SerializedName("url_externa")
    public String externalUrl;
    @SerializedName("pc_acciones")
    public String actions;
    @SerializedName("pc_actividades")
    public String contexts;
    @SerializedName("descripcion")
    public String description;
    @SerializedName("tiporecurso")
    public String type;
    @SerializedName("idioma")
    public String lang;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public String getContexts() {
        return contexts;
    }

    public void setContexts(String contexts) {
        this.contexts = contexts;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

}
