package es.us.context4learning.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import java.util.Calendar;

import es.us.context4learning.Constants;
import es.us.context4learning.R;
import es.us.context4learning.data.api.google.appengine.GAEHelper;
import es.us.context4learning.utils.Utils;

public class MuteNotificationsBroadcastReceiver extends BroadcastReceiver{

    private static final String TAG = MuteNotificationsBroadcastReceiver.class.getCanonicalName();
    private Location mLocation;
    private SharedPreferences mPrefs;
    private Context mContext;
    private String mAction;
    private int mNotificationId;
    private Long mGAENotificationId;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Log.d(TAG, "onReceive");
        mAction = intent.getAction();
        mNotificationId = intent.getIntExtra(Constants.EXTRA_NOTIFICATION_ID, 1);
        mGAENotificationId = intent.getLongExtra(Constants.EXTRA_GAE_NOTIFICATION_ID, 0L);
        if(intent.hasExtra(Constants.EXTRA_LOCATION)){
            mLocation = intent.getParcelableExtra(Constants.EXTRA_LOCATION);
        }
        mPrefs = Utils.getSharedPreferences(context);
        muteNotifications();
    }

    private void muteNotifications(){
        Log.d(TAG, "muteNotifications mAction = " + mAction);
        if(mAction.equals(Constants.ACTION_MUTE_NOTIFICATION_1_HOUR)){
            add1HourNotificationRestriction();
        } else if (mAction.equals(Constants.ACTION_MUTE_NOTIFICATION_1_DAY)){
            add1DayNotificationRestriction();
        }
    }

    private void add1HourNotificationRestriction(){
        Calendar calendar = Calendar.getInstance();
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putLong(Constants.PROPERTY_1_HOUR_NOTIFICATION_RESTRICTION_START, calendar.getTimeInMillis());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        editor.putLong(Constants.PROPERTY_1_HOUR_NOTIFICATION_RESTRICTION_END,calendar.getTimeInMillis());
        editor.apply();
        ((NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(mNotificationId);
        Utils.showToast(mContext, mContext.getString(R.string.notifications_muted_1_hour));
        Log.d(TAG, "1 hour notification restriction added to SharedPreferences");
        GAEHelper.saveNotificationToServer(mContext, mGAENotificationId, Constants.NOTIFICATION_ACTION_MUTED_1_HOUR);
        GAEHelper.sendAuditEventToServer(
                mContext,
                Constants.AUDIT_CATEGORY_NOTIFICATION,
                Constants.AUDIT_ACTION_MUTED,
                mPrefs.getString(Constants.PROPERTY_USER_NAME, "") + " - " + "1 hour");
    }

    private void add1DayNotificationRestriction(){
        Calendar calendar = Calendar.getInstance();
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putLong(Constants.PROPERTY_1_DAY_NOTIFICATION_RESTRICTION_START, calendar.getTimeInMillis());
        calendar.add(Calendar.DAY_OF_MONTH,1);
        editor.putLong(Constants.PROPERTY_1_DAY_NOTIFICATION_RESTRICTION_END, calendar.getTimeInMillis());
        if(mLocation != null){
            editor.putLong(Constants.PROPERTY_1_DAY_NOTIFICATION_RESTRICTION_LATITUDE, Double.doubleToRawLongBits(mLocation.getLatitude()));
            editor.putLong(Constants.PROPERTY_1_DAY_NOTIFICATION_RESTRICTION_LONGITUDE, Double.doubleToRawLongBits(mLocation.getLongitude()));
            Utils.showToast(mContext, mContext.getString(R.string.notifications_muted_1_day_location));
            GAEHelper.saveNotificationToServer(mContext, mGAENotificationId, Constants.NOTIFICATION_ACTION_MUTED_24_HOURS_LOCATION);
        }else{
            Utils.showToast(mContext, mContext.getString(R.string.notifications_muted_1_day));
            GAEHelper.saveNotificationToServer(mContext, mGAENotificationId, Constants.NOTIFICATION_ACTION_MUTED_24_HOURS);
        }
        editor.apply();
        ((NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(mNotificationId);
        Log.d(TAG, "1 day notification restriction added to SharedPreferences");
        GAEHelper.sendAuditEventToServer(
                mContext,
                Constants.AUDIT_CATEGORY_NOTIFICATION,
                Constants.AUDIT_ACTION_MUTED,
                mPrefs.getString(Constants.PROPERTY_USER_NAME, "") + " - " + "1 day");
    }
}
