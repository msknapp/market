package knapp.simulation.functions;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import java.util.Random;

public class Normal implements EvolvableFunction {
    private static Random random = new Random();
    private final double intercept;
    private final double slope;

    private Normal(double intercept, double slope) {
        this.intercept = intercept;
        this.slope = slope;
    }

    public static EvolvableFunction initialNormal() {
        return slope(1).intercept(0).toNormal();
    }

    public double getIntercept() {
        return intercept;
    }

    public double getSlope() {
        return slope;
    }


    public static Normal.NormalBuilder slope(double x) {
        return new Normal.NormalBuilder().slope(x);
    }

    public static Normal.NormalBuilder intercept(double x) {
        return new Normal.NormalBuilder().intercept(x);
    }

    @Override
    public EvolvableFunction deviateRandomly(double deviation) {
        return randomNear(this,deviation);
    }

    @Override
    public String describe() {
        return String.format("Best Strategy Equation: percent stock = %f + %f * Cum. Prob. (sigma)\n",
                getIntercept(), getSlope());
    }

    @Override
    public String describe(double value) {
        double y = apply(value);
        return String.format("Solving: %f = %f + %f * Cum. Prob. (%f)",y,
                getIntercept(), getSlope(), value);
    }

    public static class NormalBuilder {
        private double slope;
        private double intercept;

        public Normal.NormalBuilder slope(double x) {
            this.slope = x;
            return this;
        }

        public Normal.NormalBuilder intercept(double x) {
            this.intercept = x;
            return this;
        }

        public Normal toNormal() {
            return new Normal(intercept, slope);
        }
    }

    @Override
    public Double apply(Double x) {
        // a positive sigmas means the estimate is higher than the actual value,
        // so the market is undervalued.
        NormalDistribution normalDistribution = new NormalDistributionImpl(0, 1);
        double prob = 0;
        try {
            prob = normalDistribution.cumulativeProbability(x);
        } catch (MathException e) {
            e.printStackTrace();
        }
        return intercept + slope * prob;
    }


    public static Normal randomNear(Normal original, double deviation) {
        double intercept = original.intercept + (random.nextDouble() - 0.5) * deviation;
        double slope = original.slope + (random.nextDouble() - 0.5) * deviation;
        return slope(slope).intercept(intercept).toNormal();
    }
}
