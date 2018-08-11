package knapp.predict;

import org.apache.commons.math.stat.regression.OLSMultipleLinearRegression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleModel implements NormalModel {

    private final List<ParameterInfo> parameters;
    private final double standardDeviation;
    private final double rsquared;

    private double trustScore = 1;

    public SimpleModel(OLSMultipleLinearRegression regression, List<String> inputNames) {
        double[] beta = regression.estimateRegressionParameters();
        double[] parmStdErr = regression.estimateRegressionParametersStandardErrors();
        this.rsquared = regression.calculateRSquared();
        this.standardDeviation = regression.estimateRegressionStandardError();
        List<ParameterInfo> tmp = new ArrayList<>();
        for (int i = 0; i < beta.length;i++) {
            String name = (i < 1) ? "INTERCEPT" : inputNames.get(i-1);
            ParameterInfo parameterInfo = new ParameterInfo(name,parmStdErr[i],beta[i]);
            tmp.add(parameterInfo);
        }
        this.parameters = Collections.unmodifiableList(tmp);
    }

    public SimpleModel(List<ParameterInfo> parameters, double standardDeviation, double rsquared) {
        this.parameters = Collections.unmodifiableList(new ArrayList<>(parameters));
        this.standardDeviation = standardDeviation;
        this.rsquared = rsquared;
    }

    @Override
    public double getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(double trustScore) {
        this.trustScore = trustScore;
    }

    @Override
    public List<ParameterInfo> getParameters() {
        return parameters;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public double getRsquared() {
        return rsquared;
    }

    @Override
    public double estimateValue(MarketSlice marketSlice) {
        double est = 0;
        for (ParameterInfo parameterInfo : parameters) {
            if ("INTERCEPT".equalsIgnoreCase(parameterInfo.getName())) {
                est += parameterInfo.getValue();
            } else {
                if (!marketSlice.contains(parameterInfo.getName())) {
                    throw new IllegalArgumentException("The market slice is missing the input " + parameterInfo.getName());
                }
                double marketValue = marketSlice.getValue(parameterInfo.getName());
                est += parameterInfo.getValue() * marketValue;
            }
        }
        return est;
    }

    @Override
    public double getWeight() {
        return 1;
    }

}
