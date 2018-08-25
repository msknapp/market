package knapp.advisor;

import knapp.predict.MarketSlice;
import knapp.predict.Model;
import knapp.predict.NormalModel;
import knapp.simulation.SimulationResults;
import knapp.simulation.functions.Evolvable;
import knapp.simulation.functions.EvolvableFunction;
import knapp.table.Table;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

public class BasicAdvice implements Advice {
    private final Model model;
    private final Table inputs;
    private final Table market;
    private final LocalDate start;
    private final double currentValue;
    private final SimulationResults simulationResults;
    private final Evolvable bestFunction;
    private final Map<String,Integer> lags;

    BasicAdvice(Model model, Table inputs, Table market, LocalDate start, double currentValue,
                SimulationResults simulationResults, Evolvable bestFunction) {
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
    public Model getModel() {
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
    public SimulationResults getBestSimulationResults() {
        return simulationResults;
    }

    @Override
    public Evolvable getBestFunction() {
        return bestFunction;
    }

    @Override
    public double getRecommendedPercentStock() {
        if (getBestFunction() instanceof EvolvableFunction) {

            return ((EvolvableFunction)getBestFunction()).apply(getSigmas());
        }
        return 0;
    }

    @Override
    public double getSigmas() {
        if (model instanceof NormalModel) {
            NormalModel normalModel = (NormalModel) model;
            MarketSlice marketSlice = inputs.getPresentDayMarketSlice(lags);
            double estimate = normalModel.estimateValue(marketSlice);
            return (estimate - getCurrentMarketValue()) / normalModel.getStandardDeviation();
        }
        throw new UnsupportedOperationException("Can't get the sigmas of this model.");
    }

    public static BasicAdviceBuilder define() {
        return new BasicAdviceBuilder();
    }

    public static class BasicAdviceBuilder {
        private Model model;
        private Table inputs;
        private Table market;
        private LocalDate start;
        private double currentValue;
        private SimulationResults simulationResults;
        private Evolvable bestFunction;

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

        public BasicAdviceBuilder simulationResults(SimulationResults simulationResults) {
            this.simulationResults = simulationResults;
            return this;
        }

        public BasicAdviceBuilder start(LocalDate start) {
            this.start = start;
            return this;
        }

        public BasicAdviceBuilder model(Model x) {
            this.model = x;
            return this;
        }

        public BasicAdviceBuilder bestFunction(Evolvable x) {
            this.bestFunction = x;
            return this;
        }
        
        public BasicAdvice build() {
            return new BasicAdvice(model, inputs, market, start, currentValue,simulationResults,bestFunction);
        }
    }
}
