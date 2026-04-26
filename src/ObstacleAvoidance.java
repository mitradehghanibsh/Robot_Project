import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.utility.Delay;

/**
 * ObstacleAvoidance class handles the detour maneuver.
 * When an object is detected, it moves the robot in a box-shaped path.
 */
public class ObstacleAvoidance {
    private EV3LargeRegulatedMotor left;
    private EV3LargeRegulatedMotor right;

    public ObstacleAvoidance(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) {
        this.left = leftMotor;
        this.right = rightMotor;
    }

    /**
     * Performs a 90-degree detour around an obstacle.
     */
    public void doDetour() {
        // Set a constant speed for precise turns
        left.setSpeed(200);
        right.setSpeed(200);

        // 1. Turn Right 90 degrees
        left.rotate(380, true); 
        right.rotate(-380);

        // 2. Drive Forward past the obstacle side
        left.forward();
        right.forward();
        Delay.msDelay(1500);

        // 3. Turn Left 90 degrees
        left.rotate(-380, true);
        right.rotate(380);

        // 4. Drive Forward past the obstacle length
        left.forward();
        right.forward();
        Delay.msDelay(2500);

        // 5. Turn Left 90 degrees to face back toward the line
        left.rotate(-380, true);
        right.rotate(380);
    }
}