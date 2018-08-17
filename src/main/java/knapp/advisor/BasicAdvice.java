package knapp.advisor;

import knapp.predict.MarketSlice;
import knapp.predict.NormalModel;
import knapp.simulation.Simulater;
import knapp.simulation.functions.EvolvableFunction;
import knapp.table.Table;
import knapp.table.values.GetMethod;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

public class BasicAdvice implements Advice {
    private final NormalModel model;
    private final Table inputs;
    private final Table market;
    private final LocalDate start;
    private final double currentValue;
    private final Simulater.SimulationResults simulationResults;
    private final EvolvableFunction bestFunction;
    private final Map<String,Integer> lags;

    BasicAdvice(NormalModel model, Table inputs, Table market, LocalDate start, double currentValue,
                Simulater.SimulationResults simulationResults, EvolvableFunction bestFunction) {
        this.model = model;
        this.inputs = inputs;
        this.market = market;
        this.start = start;
        this.currentValue = currentValue;
        this.simulationResults = simulationResults;
        this.bestFunction = bestFunction;
        Map<String,Integer> tmp = new HashMap<>();
        for (String column : inputs.getColumns()) {
            int lag = (int) DAYS.between(inputs.getLastDateOf(column), LocalDate.now());
            tmp.put(column,lag);
        }
        this.lags = Collections.unmodifiableMap(tmp);
    }

    @Override
    public NormalModel getModel() {
        return model;
    }

    @Override
    public Table getInputs() {
        return inputs;
    }

    @Override
    public Table getMarket() {
        return market;
    }

    @Override
    public LocalDate getStart() {
        return start;
    }

    @Override
    public double getCurrentMarketValue() {
        return currentValue;
    }

    @Override
    public Simulater.SimulationResults getBestSimulationResults() {
        return simulationResults;
    }

    @Override
    public EvolvableFunction getBestFunction() {
        return bestFunction;
    }

    @Override
    public double getRecommendedPercentStock() {
        return getBestFunction().apply(getSigmas());
    }

    @Override
    public double getSigmas() {
        MarketSlice marketSlice = inputs.getPresentDayMarketSlice(lags);
        double estimate = getModel().estimateValue(marketSlice);
        return (estimate - getCurrentMarketValue()) / getModel().getStandardDeviation();
    }

    public static BasicAdviceBuilder define() {
        return new BasicAdviceBuilder();
    }

    public static class BasicAdviceBuilder {
        private NormalModel model;
        private Table inputs;
        private Table market;
        private LocalDate start;
        private double currentValue;
        private Simulater.SimulationResults simulationResults;
        private EvolvableFunction bestFunction;

        public BasicAdviceBuilder() {

        }

        public BasicAdviceBuilder inputs(Table x) {
            this.inputs = x;
            return this;
        }

        public BasicAdviceBuilder market(Table x) {
            this.market = x;
            return this;
        }

        public BasicAdviceBuilder currentValue(double currentValue) {
            this.currentValue = currentValue;
            return this;
        }

        public BasicAdviceBuilder simulationResults(Simulater.SimulationResults simulationResults) {
            this.simulationResults = simulationResults;
            return this;
        }

        public BasicAdviceBuilder start(LocalDate start) {
            this.start = start;
            return this;
        }

        public BasicAdviceBuilder model(NormalModel x) {
            this.model = x;
            return this;
        }

        public BasicAdviceBuilder bestFunction(EvolvableFunction x) {
            this.bestFunction = x;
            return this;
        }
        
        public BasicAdvice build() {
            return new BasicAdvice(model, inputs, market, start, currentValue,simulationResults,bestFunction);
        }
    }
}
