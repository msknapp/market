package knapp.simulation.evolution;

import knapp.predict.TrendFinder;
import knapp.simulation.SimulationResults;
import knapp.simulation.USDollars;
import knapp.simulation.functions.EvolvableFunction;
import knapp.simulation.strategy.FunctionStrategy;
import knapp.simulation.strategy.InvestmentStrategy;
import knapp.simulation.functions.Line;

import java.util.Map;
import java.util.function.Function;

public class Evolver {

    private final Function<InvestmentStrategy,SimulationResults> sim;
    private final TrendFinder trendFinder;
    private final double accuracy;
    private final Map<String, Integer> lags;

    public Evolver(Function<InvestmentStrategy,SimulationResults> sim, TrendFinder trendFinder, Map<String, Integer> lags) {
        this(sim,trendFinder,0.01,lags);
    }

    public Evolver(Function<InvestmentStrategy,SimulationResults> sim, TrendFinder trendFinder,
                   double accuracy, Map<String, Integer> lags) {
        this.sim = sim;
        this.trendFinder = trendFinder;
        this.accuracy = accuracy;
        this.lags = lags;
    }

    public EvolvableFunction evolve() {
        return evolve(Line.slope(0.85).intercept(0.45).toLine());
    }

    public EvolvableFunction evolve(EvolvableFunction initialFunction) {
        EvolvableFunction currentBest = initialFunction;
        double deviation = 1.0;
        USDollars bestFinalDollars = USDollars.dollars(-1);
        int iterations = 0;
        USDollars lastBestFinalDollars = USDollars.dollars(-2);
        while (deviation > accuracy) {
            boolean improved = false;
            for (int i = 0;i<10;i++) {
                EvolvableFunction spawn = currentBest.deviateRandomly(deviation);
                FunctionStrategy strategy = new FunctionStrategy(trendFinder,spawn, lags);
                SimulationResults res = sim.apply(strategy);
                if (res.getFinalDollars().isGreaterThan(bestFinalDollars)) {
                    currentBest = spawn;
                    lastBestFinalDollars = bestFinalDollars;
                    bestFinalDollars = res.getFinalDollars();
                    improved = true;
                }
            }
            iterations++;
            if (iterations > 3) {
                if (!improved) {
                    System.out.println("The evolver is stopping early since there was no improvement in the last iteration.");
                    break;
                }
                if (!lastBestFinalDollars.isDebt()) {
                    double chng = (bestFinalDollars.dividedBy(lastBestFinalDollars)) - 1.0;
                    if (chng < 1e-3) {
                        break;
                    }
                }
            }
            deviation = deviation * 0.6;
            System.out.println("Deviation is now: "+deviation);
        }
        return currentBest;
    }
}
