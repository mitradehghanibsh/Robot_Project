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

class UltrasonicBase {
    public static void main(String[] args) {
        EV3UltrasonicSensor ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S2);
        UltrasonicReader sensorLogic = new UltrasonicReader(ultrasonicSensor);
        Thread sensorThread = new Thread(sensorLogic);
        sensorThread.setDaemon(true);
        sensorThread.start();
        while (!Button.ESCAPE.isDown()) {
            Delay.msDelay(50);
        }
        ultrasonicSensor.close();
    }
}