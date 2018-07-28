package knapp;

import knapp.history.Frequency;
import knapp.indicator.Indicator;
import knapp.simulation.Simulater;
import knapp.simulation.evolution.Evolver;
import knapp.simulation.strategy.InvestmentStrategy;
import knapp.simulation.strategy.Line;
import knapp.simulation.strategy.LinearInvestmentStrategy;
import knapp.simulation.strategy.StrategyBank;
import knapp.table.Table;
import knapp.table.TableParser;
import knapp.util.CurrentDirectory;
import knapp.util.InputLoader;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

public class Advisor {
    private LocalDate simulationInputStart = LocalDate.of(1970,01,01);
    private LocalDate inputStart = LocalDate.of(2000,01,01);
    private LocalDate marketStart = LocalDate.of(2000,06,01);
    private LocalDate end = LocalDate.now();
    private IEXRetriever iexRetriever = new IEXRetriever();
    private String marketSymbol = "IVE";
    private List<String> inputSeries = Arrays.asList("INDPRO","UNRATE","TCU","WPRIME","WTB3MS");

    public Advisor() {

    }

    public LocalDate getInputStart() {
        return inputStart;
    }

    public LocalDate getMarketStart() {
        return marketStart;
    }

    public LocalDate getEnd() {
        return end;
    }

    public IEXRetriever getIexRetriever() {
        return iexRetriever;
    }

    public String getMarketSymbol() {
        return marketSymbol;
    }

    public List<String> getInputSeries() {
        return inputSeries;
    }

    public Reporter recommendInvestmentAllocationToday() throws IOException {
        System.out.println("Downloading IEX data for "+marketSymbol);
        Table recentData = iexRetriever.getChart(marketSymbol,IEXRetriever.ChartLength.FIVEYEARS);
        System.out.println("Done downloading IEX data.");

        String stockMarketText = InputLoader.loadTextFromClasspath("/market/"+marketSymbol+".csv");

        Table stockMarket = TableParser.parse(stockMarketText,true,Frequency.Weekly);
        stockMarket = stockMarket.retainColumns(Collections.singleton("Adj Close"));
        stockMarket = TableParser.mergeTableRows(marketStart,LocalDate.now(),
                Arrays.asList(recentData,stockMarket),Frequency.Weekly);

        TrendFinder trendFinder = new TrendFinder();

        DataRetriever dataRetriever = new DataRetriever();

        System.out.println("Downloading financial indicator data.");
        Map<String,Table> inputDownloads = dataRetriever.retrieveData(simulationInputStart,end,Indicator.toIndicators(inputSeries,inputStart));
        System.out.println("Done downloading financial indicator data.");
        List<Table> inputList = new ArrayList<>(inputDownloads.values());
        Table inputs = TableParser.mergeTables(inputStart, end, inputList, Frequency.Monthly);

        TrendFinder.Analasys analasys = trendFinder.startAnalyzing()
                .market(stockMarket)
                .inputs(inputs)
                .frequency(Frequency.Monthly)
                .start(marketStart)
                .end(end)
                .build();
        Model model = analasys.deriveModel();
        double lastMarketValue = iexRetriever.getPrice(marketSymbol);

        Table bondMarket = TableParser.produceConstantTable(100.0,inputStart,
                end,Frequency.Monthly);

        int frameYears = 8;
        LocalDate simStart = marketStart.plusYears(frameYears);

        Simulater simulater = new Simulater.SimulaterBuilder()
                .bondROI(0.04)
                .frameYears(frameYears)
                .stockMarket(stockMarket)
                .bondMarket(bondMarket)
                .inputs(inputs)
                .build();
        Function<InvestmentStrategy, Simulater.SimulationResults> sim = strategy -> {
            return simulater.simulate(simStart, end, 10000, strategy);
        };

        Evolver evolver = new Evolver(sim, trendFinder,0.01);
        System.out.println("Running the evolver to find the best investment strategy.");
        Line bestLine = evolver.evolve();

        LinearInvestmentStrategy linearInvestmentStrategy = new LinearInvestmentStrategy(trendFinder,bestLine);
        Simulater.SimulationResults results = simulater.simulate(simStart, end, 10000, linearInvestmentStrategy);

        Reporter reporter = new Reporter();
        reporter.setBestLine(bestLine);
        reporter.setBestSimulationResults(results);
        reporter.setCurrentMarketValue(lastMarketValue);
        reporter.setInputs(inputs);
        reporter.setMarket(stockMarket);
        reporter.setStart(marketStart);
        reporter.setModel(model);
        return reporter;
    }
}
