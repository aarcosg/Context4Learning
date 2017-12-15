package es.us.context4learning.data;


import android.location.Location;

import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.location.DetectedActivity;

public class AwarenessContext {

    private DetectedActivity detectedActivity;
    private Location location;
    private HeadphoneState headphoneState;
    private Weather weather;
    private BatteryStatus batteryStatus;

    public AwarenessContext() {
    }

    public AwarenessContext(DetectedActivity detectedActivity, Location location, HeadphoneState headphoneState, Weather weather, BatteryStatus batteryStatus) {
        this.detectedActivity = detectedActivity;
        this.location = location;
        this.headphoneState = headphoneState;
        this.weather = weather;
        this.batteryStatus = batteryStatus;
    }

    public DetectedActivity getDetectedActivity() {
        return detectedActivity;
    }

    public void setDetectedActivity(DetectedActivity detectedActivity) {
        this.detectedActivity = detectedActivity;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public HeadphoneState getHeadphoneState() {
        return headphoneState;
    }

    public void setHeadphoneState(HeadphoneState headphoneState) {
        this.headphoneState = headphoneState;
    }

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    public BatteryStatus getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(BatteryStatus batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    @Override
    public String toString() {
        return "AwarenessContext{" +
                "detectedActivity=" + detectedActivity +
                ", location=" + location +
                ", headphoneState=" + headphoneState +
                ", weather=" + weather +
                ", batteryStatus=" + batteryStatus +
                '}';
    }
}
