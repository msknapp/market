package knapp.simulation.functions;

import java.util.Random;

public class CubicPolynomial implements EvolvableFunction {
    private static Random random = new Random();

    private final double intercept;
    private final double cubeSlope;
    private final double lineSlope;

    private CubicPolynomial(double intercept, double cubeSlope, double lineSlope) {
        this.intercept = intercept;
        this.cubeSlope = cubeSlope;
        this.lineSlope = lineSlope;
    }

    public double getIntercept() {
        return intercept;
    }


    public double getCubeSlope() {
        return cubeSlope;
    }
    public double getLineSlope() {
        return lineSlope;
    }

    public static CubicPolynomial initialCubed() {
        return cubeSlope(0.1).lineSlope(0.4).intercept(0.5).build();
    }

    @Override
    public Double apply(Double x) {
        // a positive sigmas means the estimate is higher than the actual value,
        // so the market is undervalued.
        // generally your cubeSlope should be positive

        return intercept + cubeSlope * Math.pow(x, 3) + lineSlope * (x);
    }

    public static CubicPolynomialBuilder intercept(double x) {
        return new CubicPolynomialBuilder().intercept(x);
    }

    public static CubicPolynomialBuilder cubeSlope(double x) {
        return new CubicPolynomialBuilder().cubeSlope(x);
    }
    public static CubicPolynomialBuilder lineSlope(double x) {
        return new CubicPolynomialBuilder().lineSlope(x);
    }

    @Override
    public EvolvableFunction deviateRandomly(double deviation) {
        return randomNear(this,deviation);
    }

    @Override
    public String describe() {
        return String.format("percent stock = %f + %f * ( sigma ) ^ 3 + %f * ( sigma ) ",
                getIntercept(), getCubeSlope(),
                getLineSlope());
    }

    @Override
    public String describe(double value) {
        double y = apply(value);
        return String.format("%f = %f + %f * ( %f ) ^ 3 + %f * ( %f )",
                y,getIntercept(),
                getCubeSlope(),
                value,
                getLineSlope(), value);
    }

    public static class CubicPolynomialBuilder {

        private double intercept;
        private double cubeSlope;
        private double lineSlope;

        public CubicPolynomialBuilder intercept(double x) {
            this.intercept = x;
            return this;
        }


        public CubicPolynomialBuilder cubeSlope(double x) {
            this.cubeSlope = x;
            return this;
        }

        public CubicPolynomialBuilder lineSlope(double x) {
            this.lineSlope = x;
            return this;
        }

        public CubicPolynomial build() {
            return new CubicPolynomial(intercept, cubeSlope, lineSlope);
        }
    }

    public static CubicPolynomial randomNear(CubicPolynomial original, double deviation) {
        double intercept = original.intercept + (random.nextDouble() - 0.5) * deviation;
        double cubeSlope = original.cubeSlope + (random.nextDouble() - 0.5) * deviation;
        double lineSlope = original.cubeSlope + (random.nextDouble() - 0.5) * deviation;
        return cubeSlope(cubeSlope).lineSlope(lineSlope).intercept(intercept).build();
    }
}
