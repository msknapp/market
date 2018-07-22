package knapp;

import knapp.history.Frequency;
import knapp.indicator.Indicator;
import knapp.table.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;

import static knapp.util.Util.doWithWriter;

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

        analasys.analyzeTrend(marketContext.getPredictionFile());
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
        if (args.length > 0 && "analyze".equals(args[0])) {
            // don't use logarithmic method, it appears to be less
            // accurate.
            market.analyzeMarket(false);
        } else {
            market.retrieveData();
        }
    }

    public static MarketContext createContext() throws IOException {
        MarketContext mc = new MarketContext();
        mc.setIndicatorsFile("current-indicators.csv");
        mc.setPredictionFile("prediction.csv");
        mc.setConsolidatedDataFile("consolidated-data.csv");
        mc.setMarketFile("nasdaq-history.csv");
        mc.loadMarketData();

        DefaultGetMethod gmc = new DefaultGetMethod();
        TrendFinder trendFinder = new TrendFinder(mc.getCurrentDirectory(),gmc);
        mc.setTrendFinder(trendFinder);

        DataRetriever dataRetriever = new DataRetriever(mc.getMarket(),mc.getCurrentDirectory(),gmc);
        mc.setDataRetriever(dataRetriever);
        return mc;
    }

    private static class DefaultGetMethod implements BiFunction<Table,Integer,TableImpl.GetMethod> {
        private Set<String> extrapolated = new HashSet<>(Arrays.asList("indpro","m1sl","cpiaucsl"));
        private Set<String> interpolated = new HashSet<>(Arrays.asList("market","nasdaq","market price"));

        @Override
        public TableImpl.GetMethod apply(Table table, Integer columnNumber) {
            String cname = table.getColumn(columnNumber);
            for (String ex : extrapolated) {
                if (cname.toLowerCase().endsWith(ex.toLowerCase())) {
                    return TableImpl.GetMethod.EXTRAPOLATE;
                }
            }
            for (String ex : interpolated) {
                if (cname.toLowerCase().endsWith(ex.toLowerCase())) {
                    return TableImpl.GetMethod.INTERPOLATE;
                }
            }
            return TableImpl.GetMethod.LAST_KNOWN_VALUE;
        }
    }
}