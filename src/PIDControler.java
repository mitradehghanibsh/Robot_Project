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

        float error = setpoint - measuredValue;
        //Integral term
        integral += error;

        //Derivative term
        float derivative = error - previousError;

        // pid output
        float output = KP * error + KI * integral + KD * derivative;

        //update pervios error for next derivative calculation
        previousError = error;

        return output;
    }

}

class PIDTest {
    public static void main(String[] args) {
        PIDControler pid = new PIDControler(600f, 0f, 200f);

        float setpoint = 10.0f; // desired value
        float measuredValue = 0.0f; // initial value

        for (int i = 0; i < 100; i++) {
            float output = pid.calculate(setpoint, measuredValue);
            System.out.println("Step " + i +" ,Measured: " + measuredValue + ",Output: " + output);
            // Simulate the system response (for testing purposes)
            measuredValue += output * 0.02f; // Adjust this factor as needed
            measuredValue *=0.98f;
            try {
                // time delay
                Thread.sleep(100); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}