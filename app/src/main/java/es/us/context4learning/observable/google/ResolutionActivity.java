package es.us.context4learning.observable.google;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;

public class ResolutionActivity extends Activity {

    static final String ARG_CONNECTION_RESULT = "connectionResult";

    private static final int REQUEST_CODE_RESOLUTION = 123;

    private static boolean resolutionShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            handleIntent();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent();
    }

    private void handleIntent() {
        try {
            ConnectionResult connectionResult = getIntent().getParcelableExtra(ARG_CONNECTION_RESULT);
            connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
            resolutionShown = true;
        } catch (IntentSender.SendIntentException|NullPointerException e) {
            setResolutionResultAndFinish(Activity.RESULT_CANCELED);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_RESOLUTION) {
            setResolutionResultAndFinish(resultCode);
        } else {
            setResolutionResultAndFinish(Activity.RESULT_CANCELED);
        }
    }

    private void setResolutionResultAndFinish(int resultCode) {
        resolutionShown = false;
        BaseObservable.onResolutionResult(resultCode, (ConnectionResult) getIntent().getParcelableExtra(ARG_CONNECTION_RESULT));
        finish();
    }

    static boolean isResolutionShown() {
        return resolutionShown;
    }
}