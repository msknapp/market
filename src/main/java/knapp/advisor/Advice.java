package knapp.advisor;

import knapp.predict.Model;
import knapp.simulation.SimulationResults;
import knapp.simulation.functions.EvolvableFunction;
import knapp.table.Table;

import java.time.LocalDate;

public interface Advice {
    Model getModel();
    Table getInputs();
    Table getMarket();
    LocalDate getStart();
    double getCurrentMarketValue();
    SimulationResults getBestSimulationResults();
    EvolvableFunction getBestFunction();
    double getRecommendedPercentStock();
    double getSigmas();

//    default double getSigmas() {
//        MarketSlice marketSlice = getInputs().getLastMarketSlice();
//        double est = getModel().estimateValue(marketSlice);
//        return (est - getCurrentMarketValue()) / getModel().
//    }
}
