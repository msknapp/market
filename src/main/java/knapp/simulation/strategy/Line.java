package knapp.simulation.strategy;

import java.util.Random;
import java.util.function.Function;

public class Line implements Function<Double, Double> {
    private static Random random = new Random();
    private final double intercept;
    private final double slope;

    private Line(double intercept, double slope) {
        this.intercept = intercept;
        this.slope = slope;
    }

    public double getIntercept() {
        return intercept;
    }

    public double getSlope() {
        return slope;
    }


    public static LineBuilder slope(double x) {
        return new LineBuilder().slope(x);
    }

    public static LineBuilder intercept(double x) {
        return new LineBuilder().intercept(x);
    }

    public static class LineBuilder {
        private double slope;
        private double intercept;

        public LineBuilder slope(double x) {
            this.slope = x;
            return this;
        }

        public LineBuilder intercept(double x) {
            this.intercept = x;
            return this;
        }

        public Line toLine() {
            return new Line(intercept, slope);
        }
    }

    @Override
    public Double apply(Double aDouble) {
        // a positive sigmas means the estimate is higher than the actual value,
        // so the market is undervalued.
        // generally your slope should be positive, so
        // as the estimate is increasingly higher than the actual,
        // the sigma increases,
        // and your percent stock increases.
        // the input is typically between +/- 3.
        // so it's smart to craft it so +3 returns 100.
        return intercept + slope * aDouble;
    }

    public static Line randomSlope() {
        double intercept = random.nextDouble()*0.4 + 0.55; // between 0.55 and 0.95
        double slope = random.nextDouble()*0.2 + 0.3; // between 0.3 and 0.5
        return slope(slope).intercept(intercept).toLine();
    }

    public static Line randomNear(Line original, double deviation) {
        double intercept = original.intercept + (random.nextDouble() - 0.5) * deviation;
        double slope = original.slope + (random.nextDouble() - 0.5) * deviation;
        return slope(slope).intercept(intercept).toLine();
    }
}
