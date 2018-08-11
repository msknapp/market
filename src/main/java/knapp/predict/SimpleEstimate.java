package knapp.predict;

public class SimpleEstimate implements Estimate {
    private final double realValue;
    private final double estimatedValue;
    private final double percentile;
    private final double sigmas;

    public SimpleEstimate(double realValue, double estimatedValue, double percentile, double sigmas) {
        this.realValue = realValue;
        this.estimatedValue = estimatedValue;
        this.percentile = percentile;
        this.sigmas = sigmas;
    }

    public double getSigmas() {
        return sigmas;
    }

    public double getRealValue() {
        return realValue;
    }

    public double getEstimatedValue() {
        return estimatedValue;
    }

    @Override
    public double getStandardDeviation() {
        return 0;
    }

    @Override
    public double getDeviationSigmas(double currentValue) {
        return 0;
    }

    @Override
    public double getPercentile(double currentValue) {
        return 0;
    }

    public double getPercentile() {
        return percentile;
    }
}
