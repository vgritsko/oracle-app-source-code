package app.we.go.oracle.features.recording;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import app.we.go.oracle.R;
import app.we.go.oracle.features.landing.SensorCollection;
import app.we.go.oracle.helper.ApplicationHelper;
import app.we.go.oracle.obd2.commands.OBDCommand;
import app.we.go.oracle.obd2.commands.OBDCurrentDataRequest;
import app.we.go.oracle.obd2.commands.OBDRequest;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_GYROSCOPE;
import static android.hardware.Sensor.TYPE_LINEAR_ACCELERATION;
import static android.hardware.Sensor.TYPE_MAGNETIC_FIELD;
import static android.hardware.Sensor.TYPE_PROXIMITY;
import static android.hardware.Sensor.TYPE_ROTATION_VECTOR;

/**
 * Created by Vadim on 21.02.2017.
 */

public class SensorDataWriterService extends Service implements
        SensorEventListener, LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String BASE_FILENAME = "sensor_data_";
    public static final int MSG_BUTTON = 1;
    public  static  final int MSG_BLUETOOTH_IS_ON =2;
    private   GoogleApiClient mGoogleApiClient;

    private Handler obdThreadHandler;
    private Handler sensorHandler;
    private int sampleRate;
    private FileOutputStream fos;
    private Context context;
    private SensorDataWriterService.Entry currentValues;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor proximity;
    private Sensor magnetometer;
    private Sensor rotationVector;
    private Sensor linearAcceleration;
    private SensorManager sensorManager;
    private Runnable writeValuesTask;
    private boolean recordingCancelled;
    private PendingIntent activityDetectionPendingIntent;
    private boolean isRecording =false;
    private long elapsed;
    private long startTime;
    private LocationRequest locationRequest;
    private PowerManager.WakeLock wakeLock;

    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class LocalBinder extends Binder {

        public SensorDataWriterService getService() {

            return SensorDataWriterService.this;
        }
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        initLocation();
        PowerManager powerManager  = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,getClass().getSimpleName());
        mGoogleApiClient.connect();
    }


    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private void initLocation() {

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(ActivityRecognition.API)
                    .build();


        }

        locationRequest = new LocationRequest();
        locationRequest.setInterval(50);
        locationRequest.setFastestInterval(50);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
       /* result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                       // checkLocationPermissions();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                       // initLandingFragment();
                        break;
                }
            }
        });*/
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();


        return START_STICKY;
    }

    public void initService(boolean fourWheeler, boolean handheld, int sampleRate,
                            LocationRequest locationReques) {

        wakeLock.acquire();

        this.sampleRate = sampleRate;

        recordingCancelled=false;

        this.locationRequest=locationReques;

        context=this.getApplicationContext();

        currentValues = new SensorDataWriterService.Entry(fourWheeler, handheld);

        HandlerThread thread = new HandlerThread("IOThread");
        thread.start();
        sensorHandler = new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_BUTTON) {
                    PressButton.Pressed obj = (PressButton.Pressed) msg.obj;
                    switch (obj.button) {
                        case ACCELERATION:
                            currentValues.setHardAcceleration(obj.pressed);
                            break;
                        case LANE_CHANGE:
                            currentValues.setLaneChange(obj.pressed);
                            break;
                        case BUMP:
                            currentValues.setBump(obj.pressed);
                            break;
                        case BREAK:
                            currentValues.setHardBreak(obj.pressed);
                            break;
                        case LEFT_TURN:
                            currentValues.setLeftTurn(obj.pressed);
                            break;
                        case RIGHT_TURN:
                            currentValues.setRightTurn(obj.pressed);
                            break;
                        case RED_LIGHT:
                            currentValues.setRedLight(obj.pressed);
                            break;
                        case PUBLIC:
                            currentValues.setPublicTransport(obj.pressed);
                            break;
                        case SLOW_TRAFFIC:
                            currentValues.setSlowTraffic(obj.pressed);
                            break;
                        case ROUGH_PATCH:
                            currentValues.setRoughPatch(obj.pressed);
                            break;

                    }
                }

                if (msg.what== MSG_BLUETOOTH_IS_ON) {

                    currentValues.setBlueToothConnection(true);

                }
            }
        };

        obdThreadHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                OBDRequest response = (OBDCurrentDataRequest) msg.obj;
                OBDCommand rpmCmd = response.getCommandsList().get(0);

                currentValues.setRPMvalue(Integer.valueOf(rpmCmd.getCalculatedResult()));

                OBDCommand speedCmd = response.getCommandsList().get(1);
                currentValues.setSpeed(Integer.valueOf(speedCmd.getCalculatedResult()));

                OBDCommand throttlePositionCmd = response.getCommandsList().get(2);
                currentValues.setThrottlePosition(Integer.valueOf(throttlePositionCmd.getCalculatedResult()));

                OBDCommand engineRuntimeCmd = response.getCommandsList().get(3);
                currentValues.setEngineRuntime(Integer.valueOf(engineRuntimeCmd.getCalculatedResult()));

            }
        };

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);


    }



    public void initSensors(SensorCollection sensorAvailability) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (sensorAvailability.hasAccelerometer()) {
            accelerometer = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, sampleRate * 1000, sensorHandler);
        }
        if (sensorAvailability.hasGyroscope()) {
            gyroscope = sensorManager.getDefaultSensor(TYPE_GYROSCOPE);
            sensorManager.registerListener(this, gyroscope, sampleRate * 1000, sensorHandler);
        }
        if (sensorAvailability.hasRotationVector()) {
            rotationVector = sensorManager.getDefaultSensor(TYPE_ROTATION_VECTOR);
            sensorManager.registerListener(this, rotationVector, sampleRate * 1000, sensorHandler);
        }
        if (sensorAvailability.hasProximity()) {
            proximity = sensorManager.getDefaultSensor(TYPE_PROXIMITY);
            sensorManager.registerListener(this, proximity, sampleRate * 1000, sensorHandler);
        }
        if (sensorAvailability.hasMagnetometer()) {
            magnetometer = sensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD);
            sensorManager.registerListener(this, magnetometer, sampleRate * 1000, sensorHandler);
        }

        if (sensorAvailability.hasLinearAcceleration()) {
            linearAcceleration=sensorManager.getDefaultSensor(TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this,linearAcceleration,sampleRate*1000,sensorHandler);

        }

        if (sensorAvailability.hasGps()) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient
            );
            if (lastLocation != null) {
                currentValues.setLocation(lastLocation);
            }

           LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, locationRequest, this, sensorHandler.getLooper());
        }
        Intent intent = new Intent(context, ActivityRecognizedService.class );
        activityDetectionPendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mGoogleApiClient, sampleRate, activityDetectionPendingIntent);


        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final ArrayList<DetectedActivity> detectedActivities =
                        intent.getParcelableArrayListExtra(ActivityRecognizedService.EXTRA_DETECTED_ACTIVITIES);

                sensorHandler.post(
                        new Runnable() {
                            @Override
                            public void run() {
                                for (DetectedActivity detectedActivity : detectedActivities) {
                                    switch(detectedActivity.getType()) {
                                        case DetectedActivity.STILL :
                                            currentValues.setStillConfidence(detectedActivity.getConfidence());
                                            break;
                                        case DetectedActivity.ON_FOOT :
                                            currentValues.setOnFootConfidence(detectedActivity.getConfidence());
                                            break;
                                        case DetectedActivity.IN_VEHICLE :
                                            currentValues.setVehicleConfidence(detectedActivity.getConfidence());
                                            break;
                                    }
                                }

                            }
                        }
                );
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ActivityRecognizedService.ACTION_DETECT_ACTIVITY);
        context.registerReceiver(receiver, filter);
    }


    public String initFile() {
        String ds = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        final String filename = BASE_FILENAME + ds + ".csv";

        sensorHandler.post(new Runnable() {
            @Override
            public void run() {

                try {
                    fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
                    fos.write(currentValues.getHeaderLine().getBytes());
                } catch (IOException e) {
                    handleIOException(e);
                }
            }
        });

        return filename;
    }



    public void start(final long startTime) {
        this.startTime=startTime;
        writeValuesTask = new Runnable() {
            @Override
            public void run() {
                try {
                    if (!recordingCancelled) {
                        sensorHandler.postDelayed(this, sampleRate);
                        long currentTimestamp = System.currentTimeMillis();
                        currentValues.setTimestamp(currentTimestamp);
                        currentValues.setElapsed(currentTimestamp-startTime);
                        currentValues.setBatteryLevel(calculateBatteryLife());

                        float[] R = new float[9];
                        float[] I = new float[9];

                        SensorManager.getRotationMatrix(R, I, currentValues.accelerometerValues, currentValues.magnetometerValues);

                        float[] orientation = new float[3];
                        SensorManager.getOrientation(R, orientation);
                        currentValues.setOrientationValues(orientation);

                        fos.write(currentValues.getCSVLine().getBytes());

                    }
                } catch (IOException e) {
                    handleIOException(e);
                }
            }
        };

        sensorHandler.removeCallbacks(writeValuesTask);
        sensorHandler.post(writeValuesTask);
        isRecording =true;
    }

    public void stopRecording() {
        if (!recordingCancelled) {
            cleanup();
            isRecording=false;
            wakeLock.release();
        }

    }


    private float calculateBatteryLife() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return level / (float) scale;

    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.equals(accelerometer)) {
            currentValues.setAccelerometerValues(event.values);
        } else if (event.sensor.equals(gyroscope)) {
            currentValues.setGyroscopeValues(event.values);
        } else if (event.sensor.equals(rotationVector)) {
            currentValues.setRotationValues(event.values);
        } else if (event.sensor.equals(proximity)) {
            currentValues.setProximityValues(event.values);
        } else if (event.sensor.equals(magnetometer)) {
            currentValues.setMagnetometerValues(event.values);
        }
        else if (event.sensor.equals(linearAcceleration)) {
            currentValues.setLinearAccelerationValues(event.values);
        }


    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        currentValues.setLocation(location);
        Log.i("LOCTION",location.toString());
    }



    private void handleIOException(IOException e) {
        if (!recordingCancelled) {
            Toast.makeText(context, R.string.io_error_message, Toast.LENGTH_SHORT).show();
        }
    }
    public void cleanup() {
        sensorHandler.post(new Runnable() {
            @Override
            public void run() {
                recordingCancelled = true;
                try {
                    fos.close();
                } catch (IOException e) {
                    handleIOException(e);
                }

            }
        });

        sensorHandler.removeCallbacks(writeValuesTask);
        sensorManager.unregisterListener(this);
        //sensorManager=null;
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, activityDetectionPendingIntent);

        this.context = null;
    }


    public Handler getHandler() {
        return sensorHandler;
    }

    public Handler getObdThreadHandler (){
        return obdThreadHandler;
    }

    public  long getStartTime() {return startTime; }

    public boolean isRecording() {return isRecording;}




    class Entry {
        private static final char NL = '\n';
        private final boolean fourWheeler;
        private final boolean handheld;
        long timestamp;
        long elapsed;
        float[] accelerometerValues = new float[3];
        float[] gyroscopeValues = new float[3];
        float[] rotationValues = new float[4];
        float[] orientationValues = new float[3];
        float[] proximityValues = new float[1];
        float[] magnetometerValues = new float[3];
        float[] linearAccelerationValues = new float[3];
        Location location;
        int vehicleConfidence;
        int onFootConfidence;
        int stillConfidence;
        float batteryLevel;
        boolean hardAcceleration;
        boolean laneChange;
        boolean bump;
        boolean hardBreak;
        boolean leftTurn;
        boolean rightTurn;
        boolean redLight;
        boolean publicTransport;
        boolean slowTraffic;

        boolean roughPatch;
        int rpm;
        int speed;
        int throttlePosition;
        int engineRuntime;
        boolean blueToothConnection;
        String gmtOffset;

        private static final char SEP = ';';

        public Entry(boolean fourWheeler, boolean handheld) {
            this.fourWheeler = fourWheeler;
            this.handheld = handheld;

            TimeZone tz = TimeZone.getDefault();
            Calendar cal = GregorianCalendar.getInstance(tz);
            int offsetInMillis = tz.getOffset(cal.getTimeInMillis());

            String offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
            gmtOffset = "GMT"+(offsetInMillis >= 0 ? "+" : "-") + offset;


        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public void setElapsed(long elapsed) {
            this.elapsed = elapsed;
        }

        public void setAccelerometerValues(float[] accelerometerValues) {
            this.accelerometerValues = accelerometerValues;
        }

        public void setGyroscopeValues(float[] gyroscopeValues) {
            this.gyroscopeValues = gyroscopeValues;
        }
        public void setOrientationValues(float[] orientationValues) {
            this.orientationValues = orientationValues;
        }

        public void setRotationValues(float[] rotationValues) {
            this.rotationValues = rotationValues;
        }

        public void setProximityValues(float[] proximityValues) {
            this.proximityValues = proximityValues;
        }

        public void setMagnetometerValues(float[] magnetometerValues) {
            this.magnetometerValues = magnetometerValues;
        }

        public void setLinearAccelerationValues(float [] linearAccelerationValues){
            this.linearAccelerationValues=linearAccelerationValues;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public void setVehicleConfidence(int vehicleConfidence) {
            this.vehicleConfidence = vehicleConfidence;
        }

        public void setOnFootConfidence(int onFootConfidence) {
            this.onFootConfidence = onFootConfidence;
        }

        public void setStillConfidence(int stillConfidence) {
            this.stillConfidence = stillConfidence;
        }


        public void setBatteryLevel(float batteryLevel) {
            this.batteryLevel = batteryLevel;
        }

        public void setHardAcceleration(boolean hardAcceleration) {
            this.hardAcceleration = hardAcceleration;
        }

        public void setLaneChange(boolean laneChange) {
            this.laneChange = laneChange;
        }

        public void setBump(boolean bump) {
            this.bump = bump;
        }

        public void setHardBreak(boolean hardBreak) {
            this.hardBreak = hardBreak;
        }

        public void setLeftTurn(boolean leftTurn) {
            this.leftTurn = leftTurn;
        }

        public void setRightTurn(boolean rightTurn) {
            this.rightTurn = rightTurn;
        }

        public void setRedLight(boolean redLight) {
            this.redLight = redLight;
        }

        public void setPublicTransport(boolean publicTransport) {
            this.publicTransport = publicTransport;
        }

        public void setSlowTraffic(boolean slowTraffic) {
            this.slowTraffic = slowTraffic;
        }

        public void setRoughPatch(boolean roughPatch) {
            this.roughPatch = roughPatch;
        }

        public void setRPMvalue(int rpm) {this.rpm=rpm;   }

        public void setSpeed (int speed) {this.speed=speed;}

        public void setThrottlePosition (int throttlePosition) {
            this.throttlePosition=throttlePosition;
        }

        public void setEngineRuntime (int engineRuntime) { this.engineRuntime=engineRuntime;}

        public  void setBlueToothConnection (boolean blueToothConnection) {
            this.blueToothConnection=blueToothConnection;
        }

        public String getHeaderLine() {
            StringBuilder sb = new StringBuilder();
            sb.append("TIMESTAMP").append(SEP)
                    .append("ELAPSED").append(SEP)
                    .append("ACCELERATOR-x").append(SEP)
                    .append("ACCELERATOR-y").append(SEP)
                    .append("ACCELERATOR-z").append(SEP)
                    .append("GYROSCOPE-x").append(SEP)
                    .append("GYROSCOPE-y").append(SEP)
                    .append("GYROSCOPE-z").append(SEP)
                    .append("ORIENTATION-azimuth").append(SEP)
                    .append("ORIENTATION-pitch").append(SEP)
                    .append("ORIENTATION-roll").append(SEP)
                    .append("ROTATION-VECTOR-x").append(SEP)
                    .append("ROTATION-VECTOR-y").append(SEP)
                    .append("ROTATION-VECTOR-z").append(SEP)
                    .append("ROTATION-VECTOR-w").append(SEP)
                    .append("PROXIMITY").append(SEP)
                    .append("MAGNETOMETER-x").append(SEP)
                    .append("MAGNETOMETER-y").append(SEP)
                    .append("MAGNETOMETER-z").append(SEP)
                    .append("LOCATION-lat").append(SEP)
                    .append("LOCATION-long").append(SEP)
                    .append("LOCATION-accuracy").append(SEP)
                    .append("LOCATION-bearing").append(SEP)
                    .append("LOCATION-speed").append(SEP)
                    .append("ACTIVITY-CONFIDENCE-vehicle").append(SEP)
                    .append("ACTIVITY-CONFIDENCE-foot").append(SEP)
                    .append("ACTIVITY-CONFIDENCE-still").append(SEP)
                    .append("BATTERY-LEVEL").append(SEP)
                    .append("FOUR_WHEELER").append(SEP)
                    .append("HANDHELD").append(SEP)
                    .append("HARD-ACCELERATION").append(SEP)
                    .append("LANE-CHANGE").append(SEP)
                    .append("BUMP").append(SEP)
                    .append("HARD-BREAK").append(SEP)
                    .append("LEFT-TURN").append(SEP)
                    .append("RIGHT-TURN").append(SEP)
                    .append("RED-LIGHT").append(SEP)
                    .append("PUBLIC-TRANSPORT").append(SEP)
                    .append("SLOW-TRAFFIC").append(SEP)
                    .append("ROUGH-PATCH").append(SEP)
                    .append("RPM").append(SEP)
                    .append("SPEED").append(SEP)
                    .append("THROTTLE-POSITION").append(SEP)
                    .append("ENGINE-RUNTIME").append(SEP)
                    .append("BLUETOOTH").append(SEP)
                    .append("TIMEZONE").append(SEP)
                    .append("LINEAR-ACCELERATION-x").append(SEP)
                    .append("LINEAR-ACCELERATION-y").append(SEP)
                    .append("LINEAR-ACCELERATION-z")
                    .append(NL);
            return sb.toString();
        }

        public String getCSVLine() {

            StringBuilder sb = new StringBuilder();
            /*Log.i("ELAPSED",String.valueOf(elapsed));
            Log.i("BLUETOOTH",String.valueOf(blueToothConnection));
            Log.i("RPM",String.valueOf(rpm));*/
            sb.append(timestamp).append(SEP)
                    .append(elapsed).append(SEP)
                    .append(accelerometerValues[0]).append(SEP)
                    .append(accelerometerValues[1]).append(SEP)
                    .append(accelerometerValues[2]).append(SEP)
                    .append(gyroscopeValues[0]).append(SEP)
                    .append(gyroscopeValues[1]).append(SEP)
                    .append(gyroscopeValues[2]).append(SEP)
                    .append(orientationValues[0]).append(SEP)
                    .append(orientationValues[1]).append(SEP)
                    .append(orientationValues[2]).append(SEP)
                    .append(rotationValues[0]).append(SEP)
                    .append(rotationValues[1]).append(SEP)
                    .append(rotationValues[2]).append(SEP)
                    .append(rotationValues[3]).append(SEP)
                    .append(proximityValues[0]).append(SEP)
                    .append(magnetometerValues[0]).append(SEP)
                    .append(magnetometerValues[1]).append(SEP)
                    .append(magnetometerValues[2]).append(SEP)
                    .append(location != null ? location.getLatitude() : 0).append(SEP)
                    .append(location != null ? location.getLongitude() : 0).append(SEP)
                    .append(location != null ? location.getAccuracy() : 0).append(SEP)
                    .append(location != null ? location.getBearing() : 0).append(SEP)
                    .append(location != null ? location.getSpeed() : 0).append(SEP)
                    .append(vehicleConfidence).append(SEP)
                    .append(onFootConfidence).append(SEP)
                    .append(stillConfidence).append(SEP)
                    .append(batteryLevel).append(SEP)
                    .append(fourWheeler).append(SEP)
                    .append(handheld).append(SEP)
                    .append(hardAcceleration).append(SEP)
                    .append(laneChange).append(SEP)
                    .append(bump).append(SEP)
                    .append(hardBreak).append(SEP)
                    .append(leftTurn).append(SEP)
                    .append(rightTurn).append(SEP)
                    .append(redLight).append(SEP)
                    .append(publicTransport).append(SEP)
                    .append(slowTraffic).append(SEP)
                    .append(roughPatch).append(SEP)
                    .append(rpm).append(SEP)
                    .append(speed).append(SEP)
                    .append(throttlePosition).append(SEP)
                    .append(engineRuntime).append(SEP)
                    .append(blueToothConnection).append(SEP)
                    .append(gmtOffset).append(SEP)
                    .append(linearAccelerationValues[0]).append(SEP)
                    .append(linearAccelerationValues[1]).append(SEP)
                    .append(linearAccelerationValues[2])
                    .append(NL);
            return sb.toString();

        }
    }




}
