package app.we.go.oracle.features.recording;

public enum PressButton {
    ACCELERATION,
    LANE_CHANGE,
    BUMP,
    BREAK,
    LEFT_TURN,
    RIGHT_TURN,
    RED_LIGHT,
    PUBLIC,
    SLOW_TRAFFIC,
    ROUGH_PATCH;


    static class Pressed {
        PressButton button;
        boolean pressed;

        public Pressed(PressButton button, boolean pressed) {
            this.button = button;
            this.pressed = pressed;
        }
    }
}
