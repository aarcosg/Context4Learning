package es.us.context4learning;


import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.location.DetectedActivity;

public class Constants {

    public static final String PACKAGE_NAME = "es.us.context4learning";
    public static final String GOOGLE_APPENGINE_URL = "https://context4learning-us.appspot.com/_ah/api/";
    public static final String GOOGLE_APPENGINE_APP_NAME = "context4learning-us";
    public static final String GCM_SENDER_ID = "379588751087";
    public static final String PROPERTY_USER_ID = "user_id";
    public static final String PROPERTY_USER_NAME = "user_name";
    public static final String PROPERTY_USER_PASS = "user_pass";
    public static final String PROPERTY_DEVICE_ID = "device_id";
    public static final String PROPERTY_INSTANCE_ID = "instance_id";
    public static final String PROPERTY_MOODLE_TOKEN = "moodle_token";
    public static final String PROPERTY_MOODLE_USER_ID = "moodle_user_id";
    public static final String PROPERTY_APP_VERSION = "app_version";
    public static final String PROPERTY_DRAWER_LEARNED = "drawer_learned";
    public static final String PROPERTY_LAST_ACTIVITY = "last_activity";
    public static final String PROPERTY_LAST_CANDIDATE_ACTIVITY = "last_candidate_activity";
    public static final String PROPERTY_ACTIVITY_UPDATES = "activity_updates";
    public static final String PROPERTY_1_HOUR_NOTIFICATION_RESTRICTION_START = "1_hour_noti_res_start";
    public static final String PROPERTY_1_HOUR_NOTIFICATION_RESTRICTION_END = "1_hour_noti_res_end";
    public static final String PROPERTY_1_DAY_NOTIFICATION_RESTRICTION_START = "1_day_noti_res_start";
    public static final String PROPERTY_1_DAY_NOTIFICATION_RESTRICTION_END = "1_day_noti_res_end";
    public static final String PROPERTY_1_DAY_NOTIFICATION_RESTRICTION_LATITUDE = "1_day_noti_res_lat";
    public static final String PROPERTY_1_DAY_NOTIFICATION_RESTRICTION_LONGITUDE = "1_day_noti_res_lon";
    public static final String PROPERTY_RECEIVE_NOTIFICATIONS = "receive_notifications";
    public static final String PROPERTY_SHOW_VIDEO_WIFI = "show_video_wifi";
    public static final String PROPERTY_LAST_NOTIFICATION_TIME = "last_notification_time";
    public static final String PROPERTY_LAST_LATITUDE = "last_latitude";
    public static final String PROPERTY_LAST_LONGITUDE = "last_longitude";
    public static final String PROCESS_DETECTED_CONTEXT = PACKAGE_NAME + ".PROCESS_DETECTED_CONTEXT";
    public static final String PROCESS_MUTE_NOTIFICATION = PACKAGE_NAME + ".PROCESS_MUTE_NOTIFICATION";

    public static final int PLAY_SERVICES_RESOLUTION_REQUEST  = 9000;
    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 3 * 60 * 1000;
    public static final String EXTRA_DETECTED_CONTEXTS = PACKAGE_NAME + ".EXTRA_DETECTED_CONTEXTS";
    public static final String EXTRA_RECEIVER = PACKAGE_NAME + ".EXTRA_RECEIVER";
    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".EXTRA_LOCATION";
    public static final String EXTRA_LATITUDE = PACKAGE_NAME + ".EXTRA_LATITUDE";
    public static final String EXTRA_LONGITUDE = PACKAGE_NAME + ".EXTRA_LONGITUDE";
    public static final String EXTRA_ID = PACKAGE_NAME + ".EXTRA_ID";
    public static final String EXTRA_RESULT = PACKAGE_NAME + ".EXTRA_RESULT";
    public static final String EXTRA_ADDRESS = PACKAGE_NAME + ".EXTRA_ADDRESS";
    public static final String EXTRA_DETECTED_CONTEXT = PACKAGE_NAME + ".EXTRA_DETECTED_CONTEXT";
    public static final String EXTRA_DETECTED_CONTEXT_CONFIDENCE = PACKAGE_NAME + ".EXTRA_DETECTED_CONTEXT_CONFIDENCE";
    public static final String EXTRA_URL = PACKAGE_NAME + ".EXTRA_URL";
    public static final String EXTRA_NOTIFICATION_ID = PACKAGE_NAME + ".EXTRA_NOTIFICATION_ID";
    public static final String EXTRA_GAE_NOTIFICATION_ID = PACKAGE_NAME + ".EXTRA_GAE_NOTIFICATION_ID";
    public static final String EXTRA_COURSE = PACKAGE_NAME + ".EXTRA_COURSE";
    public static final String EXTRA_FRAGMENT = PACKAGE_NAME + ".EXTRA_FRAGMENT";

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final int MIN_DETECTED_CONFIDENCE = 70;
    public static final double DEFAULT_LATITUDE = 37.359954;
    public static final double DEFAULT_LONGITUDE = -5.987395;
    public static final float DEFAULT_RADIUS_RESTRICTION = 200; // meters
    public static final String FILTER_WALKING = "Andando";
    public static final String FILTER_STILL = "Parado";
    public static final String FILTER_VEHICLE = "Vehiculo";
    public static final String ACTION_MUTE_NOTIFICATION_1_HOUR = PACKAGE_NAME + ".ACTION_MUTE_NOTIFICATION_1_HOUR";
    public static final String ACTION_MUTE_NOTIFICATION_1_DAY = PACKAGE_NAME + ".ACTION_MUTE_NOTIFICATION_1_DAY";
    public static final String ACTION_START_ACTIVITY_RECOGNITION = PACKAGE_NAME + ".ACTION_START_ACTIVITY_RECOGNITION";
    public static final String ACTION_STOP_ACTIVITY_RECOGNITION = PACKAGE_NAME + ".ACTION_STOP_ACTIVITY_RECOGNITION";
    public static final String ACTION_ACTIVITY_RECOGNITION_RESULT = PACKAGE_NAME + ".ACTION_ACTIVITY_RECOGNITION_RESULT";
    public static final String NOTIFICATION_ACTION_OPENED = "opened";
    public static final String NOTIFICATION_ACTION_MUTED_1_HOUR = "muted_1_hour";
    public static final String NOTIFICATION_ACTION_MUTED_24_HOURS = "muted_24_hours";
    public static final String NOTIFICATION_ACTION_MUTED_24_HOURS_LOCATION = "muted_24_hours_location";
    public static final String[] FCM_TOPICS = {"global"};
    public static final String REMOTE_CONFIG_CONTEXT_RECOGNITION = "is_context_recognition_on";
    public static final String REMOTE_CONFIG_DETECTION_INTERVAL_IN_MILLIS = "detection_interval_in_millis";
    public static final String REMOTE_CONFIG_STILL_RESET_INTERVAL_IN_MILLIS = "still_reset_interval_in_millis";
    public static final String REMOTE_CONFIG_STILL_CONTEXT_RESET = "is_still_context_reset_on";
    public static final String AUDIT_CATEGORY_NOTIFICATION = "Notification";
    public static final String AUDIT_CATEGORY_ACTIVITY = "Activity";
    public static final String AUDIT_CATEGORY_CONTEXT = "Context";
    public static final String AUDIT_CATEGORY_TASK = "Task";
    public static final String AUDIT_CATEGORY_LIMIT_NOTIFICATION = "Limit Notification";
    public static final String AUDIT_CATEGORY_LOCATION_RESTRICTION = "Location Restriction";
    public static final String AUDIT_CATEGORY_TIME_RESTRICTION = "Time Restriction";
    public static final String AUDIT_CATEGORY_VIDEO_ONLY_WIFI = "Video Only Wifi";
    public static final String AUDIT_CATEGORY_SCREEN_VIEW = "Screen View";
    public static final String AUDIT_ACTION_OPENED = "Opened";
    public static final String AUDIT_ACTION_CLOSED = "Closed";
    public static final String AUDIT_ACTION_DETECTED = "Detected";
    public static final String AUDIT_ACTION_MUTED = "Muted";
    public static final String AUDIT_ACTION_RECEIVED = "Received";
    public static final String AUDIT_ACTION_EDITED = "Edited";
    public static final String AUDIT_ACTION_ADDED = "Added";
    public static final String AUDIT_ACTION_REMOVED = "Removed";
    public static final String AUDIT_ACTION_ENABLED = "Enabled";
    public static final String AUDIT_ACTION_DISABLED = "Disabled";
    public static final String AUDIT_ACTION_HIT = "Hit";
    public static final String SCREEN_NAME_MOODLE_MESSAGES_WEB = "Moodle Messages Web";
    public static final String SCREEN_NAME_STUDENT_PROGRESS_REPORT = "Student Progress Report";
    public static final Long PILOT_COURSE_ID = 11L;
    public static final String PILOT_GENERAL_COURSE_INFO_URL = "https://context4learning.cica.es/mod/resource/view.php?id=343";
    public static final String EMPTY_RESULTS_ERROR_MSG = "Expected BEGIN_ARRAY but was BEGIN_OBJECT at line 1 column 2 path $";

    /**
     * List of DetectedActivity types that we monitor in this sample.
     */
    public static final int[] MONITORED_ACTIVITIES = {
/*            DetectedActivity.STILL,
            DetectedActivity.ON_FOOT,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.TILTING,
            DetectedActivity.UNKNOWN*/
            DetectedActivity.STILL,
            DetectedActivity.ON_FOOT,
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.UNKNOWN
    };

    /**
     * Returns a human readable String corresponding to a detected activity type.
     */
    public static String getActivityString(Context context, int detectedActivityType) {
        Resources resources = context.getResources();
        switch(detectedActivityType) {
/*            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.UNKNOWN:
                return resources.getString(R.string.unknown);
            default:
                return resources.getString(R.string.unknown);*/
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_FOOT:
            case DetectedActivity.RUNNING:
            case DetectedActivity.WALKING:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            default:
                return resources.getString(R.string.unknown);
        }
    }
}
