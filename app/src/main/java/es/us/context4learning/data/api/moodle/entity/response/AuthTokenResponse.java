package es.us.context4learning.data.api.moodle.entity.response;

public class AuthTokenResponse implements MoodleResponse {

    String token;
    String error;
    String stacktrace;
    String debuginfo;
    String reproductionlink;

    public AuthTokenResponse(String token, String error, String stacktrace, String debuginfo, String reproductionlink) {
        this.token = token;
        this.error = error;
        this.stacktrace = stacktrace;
        this.debuginfo = debuginfo;
        this.reproductionlink = reproductionlink;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public void setStacktrace(String stacktrace) {
        this.stacktrace = stacktrace;
    }

    public String getDebuginfo() {
        return debuginfo;
    }

    public void setDebuginfo(String debuginfo) {
        this.debuginfo = debuginfo;
    }

    public String getReproductionlink() {
        return reproductionlink;
    }

    public void setReproductionlink(String reproductionlink) {
        this.reproductionlink = reproductionlink;
    }
}
