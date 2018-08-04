package knapp.simulation.evolution;

import knapp.TrendFinder;
import knapp.simulation.Simulater;
import knapp.simulation.functions.EvolvableFunction;
import knapp.simulation.strategy.FunctionStrategy;
import knapp.simulation.strategy.InvestmentStrategy;
import knapp.simulation.functions.Line;

import java.util.function.Function;

public class Evolver {

    private final Function<InvestmentStrategy,Simulater.SimulationResults> sim;
    private final TrendFinder trendFinder;
    private final double accuracy;

    public Evolver(Function<InvestmentStrategy,Simulater.SimulationResults> sim, TrendFinder trendFinder) {
        this(sim,trendFinder,0.01);
    }

    public Evolver(Function<InvestmentStrategy,Simulater.SimulationResults> sim, TrendFinder trendFinder, double accuracy) {
        this.sim = sim;
        this.trendFinder = trendFinder;
        this.accuracy = accuracy;
    }

    public EvolvableFunction evolve() {
        return evolve(Line.slope(0.85).intercept(0.45).toLine());
    }

    public EvolvableFunction evolve(EvolvableFunction initialFunction) {
        EvolvableFunction currentBest = initialFunction;
        double deviation = 1.0;
        int bestFinalDollars = 0;
        while (deviation > accuracy) {
            for (int i = 0;i<10;i++) {
                EvolvableFunction spawn = currentBest.deviateRandomly(deviation);
                FunctionStrategy strategy = new FunctionStrategy(trendFinder,spawn);
                Simulater.SimulationResults res = sim.apply(strategy);
                if (res.getFinalDollars() > bestFinalDollars) {
//                    System.out.println(String.format("There is a new best strategy, '%f + %f * sigmas' yielded $%d",spawn.getIntercept(),spawn.getCubeSlope(),res.getFinalDollars()));
                    currentBest = spawn;
                    bestFinalDollars = res.getFinalDollars();
                }
            }
            deviation = deviation * 0.6;
            System.out.println("Deviation is now: "+deviation);
        }
        return currentBest;
    }
}
