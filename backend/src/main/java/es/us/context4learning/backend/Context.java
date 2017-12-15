package es.us.context4learning.backend;

import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

import java.util.Date;
import java.util.List;

@Entity
public class Context {

    @Id Long id;
    @Index @Load Ref<Device> device;
    @Index String activity;
    @Index GeoPt location;
    Float battery;
    boolean headphone;
    List<String> weatherConditions;
    @Index Date time;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Device getDevice() {
        return device.get();
    }

    public void setDevice(Device device) {
        this.device = Ref.create(device);
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public GeoPt getLocation() {
        return location;
    }

    public void setLocation(GeoPt location) {
        this.location = location;
    }

    public Float getBattery() {
        return battery;
    }

    public void setBattery(Float battery) {
        this.battery = battery;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public boolean isHeadphone() {
        return headphone;
    }

    public void setHeadphone(boolean headphone) {
        this.headphone = headphone;
    }

    public List<String> getWeatherConditions() {
        return weatherConditions;
    }

    public void setWeatherConditions(List<String> weatherConditions) {
        this.weatherConditions = weatherConditions;
    }
}
