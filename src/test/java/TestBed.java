import knapp.TrendFinder;
import knapp.history.Frequency;
import knapp.simulation.Account;
import knapp.simulation.Simulater;
import knapp.simulation.strategy.AllStockStrategy;
import knapp.simulation.strategy.IntelligentStrategy;
import knapp.simulation.strategy.InvestmentStrategy;
import knapp.table.DefaultGetMethod;
import knapp.table.Table;
import knapp.table.TableParser;
import knapp.util.InputLoader;
import knapp.util.Util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestBed {

    private Simulater simulater;
    private LocalDate simulationStart, simulationEnd, tableStart;
    DefaultGetMethod defaultGetMethod = new DefaultGetMethod();
    TrendFinder trendFinder = new TrendFinder(defaultGetMethod);
    InvestmentStrategy holdForeverStrategy = new AllStockStrategy();
    InvestmentStrategy strategy = new IntelligentStrategy(trendFinder);

    public TestBed() {

    }

    public Simulater getSimulater() {
        return simulater;
    }

    public LocalDate getSimulationStart() {
        return simulationStart;
    }

    public LocalDate getSimulationEnd() {
        return simulationEnd;
    }

    public LocalDate getTableStart() {
        return tableStart;
    }

    public DefaultGetMethod getDefaultGetMethod() {
        return defaultGetMethod;
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
        simulationStart = LocalDate.of(1990,01,01);
        simulationEnd = LocalDate.of(2018,06,01);
        String stockMarketText = InputLoader.loadTextFromClasspath("/market/s-and-p-500-weekly.csv");
        Table stockMarket = TableParser.parse(stockMarketText,true,Frequency.Weekly);
        stockMarket = stockMarket.retainColumns(Collections.singleton("Adj Close"));

        Table bondMarket = TableParser.produceConstantTable(100.0,tableStart,
                simulationEnd,Frequency.Monthly);

        List<String> series = Arrays.asList("INDPRO","UNRATE","TCU","WPRIME","WTB3MS");
        Table inputs = InputLoader.loadInputsTableFromClasspath(series,tableStart,simulationEnd,Frequency.Monthly);

        this.simulater = new Simulater.SimulaterBuilder().stockMarket(stockMarket)
                .bondMarket(bondMarket).bondROI(0.04).frameYears(20).inputs(inputs).build();
    }

    public Simulater.SimulationResults testIntelligentInvestment() {
        return simulater.simulate(simulationStart,simulationEnd,10000,strategy);
    }

    public Simulater.SimulationResults testHoldForever() {
        return simulater.simulate(simulationStart,simulationEnd,10000,holdForeverStrategy);
    }

    public Simulater.SimulationResults testStrategy(InvestmentStrategy strategy) {
        return simulater.simulate(simulationStart,simulationEnd,10000,strategy);
    }

    public static void printResults(Simulater.SimulationResults simulationResults,String name) {
        printWorthOverTime(simulationResults,name);
        System.out.println(String.format("The strategy '%s' ended with this much money: $%d",name,simulationResults.getFinalDollars()));
        System.out.println(String.format("The strategy '%s' ended with this average ROI: %f%%",name,simulationResults.getAverageROI()));
    }

    public static void printWorthOverTime(Simulater.SimulationResults simulationResults,String name) {
        System.out.println(String.format("The strategy '%s' had this worth over time:",name));
        List<LocalDate> dates = new ArrayList<>(simulationResults.getWorthOverTime().keySet());
        Collections.sort(dates);
        System.out.println("Date,Value,Percent Stock");
        for (LocalDate d : dates) {
            Simulater.Stance v = simulationResults.getWorthOverTime().get(d);
            System.out.println(d.toString()+","+v.getNetWorthDollars()+","+v.getPercentStock());
        }
    }


}
