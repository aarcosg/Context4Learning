package es.us.context4learning.data;


import android.content.Intent;
import android.os.BatteryManager;

public class BatteryStatus {

    private boolean isCharging;
    private boolean isUsbCharge;
    private boolean isAcCharge;
    private int level;
    private int scale;
    private float batteryPct;

    public BatteryStatus(Intent batteryStatus) {
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        this.isCharging =
                status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        this.isUsbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        this.isAcCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        this.level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        this.scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        this.batteryPct = level / (float)scale;
    }

    public boolean isCharging() {
        return isCharging;
    }

    public void setCharging(boolean charging) {
        isCharging = charging;
    }

    public boolean isUsbCharge() {
        return isUsbCharge;
    }

    public void setUsbCharge(boolean usbCharge) {
        isUsbCharge = usbCharge;
    }

    public boolean isAcCharge() {
        return isAcCharge;
    }

    public void setAcCharge(boolean acCharge) {
        isAcCharge = acCharge;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public float getBatteryPct() {
        return batteryPct;
    }

    public void setBatteryPct(float batteryPct) {
        this.batteryPct = batteryPct;
    }

    @Override
    public String toString() {
        return "BatteryStatus{" +
                "isCharging=" + isCharging +
                ", isUsbCharge=" + isUsbCharge +
                ", isAcCharge=" + isAcCharge +
                ", level=" + level +
                ", scale=" + scale +
                ", batteryPct=" + batteryPct +
                '}';
    }
}
