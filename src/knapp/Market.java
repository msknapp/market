package knapp;

import knapp.history.Frequency;
import knapp.indicator.Indicator;
import knapp.table.Table;
import knapp.table.TableImpl;
import knapp.table.TableParser;
import knapp.table.TableWithoutColumn;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.BiFunction;

import static knapp.util.Util.doWithWriter;

public class Market {

    // https://fred.stlouisfed.org/categories

    // http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math4/stat/regression/OLSMultipleLinearRegression.html
    // http://commons.apache.org/proper/commons-math/userguide/stat.html#a1.4_Simple_regression

    public static void main(String[] args) throws IOException {
        MarketContext marketContext = createContext();
        if (args.length > 0 && "analyze".equals(args[0])) {

            TrendFinder tf = marketContext.getTrendFinder();
            String inputText = marketContext.getCurrentDirectory().toText("good-inputs.csv");
            Table tmpInput = TableParser.parse(inputText,true);
            tmpInput = new TableWithoutColumn(tmpInput,"Nasdaq");
            tmpInput = TableParser.solidifyTable(tmpInput);
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusYears(20);
            Frequency frequency = Frequency.Monthly;
            int[] inputColumns = new int[]{1,2,3,4,5};
            final Table inputs = tmpInput;

            String text = doWithWriter(w -> {
                tf.analyzeTrend(start,end,inputs,inputColumns, frequency,"prediction.csv", w);
            });
            System.out.println(text);
        } else {
//        ConsolidateData();
            DataRetriever dataRetriever = marketContext.getDataRetriever();

            LocalDate end = LocalDate.now();
            LocalDate start = end.minusYears(50);
            String indicatorText = marketContext.getCurrentDirectory().toText("current-indicators.csv");
            List<Indicator> indicators = Indicator.parseFromText(indicatorText,true);
            dataRetriever.consolidateData(start,end,indicators,"good-inputs.csv");
        }
    }

    public static MarketContext createContext() throws IOException {
        MarketContext mc = new MarketContext();
        String marketText = mc.getCurrentDirectory().toText("nasdaq-history.csv");
        Table market = TableParser.parse(marketText,true);

        BiFunction<Table,Integer,TableImpl.GetMethod> gmc = (tbl,col) -> {
            return TableImpl.GetMethod.LAST_KNOWN_VALUE;
        };
        TrendFinder trendFinder = new TrendFinder(market,mc.getCurrentDirectory(),gmc);
        mc.setTrendFinder(trendFinder);

        DataRetriever dataRetriever = new DataRetriever(market,mc.getCurrentDirectory(),gmc);
        mc.setDataRetriever(dataRetriever);
        return mc;
    }

}
