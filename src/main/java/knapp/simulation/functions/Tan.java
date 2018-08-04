package knapp.simulation.functions;

import java.util.Random;

public class Tan implements EvolvableFunction {
    private static Random random = new Random();

    private final double intercept;
    private final double slope;
    private final double sensitivity;

    private Tan(double intercept, double slope, double sensitivity) {
        this.intercept = intercept;
        this.slope = slope;
        this.sensitivity = sensitivity;
    }

    public double getSensitivity() {
        return sensitivity;
    }

    public double getIntercept() {
        return intercept;
    }

    public double getSlope() {
        return slope;
    }

    public static Tan initialTan() {
        return slope(1).intercept(0.5).sensitivity(1.0).build();
    }

    @Override
    public Double apply(Double x) {
        // a positive sigmas means the estimate is higher than the actual value,
        // so the market is undervalued.
        // generally your slope should be positive

        double tmp = x * sensitivity;
        if (tmp < - Math.PI / 2.0) {
            tmp = Math.PI / 2.0 + 1e-4;
        }
        if (tmp > Math.PI / 2.0) {
            tmp = Math.PI / 2.0 - 1e-4;
        }

        return intercept + slope * Math.tan(tmp);
    }

    public static Tan.TanBuilder intercept(double x) {
        return new Tan.TanBuilder().intercept(x);
    }

    public static Tan.TanBuilder sensitivity(double x) {
        return new Tan.TanBuilder().sensitivity(x);
    }


    public static Tan.TanBuilder slope(double x) {
        return new Tan.TanBuilder().slope(x);
    }

    @Override
    public EvolvableFunction deviateRandomly(double deviation) {
        return randomNear(this,deviation);
    }

    @Override
    public String describe() {
        return String.format("percent stock = %f + %f * tan( %f * sigma )",getIntercept(),getSlope(),
                getSensitivity());
    }

    @Override
    public String describe(double value) {
        double y = apply(value);
        return String.format("%f = %f + %f * tan( %f * %f )",y,getIntercept(),getSlope(),getSensitivity(),value);
    }

    public static class TanBuilder {
        private double intercept;
        private double slope;
        private double sensitivity;

        public Tan.TanBuilder intercept(double x) {
            this.intercept = x;
            return this;
        }

        public Tan.TanBuilder slope(double x) {
            this.slope = x;
            return this;
        }

        public Tan.TanBuilder sensitivity(double x) {
            this.sensitivity = x;
            return this;
        }

        public Tan build() {
            return new Tan(intercept,slope,sensitivity);
        }
    }

    public static Tan randomNear(Tan original, double deviation) {
        double intercept = original.intercept + (random.nextDouble() - 0.5) * deviation;
        double slope = original.slope + (random.nextDouble() - 0.5) * deviation;
        double sensitivity = original.slope + (random.nextDouble() - 0.5) * deviation;
        return slope(slope).intercept(intercept).sensitivity(sensitivity).build();
    }
}
