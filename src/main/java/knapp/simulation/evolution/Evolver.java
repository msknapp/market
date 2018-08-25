package knapp.simulation.evolution;

import knapp.predict.TrendFinder;
import knapp.simulation.SimulationResults;
import knapp.simulation.USDollars;
import knapp.simulation.functions.Evolvable;
import knapp.simulation.functions.EvolvableFunction;
import knapp.simulation.functions.RangeLimitedFunction;
import knapp.simulation.strategy.FunctionStrategy;
import knapp.simulation.strategy.InvestmentStrategy;
import knapp.simulation.functions.Line;
import knapp.simulation.strategy.StrategySupplier;
import knapp.table.DoubleRange;

import java.util.Map;
import java.util.function.Function;

public class Evolver {

    private final Function<InvestmentStrategy,SimulationResults> sim;
    private final TrendFinder trendFinder;
    private final double accuracy;
    private final Map<String, Integer> lags;
    private final StrategySupplier strategySupplier;

    private USDollars initialDollars;

    public USDollars getInitialDollars() {
        return initialDollars;
    }

    public void setInitialDollars(USDollars initialDollars) {
        this.initialDollars = initialDollars;
    }

    public Evolver(Function<InvestmentStrategy,SimulationResults> sim, TrendFinder trendFinder,
                   Map<String, Integer> lags, StrategySupplier strategySupplier) {
        this(sim,trendFinder,0.01,lags, strategySupplier);
    }

    public Evolver(Function<InvestmentStrategy,SimulationResults> sim, TrendFinder trendFinder,
                   double accuracy, Map<String, Integer> lags, StrategySupplier strategySupplier) {
        this.sim = sim;
        this.trendFinder = trendFinder;
        this.accuracy = accuracy;
        this.lags = lags;
        this.strategySupplier = strategySupplier;
    }

    public Evolvable evolve() {
        return evolve(Line.slope(0.85).intercept(0.45).toLine());
    }

    public Evolvable evolve(Evolvable initialFunction) {
        if (initialFunction instanceof EvolvableFunction) {
            validateFunction((EvolvableFunction) initialFunction);
        }
        if (!isAcceptable(initialFunction)) {
            System.out.println("Warning: the initial function is not even acceptable.");
        }

        Evolvable currentBest = initialFunction;
        double deviation = 1.0;
        USDollars bestFinalDollars = USDollars.dollars(-1);
        int iterations = 0;
        USDollars lastBestFinalDollars = USDollars.dollars(-2);
        while (deviation > accuracy) {
            boolean improved = false;
            for (int i = 0;i<10;i++) {
                Evolvable spawn = currentBest.deviateRandomly(deviation);
                int attempts = 0;
                while (!isAcceptable(spawn) && attempts < 20) {
                    spawn = currentBest.deviateRandomly(deviation);
                    attempts++;
                }
                if (attempts == 20) {
                    System.out.println("An iteration is being skipped because the " +
                            "evolver continually chooses unacceptable functions.");
                    break;
                }
                InvestmentStrategy strategy = strategySupplier.getStrategy(trendFinder,spawn, lags);
                SimulationResults res = sim.apply(strategy);
                if (res.getFinalDollars().isGreaterThan(bestFinalDollars)) {
                    currentBest = spawn;
                    lastBestFinalDollars = bestFinalDollars;
                    bestFinalDollars = res.getFinalDollars();
                    improved = true;
                }
            }
            iterations++;
//            if (iterations > 3) {
//                if (!improved) {
//                    System.out.println("The evolver is stopping early since there was no improvement in the last iteration.");
//                    break;
//                }
//                if (!lastBestFinalDollars.isDebt()) {
//                    double chng = (bestFinalDollars.dividedBy(lastBestFinalDollars)) - 1.0;
//                    if (chng < 1e-3) {
//                        break;
//                    }
//                }
//            }
            deviation = deviation * 0.6;
            System.out.println("Deviation is now: "+deviation);
        }
        if (currentBest instanceof EvolvableFunction) {
            DoubleRange range = getRange((EvolvableFunction) currentBest);
            System.out.println(String.format("The best function ranges from %.3f to %.3f", range.getMin(), range.getMax()));
        }
        return currentBest;
    }

    public static void validateFunction(EvolvableFunction evolvableFunction) {
        String msg = checkForFunctionBias(evolvableFunction);
        if (msg != null) {
            throw new IllegalStateException(msg);
        }
    }

    public static boolean functionIsBiased(EvolvableFunction evolvableFunction) {
        return checkForFunctionBias(evolvableFunction) != null;
    }

    public static String checkForFunctionBias(EvolvableFunction evolvableFunction) {
        evolvableFunction = new RangeLimitedFunction(evolvableFunction);
        double low = ((RangeLimitedFunction) evolvableFunction).apply(-3.0);
        double high = ((RangeLimitedFunction) evolvableFunction).apply(3.0);
        if (low > 0.90) {
            return "You have an extremely biased function";
        }
        if (high < 0.10) {
            return "You have an extremely biased function";
        }
        if (Math.abs(high-low) < 0.1) {
            return "You have an extremely biased function";
        }
        return null;
    }

    public static DoubleRange getRange(EvolvableFunction evolvableFunction) {
        evolvableFunction = new RangeLimitedFunction(evolvableFunction);
        double low = ((RangeLimitedFunction) evolvableFunction).apply(-3.0);
        double high = ((RangeLimitedFunction) evolvableFunction).apply(3.0);
        return new DoubleRange(low,high);
    }

    public static boolean isAcceptable(Evolvable evolvableFunction) {
        if (evolvableFunction instanceof EvolvableFunction) {
            return isAcceptable((EvolvableFunction)evolvableFunction);
        }
        return true;
    }

    public static boolean isAcceptable(EvolvableFunction evolvableFunction) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (double x = -4.0;x <= 4.0; x += 0.5) {
            double y = evolvableFunction.apply(x);
            if (y < min) {
                min = y;
            }
            if (y > max) {
                max = y;
            }
        }
        return min < 0.25 && max > 0.75;
    }
}
