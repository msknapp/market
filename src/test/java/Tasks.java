public class Tasks {

//    @Test
//    public void download() throws IOException {
//        MarketContext marketContext = Market.createContext();
//        Market market = new Market(marketContext);
//        market.retrieveData();
//    }
//
//    @Test
//    public void testStrategy() throws IOException {
//        TestBed testBed = new TestBed();
//        testBed.init();
//        Simulater.SimulationResults results = testBed.testIntelligentInvestment();
//        TestBed.printResults(results,"smart");
//    }
//
//    @Test
//    public void testBestEvolver() throws IOException {
//        TestBed testBed = new TestBed();
//        testBed.init();
//
////        The strategy 'evolved' ended with this much money: $2885267
////        The strategy 'evolved' ended with this average ROI: 0.266216%
////        Winner has equation: 0.867733 + 0.483158 * sigma
//
//        // this equation was found by training up until June 2014.
//        // now it gets tested for the four years after it was trained.
//        Line line = Line.slope(0.481578).intercept(0.766013).toLine();
//
//        InvestmentStrategy strategy = new FunctionStrategy(testBed.getTrendFinder(), line);
//        Simulater.SimulationResults results = testBed.testStrategy(strategy);
//        TestBed.printResults(results,"smart");
//
//        // even though in training the ROI was 26.6%,
//        // it was not able to carry that performance into the future.
//
////        The strategy 'smart' ended with this much money: $19843
////        The strategy 'smart' ended with this average ROI: 0.186866%
//
//        // however, 18.6% is still pretty dang good.
//    }
//
//    @Test
//    public void trainEvolver() throws IOException {
//        TestBed testBed = new TestBed();
//        testBed.init();
//
//        Function<InvestmentStrategy,Simulater.SimulationResults> sim = strategy -> {
//            return testBed.trainStrategy(strategy);
//        };
//        Evolver evolver = new Evolver(sim, testBed.getTrendFinder());
//        Line best = (Line) evolver.evolve();
//
//        FunctionStrategy strategy = new FunctionStrategy(testBed.getTrendFinder(),best);
//        Simulater.SimulationResults results = testBed.trainStrategy(strategy);
//        TestBed.printResults(results, "evolved");
//        System.out.println(String.format("Winner has equation: %f + %f * sigma",best.getIntercept(),best.getSlope()));
//
//        Simulater.SimulationResults realResults = testBed.testStrategy(strategy);
//        System.out.println("=====================");
//        System.out.println("Post training they get these results:");
//        TestBed.printResults(realResults, "evolved");
//    }
//
//    @Test
//    public void getBestGuessSlopeToday() throws IOException {
//        TestBed testBed = new TestBed();
//        testBed.init();
//
//        Function<InvestmentStrategy,Simulater.SimulationResults> sim = strategy -> {
//            return testBed.trainStrategyWithoutTestHoldout(strategy);
//        };
//        Evolver evolver = new Evolver(sim, testBed.getTrendFinder());
//        Line best = (Line) evolver.evolve();
//
//        FunctionStrategy strategy = new FunctionStrategy(testBed.getTrendFinder(),best);
//        Simulater.SimulationResults results = testBed.trainStrategyWithoutTestHoldout(strategy);
//        TestBed.printResults(results, "evolved");
//        System.out.println(String.format("Winner has equation: %f + %f * sigma",best.getIntercept(),best.getSlope()));
//    }
//
//    @Test
//    public void recommendInvestmentAllocationLast() {
//        LocalDate tableStart = LocalDate.of(1969,01,01);
//        LocalDate start = LocalDate.of(1990,01,01);
//        LocalDate end = LocalDate.of(2018,06,01);
//        String stockMarketText = InputLoader.loadTextFromClasspath("/market/s-and-p-500-weekly.csv");
//        Table stockMarket = TableParser.parse(stockMarketText,true,Frequency.Weekly);
//        stockMarket = stockMarket.retainColumns(Collections.singleton("Adj Close"));
//
//        TrendFinder trendFinder = new TrendFinder();
//
//        List<String> series = Arrays.asList("INDPRO","UNRATE","TCU","WPRIME","WTB3MS");
//        Table inputs = InputLoader.loadInputsTableFromClasspath(series,tableStart, end,Frequency.Monthly);
//
//        TrendFinder.Analasys analasys = trendFinder.startAnalyzing()
//                .market(stockMarket)
//                .inputs(inputs)
//                .frequency(Frequency.Monthly)
//                .start(start)
//                .end(end)
//                .build();
//        NormalModel model = analasys.deriveModel();
////        LocalDate lastInputDate = inputs.getLastDate();
////        double[] lastValues = inputs.getExactValues(lastInputDate);
////        LocalDate lastMarketDate = stockMarket.getLastDate();
////        double[] mvs = stockMarket.getExactValues(lastMarketDate);
////        double lastMarketValue = mvs[0];
////        double estimate = model.estimateValue(inputs.getLastMarketSlice());
////        evalWith(estimate,StrategyBank.trainedUntil2014_Equation(),"trained until 2014");
////        evalWith(estimate,StrategyBank.trainedUntil2018_Equation(),"trained until 2018");
////        evalWith(estimate,StrategyBank.winner1_Equation(),"winner1");
//    }
//
//    @Test
//    public void produceReport() throws IOException {
////        AdvisorImpl advisorImpl = new AdvisorImpl();
////        Reporter reporter = advisorImpl.recommendInvestmentAllocationToday();
////        reporter.produceReportBeneathHome();
//    }
//
//    @Test
//    public void recommendInvestmentAllocationToday() throws IOException {
////        LocalDate inputStart = LocalDate.of(2000,01,01);
////        LocalDate marketStart = LocalDate.of(2000,06,01);
////        LocalDate end = LocalDate.now();
////
////
////        IEXRetriever iexRetriever = new IEXRetriever();
////        Table recentData = iexRetriever.getChart("IVE",IEXRetriever.ChartLength.FIVEYEARS);
////
////        String stockMarketText = InputLoader.loadTextFromClasspath("/market/IVE.csv");
////        Table stockMarket = TableParser.parse(stockMarketText,true,Frequency.Weekly);
////        stockMarket = stockMarket.retainColumns(Collections.singleton("Adj Close"));
////        stockMarket = TableParser.mergeTableRowsApproximately(marketStart,LocalDate.now(),
////                Arrays.asList(recentData,stockMarket),Frequency.Weekly);
////
////        TrendFinder trendFinder = new TrendFinder();
////
////        List<String> series = Arrays.asList("INDPRO","UNRATE","TCU","WPRIME","WTB3MS");
////
////        DataRetriever dataRetriever = new DataRetriever();
////
////        Map<String,Table> inputDownloads = dataRetriever.retrieveData(inputStart,end,Indicator.toIndicators(series,inputStart));
////        Table[] inputTableArray = new Table[inputDownloads.size()];
////        int i = 0;
////        for (Table table : inputDownloads.values()) {
////            inputTableArray[i++] = table;
////        }
////        Table inputs = new ExactlyMergedTables(inputTableArray, DatePolicy.ANYMUSTHAVE);
////        List<Table> inputList = new ArrayList<>(inputDownloads.values());
//        // TableParser.mergeTablesApproximately(inputStart, end, inputList, Frequency.Monthly);
//
////        TrendFinder.Analasys analasys = trendFinder.startAnalyzing()
////                .market(stockMarket)
////                .inputs(inputs)
////                .frequency(Frequency.Monthly)
////                .start(marketStart)
////                .end(end)
////                .build();
////        SimpleModel model = analasys.deriveModel();
////        LocalDate lastInputDate = inputs.getLastDate();
////        double[] lastValues = inputs.getExactValues(lastInputDate);
////        LocalDate lastMarketDate = stockMarket.getLastDate();
////        double[] mvs = stockMarket.getExactValues(lastMarketDate);
////        double lastMarketValue = iexRetriever.getPrice("IVE");
////        SimpleEstimate estimate = model.produceEstimate(lastValues,lastMarketValue);
////        evalWith(estimate,StrategyBank.trainedUntil2014_Equation(),"trained until 2014");
////        evalWith(estimate,StrategyBank.trainedUntil2018_Equation(),"trained until 2018");
////        evalWith(estimate,StrategyBank.winner1_Equation(),"winner1");
//    }
//
//    // output on July 27, 2018.
////    According to 'trained until 2014', you should have 29% invested in stock and the rest in bonds.
////    According to 'trained until 2018', you should have 52% invested in stock and the rest in bonds.
////    According to 'winner1', you should have 30% invested in stock and the rest in bonds.
//
//    private void evalWith(SimpleEstimate estimate, Line line, String modelName) {
//        double pct = line.apply(estimate.getSigmas());
//        if (pct > 1) {
//            pct = 1;
//        }
//        if (pct < 0) {
//            pct = 0;
//        }
//        int percnt = (int) Math.round(pct*100);
//        System.out.println(String.format("According to '%s', you should have %d%% invested in stock and the " +
//                "rest in bonds.",modelName,percnt));
//    }
}