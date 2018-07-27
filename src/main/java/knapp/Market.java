package knapp;

import knapp.history.Frequency;
import knapp.indicator.Indicator;
import knapp.simulation.Account;
import knapp.simulation.Simulater;
import knapp.simulation.strategy.AllStockStrategy;
import knapp.simulation.strategy.IntelligentStrategy;
import knapp.simulation.strategy.InvestmentStrategy;
import knapp.table.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;

public class Market {

    // https://fred.stlouisfed.org/categories

    // http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math4/stat/regression/OLSMultipleLinearRegression.html
    // http://commons.apache.org/proper/commons-math/userguide/stat.html#a1.4_Simple_regression

    private final MarketContext marketContext;

    public Market(MarketContext marketContext) {
        this.marketContext = marketContext;
    }

    public void analyzeMarket(boolean logarithmicMethod) throws IOException {
        Table market = marketContext.getMarket();
        String inputText = marketContext.getCurrentDirectory().toText(marketContext.getConsolidatedDataFile());
        Table tmpInput = TableParser.parse(inputText,true,Frequency.Monthly);
        tmpInput = new TableWithoutColumn(tmpInput,"Market Price");
        if (logarithmicMethod) {
            // experimentally this seems to be less accurate.
            LogDeriver logDeriver = new LogDeriver("Adj Close");
            market = market.withDerivedColumn(logDeriver)
                    .retainColumns(Collections.singleton("Log Adj Close"));

            // it seems that if you take the log of any input, it becomes a singular matrix
            // and cannot be solved.
//            tmpInput = tmpInput.replaceColumnWithLog("INDPRO");
//                    .replaceColumnWithLog("CPIAUCSL")
//                    .replaceColumnWithLog("M1SL");
        } else {
            market = market.retainColumns(Collections.singleton("Adj Close"));
        }

        tmpInput = TableParser.solidifyTable(tmpInput);
        TrendFinder tf = marketContext.getTrendFinder();
        final Table inputs = tmpInput;

        LocalDate end = LocalDate.now().minusMonths(2);
        TrendFinder.Analasys analasys = tf.startAnalyzing().market(market).inputs(inputs)
                .end(end).start(end.minusYears(30)).build();

        analasys.analyzeTrend(marketContext.getPredictionFile(),marketContext.getCurrentDirectory());
    }

    public void retrieveData() throws IOException {
        DataRetriever dataRetriever = marketContext.getDataRetriever();

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusYears(50);
        String indicatorText = marketContext.getCurrentDirectory().toText(marketContext.getIndicatorsFile());
        List<Indicator> indicators = Indicator.parseFromText(indicatorText,true);
        dataRetriever.consolidateData(start,end,indicators,marketContext.getConsolidatedDataFile());
    }

    public static void main(String[] args) throws IOException {
        MarketContext marketContext = createContext();
        Market market = new Market(marketContext);

        market.simulate();

//        if (args.length > 0 && "analyze".equals(args[0])) {
//            // don't use logarithmic method, it appears to be less
//            // accurate.
//            market.analyzeMarket(false);
//        } else {
//            market.retrieveData();
//        }
    }

    public void simulate() throws IOException {
//        String bmText = marketContext.getCurrentDirectory().toText("ishares-20year-t-bond.csv");
//        Table bondMarket = TableParser.parse(bmText,true,Frequency.Monthly);
//        bondMarket = bondMarket.retainColumns(Collections.singleton("Adj Close"));

        // for simulation purposes, bonds have a constant value,
        // however, bonds pay dividends in the simulation while stocks only
        // benefit from capital gains.
        Table bondMarket = TableParser.produceConstantTable(100.0,LocalDate.parse("1950-01-01"),
                LocalDate.now(),Frequency.Monthly);
        String smText = marketContext.getCurrentDirectory().toText(marketContext.getMarketFile());
        Table stockMarket = TableParser.parse(smText,true,Frequency.Monthly);
        stockMarket = stockMarket.retainColumns(Collections.singleton("Adj Close"));
        String inputText = marketContext.getCurrentDirectory().toText(marketContext.getConsolidatedDataFile());
        Table tmpInput = TableParser.parse(inputText,true,Frequency.Monthly);
        tmpInput = new TableWithoutColumn(tmpInput,"Market Price");
        tmpInput = TableParser.solidifyTable(tmpInput);
        final Table inputs = tmpInput;

        Simulater simulater = new Simulater.SimulaterBuilder().frameYears(20)
                .bondMarket(bondMarket).inputs(inputs).bondROI(0.04)
                .stockMarket(stockMarket).build();

        LocalDate end = LocalDate.now().minusMonths(2);
        LocalDate start = LocalDate.of(2000,02,01);

        InvestmentStrategy strategy = new AllStockStrategy();
        Account finalAccount = simulater.simulate(start,end, 10000, strategy).getAccount();
        long finalCents = finalAccount.getCurrentCents();

        System.out.println("The investor that just bought stock and never sold it wound up with: $"+(finalCents / 100));

        strategy = new IntelligentStrategy(marketContext.getTrendFinder());
        finalAccount = simulater.simulate(start,end, 10000, strategy).getAccount();
        finalCents = finalAccount.getCurrentCents();

        System.out.println("The intelligent investor ends up with: $"+(finalCents / 100));

    }

    public static MarketContext createContext() throws IOException {
        MarketContext mc = new MarketContext();
        mc.setIndicatorsFile("indicators/current-indicators.csv");
        mc.setPredictionFile("prediction.csv");
        mc.setConsolidatedDataFile("consolidated-data.csv");
        mc.setMarketFile("nasdaq-history.csv");
        mc.loadMarketData();

        DefaultGetMethod gmc = new DefaultGetMethod();
        TrendFinder trendFinder = new TrendFinder(gmc);
        mc.setTrendFinder(trendFinder);

        DataRetriever dataRetriever = new DataRetriever(mc.getMarket(),mc.getCurrentDirectory(),gmc);
        mc.setDataRetriever(dataRetriever);

        return mc;
    }
}