import knapp.Market;
import knapp.MarketContext;
import knapp.simulation.Simulater;
import knapp.simulation.strategy.LinearInvestmentStrategy;
import knapp.simulation.strategy.MySlope;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static knapp.simulation.strategy.MySlope.randomSlope;

public class Tasks {

    @Test
    public void download() throws IOException {
        MarketContext marketContext = Market.createContext();
        Market market = new Market(marketContext);
        market.retrieveData();
    }

    @Test
    public void testStrategy() throws IOException {
        TestBed testBed = new TestBed();
        testBed.init();
        Simulater.SimulationResults results = testBed.testIntelligentInvestment();
        TestBed.printResults(results,"smart");
    }

    @Test
    public void testEvolver() throws IOException {
        TestBed testBed = new TestBed();
        testBed.init();

        Map<MySlope,Simulater.SimulationResults> results = new HashMap<>();
        for (int i = 0;i<100;i++) {
            MySlope mySlope = randomSlope();
            LinearInvestmentStrategy linearInvestmentStrategy = new LinearInvestmentStrategy(testBed.getTrendFinder(),mySlope);

            Simulater.SimulationResults res = testBed.testStrategy(linearInvestmentStrategy);
            results.put(mySlope,res);
            System.out.println(String.format("On Test %d, with equation (%f * sigma + %f), ended with $%d",i,mySlope.slope,mySlope.intercept,res.getFinalDollars()));
        }

        MySlope best = null;
        Simulater.SimulationResults bestResults = null;
        for (MySlope key : results.keySet()) {
            Simulater.SimulationResults curr = results.get(key);
            if (bestResults == null) {
                bestResults = curr;
                best = key;
            } else {
                if (curr.getFinalDollars() > bestResults.getFinalDollars()) {
                    best = key;
                    bestResults = curr;
                }
            }
        }
        TestBed.printResults(bestResults,"evolved");
        System.out.println(String.format("Winner has equation: %f + %f * sigma",best.intercept,best.slope));
    }

    // winner:
//    The strategy 'evolved' ended with this much money: $6762423
//    The strategy 'evolved' ended with this average ROI: 0.262046%
//    Winner has equation: 0.833908 + 0.456409 * sigma
}