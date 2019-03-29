package es.situm.gettingstarted.drawbuilding.router;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

public class DetectedActivitiesIntentService extends IntentService {

    protected static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();

    public DetectedActivitiesIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        ArrayList<DetectedActivity> detectedActivities = (ArrayList<DetectedActivity>) result.getProbableActivities();
        for (DetectedActivity activity : detectedActivities) {
            broadcastActivity(activity);
        }
    }

    private void broadcastActivity(DetectedActivity detectedActivity) {
        Intent intent = new Intent(RouterConstants.BROADCAST_DETECTED_ACTIVITY);
        intent.putExtra("type", detectedActivity.getType());
        intent.putExtra("confidence", detectedActivity.getConfidence());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
