package es.us.context4learning.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;

@Entity
public class User {

    @Id Long id;
    @Index String username;
    String password;
    @Index String moodleServerName;
    @Index String moodleToken;
    @Index Date time;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMoodleServerName() {
        return moodleServerName;
    }

    public void setMoodleServerName(String moodleServerName) {
        this.moodleServerName = moodleServerName;
    }

    public String getMoodleToken() {
        return moodleToken;
    }

    public void setMoodleToken(String moodleToken) {
        this.moodleToken = moodleToken;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
