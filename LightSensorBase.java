import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.Button;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

class LightSensorReader implements Runnable {
    private SampleProvider lightMode;
    private float[] sample;

    // Reflected light value (0.0 - 1.0), updated every 50 ms
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

public class LightSensorBase {
    public static void main(String[] args) {

        EV3ColorSensor lightSensor = new EV3ColorSensor(SensorPort.S1);

        LightSensorReader sensorLogic = new LightSensorReader(lightSensor);
        Thread sensorThread = new Thread(sensorLogic);
        sensorThread.setDaemon(true);
        sensorThread.start();

        while (!Button.ESCAPE.isDown()) {

            // Test loop to keep the program running

            Delay.msDelay(50);
        }

        lightSensor.close();
    }
}
