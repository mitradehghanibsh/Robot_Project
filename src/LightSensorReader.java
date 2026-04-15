package src;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.Button;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class LightSensorReader implements Runnable {
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




