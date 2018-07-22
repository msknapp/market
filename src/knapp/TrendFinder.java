package knapp;

import knapp.history.Frequency;
import knapp.table.Table;
import knapp.table.TableImpl;
import knapp.util.CurrentDirectory;
import org.apache.commons.math.stat.regression.OLSMultipleLinearRegression;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.util.function.BiFunction;

import static knapp.util.Util.doWithDate;
import static knapp.util.Util.doWithWriter;
import static knapp.util.Util.writeToFile;

public class TrendFinder {

    private final Table market;
    private final CurrentDirectory currentDirectory;
    private final BiFunction<Table,Integer,TableImpl.GetMethod> getMethodChooser;

    public TrendFinder(TableImpl market, CurrentDirectory currentDirectory,
                       BiFunction<Table,Integer,TableImpl.GetMethod> getMethodChooser) {
        if (market == null) {
            throw new IllegalArgumentException("market data can't be null");
        }
        this.market = market;
        this.getMethodChooser = getMethodChooser;
        this.currentDirectory = currentDirectory;
    }

    public void analyzeTrend(TableImpl inputs, int[] inputColumns, Frequency frequency) throws IOException {
        LocalDate start = LocalDate.of(1979,1,1);
        LocalDate end = LocalDate.of(2018,5,2);
        String text = doWithWriter(w -> {
            analyzeTrend(start,end,inputs,inputColumns, frequency,"prediction.csv", w);
        });
        System.out.println(text);
    }

    public void analyzeTrend(LocalDate start, LocalDate end, Table inputs, int[] inputColumns, Frequency frequency,
                             String outFileRelativePath, Writer out) throws IOException {
        TableImpl.GetMethod marketMethod = getMethodChooser.apply(inputs,0);
        double[][] x = inputs.toDoubleRows(inputColumns,start,end,frequency,marketMethod);
        double[][] yy = market.toDoubleColumns(new int[]{1},start,end,frequency, marketMethod);
        double[] y = yy[0];
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(y, x);

        double[] beta = regression.estimateRegressionParameters();
        double[] residuals = regression.estimateResiduals();
//        double[][] parametersVariance = regression.estimateRegressionParametersVariance();
        double regressandVariance = regression.estimateRegressandVariance();
        double rSquared = regression.calculateRSquared();
        double sigma = regression.estimateRegressionStandardError();

        for (int i = 0; i < beta.length;i++) {
            System.out.printf("Parameter %d: %d; residual: %d",i,beta[i],residuals[i]);
        }
        out.write("Regressand variance: "+regressandVariance);
        out.write("R Squared: "+rSquared);
        out.write("Sigma: "+sigma);

        out.write("The inputs are: ");
        for (int i : inputColumns) {
            System.out.println(inputs.getColumn(i));
        }
        out.write("The last known values are: ");
        double[] t = x[x.length-1];
        for (int i = 0;i<t.length;i++) {
            out.write(t[i]+", ");
        }
        out.write("");

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
                writer.write(String.valueOf(market.getValue(d,1,marketMethod)));
                writer.write(",");
                double est = 0;
                for (int i = 0;i<inputDoubles.length;i++) {
                    double b = beta[i];
                    est += b * inputDoubles[i];
                }
                writer.write(est+"\n");
            });
        },outFile);
    }
}
