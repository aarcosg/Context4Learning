package es.us.context4learning.data.api.moodle.entity.response;

import java.util.HashMap;
import java.util.Map;

public class SiteInfoFunction {

    private String name;
    private String version;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     */
    public SiteInfoFunction() {
    }

    /**
     * @param name
     * @param version
     */
    public SiteInfoFunction(String name, String version) {
        this.name = name;
        this.version = version;
    }

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    public SiteInfoFunction withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return The version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version The version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    public SiteInfoFunction withVersion(String version) {
        this.version = version;
        return this;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public SiteInfoFunction withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}