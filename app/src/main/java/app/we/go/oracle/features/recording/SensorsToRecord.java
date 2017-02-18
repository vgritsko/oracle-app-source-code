package app.we.go.oracle.features.recording;

import android.os.Parcel;
import android.os.Parcelable;

import app.we.go.oracle.features.landing.SensorCollection;

public class SensorsToRecord implements SensorCollection, Parcelable {

    private boolean magnetometer;
    private boolean accelerometer;
    private boolean gyro;
    private boolean rotationVector;
    private boolean orientation;
    private boolean proximity;
    private boolean gps;
    private boolean linearAcceleration;

    public boolean hasMagnetometer() {
        return magnetometer;
    }



    public void setMagnetometer(boolean magnetometer) {
        this.magnetometer = magnetometer;
    }

    public boolean hasAccelerometer() {
        return accelerometer;
    }

    public void setAccelerometer(boolean accelerometer) {
        this.accelerometer = accelerometer;
    }

    public boolean hasGyroscope() {
        return gyro;
    }

    public void setGyroscope(boolean gyro) {
        this.gyro = gyro;
    }

    public boolean hasRotationVector() {
        return rotationVector;
    }

    public void setRotationVector(boolean rotationVector) {
        this.rotationVector = rotationVector;
    }

    public boolean hasOrientation() {
        return orientation;
    }

    public void setOrientation(boolean orientation) {
        this.orientation = orientation;
    }

    public boolean hasProximity() {
        return proximity;
    }

    public void setProximity(boolean proximity) {
        this.proximity = proximity;
    }

    public boolean hasGps() {
        return gps;
    }

    public void setGps(boolean gps) {
        this.gps = gps;
    }

    @Override
    public boolean hasLinearAcceleration() {
        return linearAcceleration;
    }

    public  void setLinearAcceleration(boolean linearAcceleration) {this.linearAcceleration=linearAcceleration;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.magnetometer ? (byte) 1 : (byte) 0);
        dest.writeByte(this.accelerometer ? (byte) 1 : (byte) 0);
        dest.writeByte(this.gyro ? (byte) 1 : (byte) 0);
        dest.writeByte(this.rotationVector ? (byte) 1 : (byte) 0);
        dest.writeByte(this.orientation ? (byte) 1 : (byte) 0);
        dest.writeByte(this.proximity ? (byte) 1 : (byte) 0);
        dest.writeByte(this.gps ? (byte) 1 : (byte) 0);
        dest.writeByte(this.linearAcceleration ? (byte)1 : (byte) 0);
    }

    public SensorsToRecord() {
    }

    protected SensorsToRecord(Parcel in) {
        this.magnetometer = in.readByte() != 0;
        this.accelerometer = in.readByte() != 0;
        this.gyro = in.readByte() != 0;
        this.rotationVector = in.readByte() != 0;
        this.orientation = in.readByte() != 0;
        this.proximity = in.readByte() != 0;
        this.gps = in.readByte() != 0;
        this.linearAcceleration = in.readByte() !=0;
    }

    public static final Parcelable.Creator<SensorsToRecord> CREATOR = new Parcelable.Creator<SensorsToRecord>() {
        @Override
        public SensorsToRecord createFromParcel(Parcel source) {
            return new SensorsToRecord(source);
        }

        @Override
        public SensorsToRecord[] newArray(int size) {
            return new SensorsToRecord[size];
        }
    };
}
