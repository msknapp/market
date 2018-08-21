package knapp.advisor;

import knapp.predict.Model;
import knapp.predict.ModelOfModels;
import knapp.predict.WithWeight;
import knapp.simulation.Simulater;
import knapp.simulation.functions.EvolvableFunction;
import knapp.simulation.functions.MixedFunction;
import knapp.table.Table;

import java.time.LocalDate;
import java.util.*;

public class CombinedAdvice implements Advice {
    private final Advice first;
    private final Map<Advice,Double> allAdvice;
    private final Table allInputs;
    private final Model model;

    private EvolvableFunction mixedFunction;
    private Simulater.SimulationResults simulationResults;

    public Map<Advice,Double> getAllAdvice() {
        return Collections.unmodifiableMap(allAdvice);
    }

    public CombinedAdvice(List<Advice> adviceList, Table allInputs) {
        this.allAdvice = new HashMap<>(adviceList.size());
        this.allInputs = allInputs;
        List<Model> models = new ArrayList<>();
        Map<EvolvableFunction,Double> weightedFunctions = new HashMap<>();
        Advice f = null;
        for (Advice advice : adviceList) {
            if (f == null) {
                f = advice;
            }
            // I square the average ROI so we are heavily slanted towards the biggest winners.
            double weight = Math.pow(advice.getBestSimulationResults().getAverageROI() * 100.0,2);

            models.add(new WithWeight(advice.getModel(), weight));
            weightedFunctions.put(advice.getBestFunction(), weight);

            allAdvice.put(advice,weight);
        }
        this.first = f;
        this.model = new ModelOfModels(models);


        this.mixedFunction = new MixedFunction(weightedFunctions);
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public Table getInputs() {
        return allInputs;
    }

    @Override
    public Table getMarket() {
        return first.getMarket();
    }

    @Override
    public LocalDate getStart() {
        return first.getStart();
    }

    @Override
    public double getCurrentMarketValue() {
        return first.getCurrentMarketValue();
    }

    public void setSimulationResults(Simulater.SimulationResults simulationResults) {
        this.simulationResults = simulationResults;
    }

    public void setMixedFunction(EvolvableFunction mixedFunction) {
        this.mixedFunction = mixedFunction;
    }

    @Override
    public Simulater.SimulationResults getBestSimulationResults() {
        return simulationResults;
    }

    @Override
    public EvolvableFunction getBestFunction() {
        return mixedFunction;
    }

    @Override
    public double getRecommendedPercentStock() {
        double pct = 0;
        double cumulativeWeight = 0;
        for (Advice advice : allAdvice.keySet()) {
            double s = advice.getRecommendedPercentStock();
            double weight = allAdvice.get(advice);
            pct += s * weight;
            cumulativeWeight += weight;
        }
        return pct / cumulativeWeight;
    }

    @Override
    public double getSigmas() {
        double x = 0;
        double cumulativeWeight = 0;
        for (Advice advice : allAdvice.keySet()) {
            double s = advice.getSigmas();
            double weight = allAdvice.get(advice);
            x += s * weight;
            cumulativeWeight += weight;
        }
        return x / cumulativeWeight;
    }
}
