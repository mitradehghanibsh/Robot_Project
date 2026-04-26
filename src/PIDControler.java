/**
 * PIDControler class handles the math for smooth line following.
 * It uses Proportional, Integral, and Derivative values to calculate 
 * the steering correction.
 */
public class PIDControler {
    private float KP, KI, KD;
    private float previousError = 0.0f;
    private float integral = 0.0f;

    public PIDControler(float KP, float KI, float KD) {
        this.KP = KP;
        this.KI = KI;
        this.KD = KD;
    }

    /**
     * Calculates the PID output based on the target and current light value.
     */
    public float calculate(float setpoint, float measuredValue) {
        float error = setpoint - measuredValue;
        
        // Proportional: How far we are from the line
        float P = KP * error;
        
        // Integral: Sum of errors over time
        integral += error;
        float I = KI * integral;
        
        // Derivative: How fast we are moving toward/away from the line
        float derivative = error - previousError;
        float D = KD * derivative;
        
        previousError = error;
        
        return P + I + D;
    }

    /**
     * Resets the error history when starting a new movement.
     */
    public void reset() {
        previousError = 0.0f;
        integral = 0.0f;
    }
}