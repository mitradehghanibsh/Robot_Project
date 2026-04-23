import lejos.robotics.SampleProvider;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.Button;
import lejos.utility.Delay;

// --- LIGHT SENSOR THREAD ---

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
            Delay.msDelay(10);
        }
    }
}

// --- LINE FINDER ---

class LineFinderClass {
    
    private LightSensorReader lightSensor;
    private final float BLACK_THRESHOLD = 0.45f;

    public LineFinderClass(LightSensorReader lightSensor) {
        this.lightSensor = lightSensor;
    }

    public void findLine(EV3LargeRegulatedMotor left, EV3LargeRegulatedMotor right) {
        
        left.setSpeed(180);
        right.setSpeed(180);

        left.forward();
        right.backward();

        while (lightSensor.lightValue > BLACK_THRESHOLD && !Button.ESCAPE.isDown()) {
            Delay.msDelay(10);
        }

        left.stop(true);
        right.stop();

        Delay.msDelay(200);
    }
}

// --- TEST MAIN ---

public class LineFinder {
    public static void main(String[] args) {

        EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);
        EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.A);
        EV3ColorSensor colorSensor = new EV3ColorSensor(SensorPort.S1);

        LightSensorReader lightReader = new LightSensorReader(colorSensor);
        Thread sensorThread = new Thread(lightReader);
        sensorThread.setDaemon(true);
        sensorThread.start();

        LineFinderClass finder = new LineFinderClass(lightReader);

        Button.waitForAnyPress();

        finder.findLine(leftMotor, rightMotor);

        leftMotor.stop(true);
        rightMotor.stop();

        leftMotor.close();
        rightMotor.close();
        colorSensor.close();
    }
}