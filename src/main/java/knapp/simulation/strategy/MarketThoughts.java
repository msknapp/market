package knapp.simulation.strategy;

import knapp.simulation.InvestmentAllocation;
import knapp.simulation.USDollars;

/**
 * This is just used to report what you were thinking on a date in the simulation.
 */
public class MarketThoughts {
    private double sigma;
    private USDollars expectedValue;
    private USDollars actualValue;
    private USDollars standardDeviation;
    private String decisionComment;
    private InvestmentAllocation currentAllocation;
    private InvestmentAllocation idealAllocation;
    private boolean meetsMinimumPercentChange;
    private double percentile;
    private double rsquared;
    private boolean overvalued;
    private boolean rising;
    private boolean didWhatIExpected;

    public MarketThoughts() {

    }

    public boolean isOvervalued() {
        return overvalued;
    }

    public void setOvervalued(boolean overvalued) {
        this.overvalued = overvalued;
    }

    public boolean isRising() {
        return rising;
    }

    public void setRising(boolean rising) {
        this.rising = rising;
    }

    public boolean isDidWhatIExpected() {
        return didWhatIExpected;
    }

    public void setDidWhatIExpected(boolean didWhatIExpected) {
        this.didWhatIExpected = didWhatIExpected;
    }

    public double getRsquared() {
        return rsquared;
    }

    public void setRsquared(double rsquared) {
        this.rsquared = rsquared;
    }

    public double getPercentile() {
        return percentile;
    }

    public void setPercentile(double percentile) {
        this.percentile = percentile;
    }

    public boolean isMeetsMinimumPercentChange() {
        return meetsMinimumPercentChange;
    }

    public void setMeetsMinimumPercentChange(boolean meetsMinimumPercentChange) {
        this.meetsMinimumPercentChange = meetsMinimumPercentChange;
    }

    public double getSigma() {
        return sigma;
    }

    public void setSigma(double sigma) {
        this.sigma = sigma;
        this.overvalued = sigma < 0;
    }

    public USDollars getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(USDollars expectedValue) {
        this.expectedValue = expectedValue;
    }

    public USDollars getActualValue() {
        return actualValue;
    }

    public void setActualValue(USDollars actualValue) {
        this.actualValue = actualValue;
    }

    public USDollars getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(USDollars standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public String getDecisionComment() {
        return decisionComment;
    }

    public void setDecisionComment(String decisionComment) {
        this.decisionComment = decisionComment;
    }

    public InvestmentAllocation getIdealAllocation() {
        return idealAllocation;
    }

    public void setIdealAllocation(InvestmentAllocation idealAllocation) {
        this.idealAllocation = idealAllocation;
    }

    public InvestmentAllocation getCurrentAllocation() {
        return currentAllocation;
    }

    public void setCurrentAllocation(InvestmentAllocation currentAllocation) {
        this.currentAllocation = currentAllocation;
    }
}
