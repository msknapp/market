package knapp;

import knapp.history.Frequency;
import knapp.indicator.Indicator;
import knapp.simulation.Simulater;
import knapp.simulation.evolution.Evolver;
import knapp.simulation.functions.Cubed;
import knapp.simulation.functions.CubicPolynomial;
import knapp.simulation.functions.EvolvableFunction;
import knapp.simulation.strategy.*;
import knapp.table.Table;
import knapp.table.TableParser;
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
//    private List<String> inputSeries = Arrays.asList("INDPRO","UNRATE","TCU","WPRIME","WTB3MS");
    private List<String> inputSeries = Arrays.asList("UNRATE","WTB3MS","M1SL","M2SL","M2MSL","M3SL","IPMAN");

    private EvolvableFunction initialFunction;

    public Advisor() {

    }

    public EvolvableFunction getInitialFunction() {
        return initialFunction;
    }

    public void setInitialFunction(EvolvableFunction initialFunction) {
        this.initialFunction = initialFunction;
    }

    public void setSimulationInputStart(LocalDate simulationInputStart) {
        this.simulationInputStart = simulationInputStart;
    }

    public void setInputStart(LocalDate inputStart) {
        this.inputStart = inputStart;
    }

    public void setMarketStart(LocalDate marketStart) {
        this.marketStart = marketStart;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public void setIexRetriever(IEXRetriever iexRetriever) {
        this.iexRetriever = iexRetriever;
    }

    public void setMarketSymbol(String marketSymbol) {
        this.marketSymbol = marketSymbol;
    }

    public void setInputSeries(List<String> inputSeries) {
        this.inputSeries = inputSeries;
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
        if (initialFunction == null) {
            initialFunction = CubicPolynomial.initialCubed();
        }
        System.out.println("Downloading IEX data for "+marketSymbol);
        Table recentData = iexRetriever.getChart(marketSymbol,IEXRetriever.ChartLength.FIVEYEARS);
        System.out.println("Done downloading IEX data.");

        String stockMarketText = InputLoader.loadTextFromClasspath("/market/"+marketSymbol+".csv");

        Table stockMarket = TableParser.parse(stockMarketText,true,Frequency.Weekly);
        stockMarket = stockMarket.retainColumns(Collections.singleton("Adj Close"));
        stockMarket = TableParser.mergeTableRows(marketStart,LocalDate.now(),
                Arrays.asList(recentData,stockMarket),Frequency.Weekly);

        stockMarket.setName(marketSymbol);

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
                .bondROI(0.02)
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
        EvolvableFunction bestArcTan = evolver.evolve(initialFunction);

        FunctionStrategy strategy = new FunctionStrategy(trendFinder,bestArcTan);
        Simulater.SimulationResults results = simulater.simulate(simStart, end, 10000, strategy);

        Reporter reporter = new Reporter();
        reporter.setBestFunction(bestArcTan);
        reporter.setBestSimulationResults(results);
        reporter.setCurrentMarketValue(lastMarketValue);
        reporter.setInputs(inputs);
        reporter.setMarket(stockMarket);
        reporter.setStart(marketStart);
        reporter.setModel(model);
        return reporter;
    }
}
