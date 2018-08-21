package knapp.advisor;

import knapp.download.DataRetriever;
import knapp.download.IEXRetriever;
import knapp.history.Frequency;
import knapp.indicator.Indicator;
import knapp.predict.Model;
import knapp.predict.NormalModel;
import knapp.predict.TrendFinder;
import knapp.simulation.Simulater;
import knapp.simulation.evolution.Evolver;
import knapp.simulation.functions.EvolvableFunction;
import knapp.simulation.functions.Normal;
import knapp.simulation.strategy.FunctionStrategy;
import knapp.simulation.strategy.InvestmentStrategy;
import knapp.table.*;
import knapp.table.util.TableParser;
import knapp.util.InputLoader;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

public class AdvisorImpl implements Advisor {
    private final IEXRetriever iexRetriever = new IEXRetriever();
    private final TrendFinder trendFinder = new TrendFinder();
    DataRetriever dataRetriever = new DataRetriever();

    private final LocalDate simulationInputStart;
    private final LocalDate inputStart;
    private final LocalDate marketStart;
    private final LocalDate end;
    private final String marketSymbol;
    private final EvolvableFunction initialFunction;
    private final List<String> allPossibleInputs;

    // these are established in the init function.
    private Table stockMarket;
    private Map<String,Table> allInputs;
    private double lastMarketValue;
    private Table bondMarket;

    @Override
    public Map<String,Table> getAllInputs() {
        return Collections.unmodifiableMap(allInputs);
    }

    @Override
    public Table getAllInputsTable() {
        List<Table> inputList = new ArrayList<>(allInputs.values());
        Table[] ts = new Table[allInputs.size()];
        int i = 0;
        for (Table table : allInputs.values()) {
            ts[i++] = table;
        }
        Table inputs = UnevenTable.from(inputList);
        return inputs;
    }

    AdvisorImpl(LocalDate simulationInputStart,
            LocalDate inputStart,
            LocalDate marketStart,
            LocalDate end,
            String marketSymbol,
            EvolvableFunction initialFunction,
            List<String> allPossibleInputs) {
        this.simulationInputStart = simulationInputStart;
        this.inputStart = inputStart;
        this.marketStart = marketStart;
        this.end = end;
        this.marketSymbol = marketSymbol;
        this.initialFunction = initialFunction;
        this.allPossibleInputs = Collections.unmodifiableList(new ArrayList<>(allPossibleInputs));
    }

    @Override
    public void initialize() {
        System.out.println("Downloading IEX data for "+marketSymbol);
        // recentData has exact values.
        Table recentData = iexRetriever.getChart(marketSymbol,IEXRetriever.ChartLength.FIVEYEARS);
        System.out.println("Done downloading IEX data.");

        String stockMarketText = InputLoader.loadTextFromClasspath("/market/"+marketSymbol+".csv");

        stockMarket = TableParser.parse(stockMarketText,true,Frequency.Weekly);
        stockMarket = stockMarket.retainColumns(Collections.singleton("Adj Close"));
        stockMarket = TableParser.mergeTableRowsExacly(Arrays.asList(stockMarket, recentData));

//        Table[] combined = new Table[] {stockMarket, recentData};
//        stockMarket = new ExactlyMergedTables(combined, DatePolicy.ANYMUSTHAVE);

//        stockMarket = TableParser.mergeTableRowsApproximately(marketStart,LocalDate.now(),
//                Arrays.asList(recentData,stockMarket),Frequency.Weekly);

        stockMarket.setName(marketSymbol);

        lastMarketValue = iexRetriever.getPrice(marketSymbol);

        bondMarket = TableParser.produceConstantTable(100.0,inputStart,
                end,Frequency.Monthly);

        System.out.println("Downloading financial indicator data.");
        try {
            allInputs = dataRetriever.retrieveData(simulationInputStart,end,Indicator.toIndicators(allPossibleInputs,inputStart));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done downloading financial indicator data.");
    }

    @Override
    public Advice getAdvice(List<String> inputSeries) {
        Table[] inputArray = new Table[inputSeries.size()];
        int i = 0;
        for (String s : inputSeries) {
            inputArray[i++] = allInputs.get(s);
        }
        Table inputs = UnevenTable.from(Arrays.asList(inputArray));

        TrendFinder.Analasys analasys = trendFinder.startAnalyzing()
                .market(stockMarket)
                .inputs(inputs)
                .frequency(Frequency.Monthly)
                .start(marketStart)
                .end(end)
                .lags(inputs.getLags(LocalDate.now()))
                .build();
        NormalModel model = analasys.deriveModel();

        return getAdvice(inputs, model);
    }

    public Advice getAdvice(Table inputs, Model model) {
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

        Map<String,Integer> lags = inputs.getLags(LocalDate.now());

        Evolver evolver = new Evolver(sim, trendFinder,0.01,lags);
        System.out.println("Running the evolver to find the best investment strategy.");
        EvolvableFunction evolvableFunction = evolver.evolve(initialFunction);

        FunctionStrategy strategy = new FunctionStrategy(trendFinder,evolvableFunction, lags);
        Simulater.SimulationResults results = simulater.simulate(simStart, end, 10000, strategy);

        return BasicAdvice.define()
                .bestFunction(evolvableFunction)
                .simulationResults(results)
                .currentValue(lastMarketValue)
                .inputs(inputs)
                .market(stockMarket)
                .start(marketStart)
                .model(model)
                .build();
    }

    public static AdvisorImplBuilder define() {
        return new AdvisorImplBuilder();
    }

    public static final class AdvisorImplBuilder {
        private LocalDate simulationInputStart = LocalDate.of(1970,1,1);
        private LocalDate inputStart = LocalDate.of(2000,1,1);
        private LocalDate marketStart = LocalDate.of(2000,6,1);
        private LocalDate end = LocalDate.now();
        private String marketSymbol = "IVE";
        private EvolvableFunction initialFunction = Normal.initialNormal();
        private List<String> allPossibleInputs = new ArrayList<>();

        public AdvisorImplBuilder() {

        }

        public AdvisorImplBuilder simulationInputStart(LocalDate x) {
            this.simulationInputStart = x;
            return this;
        }

        public AdvisorImplBuilder inputStart(LocalDate x) {
            this.inputStart = x;
            return this;
        }

        public AdvisorImplBuilder marketStart(LocalDate x) {
            this.marketStart = x;
            return this;
        }

        public AdvisorImplBuilder end(LocalDate x) {
            this.end = x;
            return this;
        }

        public AdvisorImplBuilder marketSymbol(String x) {
            this.marketSymbol = x;
            return this;
        }

        public AdvisorImplBuilder initialFunction(EvolvableFunction x) {
            this.initialFunction = x;
            return this;
        }

        public AdvisorImplBuilder addInput(String input) {
            this.allPossibleInputs.add(input);
            return this;
        }

        public AdvisorImplBuilder addInputs(String... input) {
            this.allPossibleInputs.addAll(Arrays.asList(input));
            return this;
        }

        public AdvisorImplBuilder addInputs(Collection<String> input) {
            this.allPossibleInputs.addAll(input);
            return this;
        }

        public AdvisorImpl build() {
            // remove duplicates
            Set<String> tmp = new HashSet<>(allPossibleInputs);
            allPossibleInputs = new ArrayList<>(tmp);
            Collections.sort(allPossibleInputs);
            return new AdvisorImpl(simulationInputStart,inputStart,marketStart,end,marketSymbol,initialFunction, allPossibleInputs);
        }
        
    }
}
