import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.utility.Delay;

public class ObstacleAvoidance {

    private UltrasonicReader ultrasonic;
    private float threshold = 0.15f; // 15 cm

    public ObstacleAvoidance(UltrasonicReader ultrasonic) {
        this.ultrasonic = ultrasonic;
    }

    public boolean detected() {
        return ultrasonic.distanceValue < threshold;
    }

    private boolean stillSeesObject() {
        return ultrasonic.distanceValue < threshold;
    }

    public void avoid(EV3LargeRegulatedMotor left, EV3LargeRegulatedMotor right) {

        // STOP
        left.stop(true);
        right.stop();
        Delay.msDelay(300);

        // TURN LEFT 90°
        left.rotate(-180, true);
        right.rotate(180);
        Delay.msDelay(200);

        // MOVE FORWARD
        moveForward(left, right, 500);

        // CHECK OBJECT
        if (!stillSeesObject()) return;

        // TURN RIGHT (-90°)
        left.rotate(180, true);
        right.rotate(-180);
        Delay.msDelay(200);

        // MOVE FORWARD
        moveForward(left, right, 500);

        // CHECK AGAIN
        if (!stillSeesObject()) return;

        // TURN RIGHT AGAIN (-90°)
        left.rotate(180, true);
        right.rotate(-180);
        Delay.msDelay(200);

        // MOVE FORWARD
        moveForward(left, right, 500);
    }

    private void moveForward(EV3LargeRegulatedMotor left, EV3LargeRegulatedMotor right, int timeMs) {
        left.forward();
        right.forward();
        Delay.msDelay(timeMs);
        left.stop(true);
        right.stop();
    }
}