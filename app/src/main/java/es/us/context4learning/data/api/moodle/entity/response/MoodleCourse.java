package es.us.context4learning.data.api.moodle.entity.response;

import java.util.HashMap;
import java.util.Map;

public class MoodleCourse {

    private Integer id;
    private String shortname;
    private String fullname;
    private Integer enrolledusercount;
    private String idnumber;
    private Integer visible;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The shortname
     */
    public String getShortname() {
        return shortname;
    }

    /**
     *
     * @param shortname
     * The shortname
     */
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    /**
     *
     * @return
     * The fullname
     */
    public String getFullname() {
        return fullname;
    }

    /**
     *
     * @param fullname
     * The fullname
     */
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    /**
     *
     * @return
     * The enrolledusercount
     */
    public Integer getEnrolledusercount() {
        return enrolledusercount;
    }

    /**
     *
     * @param enrolledusercount
     * The enrolledusercount
     */
    public void setEnrolledusercount(Integer enrolledusercount) {
        this.enrolledusercount = enrolledusercount;
    }

    /**
     *
     * @return
     * The idnumber
     */
    public String getIdnumber() {
        return idnumber;
    }

    /**
     *
     * @param idnumber
     * The idnumber
     */
    public void setIdnumber(String idnumber) {
        this.idnumber = idnumber;
    }

    /**
     *
     * @return
     * The visible
     */
    public Integer getVisible() {
        return visible;
    }

    /**
     *
     * @param visible
     * The visible
     */
    public void setVisible(Integer visible) {
        this.visible = visible;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
