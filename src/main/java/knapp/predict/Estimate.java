package knapp.predict;

public interface Estimate {
    double getEstimatedValue();
    double getStandardDeviation();
    double getDeviationSigmas(double currentValue);
    double getPercentile(double currentValue);

}
