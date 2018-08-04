package knapp.simulation.functions;

import java.util.Random;

public class Cubed implements EvolvableFunction {
    private static Random random = new Random();

    private final double intercept;
    private final double slope;

    private Cubed(double intercept, double slope) {
        this.intercept = intercept;
        this.slope = slope;
    }

    public double getIntercept() {
        return intercept;
    }

    public double getSlope() {
        return slope;
    }

    public static Cubed initialCubed() {
        return slope(1).intercept(0.5).build();
    }

    @Override
    public Double apply(Double x) {
        // a positive sigmas means the estimate is higher than the actual value,
        // so the market is undervalued.
        // generally your slope should be positive

        return intercept + slope * Math.pow(x, 3);
    }

    public static Cubed.CubedBuilder intercept(double x) {
        return new Cubed.CubedBuilder().intercept(x);
    }

    public static Cubed.CubedBuilder slope(double x) {
        return new Cubed.CubedBuilder().slope(x);
    }

    @Override
    public EvolvableFunction deviateRandomly(double deviation) {
        return randomNear(this,deviation);
    }

    @Override
    public String describe() {
        return String.format("percent stock = %f + %f * ( sigma ) ^ 3",getIntercept(),getSlope());
    }

    @Override
    public String describe(double value) {
        double y = apply(value);
        return String.format("%f = %f + %f * ( %f ) ^ 3",y,getIntercept(),getSlope(),value);
    }

    public static class CubedBuilder {

        private double intercept;
        private double slope;

        public Cubed.CubedBuilder intercept(double x) {
            this.intercept = x;
            return this;
        }

        public Cubed.CubedBuilder slope(double x) {
            this.slope = x;
            return this;
        }

        public Cubed build() {
            return new Cubed(intercept,slope);
        }
    }

    public static Cubed randomNear(Cubed original, double deviation) {
        double intercept = original.intercept + (random.nextDouble() - 0.5) * deviation;
        double slope = original.slope + (random.nextDouble() - 0.5) * deviation;
        return slope(slope).intercept(intercept).build();
    }
}
