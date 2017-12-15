package es.us.context4learning.data.api.moodle;


import android.app.Activity;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import es.us.context4learning.Constants;
import es.us.context4learning.chrometabs.CustomTabActivityHelper;
import es.us.context4learning.utils.Utils;

import static es.us.context4learning.utils.Utils.openChromeTab;

public class MoodleHelper {

    public static void openMoodleMessagesWeb(Activity activity, CustomTabActivityHelper customTabActivityHelper, @Nullable Long courseId){
        SharedPreferences prefs = Utils.getSharedPreferences(activity.getApplicationContext());
        String user = prefs.getString(Constants.PROPERTY_USER_NAME, "");
        String pass = prefs.getString(Constants.PROPERTY_USER_PASS, "");
        if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)) {
            String url = MoodleApi.MOODLE_TASK_ACCESS_URL + "?user=" + user + "&pass=" + pass + "&url_acceso=" + MoodleApi.SERVICE_ENDPOINT
                    + "message/index.php"
                    + (courseId != null ? "?viewing=course_" + courseId : "");
            openChromeTab(activity, customTabActivityHelper, url);
        }
    }

    public static void openMoodleMarksWeb(Activity activity, CustomTabActivityHelper customTabActivityHelper, Long courseId){
        SharedPreferences prefs = Utils.getSharedPreferences(activity.getApplicationContext());
        String user = prefs.getString(Constants.PROPERTY_USER_NAME, "");
        String pass = prefs.getString(Constants.PROPERTY_USER_PASS, "");
        if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)) {
            String url = MoodleApi.MOODLE_TASK_ACCESS_URL + "?user=" + user + "&pass=" + pass + "&url_acceso=" + MoodleApi.SERVICE_ENDPOINT
                    + "grade/report/user/index.php"
                    + "?id=" + courseId;
            openChromeTab(activity, customTabActivityHelper, url);
        }
    }
}
