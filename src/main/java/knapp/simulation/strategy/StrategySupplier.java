package knapp.simulation.strategy;

import knapp.predict.TrendFinder;
import knapp.simulation.functions.EvolvableFunction;

import java.util.Map;

public interface StrategySupplier {
    InvestmentStrategy getStrategy(TrendFinder trendFinder, EvolvableFunction evolvableFunction,
                                   Map<String,Integer> lags);
}
