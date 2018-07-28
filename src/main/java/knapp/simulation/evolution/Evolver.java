package knapp.simulation.evolution;

import knapp.TrendFinder;
import knapp.simulation.Simulater;
import knapp.simulation.strategy.InvestmentStrategy;
import knapp.simulation.strategy.LinearInvestmentStrategy;
import knapp.simulation.strategy.Line;

import java.util.function.Function;

public class Evolver {

    private final Function<InvestmentStrategy,Simulater.SimulationResults> sim;
    private final TrendFinder trendFinder;

    public Evolver(Function<InvestmentStrategy,Simulater.SimulationResults> sim, TrendFinder trendFinder) {
        this.sim = sim;
        this.trendFinder = trendFinder;
    }

    public Line evolve() {
        Line currentBest = new Line();
        currentBest.intercept = 0.45;
        currentBest.slope = 0.85;
        double deviation = 1.0;
        int bestFinalDollars = 0;
        while (deviation > 0.01) {
            for (int i = 0;i<10;i++) {
                Line spawn = Line.randomNear(currentBest,deviation);
                LinearInvestmentStrategy strategy = new LinearInvestmentStrategy(trendFinder,spawn);
                Simulater.SimulationResults res = sim.apply(strategy);
                if (res.getFinalDollars() > bestFinalDollars) {
                    System.out.println(String.format("There is a new best strategy, '%f + %f * sigmas' yielded $%d",spawn.intercept,spawn.slope,res.getFinalDollars()));
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
