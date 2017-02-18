package app.we.go.oracle.features.recording;

/**
 * Created by apapad on 23/11/16.
 */
public interface RecordingCallbacks {
    void startRecording(SensorsToRecord sensorsToRecord, boolean checked, boolean handheldRadioButtonChecked);
    void onRecordingStopped(boolean success);
}
