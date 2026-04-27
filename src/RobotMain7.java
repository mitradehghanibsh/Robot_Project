import lejos.hardware.Button;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

/**
 * SensorSystem class runs as a separate thread.
 * It constantly reads the color and ultrasonic sensors.
 */
class SensorSystem implements Runnable {
    private SampleProvider colorProvider;
    private SampleProvider distProvider;
    
    // volatile ensures the main thread sees updated values instantly
    public volatile float lightValue = 0.0f;
    public volatile float distanceValue = 1.0f;

    public SensorSystem(EV3ColorSensor sCol, EV3UltrasonicSensor sDist) {
        this.colorProvider = sCol.getRedMode();
        this.distProvider = sDist.getDistanceMode();
    }

    public void run() {
        float[] cSample = new float[colorProvider.sampleSize()];
        float[] dSample = new float[distProvider.sampleSize()];
        
        while (!Button.ESCAPE.isDown()) {
            colorProvider.fetchSample(cSample, 0);
            lightValue = cSample[0];
            
            distProvider.fetchSample(dSample, 0);
            distanceValue = dSample[0];
            
            Delay.msDelay(15);
        }
    }
}

/**
 * RobotMain7 is the main execution class.
 * It manages the robot states: Following, Avoiding, and Recovery.
 */
public class RobotMain7 {
    public static void main(String[] args) {
        // Init hardware
        EV3LargeRegulatedMotor mLeft = new EV3LargeRegulatedMotor(MotorPort.B);
        EV3LargeRegulatedMotor mRight = new EV3LargeRegulatedMotor(MotorPort.A);
        EV3ColorSensor sColor = new EV3ColorSensor(SensorPort.S1);
        EV3UltrasonicSensor sUltra = new EV3UltrasonicSensor(SensorPort.S2);

        // Create objects (OOP)
        SensorSystem sensors = new SensorSystem(sColor, sUltra);
        PIDControler pid = new PIDControler(480f, 0f, 140f);
        ObstacleAvoidance avoidance = new ObstacleAvoidance(mLeft, mRight);
        
        // Start the sensor thread
        Thread t1 = new Thread(sensors);
        t1.setDaemon(true);
        t1.start();

        float target = 0.45f; // Threshold for the edge of the line
        int baseSpeed = 320;

        Button.waitForAnyPress();

        while (!Button.ESCAPE.isDown()) {
            
            // State: Check for Obstacle
            if (sensors.distanceValue < 0.20f) {
                mLeft.stop(true); 
                mRight.stop();
                
                avoidance.doDetour();
                
                // State: Recovery (Arcing back to the line)
                mLeft.setSpeed(150); 
                mRight.setSpeed(100);
                mLeft.forward(); 
                mRight.forward();
                
                while(sensors.lightValue > target && !Button.ESCAPE.isDown()) { 
                    Delay.msDelay(5); 
                }
                pid.reset();
            } 
            // State: PID Line Following
            else {
                float turnPower = pid.calculate(target, sensors.lightValue);
                
                // Limit correction so robot does not jerk too much
                if (turnPower > 160) turnPower = 160;
                if (turnPower < -160) turnPower = -160;

                int leftSpeed = (int)(baseSpeed + turnPower);
                int rightSpeed = (int)(baseSpeed - turnPower);

                // Speed safety limits and minimum range
                if (leftSpeed < 120) leftSpeed = 120;
                if (rightSpeed < 120) rightSpeed = 120;
                
                mLeft.setSpeed(Math.min(700, leftSpeed));
                mRight.setSpeed(Math.min(700, rightSpeed));

                mLeft.forward();
                mRight.forward();
            }
            Delay.msDelay(10);
        }

        // Close connections
        mLeft.close(); 
        mRight.close(); 
        sColor.close(); 
        sUltra.close();
    }
}