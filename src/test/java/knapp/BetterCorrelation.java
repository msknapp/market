package knapp;

import knapp.download.DataRetriever;
import knapp.download.IEXRetriever;
import knapp.history.Frequency;
import knapp.indicator.Indicator;
import knapp.predict.NormalModel;
import knapp.predict.SimpleModel;
import knapp.predict.TrendFinder;
import knapp.table.*;
import knapp.util.InputLoader;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;

public class BetterCorrelation {

//    private List<String> inputSeries = Arrays.asList("INDPRO","UNRATE","TCU","WPRIME","WTB3MS");
    private List<String> inputSeries = Arrays.asList("INDPRO","UNRATE","TCU","WPRIME","WTB3MS");

    private List<String> adjustWithCpi = Arrays.asList("INDPRO","M1SL","M2SL","M2MSL","M3SL","IPMAN");
//    private List<String> adjustWithCpi = Collections.emptyList();

    @Test
    public void findBetterPrediction() throws IOException {
        // I want to find a better set of indicators.
        // also adjust things for inflation.
        String marketSymbol = "IVE";// "nasdaq-weekly";
        Frequency trendFrequency = Frequency.Monthly;
        boolean adjustForCPI = false;

        // adjust the stock market for CPI.
        BiFunction<Table,Integer,TableImpl.GetMethod> getMethodChooser = (t,i) -> {
            return TableImpl.GetMethod.INTERPOLATE;
        };

        LocalDate marketStart = LocalDate.of(1992,1,25);
        LocalDate marketEnd = LocalDate.of(2018,6,1);
        String stockMarketText = InputLoader.loadTextFromClasspath("/market/"+marketSymbol+".csv");

        Table stockMarket = TableParser.parse(stockMarketText,true,Frequency.Weekly);
        stockMarket = stockMarket.retainColumns(Collections.singleton("Adj Close"));

        TrendFinder trendFinder = new TrendFinder(getMethodChooser);

        Table inputs = InputLoader.loadInputsTableFromClasspath(inputSeries,marketStart,marketEnd,Frequency.Monthly);

        String cpiText = InputLoader.loadTextFromClasspath("/unused-inputs/cpi.csv");
        Table cpi = TableParser.parse(cpiText,true,Frequency.Weekly);

        if (adjustForCPI) {
            LocalDate cpiBaseDate = cpi.getDateOnOrAfter(marketStart);
            Set<String> retainColumns = new HashSet<>(inputs.getColumnCount());
            for (int colNumber = 0; colNumber < inputs.getColumnCount(); colNumber++) {
                String colName = inputs.getColumn(colNumber);
                if (adjustWithCpi.contains(colName)) {
                    RealDeriver realDeriver = new RealDeriver(cpi, cpiBaseDate, colName, colNumber);
                    inputs = inputs.withDerivedColumn(realDeriver);
                    retainColumns.add(realDeriver.getColumnName());
                } else {
                    retainColumns.add(colName);
                }
            }
            inputs = inputs.retainColumns(retainColumns);

            // adjust the stock market too.
            RealDeriver realDeriver = new RealDeriver(cpi, cpiBaseDate, stockMarket.getColumn(0),0);
            stockMarket = stockMarket.withDerivedColumn(realDeriver)
                    .retainColumns(Collections.singleton(realDeriver.getColumnName()));
        }

        TrendFinder.Analasys analasys = trendFinder.startAnalyzing()
                .start(marketStart)
                .end(marketEnd)
                .frequency(trendFrequency)
                .market(stockMarket)
                .inputs(inputs)
                .build();

        NormalModel model = analasys.deriveModel();

        System.out.println(model.getRsquared());
    }

    @Test
    public void findBetterPredictionToday() throws IOException {
        // I want to find a better set of indicators.
        // also adjust things for inflation.
        String marketSymbol = "SPY";
        String cpiSeries = "CPIAUCSL";

        LocalDate marketStart = LocalDate.of(1992,1,25);
        IEXRetriever iexRetriever = new IEXRetriever();
        String stockMarketText = InputLoader.loadTextFromClasspath("/market/"+marketSymbol+".csv");

        Table recentData = iexRetriever.getChart(marketSymbol,IEXRetriever.ChartLength.FIVEYEARS);
        Table stockMarket = TableParser.parse(stockMarketText,true,Frequency.Weekly);
        stockMarket = stockMarket.retainColumns(Collections.singleton("Adj Close"));
        stockMarket = TableParser.mergeTableRows(marketStart,LocalDate.now(),
                Arrays.asList(recentData,stockMarket),Frequency.Weekly);

        // adjust the stock market for CPI.
        TrendFinder trendFinder = new TrendFinder();

        DataRetriever dataRetriever = new DataRetriever();
        Indicator indicator = new Indicator(cpiSeries,marketStart,Frequency.Weekly, "CPI");
        Map<String, Table> cpiMap = dataRetriever.retrieveData(marketStart,LocalDate.now(),Collections.singletonList(indicator));
        Table cpi = cpiMap.get(cpiSeries);

        Map<String,Table> inputDownloads = dataRetriever.retrieveData(marketStart,LocalDate.now(),
                Indicator.toIndicators(inputSeries,marketStart));

        for (String series : adjustWithCpi) {
            if (inputDownloads.containsKey(series)) {
                Table t = inputDownloads.get(series);
                RealDeriver realDeriver = new RealDeriver(cpi,marketStart,t.getColumn(0), 0);
                Table real = t.withDerivedColumn(realDeriver)
                        .retainColumns(Collections.singleton(realDeriver.getColumnName()));
                inputDownloads.put(series,real);
            }
        }
        // adjust the stock market too.
        RealDeriver realDeriver = new RealDeriver(cpi, marketStart, stockMarket.getColumn(0),0);
        stockMarket = stockMarket.withDerivedColumn(realDeriver)
                .retainColumns(Collections.singleton(realDeriver.getColumnName()));
    }
}
