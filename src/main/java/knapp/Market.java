package knapp;

import knapp.advisor.Advice;
import knapp.advisor.AdvisorImpl;
import knapp.advisor.MixedAdvisor;
import knapp.download.DataRetriever;
import knapp.history.Frequency;
import knapp.indicator.Indicator;
import knapp.predict.TrendFinder;
import knapp.report.Reporter;
import knapp.simulation.Account;
import knapp.simulation.Simulater;
import knapp.simulation.strategy.AllStockStrategy;
import knapp.simulation.strategy.IntelligentStrategy;
import knapp.simulation.strategy.InvestmentStrategy;
import knapp.table.*;
import knapp.table.derivation.LogDeriver;
import knapp.table.util.TableParser;
import knapp.table.wraps.TableWithoutColumn;
import knapp.util.CurrentDirectory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Market {

    // https://fred.stlouisfed.org/categories

    // http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math4/stat/regression/OLSMultipleLinearRegression.html
    // http://commons.apache.org/proper/commons-math/userguide/stat.html#a1.4_Simple_regression

    private final MarketContext marketContext;

    public Market(MarketContext marketContext) {
        this.marketContext = marketContext;
    }

    public static void main(String[] args) throws IOException {
        String subPath = "/Documents/investing";
        String baseDir = System.getenv("HOME") + subPath;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String subDir = LocalDateTime.now().format(dtf);
        String cdDir = baseDir + "/" + subDir;
        System.out.println("Your report will be written to: " + cdDir);
        File dir = new File(cdDir);
        dir.mkdirs();
        CurrentDirectory currentDirectory = new CurrentDirectory(cdDir);

        // Cubed and Tan seem to be too wild and inconsistent.
        // CubicPolynomial is also acting too wild and inconsistent.
        // I only use the Normal function now.

        // most defaults are correct, I don't override them.
        AdvisorImpl advisorImpl = AdvisorImpl.define()
                .addInputs("M1SL", "UNRATE", "M1V", "UMCSENT", "IPMAN", "TTLCONS", "REVOLSL")
                .addInputs("CE16OV", "RSAFS", "IPMAN", "CSUSHPISA", "REVOLSL")
                .addInputs("EXUSEU", "CPIAUCSL", "INDPRO", "RSAFS")
                .build();

        MixedAdvisor mixedAdvisor = MixedAdvisor.define().core(advisorImpl)
                .addInputs("M1SL", "UNRATE", "M1V", "UMCSENT", "IPMAN", "TTLCONS", "REVOLSL")
                .addInputs("M1SL", "UNRATE", "M1V", "UMCSENT", "IPMAN", "CE16OV", "RSAFS")
                .addInputs("M1SL", "UNRATE", "M1V", "IPMAN", "CSUSHPISA", "REVOLSL")
                .addInputs("M1SL", "UNRATE", "M1V", "EXUSEU", "CE16OV")
                .addInputs("M1SL", "UNRATE", "M1V", "CPIAUCSL", "INDPRO")
                .addInputs("M1SL", "UNRATE", "M1V", "REVOLSL")
                .addInputs("M1SL", "UNRATE", "UMCSENT", "M1V")
                .addInputs("UNRATE", "CSUSHPISA", "RSAFS")
                .addInputs("UNRATE", "UMCSENT", "CPIAUCSL")
                .build();

        mixedAdvisor.initialize();
        Advice advice = mixedAdvisor.getAdvice(Collections.emptyList());

        Reporter reporter = new Reporter("");
        reporter.produceReport(currentDirectory,advice);
    }

//    public void analyzeMarket(boolean logarithmicMethod) throws IOException {
//        Table market = marketContext.getMarket();
//        String inputText = marketContext.getCurrentDirectory().toText(marketContext.getConsolidatedDataFile());
//        Table tmpInput = TableParser.parse(inputText, true, Frequency.Monthly);
//        tmpInput = new TableWithoutColumn(tmpInput, "Market Price");
//        if (logarithmicMethod) {
//            // experimentally this seems to be less accurate.
//            LogDeriver logDeriver = new LogDeriver("Adj Close");
//            market = market.withDerivedColumn(logDeriver)
//                    .retainColumns(Collections.singleton("Log Adj Close"));
//
//            // it seems that if you take the log of any input, it becomes a singular matrix
//            // and cannot be solved.
////            tmpInput = tmpInput.replaceColumnWithLog("INDPRO");
////                    .replaceColumnWithLog("CPIAUCSL")
////                    .replaceColumnWithLog("M1SL");
//        } else {
//            market = market.retainColumns(Collections.singleton("Adj Close"));
//        }
//
//        tmpInput = TableParser.solidifyTable(tmpInput);
//        TrendFinder tf = marketContext.getTrendFinder();
//        final Table inputs = tmpInput;
//
//        LocalDate end = LocalDate.now().minusMonths(2);
//        TrendFinder.Analasys analasys = tf.startAnalyzing().market(market).inputs(inputs)
//                .end(end).start(end.minusYears(30)).build();
//
//        analasys.analyzeTrend(marketContext.getPredictionFile(), marketContext.getCurrentDirectory());
//    }

    public void retrieveData() throws IOException {
        DataRetriever dataRetriever = marketContext.getDataRetriever();

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusYears(50);
        String indicatorText = marketContext.getCurrentDirectory().toText(marketContext.getIndicatorsFile());
        List<Indicator> indicators = Indicator.parseFromText(indicatorText, true);
        Map<String, Table> data = dataRetriever.retrieveData(start, end, indicators);
        dataRetriever.writeData(start, end, data, marketContext.getMarket(),
                marketContext.getCurrentDirectory(),
                marketContext.getConsolidatedDataFile());
    }

    public void simulate() throws IOException {
//        String bmText = marketContext.getCurrentDirectory().toText("ishares-20year-t-bond.csv");
//        Table bondMarket = TableParser.parse(bmText,true,Frequency.Monthly);
//        bondMarket = bondMarket.retainColumns(Collections.singleton("Adj Close"));

        // for simulation purposes, bonds have a constant value,
        // however, bonds pay dividends in the simulation while stocks only
        // benefit from capital gains.
        Table bondMarket = TableParser.produceConstantTable(100.0, LocalDate.parse("1950-01-01"),
                LocalDate.now(), Frequency.Monthly);
        String smText = marketContext.getCurrentDirectory().toText(marketContext.getMarketFile());
        Table stockMarket = TableParser.parse(smText, true, Frequency.Monthly);
        stockMarket = stockMarket.retainColumns(Collections.singleton("Adj Close"));
        String inputText = marketContext.getCurrentDirectory().toText(marketContext.getConsolidatedDataFile());
        Table tmpInput = TableParser.parse(inputText, true, Frequency.Monthly);
        tmpInput = new TableWithoutColumn(tmpInput, "Market Price");
        tmpInput = TableParser.solidifyTable(tmpInput);
        final Table inputs = tmpInput;

        Simulater simulater = new Simulater.SimulaterBuilder().frameYears(20)
                .bondMarket(bondMarket).inputs(inputs).bondROI(0.04)
                .stockMarket(stockMarket).build();

        LocalDate end = LocalDate.now().minusMonths(2);
        LocalDate start = LocalDate.of(2000, 02, 01);

        InvestmentStrategy strategy = new AllStockStrategy();
        Account finalAccount = simulater.simulate(start, end, 10000, strategy).getAccount();
        long finalCents = finalAccount.getCurrentCents();

        System.out.println("The investor that just bought stock and never sold it wound up with: $" + (finalCents / 100));

        strategy = new IntelligentStrategy(marketContext.getTrendFinder());
        finalAccount = simulater.simulate(start, end, 10000, strategy).getAccount();
        finalCents = finalAccount.getCurrentCents();

        System.out.println("The intelligent investor ends up with: $" + (finalCents / 100));

    }

    public static MarketContext createContext() throws IOException {
        MarketContext mc = new MarketContext();
        mc.setIndicatorsFile("indicators/current-indicators.csv");
        mc.setPredictionFile("prediction.csv");
        mc.setConsolidatedDataFile("consolidated-data.csv");
        mc.setMarketFile("nasdaq-history.csv");
        mc.loadMarketData();

        DefaultGetMethod gmc = new DefaultGetMethod();
        TrendFinder trendFinder = new TrendFinder();
        mc.setTrendFinder(trendFinder);

        DataRetriever dataRetriever = new DataRetriever(gmc);
        mc.setDataRetriever(dataRetriever);

        return mc;
    }
}