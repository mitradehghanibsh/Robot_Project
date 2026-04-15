package src;

import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.Button;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class UltrasonicReader implements Runnable {
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


