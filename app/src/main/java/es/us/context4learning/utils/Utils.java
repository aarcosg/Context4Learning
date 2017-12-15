package es.us.context4learning.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.us.context4learning.Constants;
import es.us.context4learning.MoodleContextApplication;
import es.us.context4learning.R;
import es.us.context4learning.backend.contextApi.model.GeoPt;
import es.us.context4learning.chrometabs.CustomTabActivityHelper;
import es.us.context4learning.chrometabs.WebviewFallback;
import es.us.context4learning.data.api.moodle.entity.Course;
import es.us.context4learning.data.api.moodle.entity.Task;
import es.us.context4learning.observable.google.PendingResultObservable;
import rx.Observable;

public class Utils {
    private static final String TAG = Utils.class.getCanonicalName();

    /**
     * @return Application's {@code SharedPreferences}.
     */
    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public static boolean isNotificationDisabledOnPrefs(Context context, @Nullable Location lastLocation){
        final SharedPreferences prefs = getSharedPreferences(context);
        boolean disabled = !prefs.getBoolean(Constants.PROPERTY_RECEIVE_NOTIFICATIONS, false);
        if(!disabled){
            long now = Calendar.getInstance().getTimeInMillis();
            long startHour = prefs.getLong(Constants.PROPERTY_1_HOUR_NOTIFICATION_RESTRICTION_START,0L);
            long endHour = prefs.getLong(Constants.PROPERTY_1_HOUR_NOTIFICATION_RESTRICTION_END,0L);
            if(startHour <= now && endHour >= now){
                disabled = true;
                Log.e(TAG, "Notification disabled: 1 hour");
            }
            if(!disabled){
                long startDay = prefs.getLong(Constants.PROPERTY_1_DAY_NOTIFICATION_RESTRICTION_START,0L);
                long endDay = prefs.getLong(Constants.PROPERTY_1_DAY_NOTIFICATION_RESTRICTION_END,0L);
                if(lastLocation != null){
                    boolean locationRestricted = isLocationRestrictedOnPrefs(prefs, lastLocation);
                    if(locationRestricted && startDay <= now && endDay >= now){
                        disabled = true;
                        Log.e(TAG, "Notification disabled: 1 day + location");
                    }
                } else {
                    if(startDay <= now && endDay >= now){
                        disabled = true;
                        Log.e(TAG, "Notification disabled: 1 day");
                    }
                }
            }
        }
        return disabled;
    }

    private static boolean isLocationRestrictedOnPrefs(SharedPreferences prefs, Location lastLocation){
        boolean restricted = false;
        double lat = Double.longBitsToDouble(prefs.getLong(Constants.PROPERTY_1_DAY_NOTIFICATION_RESTRICTION_LATITUDE,0));
        double lon = Double.longBitsToDouble(prefs.getLong(Constants.PROPERTY_1_DAY_NOTIFICATION_RESTRICTION_LONGITUDE,0));
        if(lat != 0 && lon != 0){
            Location restrictionLocation = new Location("Restriction Location");
            restrictionLocation.setLatitude(lat);
            restrictionLocation.setLongitude(lon);
            float distance = lastLocation.distanceTo(restrictionLocation);
            Log.d(TAG,"isLocationRestrictedOnPrefs:distance = " + distance);
            if(distance < Constants.DEFAULT_RADIUS_RESTRICTION){
                restricted = true;
            }
        }
        return restricted;
    }

    public static <T extends Result> Observable<T> getObservableFromPendingResult(PendingResult<T> result) {
        return Observable.create(new PendingResultObservable<>(result));
    }

    public static void openChromeTab(Activity activity, CustomTabActivityHelper customTabActivityHelper, String url){
        CustomTabsIntent customTabsIntent =
                new CustomTabsIntent.Builder(customTabActivityHelper.getSession())
                        .setToolbarColor(ContextCompat.getColor(activity, R.color.primary))
                        .setSecondaryToolbarColor(ContextCompat.getColor(activity, R.color.primary_dark))
                        .setShowTitle(true)
                        .setStartAnimations(activity, R.anim.slide_in_right, R.anim.slide_out_left)
                        .setExitAnimations(activity, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .setCloseButtonIcon(BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_arrow_back))
                        .build();
        CustomTabActivityHelper.openCustomTab(
                activity, customTabsIntent, Uri.parse(url), new WebviewFallback());
    }

    public static String getAppVersion(Context context){
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isVideoTask(Task task){
        return task.getExternalUrl().toLowerCase().contains("youtube");
    }

    public static Map<Long,Course> getCoursesMapFromList(List<Course> courses){
        Map<Long,Course> coursesMap = new LinkedHashMap<>();
        for(Course course : courses){
            coursesMap.put(course.getId(), course);
        }
        return coursesMap;
    }

    public static void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static GeoPt getLastLocationFromPreferences(Context context){
        SharedPreferences prefs = MoodleContextApplication.get(context).getApplicationComponent().getSharedPreferences();
        GeoPt geoPt = new GeoPt();
        geoPt.setLatitude(prefs.getFloat(Constants.PROPERTY_LAST_LATITUDE,(float) Constants.DEFAULT_LATITUDE));
        geoPt.setLongitude(prefs.getFloat(Constants.PROPERTY_LAST_LONGITUDE,(float) Constants.DEFAULT_LONGITUDE));
        return geoPt;
    }

    public static void saveLastLocationToPreferences(Context context, Location location){
        SharedPreferences prefs = MoodleContextApplication.get(context).getApplicationComponent().getSharedPreferences();
        prefs.edit()
                .putFloat(Constants.PROPERTY_LAST_LATITUDE, (float) location.getLatitude())
                .putFloat(Constants.PROPERTY_LAST_LONGITUDE, (float) location.getLongitude())
                .apply();
    }
}
