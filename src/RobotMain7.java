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
 * It constantly reads the color sensor.
 */
class SensorSystem implements Runnable {
    private SampleProvider colorProvider;
    public volatile float lightValue = 0.0f;

    public SensorSystem(EV3ColorSensor sCol) {
        this.colorProvider = sCol.getRedMode();
    }

    public void run() {
        float[] cSample = new float[colorProvider.sampleSize()];
        while (!Button.ESCAPE.isDown()) {
            colorProvider.fetchSample(cSample, 0);
            lightValue = cSample[0];
            Delay.msDelay(10);
        }
    }
}

/**
 * Dedicated thread for the Ultrasonic sensor to ensure 
 * maximum detection speed for obstacles.
 */
class UltrasonicThread implements Runnable {
    private SampleProvider distProvider;
    public volatile float distanceValue = 1.0f;

    public UltrasonicThread(EV3UltrasonicSensor sDist) {
        this.distProvider = sDist.getDistanceMode();
    }

    public void run() {
        float[] dSample = new float[distProvider.sampleSize()];
        while (!Button.ESCAPE.isDown()) {
            distProvider.fetchSample(dSample, 0);
            distanceValue = dSample[0];
            Delay.msDelay(5); // Ultra-fast polling
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

        // Create objects
        SensorSystem lightSensor = new SensorSystem(sColor);
        UltrasonicThread ultraSensor = new UltrasonicThread(sUltra);
        PIDControler pid = new PIDControler(480f, 0f, 140f);
        
        // Start the sensor threads
        Thread t1 = new Thread(lightSensor);
        Thread t2 = new Thread(ultraSensor);
        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();

        float target = 0.45f; 
        int baseSpeed = 320;

        Button.waitForAnyPress();

        while (!Button.ESCAPE.isDown()) {
            
            // State: Check for Obstacle (Increased range to 35cm for safety)
            if (ultraSensor.distanceValue < 0.35f) {
                mLeft.stop(true); 
                mRight.stop();
                
                // --- Integrated Obstacle Avoidance Logic ---
                mLeft.setSpeed(200); mRight.setSpeed(200);
                
                // 1. Turn away
                mLeft.rotate(-200, true);
                mRight.rotate(200);
                
                // 2. Drive around
                mLeft.forward(); mRight.forward();
                Delay.msDelay(1500);
                
                // 3. Turn back towards line
                mLeft.rotate(250, true);
                mRight.rotate(-250);
                
                // State: Recovery (Arcing back to the line)
                mLeft.setSpeed(180); 
                mRight.setSpeed(120);
                mLeft.forward(); 
                mRight.forward();
                
                while(lightSensor.lightValue > target && !Button.ESCAPE.isDown()) { 
                    Delay.msDelay(5); 
                }
                pid.reset();
            } 
            // State: PID Line Following
            else {
                float turnPower = pid.calculate(target, lightSensor.lightValue);
                
                if (turnPower > 160) turnPower = 160;
                if (turnPower < -160) turnPower = -160;

                int leftSpeed = (int)(baseSpeed + turnPower);
                int rightSpeed = (int)(baseSpeed - turnPower);

                if (leftSpeed < 120) leftSpeed = 120;
                if (rightSpeed < 120) rightSpeed = 120;
                
                mLeft.setSpeed(Math.min(700, leftSpeed));
                mRight.setSpeed(Math.min(700, rightSpeed));

                mLeft.forward();
                mRight.forward();
            }
            Delay.msDelay(10);
        }

        mLeft.close(); mRight.close(); sColor.close(); sUltra.close();
    }
}