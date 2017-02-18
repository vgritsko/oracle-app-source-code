package app.we.go.oracle.features.recording;

import android.app.IntentService;
import android.content.Intent;
import android.os.Parcelable;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;


public class ActivityRecognizedService extends IntentService {

    public static final String EXTRA_DETECTED_ACTIVITIES = "detected";
    public static final String ACTION_DETECT_ACTIVITY = "DETECT_ACTIVITY";

    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }

    public ActivityRecognizedService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities( result.getProbableActivities() );
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> detectedActivities) {
        Intent i = new Intent(ACTION_DETECT_ACTIVITY);
        i.putParcelableArrayListExtra(EXTRA_DETECTED_ACTIVITIES, (ArrayList<? extends Parcelable>) detectedActivities);
        sendBroadcast(i);
    }
}