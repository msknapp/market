package knapp;

import knapp.history.Frequency;
import knapp.table.DefaultGetMethod;
import knapp.table.Table;
import knapp.table.TableImpl;
import knapp.table.TableUtil;
import knapp.util.CurrentDirectory;
import org.apache.commons.math.stat.regression.OLSMultipleLinearRegression;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;

import static knapp.util.Util.doWithDate;
import static knapp.util.Util.doWithWriter;
import static knapp.util.Util.writeToFile;

public class TrendFinder {

    private final BiFunction<Table,Integer,TableImpl.GetMethod> getMethodChooser;

    public TrendFinder() {
        this(new DefaultGetMethod());
    }

    public TrendFinder(BiFunction<Table,Integer,TableImpl.GetMethod> getMethodChooser) {
        if (getMethodChooser == null) {
            throw new IllegalArgumentException("getMethodChooser can't be null");
        }
        this.getMethodChooser = getMethodChooser;
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

        public AnalysisBuilder() {

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

        public AnalysisBuilder inputColumns(int[] inputColumns) {
            this.inputColumns = inputColumns;
            return this;
        }

        public Analasys build() {
            return new Analasys(market,inputs,inputColumns,frequency,start,end);
        }
    }

    public final class Analasys {
        private final Table market;
        private final Table inputs;
        private final int[] inputColumns;
        private final Frequency frequency;
        private final LocalDate start;
        private final LocalDate end;

        public Analasys(Table market, Table inputs, int[] inputColumns, Frequency frequency,LocalDate start, LocalDate end) {
            if (market == null) {
                throw new IllegalArgumentException("Market cannot be null");
            }
            if (market.getColumnCount() != 1) {
                throw new IllegalArgumentException("Market must have only one column");
            }
            if (inputs == null) {
                throw new IllegalArgumentException("inputs cannot be null");
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
            if (end.minusMonths(1).isAfter(market.getLastDate())) {
                throw new IllegalArgumentException("The last date of the market data appears to be pretty old.");
            }
            if (end.minusMonths(1).isAfter(inputs.getLastDate())) {
                throw new IllegalArgumentException("The last date of the input data appears to be pretty old.");
            }
            this.market = market;
            this.inputColumns = inputColumns;
            this.inputs = inputs;
            this.frequency = frequency;
            this.start = start;
            this.end = end;
        }

        public void analyzeTrend(String outFileRelativePath, CurrentDirectory currentDirectory) throws IOException {
            String text = doWithWriter(w -> {
                analyzeTrend(outFileRelativePath, w, currentDirectory);
            });
            System.out.println(text);
        }

        public Model deriveModel() {
            TableImpl.GetMethod marketMethod = getMethodChooser.apply(inputs,0);
            double[][] x = TableUtil.toDoubleRows(inputs,inputColumns,start,end,frequency,marketMethod);
            double[][] yy = TableUtil.toDoubleColumns(market,new int[]{0},start,end,frequency, marketMethod);
            double[] y = yy[0];
            OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
            regression.newSampleData(y, x);

            try {
                double[] beta = regression.estimateRegressionParameters();
                double sigma = regression.estimateRegressionStandardError();
                double rSquared = regression.calculateRSquared();
                double[] parmStdErr = regression.estimateRegressionParametersStandardErrors();

                return new Model(beta, sigma, rSquared, parmStdErr);
            } catch (Exception e) {

            }
            return null;
        }

        public TestedModel deriveTestedModel() {
            Random random = new Random();
            TableImpl.GetMethod marketMethod = getMethodChooser.apply(inputs,0);

            List<LocalDate> dates = new ArrayList<LocalDate>(Arrays.asList(inputs.getAllDates()));
            int testSample = (int) Math.round(dates.size()*0.2);
            Set<LocalDate> testDates = new HashSet<>(testSample);
            for (int i = 0; i < testSample; i++) {
                int x = random.nextInt(dates.size());
                LocalDate d = dates.get(x);
                testDates.add(d);
                dates.remove(d);
            }

            double[][] x = getInputs(inputs,inputColumns, dates, marketMethod);
            double[] y = getColumnAsArray(market,0,dates,marketMethod);
            OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
            regression.newSampleData(y, x);

            try {
                double[] beta = regression.estimateRegressionParameters();
                double sigma = regression.estimateRegressionStandardError();
                double rSquared = regression.calculateRSquared();
                double[] parmStdErr = regression.estimateRegressionParametersStandardErrors();

                Model model = new Model(beta, sigma, rSquared, parmStdErr);
                
                // test it.
                double testStandardDeviation = testModel(model,testDates, marketMethod);
                TestedModel testedModel = new TestedModel();
                testedModel.setModel(model);
                testedModel.setTestedStandardDeviation(testStandardDeviation);
                return testedModel;
            } catch (Exception e) {

            }
            return null;
        }

        private double testModel(Model model, Set<LocalDate> testDates, TableImpl.GetMethod marketMethod) {
            double squareDeviation = 0;
            for (LocalDate date : testDates) {
                double[] rowData = new double[inputColumns.length];
                int colIndex = 0;
                for (int col : inputColumns) {
                    rowData[colIndex++] = inputs.getValue(date,col,marketMethod);
                }
                double realValue = market.getValue(date,0,marketMethod);
                Model.Estimate estimate = model.produceEstimate(rowData,realValue);
                double deviation = estimate.getEstimatedValue() - realValue;
                squareDeviation += Math.pow(deviation,2);
            }
            return Math.sqrt(squareDeviation / ((double) testDates.size()));
        }

        private double[][] getInputs(Table inputs, int[] inputColumns, List<LocalDate> dates,
                                     TableImpl.GetMethod marketMethod) {
            Collections.sort(dates);
            // each row forms one array.
            double[][] out = new double[dates.size()][];
            int row = 0;
            for (LocalDate date : dates) {
                double[] rowData = new double[inputColumns.length];
                int colIndex = 0;
                for (int col : inputColumns) {
                    rowData[colIndex++] = inputs.getValue(date,col,marketMethod);
                }
                out[row++] = rowData;
            }
            return out;
        }

        private double[] getColumnAsArray(Table inputs, int inputColumn, List<LocalDate> dates,
                                     TableImpl.GetMethod marketMethod) {
            Collections.sort(dates);
            // each row forms one array.
            double[] out = new double[dates.size()];
            int row = 0;
            for (LocalDate date : dates) {
                out[row++] = inputs.getValue(date,inputColumn,marketMethod);
            }
            return out;
        }

        public void analyzeTrend(String outFileRelativePath, Writer out, CurrentDirectory currentDirectory) throws IOException {
            TableImpl.GetMethod marketMethod = getMethodChooser.apply(inputs,0);
            double[][] x = TableUtil.toDoubleRows(inputs,inputColumns,start,end,frequency,marketMethod);
            double[][] yy = TableUtil.toDoubleColumns(market,new int[]{0},start,end,frequency, marketMethod);
            double[] y = yy[0];
            OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
            regression.newSampleData(y, x);

            double[] beta = regression.estimateRegressionParameters();
            double[] residuals = regression.estimateResiduals();
            double regressandVariance = regression.estimateRegressandVariance();
            double rSquared = regression.calculateRSquared();
            double sigma = regression.estimateRegressionStandardError();

            for (int i = 0; i < beta.length;i++) {
                System.out.printf("Parameter %d: %f; residual: %f%n",i,beta[i],residuals[i]);
            }
            out.write("Regressand variance: "+regressandVariance+"\n");
            out.write("R Squared: "+rSquared+"\n");
            out.write("Sigma: "+sigma+"\n");

            out.write("The inputs are: ");
            for (int i : inputColumns) {
                out.write("\n");
                out.write(inputs.getColumn(i));
            }
            out.write("\n");
            out.write("The last known values are: ");
            double[] t = x[x.length-1];
            for (int i = 0;i<t.length;i++) {
                out.write(t[i]+", ");
            }
            out.write("\n");

            DoublePointer lastActual = new DoublePointer();
            DoublePointer lastEstimate = new DoublePointer();
            File outFile = currentDirectory.toFile(outFileRelativePath);
            writeToFile(writer -> {
                writer.write("Date");
                for (int col : inputColumns) {
                    writer.write(",");
                    writer.write(inputs.getColumn(col));
                }
                writer.write(",Market Value,Estimate\n");
                doWithDate(start,end,Frequency.Monthly, d -> {
                    writer.write(d.toString());
                    double[] inputDoubles = new double[inputColumns.length+1];
                    inputDoubles[0] = 1;
                    int k = 1;
                    for (int col : inputColumns) {
                        writer.write(",");
                        TableImpl.GetMethod method = getMethodChooser.apply(inputs,1);
                        double v = inputs.getValue(d,col, method);
                        writer.write(String.valueOf(v));
                        inputDoubles[k++] = v;
                    }
                    writer.write(",");

                    lastActual.value = market.getValue(d,0,marketMethod);
                    writer.write(String.valueOf(lastActual.value));
                    writer.write(",");
                    double est = 0;
                    for (int i = 0;i<inputDoubles.length;i++) {
                        double b = beta[i];
                        est += b * inputDoubles[i];
                    }
                    writer.write(est+"\n");
                    lastEstimate.value = est;
                });
            },outFile);

            double sigmas = Math.abs(lastActual.value - lastEstimate.value) / sigma;

            out.write(String.format("The last actual value was %f, and estimate was %f, sigma is %f, " +
                            "it is off by %f standard deviations.",
                    lastActual.value, lastEstimate.value,sigma, sigmas));
        }
    }

    private static class DoublePointer {
        double value;
    }

    public static class TestedModel {
        private Model model;
        private double testedStandardDeviation;

        public Model getModel() {
            return model;
        }

        public void setModel(Model model) {
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