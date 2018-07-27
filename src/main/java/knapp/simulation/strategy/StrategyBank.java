package knapp.simulation.strategy;

import knapp.TrendFinder;
import knapp.table.DefaultGetMethod;

public class StrategyBank {

    public static InvestmentStrategy winner1() {
        DefaultGetMethod defaultGetMethod = new DefaultGetMethod();
        TrendFinder trendFinder = new TrendFinder(defaultGetMethod);
        MySlope mySlope = new MySlope();
        mySlope.intercept = 0.833908;
        mySlope.slope = 0.456409;
        // ended with $6762423 in the test bed.
        return new LinearInvestmentStrategy(trendFinder,mySlope);
    }
}
