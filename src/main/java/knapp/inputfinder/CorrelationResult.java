package knapp.inputfinder;

import knapp.predict.Model;
import knapp.predict.NormalModel;
import knapp.predict.SimpleModel;
import knapp.history.Frequency;

import java.util.List;

public class CorrelationResult {
    private List<String> indicators;
    private boolean cpiWasAdjusted;
    private Frequency frequency;
    private NormalModel model;

    private double trustLevel;

    public double getTrustLevel() {
        return trustLevel;
    }

    public void setTrustLevel(double trustLevel) {
        this.trustLevel = trustLevel;
    }

    public List<String> getIndicators() {
        return indicators;
    }

    public void setIndicators(List<String> indicators) {
        this.indicators = indicators;
    }

    public double getRsquared() {
        return model.getRsquared();
    }

    public double getScore() {
        return model.getRsquared() * getTrustLevel();
    }

    public boolean isCpiWasAdjusted() {
        return cpiWasAdjusted;
    }

    public void setCpiWasAdjusted(boolean cpiWasAdjusted) {
        this.cpiWasAdjusted = cpiWasAdjusted;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public NormalModel getModel() {
        return model;
    }

    public void setModel(NormalModel model) {
        this.model = model;
    }

    public void print() {
        System.out.println("Indicators: ");
        for (String ind : getIndicators()) {
            System.out.println(ind);
        }
//        System.out.println("Frequency: "+getFrequency().name());
        System.out.println("Score: "+getScore());
        System.out.println("R-Squared: "+getRsquared());
        System.out.println("Trust Score: "+getTrustLevel());
//        System.out.println("CPI was adjusted: "+isCpiWasAdjusted());
    }
}
