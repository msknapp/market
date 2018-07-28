package knapp.simulation.strategy;

import knapp.TrendFinder;
import knapp.table.DefaultGetMethod;

public class StrategyBank {

    public static InvestmentStrategy winner1() {
        DefaultGetMethod defaultGetMethod = new DefaultGetMethod();
        TrendFinder trendFinder = new TrendFinder(defaultGetMethod);
        // ended with $6762423 in the test bed.
        return new LinearInvestmentStrategy(trendFinder,winner1_Equation());
    }

    public static Line winner1_Equation() {
        Line line = new Line();
        line.intercept = 0.833908;
        line.slope = 0.456409;
        return line;
    }

    public static Line trainedUntil2014_Equation() {
        Line line = new Line();
        line.intercept = 0.869505;
        line.slope = 0.495949;
        return line;
    }

    public static Line trainedUntil2018_Equation() {
        Line line = new Line();
        // shockingly it gets much more aggressive.
        // it's a bit concerning how these lines shift so dramatically.
        line.intercept = 1.355005;
        line.slope = 0.716392;
        return line;
    }

    public static InvestmentStrategy trainedUntil2014() {
        DefaultGetMethod defaultGetMethod = new DefaultGetMethod();
        TrendFinder trendFinder = new TrendFinder(defaultGetMethod);
        return new LinearInvestmentStrategy(trendFinder,trainedUntil2014_Equation());
    }

    public static InvestmentStrategy trainedUntil2018() {
        DefaultGetMethod defaultGetMethod = new DefaultGetMethod();
        TrendFinder trendFinder = new TrendFinder(defaultGetMethod);
        return new LinearInvestmentStrategy(trendFinder,trainedUntil2018_Equation());
    }
}
