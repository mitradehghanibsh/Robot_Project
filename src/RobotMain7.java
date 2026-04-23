import lejos.robotics.SampleProvider;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.Button;
import lejos.utility.Delay;

// --- SENSOR READING CLASS ---

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

// --- MAIN ROBOT EXECUTION ---

public class RobotMain7 {
    public static void main(String[] args) {

        EV3LargeRegulatedMotor mLeft = new EV3LargeRegulatedMotor(MotorPort.B);
        EV3LargeRegulatedMotor mRight = new EV3LargeRegulatedMotor(MotorPort.A);
        EV3ColorSensor sColor = new EV3ColorSensor(SensorPort.S1);

        LightSensorReader lightLogic = new LightSensorReader(sColor);

        PIDControler pid = new PIDControler(480f, 0f, 140f);

        Thread t1 = new Thread(lightLogic);
        t1.setDaemon(true);
        t1.start();

        Button.waitForAnyPress();

        float target = 0.45f;
        int baseSpeed = 320;

        while (!Button.ESCAPE.isDown()) {

            float turnPower = pid.calculate(target, lightLogic.lightValue);

            // limit correction so robot does not jerk too much
            if (turnPower > 160) turnPower = 160;
            if (turnPower < -160) turnPower = -160;

            int leftSpeed = (int)(baseSpeed + turnPower);
            int rightSpeed = (int)(baseSpeed - turnPower);

            // keep speeds in safe range
            if (leftSpeed < 120) leftSpeed = 120;
            if (rightSpeed < 120) rightSpeed = 120;

            if (leftSpeed > 700) leftSpeed = 700;
            if (rightSpeed > 700) rightSpeed = 700;

            mLeft.setSpeed(leftSpeed);
            mRight.setSpeed(rightSpeed);

            mLeft.forward();
            mRight.forward();

            Delay.msDelay(20);
        }

        mLeft.stop(true);
        mRight.stop();

        mLeft.close();
        mRight.close();
        sColor.close();
    }
}