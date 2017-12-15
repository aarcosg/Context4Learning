package es.us.context4learning.data.api.moodle.entity.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiteInfoResponse {

    private String sitename;
    private String username;
    private String firstname;
    private String lastname;
    private String fullname;
    private String lang;
    private Integer userid;
    private String siteurl;
    private String userpictureurl;
    private List<SiteInfoFunction> siteInfoFunctions = new ArrayList<SiteInfoFunction>();
    private Integer downloadfiles;
    private String release;
    private String version;
    private String mobilecssurl;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     */
    public SiteInfoResponse() {
    }

    /**
     * @param mobilecssurl
     * @param siteInfoFunctions
     * @param sitename
     * @param downloadfiles
     * @param userid
     * @param lastname
     * @param firstname
     * @param userpictureurl
     * @param lang
     * @param version
     * @param siteurl
     * @param username
     * @param release
     * @param fullname
     */
    public SiteInfoResponse(String sitename, String username, String firstname, String lastname, String fullname, String lang, Integer userid, String siteurl, String userpictureurl, List<SiteInfoFunction> siteInfoFunctions, Integer downloadfiles, String release, String version, String mobilecssurl) {
        this.sitename = sitename;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.fullname = fullname;
        this.lang = lang;
        this.userid = userid;
        this.siteurl = siteurl;
        this.userpictureurl = userpictureurl;
        this.siteInfoFunctions = siteInfoFunctions;
        this.downloadfiles = downloadfiles;
        this.release = release;
        this.version = version;
        this.mobilecssurl = mobilecssurl;
    }

    /**
     * @return The sitename
     */
    public String getSitename() {
        return sitename;
    }

    /**
     * @param sitename The sitename
     */
    public void setSitename(String sitename) {
        this.sitename = sitename;
    }

    public SiteInfoResponse withSitename(String sitename) {
        this.sitename = sitename;
        return this;
    }

    /**
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    public SiteInfoResponse withUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * @return The firstname
     */
    public String getFirstname() {
        return firstname;
    }

    /**
     * @param firstname The firstname
     */
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public SiteInfoResponse withFirstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    /**
     * @return The lastname
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * @param lastname The lastname
     */
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public SiteInfoResponse withLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    /**
     * @return The fullname
     */
    public String getFullname() {
        return fullname;
    }

    /**
     * @param fullname The fullname
     */
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public SiteInfoResponse withFullname(String fullname) {
        this.fullname = fullname;
        return this;
    }

    /**
     * @return The lang
     */
    public String getLang() {
        return lang;
    }

    /**
     * @param lang The lang
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    public SiteInfoResponse withLang(String lang) {
        this.lang = lang;
        return this;
    }

    /**
     * @return The userid
     */
    public Integer getUserid() {
        return userid;
    }

    /**
     * @param userid The userid
     */
    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public SiteInfoResponse withUserid(Integer userid) {
        this.userid = userid;
        return this;
    }

    /**
     * @return The siteurl
     */
    public String getSiteurl() {
        return siteurl;
    }

    /**
     * @param siteurl The siteurl
     */
    public void setSiteurl(String siteurl) {
        this.siteurl = siteurl;
    }

    public SiteInfoResponse withSiteurl(String siteurl) {
        this.siteurl = siteurl;
        return this;
    }

    /**
     * @return The userpictureurl
     */
    public String getUserpictureurl() {
        return userpictureurl;
    }

    /**
     * @param userpictureurl The userpictureurl
     */
    public void setUserpictureurl(String userpictureurl) {
        this.userpictureurl = userpictureurl;
    }

    public SiteInfoResponse withUserpictureurl(String userpictureurl) {
        this.userpictureurl = userpictureurl;
        return this;
    }

    /**
     * @return The functions
     */
    public List<SiteInfoFunction> getSiteInfoFunctions() {
        return siteInfoFunctions;
    }

    /**
     * @param siteInfoFunctions The functions
     */
    public void setSiteInfoFunctions(List<SiteInfoFunction> siteInfoFunctions) {
        this.siteInfoFunctions = siteInfoFunctions;
    }

    public SiteInfoResponse withFunctions(List<SiteInfoFunction> siteInfoFunctions) {
        this.siteInfoFunctions = siteInfoFunctions;
        return this;
    }

    /**
     * @return The downloadfiles
     */
    public Integer getDownloadfiles() {
        return downloadfiles;
    }

    /**
     * @param downloadfiles The downloadfiles
     */
    public void setDownloadfiles(Integer downloadfiles) {
        this.downloadfiles = downloadfiles;
    }

    public SiteInfoResponse withDownloadfiles(Integer downloadfiles) {
        this.downloadfiles = downloadfiles;
        return this;
    }

    /**
     * @return The release
     */
    public String getRelease() {
        return release;
    }

    /**
     * @param release The release
     */
    public void setRelease(String release) {
        this.release = release;
    }

    public SiteInfoResponse withRelease(String release) {
        this.release = release;
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

    public SiteInfoResponse withVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * @return The mobilecssurl
     */
    public String getMobilecssurl() {
        return mobilecssurl;
    }

    /**
     * @param mobilecssurl The mobilecssurl
     */
    public void setMobilecssurl(String mobilecssurl) {
        this.mobilecssurl = mobilecssurl;
    }

    public SiteInfoResponse withMobilecssurl(String mobilecssurl) {
        this.mobilecssurl = mobilecssurl;
        return this;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public SiteInfoResponse withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }
}