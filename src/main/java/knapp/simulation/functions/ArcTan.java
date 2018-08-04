package knapp.simulation.functions;

import java.util.Random;

public class ArcTan implements EvolvableFunction {
    private static Random random = new Random();

    private final double intercept;
    private final double slope;
    private final double sensitivity;

    public static ArcTan initialArcTan() {
        return slope(1).intercept(0.5).sensitivity(1).build();
    }

    private ArcTan(double intercept, double slope,double sensitivity) {
        this.intercept = intercept;
        this.slope = slope;
        this.sensitivity = sensitivity;
    }

    public double getIntercept() {
        return intercept;
    }

    public double getSlope() {
        return slope;
    }

    @Override
    public Double apply(Double x) {
        // a positive sigmas means the estimate is higher than the actual value,
        // so the market is undervalued.
        // generally your slope should be positive

        return intercept + slope * Math.atan( sensitivity * x);
    }

    public static ArcTanBuilder intercept(double x) {
        return new ArcTanBuilder().intercept(x);
    }

    public static ArcTanBuilder slope(double x) {
        return new ArcTanBuilder().slope(x);
    }

    public static ArcTanBuilder sensitivity(double x) {
        return new ArcTanBuilder().sensitivity(x);
    }

    @Override
    public EvolvableFunction deviateRandomly(double deviation) {
        return randomNear(this,deviation);
    }

    @Override
    public String describe() {
        return String.format("percent stock = %f + %f * atan(%f * sigma)",getIntercept(),getSlope(), sensitivity);
    }

    @Override
    public String describe(double value) {
        double y = apply(value);
        return String.format("%f = %f + %f * atan(%f * %f)",y,getIntercept(),getSlope(),sensitivity,value);
    }

    public static class ArcTanBuilder {

        private double intercept;
        private double sensitivity;
        private double slope;

        public ArcTanBuilder intercept(double x) {
            this.intercept = x;
            return this;
        }

        public ArcTanBuilder sensitivity(double x) {
            this.sensitivity = x;
            return this;
        }

        public ArcTanBuilder slope(double x) {
            this.slope = x;
            return this;
        }

        public ArcTan build() {
            return new ArcTan(intercept,slope, sensitivity);
        }
    }

    public static ArcTan randomArcTan() {
        double intercept = random.nextDouble()*0.4 + 0.55; // between 0.55 and 0.95
        double slope = random.nextDouble()*0.2 + 0.3; // between 0.3 and 0.5
        return slope(slope).intercept(intercept).build();
    }

    public static ArcTan randomNear(ArcTan original, double deviation) {
        double intercept = original.intercept + (random.nextDouble() - 0.5) * deviation;
        double slope = original.slope + (random.nextDouble() - 0.5) * deviation;
        double sensitivity = original.sensitivity + (random.nextDouble() - 0.5) * deviation;
        return slope(slope).intercept(intercept).sensitivity(sensitivity).build();
    }
}
