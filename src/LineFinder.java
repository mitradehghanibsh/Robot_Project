import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.utility.Delay;

public class LineFinder {
    
    private LightSensorReader lightSensor;
    private final float BLACK_THRESHOLD = 0.45f;

    // Connects to the light sensor data
    public LineFinder(LightSensorReader lightSensor) {
        this.lightSensor = lightSensor;
    }

    // Rotates the robot until it finds black
    public void findLine(EV3LargeRegulatedMotor left, EV3LargeRegulatedMotor right) {
        
        left.setSpeed(180);
        right.setSpeed(180);

        // Start rotating to search
        left.forward();
        right.backward(); 

        // Loop until black line is detected
        while (lightSensor.lightValue > BLACK_THRESHOLD) {
            Delay.msDelay(10); 
        }

        // Stop once found
        left.stop(true);
        right.stop();
        
        Delay.msDelay(200);
    }
}