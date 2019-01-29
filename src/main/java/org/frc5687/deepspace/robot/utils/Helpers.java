package org.frc5687.deepspace.robot.utils;

/**
 * Created by Ben Bernard on 6/4/2018.
 */
public class Helpers {

    /**
     * Limit motor values to the -1.0 to +1.0 range.
     */
    public static double limit(double value) {
        if (value > 1.0) {
            return 1.0;
        }
        if (value < -1.0) {
            return -1.0;
        }
        return value;
    }

    /**
     * Returns 0.0 if the given value is within the specified range around zero. The remaining range
     * between the deadband and 1.0 is scaled from 0.0 to 1.0.
     *
     * @param value    value to clip
     * @param deadband range around zero
     */
    public static double applyDeadband(double value, double deadband) {
        if (Math.abs(value) > deadband) {
            if (value > 0.0) {
                return (value - deadband) / (1.0 - deadband);
            } else {
                return (value + deadband) / (1.0 - deadband);
            }
        } else {
            return 0.0;
        }
    }

    /**
     * Normalize all wheel speeds if the magnitude of any wheel is greater than 1.0.
     */
    public static void normalize(double[] wheelSpeeds) {
        double maxMagnitude = Math.abs(wheelSpeeds[0]);
        for (int i = 1; i < wheelSpeeds.length; i++) {
            double temp = Math.abs(wheelSpeeds[i]);
            if (maxMagnitude < temp) {
                maxMagnitude = temp;
            }
        }
        if (maxMagnitude > 1.0) {
            for (int i = 0; i < wheelSpeeds.length; i++) {
                wheelSpeeds[i] = wheelSpeeds[i] / maxMagnitude;
            }
        }
    }

    /**
     * Applies a transform to the input to provide better sensitivity at low speeds.
     * @param input the raw input value from a joystick
     * @param factor the raw sensitivity factor to apply
     * @return the adjusted control value
     */
    public static double applySensitivityFactor(double input, double factor){
        // See http://www.chiefdelphi.com/forums/showthread.php?p=921992

        // The transform can only work on values between -1 and 1.
        if (input>1) { return 1; }
        if (input <-1) { return -1; }

        // The sensitivity factor MUST be between 0 and 1!
        double capped = Math.max(Math.min(factor, 1),0);

        return capped*input*input*input + (1-capped)*input;
    }

}
