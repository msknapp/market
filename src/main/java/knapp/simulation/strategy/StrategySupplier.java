package knapp.simulation.strategy;

import knapp.predict.TrendFinder;
import knapp.simulation.functions.Evolvable;

import java.util.Map;

public interface StrategySupplier {
    InvestmentStrategy getStrategy(TrendFinder trendFinder, Evolvable evolvableFunction,
                                   Map<String,Integer> lags);
}
