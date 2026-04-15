import lejos.robotics.SampleProvider;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.Button;
import lejos.utility.Delay;


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
public class RobotMain {
    public static void main(String[] args) {
        EV3LargeRegulatedMotor mLeft = new EV3LargeRegulatedMotor(MotorPort.B);
        EV3LargeRegulatedMotor mRight = new EV3LargeRegulatedMotor(MotorPort.A);
        EV3ColorSensor sColor = new EV3ColorSensor(SensorPort.S1);
        EV3UltrasonicSensor sUltra = new EV3UltrasonicSensor(SensorPort.S2);

        LightSensorReader lightLogic = new LightSensorReader(sColor);
        UltrasonicReader ultraLogic = new UltrasonicReader(sUltra);
        
        Thread t1 = new Thread(lightLogic);
        Thread t2 = new Thread(ultraLogic);
        
        t1.setDaemon(true);
        t2.setDaemon(true);
        
        t1.start();
        t2.start();

        Button.waitForAnyPress();

        while (!Button.ESCAPE.isDown()) {
            if (ultraLogic.distanceValue < 0.15f) {
                mLeft.stop(true);
                mRight.stop(false);
            } else {
                if (lightLogic.lightValue < 0.45f) { 
                    mLeft.setSpeed(400);
                    mRight.setSpeed(100);
                } else {
                    mLeft.setSpeed(100);
                    mRight.setSpeed(400);
                }
                mLeft.forward();
                mRight.forward();
            }
            Delay.msDelay(20);
        }

        mLeft.close();
        mRight.close();
        sColor.close();
        sUltra.close();
    }
}