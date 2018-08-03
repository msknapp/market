package knapp.inputfinder;

import knapp.Model;
import knapp.TrendFinder;
import knapp.history.Frequency;
import knapp.table.RealDeriver;
import knapp.table.Table;
import knapp.table.TableImpl;
import knapp.table.TableParser;
import knapp.util.InputLoader;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;

public class BestInputFinder {

    private String marketSymbol = "IVE";// "nasdaq-weekly";


    //    private List<String> inputSeries = Arrays.asList("INDPRO","UNRATE","TCU","WPRIME","WTB3MS");
//    private List<String> inputSeries = Arrays.asList("INDPRO","UNRATE","TCU","WPRIME","WTB3MS");

    private List<String> adjustWithCpi = Arrays.asList("INDPRO", "M1SL", "M2SL", "M2MSL", "M3SL", "IPMAN");

    public static void main(String[] args) throws IOException {
        List<String> allSeries = Arrays.asList("INDPRO", "UNRATE", "TCU", "WPRIME", "WTB3MS",
                "M1SL", "M2SL", "M2MSL", "M3SL", "IPMAN","UMCSENT");
        BestInputFinder bestInputFinder = new BestInputFinder();
        CorrelationResult result = bestInputFinder.findBestInputs(allSeries,7);
        System.out.println("============== Finished: =============");
        result.print();
    }

    public CorrelationResult findBestInputs(List<String> allInputs, int size) throws IOException {
        Frequency[] frequencies = new Frequency[]{Frequency.Monthly}; // , Frequency.Weekly};
        boolean[] cpiBools = new boolean[]{false};//, true};
        CorrelationResult best = null;
        int frequencyOfUpdates = 100;
        List<List<String>> allCombos = deriveCombinations(allInputs, size);
        int num = 0;
        for (List<String> inputs : allCombos) {
            num++;
            if (num % frequencyOfUpdates == 0) {
                System.out.println("##################################");
                System.out.println("Working combo " + num + " of " + allCombos.size());
            }
            for (Frequency frequency : frequencies) {
                for (boolean considerInflation : cpiBools) {
                    CorrelationResult correlationResult = determineCorrelationResult(inputs, frequency, considerInflation);
                    if (best == null || correlationResult.getRsquared() > best.getRsquared()) {
                        best = correlationResult;
                    }
                }
            }
            if (num % frequencyOfUpdates == 0) {
                System.out.println("=======");
                best.print();
            }
        }
        return best;
    }

    public List<List<String>> deriveCombinations(List<String> allInputs, int size) {
        List<List<String>> out = new ArrayList<>();
        out.addAll(recursiveFindCombinations(allInputs, size, Collections.emptyList()));
        return out;
    }

    public List<List<String>> recursiveFindCombinations(List<String> allInputs, int size, List<String> current) {
        List<List<String>> out = new ArrayList<>();
        int indexLast = -1;
        if (!current.isEmpty()) {
            indexLast = allInputs.indexOf(current.get(current.size() - 1));
        }
        for (int i = indexLast + 1; i < allInputs.size(); i++) {
            String input = allInputs.get(i);
            if (!current.contains(input)) {
                List<String> tmp = new ArrayList<>(current);
                tmp.add(input);
                if (current.size() == size - 1) {
                    out.add(tmp);
                } else {
                    out.addAll(recursiveFindCombinations(allInputs,size,tmp));
                }
            }
        }
        return out;
    }

    public CorrelationResult determineCorrelationResult(List<String> inputSeries, Frequency trendFrequency, boolean adjustForInflation) throws IOException {
        // I want to find a better set of indicators.
        // also adjust things for inflation.

        // adjust the stock market for CPI.
        BiFunction<Table, Integer, TableImpl.GetMethod> getMethodChooser = (t, i) -> {
            return TableImpl.GetMethod.INTERPOLATE;
        };

        LocalDate marketStart = LocalDate.of(1992, 1, 25);
        LocalDate marketEnd = LocalDate.of(2018, 6, 1);
        String stockMarketText = InputLoader.loadTextFromClasspath("/market/" + marketSymbol + ".csv");

        Table stockMarket = TableParser.parse(stockMarketText, true, Frequency.Weekly);
        stockMarket = stockMarket.retainColumns(Collections.singleton("Adj Close"));

        TrendFinder trendFinder = new TrendFinder(getMethodChooser);

        Table inputs = InputLoader.loadInputsTableFromClasspath(inputSeries, marketStart, marketEnd, Frequency.Monthly);

        String cpiText = InputLoader.loadTextFromClasspath("/inputs/cpi.csv");
        Table cpi = TableParser.parse(cpiText, true, Frequency.Weekly);

        if (adjustForInflation) {
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
            RealDeriver realDeriver = new RealDeriver(cpi, cpiBaseDate, stockMarket.getColumn(0), 0);
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

        Model model = analasys.deriveModel();

        CorrelationResult correlationResult = new CorrelationResult();
        correlationResult.setCpiWasAdjusted(adjustForInflation);
        correlationResult.setFrequency(trendFrequency);
        correlationResult.setIndicators(inputSeries);
        correlationResult.setModel(model);
        return correlationResult;
    }
}
