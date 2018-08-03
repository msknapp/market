package knapp.inputfinder;

import knapp.Model;
import knapp.history.Frequency;

import java.util.List;

public class CorrelationResult {
    private List<String> indicators;
    private boolean cpiWasAdjusted;
    private Frequency frequency;
    private Model model;

    public List<String> getIndicators() {
        return indicators;
    }

    public void setIndicators(List<String> indicators) {
        this.indicators = indicators;
    }

    public double getRsquared() {
        return model.getRsquared();
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

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public void print() {
        System.out.println("Indicators: ");
        for (String ind : getIndicators()) {
            System.out.println(ind);
        }
        System.out.println("Frequency: "+getFrequency().name());
        System.out.println("R-Squared: "+getRsquared());
        System.out.println("CPI was adjusted: "+isCpiWasAdjusted());
    }
}
