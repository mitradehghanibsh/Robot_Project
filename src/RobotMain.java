import lejos.robotics.SampleProvider;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.Button;
import lejos.utility.Delay;

// --- SENSOR READING CLASSES ---

class UltrasonicReader implements Runnable {
    private SampleProvider distanceMode;
    private float[] sample;
    public volatile float distanceValue = 1.0f;

    public UltrasonicReader(EV3UltrasonicSensor sensor) {
        this.distanceMode = sensor.getDistanceMode();
        this.sample = new float[distanceMode.sampleSize()];
    }

    @Override
    public void run() {
        while (!Button.ESCAPE.isDown()) {
            distanceMode.fetchSample(sample, 0);
            distanceValue = sample[0];
            Delay.msDelay(50);
        }
    }
}

class LightSensorReader implements Runnable {
    private SampleProvider lightMode;
    private float[] sample;
    public volatile float lightValue = 0.0f;

    public LightSensorReader(EV3ColorSensor sensor) {
        this.lightMode = sensor.getRedMode();
        this.sample = new float[lightMode.sampleSize()];
    }

    @Override
    public void run() {
        while (!Button.ESCAPE.isDown()) {
            lightMode.fetchSample(sample, 0);
            lightValue = sample[0];
            Delay.msDelay(50);
        }
    }
}

// --- PID LOGIC ---

class PIDControler {
    private float KP, KI, KD;
    private float previousError = 0.0f;
    private float integral = 0.0f;

    public PIDControler(float KP, float KI, float KD) {
        this.KP = KP;
        this.KI = KI;
        this.KD = KD;
    }

    public float calculate(float setpoint, float measuredValue) {
        float error = setpoint - measuredValue;
        float P = KP * error;
        integral += error;
        float I = KI * integral;
        float derivative = error - previousError;
        float D = KD * derivative;
        previousError = error;
        return P + I + D;
    }

    public void reset() {
        previousError = 0.0f;
        integral = 0.0f;
    }
}

// --- LINE RECOVERY LOGIC ---

class LineFinder {
    private LightSensorReader lightSensor;
    private final float BLACK_THRESHOLD = 0.45f;

    public LineFinder(LightSensorReader lightSensor) {
        this.lightSensor = lightSensor;
    }

    public void findLine(EV3LargeRegulatedMotor left, EV3LargeRegulatedMotor right) {
        left.setSpeed(180);
        right.setSpeed(180);
        left.forward();
        right.backward(); 
        while (lightSensor.lightValue > BLACK_THRESHOLD) {
            Delay.msDelay(10); 
        }
        left.stop(true);
        right.stop();
        Delay.msDelay(200);
    }
}

// --- MAIN ROBOT EXECUTION ---

public class RobotMain {
    public static void main(String[] args) {
        
        // Setup hardware
        EV3LargeRegulatedMotor mLeft = new EV3LargeRegulatedMotor(MotorPort.B);
        EV3LargeRegulatedMotor mRight = new EV3LargeRegulatedMotor(MotorPort.A);
        EV3ColorSensor sColor = new EV3ColorSensor(SensorPort.S1);
        EV3UltrasonicSensor sUltra = new EV3UltrasonicSensor(SensorPort.S2);

        // Setup threads
        LightSensorReader lightLogic = new LightSensorReader(sColor);
        UltrasonicReader ultraLogic = new UltrasonicReader(sUltra);
        
        // Setup modules
        PIDControler pid = new PIDControler(600f, 0f, 200f); 
        ObstacleAvoidance avoidance = new ObstacleAvoidance(ultraLogic);
        LineFinder recovery = new LineFinder(lightLogic);
        
        // Start background sensors
        Thread t1 = new Thread(lightLogic);
        Thread t2 = new Thread(ultraLogic);
        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();

        Button.waitForAnyPress();

        while (!Button.ESCAPE.isDown()) {
            
            if (avoidance.detected()) {
                // Obstacle state
                avoidance.avoid(mLeft, mRight);
                recovery.findLine(mLeft, mRight);
                pid.reset();
            } else {
                // PID Following state
                float target = 0.45f;
                float turnPower = pid.calculate(target, lightLogic.lightValue);
                
                int baseSpeed = 250;
                mLeft.setSpeed(baseSpeed + turnPower);
                mRight.setSpeed(baseSpeed - turnPower);
                
                mLeft.forward();
                mRight.forward();
            }
            
            Delay.msDelay(20);
        }

        // System shutdown
        mLeft.close();
        mRight.close();
        sColor.close();
        sUltra.close();
    }
}