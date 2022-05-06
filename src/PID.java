public class PID {

    // PID controller variables
    private double P, I,D,max_i,integral,last_error;
    private boolean first_run;

    // PID constructor
    public PID(double p, double i, double d, double max_i){
        this.P = p;
        this.I = i;
        this.D = d;
        integral = 0;
        this.max_i = max_i;
        first_run=true;
    }

    // Checks if the value is in the right delta scope
    private double constrain(double value, double max, double min){
        if(value > max){ return max;}
        if(value < min){ return min;}
        return value;
    }

    // PID update
    public double update(double error,double dt) {
        if (first_run) {
            last_error = error;
            first_run = false;
        }
        integral += I * error * dt;
        double diff = (error - last_error) / dt;
        double const_integral = constrain(integral, max_i, -max_i);
        double control_out = P * error + D * diff + const_integral;
        last_error = error;
        return control_out;
    }
}
