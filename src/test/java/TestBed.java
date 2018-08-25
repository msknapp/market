import knapp.predict.TrendFinder;
import knapp.simulation.SimulationResults;
import knapp.simulation.Stance;
import knapp.simulation.USDollars;
import knapp.simulation.strategy.AllInStrategy;
import knapp.table.Frequency;
import knapp.simulation.Simulater;
import knapp.simulation.strategy.IntelligentStrategy;
import knapp.simulation.strategy.InvestmentStrategy;
import knapp.table.Table;
import knapp.table.util.TableParser;
import knapp.util.InputLoader;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestBed {

    private Simulater simulater;
    private LocalDate trainingStart, trainingEnd, testStart, testEnd, tableStart;
    TrendFinder trendFinder = new TrendFinder();
    InvestmentStrategy holdForeverStrategy = new AllInStrategy();
    InvestmentStrategy strategy = new IntelligentStrategy(trendFinder);

    public TestBed() {

    }

    public Simulater getSimulater() {
        return simulater;
    }

    public LocalDate getTrainingStart() {
        return trainingStart;
    }

    public LocalDate getTrainingEnd() {
        return trainingEnd;
    }

    public LocalDate getTableStart() {
        return tableStart;
    }

    public TrendFinder getTrendFinder() {
        return trendFinder;
    }

    public InvestmentStrategy getHoldForeverStrategy() {
        return holdForeverStrategy;
    }

    public InvestmentStrategy getStrategy() {
        return strategy;
    }

    public void init() {
        tableStart = LocalDate.of(1969,01,01);
        trainingStart = LocalDate.of(1990,01,01);
        trainingEnd = LocalDate.of(2014,06,01);
        testStart = trainingEnd;
        testEnd = LocalDate.of(2018,06,01);
        String stockMarketText = InputLoader.loadTextFromClasspath("/market/s-and-p-500-weekly.csv");
        Table stockMarket = TableParser.parse(stockMarketText,true,Frequency.Weekly);
        stockMarket = stockMarket.retainColumns(Collections.singleton("Adj Close"));

        Table bondMarket = TableParser.produceConstantTable(100.0,tableStart,
                trainingEnd,Frequency.Monthly);

        List<String> series = Arrays.asList("INDPRO","UNRATE","TCU","WPRIME","WTB3MS");
        Table inputs = InputLoader.loadInputsTableFromClasspath(series);

        this.simulater = new Simulater.SimulaterBuilder().stockMarket(stockMarket)
                .bondMarket(bondMarket).bondROI(0.04).frameYears(20).inputs(inputs).build();
    }

    public SimulationResults testIntelligentInvestment() {
        return simulater.simulate(trainingStart, trainingEnd,USDollars.dollars(10000),strategy);
    }

    public SimulationResults trainHoldForever() {
        return simulater.simulate(trainingStart, trainingEnd,USDollars.dollars(10000),holdForeverStrategy);
    }

    public SimulationResults trainStrategy(InvestmentStrategy strategy) {
        return simulater.simulate(trainingStart, trainingEnd,USDollars.dollars(10000),strategy);
    }

    public SimulationResults trainStrategyWithoutTestHoldout(InvestmentStrategy strategy) {
        return simulater.simulate(trainingStart, testEnd,USDollars.dollars(10000),strategy);
    }

    public SimulationResults testStrategy(InvestmentStrategy strategy) {
        return simulater.simulate(testStart, testEnd,USDollars.dollars(10000),strategy);
    }

    public static void printResults(SimulationResults simulationResults, String name) {
        printWorthOverTime(simulationResults,name);
        System.out.println(String.format("The strategy '%s' ended with this much money: $%d",name,simulationResults.getFinalDollars()));
        System.out.println(String.format("The strategy '%s' ended with this average ROI: %f%%",name,simulationResults.getAverageROI()));
    }

    public static void printWorthOverTime(SimulationResults simulationResults, String name) {
        System.out.println(String.format("The strategy '%s' had this worth over time:",name));
        List<LocalDate> dates = new ArrayList<>(simulationResults.getWorthOverTime().keySet());
        Collections.sort(dates);
        System.out.println("Date,Value,Percent Stock");
        for (LocalDate d : dates) {
            Stance v = simulationResults.getWorthOverTime().get(d);
            System.out.println(d.toString()+","+v.getNetWorthDollars()+","+v.getPercentStock());
        }
    }


}
