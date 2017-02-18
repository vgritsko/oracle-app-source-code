package app.we.go.oracle.features.landing;

public interface SensorCollection {
        boolean hasAccelerometer();
        boolean hasGyroscope();
        boolean hasGps();
        boolean hasOrientation();
        boolean hasRotationVector();
        boolean hasProximity();
        boolean hasMagnetometer();
        boolean hasLinearAcceleration();

}
