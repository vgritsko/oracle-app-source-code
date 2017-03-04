package app.we.go.oracle.features.recording;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.dropbox.core.v2.DbxClientV2;

import java.io.File;
import java.io.IOException;


import app.we.go.oracle.utils.NetworkUtils;
import app.we.go.oracle.R;
import app.we.go.oracle.features.landing.SensorCollection;
import app.we.go.oracle.features.common.MainActivity;
import app.we.go.oracle.upload.DropBoxUploadTask;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by apapad on 23/11/16.
 */

public class RecordingFragment extends Fragment {

    private static final String ARGS_USERNAME = "username";
    private static final String ARGS_SENSORS = "sensors";
    private static final String ARGS_FOUR_WHEELER = "fourWheeler";
    private static final String ARGS_HANDHELD = "handheld";
    private static final String ARGS_SAMPLE_RATE = "sample_rate";
    @BindView(R.id.timer)
    TextView timerView;
    private long startTime;
    private Runnable updateTimeTask;
    private Handler timerHandler;
    private MainActivity mainActicity;



    @BindView(R.id.red_light)
    ToggleButton redLightButton;

    @BindView(R.id.public_transport)
    ToggleButton publicTransportButton;

    @BindView(R.id.slow_traffic)
    ToggleButton slowTrafficButton;

    @BindView(R.id.rough_patch)
    ToggleButton roughPatchButton;

    @BindView(R.id.acceleration)
    Button accelerationButton;

    @BindView(R.id.lane_change)
    Button laneChangeButton;

    @BindView(R.id.bump)
    Button bumpButton;

    @BindView(R.id.hard_break)
    Button breakButton;

    @BindView(R.id.left_turn)
    Button leftTurnButton;

    @BindView(R.id.right_turn)
    Button rightTurnButton;


    private boolean timerCancelled;
    private DbxClientV2 client;
    private Handler sensorHandler;
   //private SensorDataWriter sensorDataWriter;
    private String filename;


    private SensorDataWriterService sensorDataWriterService;
    boolean mServiceBound = false;


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            SensorDataWriterService.LocalBinder binder = (SensorDataWriterService.LocalBinder) service;
            sensorDataWriterService = binder.getService();
            mServiceBound= true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mServiceBound = false;
        }
    };


    public RecordingFragment() {
    }

    public static RecordingFragment newInstance(String username,
                                                SensorsToRecord sensorsToRecord,
                                                boolean fourWheeler,
                                                boolean handheld, int sampleRate) {
        RecordingFragment f = new RecordingFragment();
        Bundle b = new Bundle();
        b.putString(ARGS_USERNAME, username);
        b.putParcelable(ARGS_SENSORS, sensorsToRecord);
        b.putBoolean(ARGS_FOUR_WHEELER, fourWheeler);
        b.putBoolean(ARGS_HANDHELD, handheld);
        b.putInt(ARGS_SAMPLE_RATE, sampleRate);

        f.setArguments(b);
        return f;
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.recording_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        accelerationButton.setOnTouchListener(
                new TouchButtonListener(PressButton.ACCELERATION)
        );
        laneChangeButton.setOnTouchListener(
                new TouchButtonListener(PressButton.LANE_CHANGE)
        );
        bumpButton.setOnTouchListener(
                new TouchButtonListener(PressButton.BUMP)
        );
        breakButton.setOnTouchListener(
                new TouchButtonListener(PressButton.BREAK)
        );
        leftTurnButton.setOnTouchListener(
                new TouchButtonListener(PressButton.LEFT_TURN)
        );
        rightTurnButton.setOnTouchListener(
                new TouchButtonListener(PressButton.RIGHT_TURN)
        );

        publicTransportButton.setOnCheckedChangeListener(
                new ToggleButtonListener(PressButton.PUBLIC));
        redLightButton.setOnCheckedChangeListener(
                new ToggleButtonListener(PressButton.RED_LIGHT));
        slowTrafficButton.setOnCheckedChangeListener(
                new ToggleButtonListener(PressButton.SLOW_TRAFFIC));
        roughPatchButton.setOnCheckedChangeListener(
                new ToggleButtonListener(PressButton.ROUGH_PATCH));


        timerHandler = new Handler();


        mainActicity=(MainActivity) getActivity();
        sensorDataWriterService=mainActicity.getSensorDataWriterService();


       // sensorHandler = sensorDataWriter.getHandler();
        sensorHandler = sensorDataWriterService.getHandler();
        updateTimeTask = new Runnable() {
            public void run() {
                final long start = startTime;
                long millis = System.currentTimeMillis() - start;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                int hours = minutes / 60;
                minutes = minutes % 60;
                seconds = seconds % 60;

                String elapsed = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                timerView.setText(elapsed);

                if (!timerCancelled) {
                    timerHandler.postDelayed(this, 200);
                }
            }
        };



        /*Handler obdTreadHandler = sensorDataWriter.getObdThreadHandler();
        MainActivity activity = (MainActivity)getActivity();
        OBDSenderReciverService obdService=activity.getOBDService();
        if (obdService!=null)
            if (obdService.getState()==OBDSenderReciverService.STATE_CONNECTED) {
                obdService.setHandler(obdTreadHandler);
                sensorHandler.obtainMessage(sensorDataWriter.MSG_BLUETOOTH_IS_ON,
                        OBDSenderReciverService.STATE_CONNECTED).sendToTarget();
            }*/

       // filename = sensorDataWriter.initFile();

       /* if (!sensorDataWriterService.isRecording()) {
            filename = sensorDataWriterService.initFile();
        }*/

    }





    @Override
    public void onResume() {
        super.onResume();

       /* sensorDataWriter.initSensors((
                SensorCollection) getArguments().getParcelable(ARGS_SENSORS));*/
        if (!sensorDataWriterService.isRecording()) {

            sensorDataWriterService.initService(getArguments().getBoolean(ARGS_FOUR_WHEELER),
                    getArguments().getBoolean(ARGS_HANDHELD),
                    getArguments().getInt(ARGS_SAMPLE_RATE),
                    mainActicity.getLocationRequest());

            filename = sensorDataWriterService.initFile();

            sensorDataWriterService.initSensors((
                    SensorCollection) getArguments().getParcelable(ARGS_SENSORS));
            startTime = System.currentTimeMillis();

            sensorDataWriterService.start(startTime);
        }

        else {

            startTime=sensorDataWriterService.getStartTime();

        }



        timerHandler.removeCallbacks(updateTimeTask);
        timerHandler.postDelayed(updateTimeTask, 100);


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        client = ((MainActivity) getActivity()).getClient();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            getActivity().onBackPressed();
        }
    }

    @OnClick(R.id.stop_recording_button)
    public void onStopRecording() {
        stopRecording(true);
    }


    public void stopRecording(boolean uploadFileNow) {
        timerCancelled = true;
        //sensorDataWriter.stopRecording();
        sensorDataWriterService.stopRecording();

        if (uploadFileNow) {
            if (!NetworkUtils.isNetworkAvailable(getActivity())) {
                onFileUploadComplete(false);
                return;
            }

            final ProgressDialog dialog = ProgressDialog.show(getActivity(), "",
                    getActivity().getString(R.string.uploading), true);
            dialog.show();


            DropBoxUploadTask task = new DropBoxUploadTask(client, getArguments().getString(ARGS_USERNAME),
                    new DropBoxUploadTask.Callback() {
                        @Override
                        public void onComplete() {
                            dialog.dismiss();
                            onFileUploadComplete(true);
                        }

                        @Override
                        public void onError() {
                            dialog.dismiss();
                            onFileUploadComplete(false);
                        }
                    });

            File f = new File(getActivity().getFilesDir(), filename);
            task.execute(f);
        }
    }

    private void handleIOException(IOException e) {
        Log.e("RECORD", "IOException", e);
        timerCancelled = true;
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.io_error_title)
                .setMessage(R.string.io_error_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .create();
        alertDialog.show();
    }



    public void onFileUploadComplete(boolean success) {
        Toast.makeText(getActivity(), success ? R.string.upload_success : R.string.upload_failed, Toast.LENGTH_SHORT).show();
        if (getActivity() != null) {
            ((RecordingCallbacks) getActivity()).onRecordingStopped(!success);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //sensorDataWriter.stopRecording();



    }

    class TouchButtonListener implements View.OnTouchListener {

        private PressButton button;

        public TouchButtonListener(PressButton button) {
            this.button = button;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // PRESSED
                    buttonDown();
                    return false; // if you want to handle the touch event
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // RELEASED
                    buttonUp();
                    return false; // if you want to handle the touch event
            }
            return false;
        }

        private void buttonDown() {
            PressButton.Pressed p = new PressButton.Pressed(button, true);
            sensorHandler.obtainMessage(
                    SensorDataWriter.MSG_BUTTON, p).sendToTarget();
        }

        private void buttonUp() {
            PressButton.Pressed p = new PressButton.Pressed(button, false);
            sensorHandler.obtainMessage(
                    SensorDataWriter.MSG_BUTTON, p).sendToTarget();
        }

    }

    private class ToggleButtonListener implements CompoundButton.OnCheckedChangeListener {
        private PressButton button;

        public ToggleButtonListener(PressButton button) {
            this.button = button;
        }


        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            PressButton.Pressed p = new PressButton.Pressed(button, isChecked);
            sensorHandler.obtainMessage(
                    SensorDataWriter.MSG_BUTTON, p).sendToTarget();
        }
    }
}
