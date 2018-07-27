package knapp;

public class Model {

    private final double[] parameters;
    private final double standardDeviation;
    private final double rsquared;

    public Model(double[] parameters, double standardDeviation, double rsquared) {
        this.parameters = new double[parameters.length];
        System.arraycopy(parameters,0,this.parameters,0,parameters.length);
        this.standardDeviation = standardDeviation;
        this.rsquared = rsquared;
    }

    public double[] getParameters() {
        return parameters;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public double getRsquared() {
        return rsquared;
    }

    public Estimate produceEstimate(double[] currentInputs, double presentValue) {
        double est = parameters[0];
        for (int i = 0;i<currentInputs.length;i++) {
            double b = parameters[i+1];
            est += b * currentInputs[i];
        }
        double sigmas = (est - presentValue) / standardDeviation;
        // TODO implement percentile
        return new Estimate(presentValue,est,0,sigmas);
    }

    public static class Estimate {
        private final double realValue;
        private final double estimatedValue;
        private final double percentile;
        private final double sigmas;

        public Estimate(double realValue, double estimatedValue, double percentile, double sigmas) {
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

        public double getPercentile() {
            return percentile;
        }
    }
}