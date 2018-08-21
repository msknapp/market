package knapp.predict;

import knapp.history.Frequency;
import knapp.table.Table;
import knapp.table.util.TableUtil;
import knapp.table.values.InterpolatedValuesGetter;
import knapp.table.values.LagBasedExtrapolatedValuesGetter;
import knapp.table.values.TableValueGetter;
import org.apache.commons.math.stat.regression.OLSMultipleLinearRegression;

import java.time.LocalDate;
import java.util.*;

public class TrendFinder {

    public TrendFinder() {
    }

    public AnalysisBuilder startAnalyzing() {
        return new AnalysisBuilder();
    }

    public final class AnalysisBuilder {
        private Table market;
        private Table inputs;
        private int[] inputColumns;
        private Frequency frequency;
        private LocalDate start;
        private LocalDate end;
//        private LocalDate presentDay;
        private Map<String,Integer> lags;

        public AnalysisBuilder() {

        }

        public AnalysisBuilder lags(Map<String,Integer> lags) {
            this.lags = lags;
            return this;
        }

        public AnalysisBuilder market(Table market) {
            this.market = market;
            return this;
        }

        public AnalysisBuilder inputs(Table inputs) {
            this.inputs = inputs;
            return this;
        }

        public AnalysisBuilder frequency(String frequency) {
            this.frequency = Frequency.valueOf(frequency);
            return this;
        }

        public AnalysisBuilder start(String start) {
            this.start = LocalDate.parse(start);
            return this;
        }

        public AnalysisBuilder end(String end) {
            this.end = LocalDate.parse(end);
            return this;
        }

        public AnalysisBuilder frequency(Frequency frequency) {
            this.frequency = frequency;
            return this;
        }

        public AnalysisBuilder start(LocalDate start) {
            this.start = start;
            return this;
        }

        public AnalysisBuilder end(LocalDate end) {
            this.end = end;
            return this;
        }

//        public AnalysisBuilder presentDay(LocalDate presentDay) {
//            this.presentDay = presentDay;
//            return this;
//        }

        public AnalysisBuilder inputColumns(int[] inputColumns) {
            this.inputColumns = inputColumns;
            return this;
        }

        public Analasys build() {
            return new Analasys(market,inputs,inputColumns,frequency,start,end, lags);
        }
    }

    public final class Analasys {
        private final Table market;
        private final Table inputs;
        private final int[] inputColumns;
        private final Frequency frequency;
        private final LocalDate start;
        private final LocalDate end;
        private final Map<String,Integer> lags;

        public Analasys(Table market, Table inputs, int[] inputColumns, Frequency frequency,LocalDate start,
                        LocalDate end, Map<String,Integer> lags) {
            if (market == null) {
                throw new IllegalArgumentException("Market cannot be null");
            }
            if (market.getColumnCount() != 1) {
                throw new IllegalArgumentException("Market must have only one column");
            }
            if (inputs == null) {
                throw new IllegalArgumentException("inputs cannot be null");
            }
            if (!market.isExact()) {
                throw new IllegalArgumentException("Stock market data must be exact.");
            }
            if (!inputs.isExact()) {
                throw new IllegalArgumentException("The inputs data must be exact.");
            }
            if (inputColumns == null) {
                inputColumns = new int[inputs.getColumnCount()];
                for (int i = 0;i < inputColumns.length;i++) {
                    inputColumns[i] = i;
                }
            }
            if (frequency == null) {
                frequency = Frequency.Monthly;
            }
            if (end == null) {
                end = LocalDate.now().minusMonths(2);
            }
            if (start == null) {
                start = end.minusYears(20);
            }
            if (end.isBefore(start)) {
                throw new IllegalArgumentException("End is before start.");
            }
            if (end.minusMonths(1).isAfter(market.getLastDateOf(0))) {
                throw new IllegalArgumentException("The last date of the market data appears to be pretty old.");
            }
            for (int i = 0; i < inputs.getColumnCount(); i++) {
                if (end.minusMonths(6).isAfter(inputs.getLastDateOf(i))) {
                    LocalDate x = inputs.getLastDateOf(i);
                    String wrnng = String.format("The last date of the input data '%s' appears to be pretty old, %s",
                            inputs.getColumn(i), x.toString());

                    // don't stop, just warn them.  Hopefully the lag consideration will help us here.
                    System.out.println(wrnng);
//                    throw new IllegalArgumentException(wrnng);
                }
            }
            if (lags == null || lags.isEmpty()) {
                throw new IllegalArgumentException("Lags must be defined.");
            }
            this.market = market;
            this.inputColumns = inputColumns;
            this.inputs = inputs;
            this.frequency = frequency;
            this.start = start;
            this.end = end;
            this.lags = Collections.unmodifiableMap(new HashMap<>(lags));
        }

        public NormalModel deriveModel() {
            LocalDate firstMarketDate = market.getFirstDateOf(0);
            LocalDate start = TableUtil.getLatestStartDate(inputs);//inputs.getDateOnOrAfter(firstMarketDate);
            if (firstMarketDate.isAfter(start)) {
                start = firstMarketDate;
            }

            double[][] x = TableUtil.toDoubleRows(inputs,inputColumns,start,end,frequency,
                    lags);

            // For the market price, it is safe to interpolate the value because it
            // was really known at that time.  People could have current market prices any day.
            double[][] yy = TableUtil.toDoubleColumns(market,new int[]{0},start,end,frequency,
                    new InterpolatedValuesGetter());
            double[] y = yy[0];
            OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
            regression.newSampleData(y, x);

            try {
                return new SimpleModel(regression, inputs.getColumns());
            } catch (Exception e) {

            }
            return null;
        }

        public TestedModel deriveTestedModel() {
            Random random = new Random();

            List<LocalDate> dates = new ArrayList<>(inputs.getTableColumnView(0).getAllDates());
            int testSample = (int) Math.round(dates.size()*0.2);
            Set<LocalDate> testDates = new HashSet<>(testSample);
            for (int i = 0; i < testSample; i++) {
                int x = random.nextInt(dates.size());
                LocalDate d = dates.get(x);
                testDates.add(d);
                dates.remove(d);
            }

            double[][] x = getInputs(inputs,inputColumns, dates);
            double[] y = getColumnAsArray(market,0,dates);
            OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
            regression.newSampleData(y, x);

            try {
                SimpleModel model = new SimpleModel(regression, inputs.getColumns());
                
                // test it.
                double testStandardDeviation = testModel(model,testDates);
                TestedModel testedModel = new TestedModel();
                testedModel.setModel(model);
                testedModel.setTestedStandardDeviation(testStandardDeviation);
                return testedModel;
            } catch (Exception e) {

            }
            return null;
        }

        private double testModel(SimpleModel model, Set<LocalDate> testDates) {
            double squareDeviation = 0;
            for (LocalDate date : testDates) {
                Map<String,Double> values = new HashMap<>(inputs.getColumnCount());
                for (String col : inputs.getColumns()) {
                    int colNo = inputs.getColumn(col);
                    TableValueGetter tableValueGetter = new LagBasedExtrapolatedValuesGetter(lags.get(col));
                    double v = inputs.getValue(date,colNo,tableValueGetter);
                    values.put(col,v);
                }
                MarketSlice marketSlice = new MarketSlice(values);

                // people typically have up to date information about the market price.
                double realValue = market.getValue(date,0,new InterpolatedValuesGetter());

                double estimate = model.estimateValue(marketSlice);
                double deviation = estimate - realValue;
                squareDeviation += Math.pow(deviation,2);
            }
            return Math.sqrt(squareDeviation / ((double) testDates.size()));
        }

        private double[][] getInputs(Table inputs, int[] inputColumns, List<LocalDate> dates) {
            Collections.sort(dates);
            // each row forms one array.
            double[][] out = new double[dates.size()][];
            int row = 0;
            for (LocalDate date : dates) {
                double[] rowData = new double[inputColumns.length];
                int colIndex = 0;
                for (int col : inputColumns) {
                    TableValueGetter tableValueGetter = new LagBasedExtrapolatedValuesGetter(lags.get(inputs.getColumn(col)));
                    rowData[colIndex++] = inputs.getValue(date,col,tableValueGetter);
                }
                out[row++] = rowData;
            }
            return out;
        }

        private double[] getColumnAsArray(Table inputs, int inputColumn, List<LocalDate> dates) {
            Collections.sort(dates);
            // each row forms one array.
            TableValueGetter tableValueGetter = new InterpolatedValuesGetter();
            double[] out = new double[dates.size()];
            int row = 0;
            for (LocalDate date : dates) {
                out[row++] = inputs.getValue(date,inputColumn,tableValueGetter);
            }
            return out;
        }

    }

    public static class TestedModel {
        private SimpleModel model;
        private double testedStandardDeviation;

        public Model getModel() {
            return model;
        }

        public void setModel(SimpleModel model) {
            this.model = model;
        }

        public double getTestedStandardDeviation() {
            return testedStandardDeviation;
        }

        public void setTestedStandardDeviation(double testedStandardDeviation) {
            this.testedStandardDeviation = testedStandardDeviation;
        }

        public double getTrustLevel() {
            double delta = Math.abs(1 - (testedStandardDeviation/model.getStandardDeviation()));
            return 1 - delta;
        }

        public boolean trustIt() {
            // the test standard deviation should be within 10%.
            return getTrustLevel() > 0.9;
        }
    }
}