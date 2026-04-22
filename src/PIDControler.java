import lejos.robotics.SampleProvider;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.Button;
import lejos.utility.Delay;
public class PIDControler {
    private float KP , KI , KD ;
    private float previousError = 0.0f;
    private float integral = 0.0f;
    
public PIDControler(float KP, float KI, float KD) {
        this.KP = KP;
        this.KI = KI;
        this.KD = KD;
    }
    // calculate the pid output that control robot moving
    public float calculate(float setpoint, float measuredValue) {
        // Current difference between target and measured value
        float error = setpoint - measuredValue;
        // Proportional part
        float P = KP * error;

        // Integral part
        integral += error;
        float I = KI * integral;

        // Derivative part
        float derivative = error - previousError;
        float D = KD * derivative;

        // Save current error for next step
        previousError = error;

        // Final PID output
        return P + I + D;
    
    }

    public void reset() {
        previousError = 0.0f;
        integral = 0.0f;
    }

    public void setTunings(float KP, float KI, float KD) {
        this.KP = KP;
        this.KI = KI;
        this.KD = KD;
    }

}