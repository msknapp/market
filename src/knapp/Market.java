package knapp;

import knapp.download.DownloadRequest;
import knapp.download.Downloader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.math.stat.regression.OLSMultipleLinearRegression;

import java.io.*;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static knapp.download.Downloader.*;

public class Market {

    private static String BASE_DIR="/home/michael/Documents/investing/";
    // https://fred.stlouisfed.org/categories

    // http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math4/stat/regression/OLSMultipleLinearRegression.html
    // http://commons.apache.org/proper/commons-math/userguide/stat.html#a1.4_Simple_regression

    public static void main(String[] args) throws IOException {
//        ConsolidateData();
        AnalyzeTrend();
    }

    public static void AnalyzeTrend() throws IOException {
        DownloadRequest.Frequency frequency = DownloadRequest.Frequency.Monthly;
        String data = FileUtils.readFileToString(new File(BASE_DIR+"good-input.csv"));
        ValueHistory valueHistory = ValueHistory.parseText(data);
        LocalDate start = LocalDate.of(1979,1,1);
        LocalDate end = LocalDate.of(2018,5,2);
        int[] inputColumns = new int[]{1,2,3,4,5,6};
        double[][] x = valueHistory.toDoubleRows(inputColumns,start,end,frequency);
        String marketText = toText("nasdaq-history.csv");
        ValueHistory market = ValueHistory.parseText(marketText);
        double[][] yy = market.toDoubleColumns(new int[]{1},start,end,frequency);
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
            System.out.println("Parameter "+i+": "+beta[i]+"; residual: "+residuals[i]);
        }
        System.out.println("Regressand variance: "+regressandVariance);
        System.out.println("R Squared: "+rSquared);
        System.out.println("Sigma: "+sigma);

        System.out.println("The inputs are: "+data.split("\n")[0]);
        System.out.print("The last known values are: ");
        double[] t = x[x.length-1];
        for (int i = 0;i<t.length;i++) {
            System.out.print(t[i]+", ");
        }
        System.out.println("");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(byteArrayOutputStream);
        writer.write("Date");
        for (int col : inputColumns) {
            writer.write(",");
            writer.write(valueHistory.getColumn(col));
        }
        writer.write(",Nasdaq,Estimate\n");
        DoWithDate(start,end,DownloadRequest.Frequency.Monthly,d -> {
            try {
                writer.write(d.toString());
                double[] inputs = new double[inputColumns.length+1];
                inputs[0] = 1;
                int k = 1;
                for (int col : inputColumns) {
                    writer.write(",");
                    String v = valueHistory.getValueOn(d,col);
                    writer.write(v);
                    double dbl = Double.parseDouble(v);
                    inputs[k++] = dbl;
                }
                writer.write(",");
                writer.write(market.getValueOn(d,1));
                writer.write(",");
                double est = 0;
                for (int i = 0;i<inputs.length;i++) {
                    double b = beta[i];
                    est += b * inputs[i];
                }
                writer.write(est+"\n");
            } catch (Exception e) {

            }
        });
        writer.close();
        byteArrayOutputStream.close();
        String out = new String(byteArrayOutputStream.toByteArray());
        File outFile = new File(BASE_DIR+"prediction.csv");
        FileUtils.writeStringToFile(outFile, out);
    }

    public static void ConsolidateData() throws IOException {
        String order = FileUtils.readFileToString(new File(BASE_DIR+"current-indicators.csv"));
        boolean first = true;
        Map<String,ValueHistory> downloadedData = new TreeMap<String,ValueHistory>();
        for (String line : order.split("\n")) {
            if (first) {
                first = false;
            } else {
                String[] parts = line.split(",");
                if ("NASDAQCOM".equals(parts[0]) || "SP500".equals(parts[0])) {
                    // these don't give me accurate data from FRED.
                    continue;
                }
                DownloadRequest downloadRequest = new DownloadRequest();
                downloadRequest.setSeries(parts[0]);
                downloadRequest.setStart(LocalDate.parse(parts[1]));
                downloadRequest.setEnd(LocalDate.now());
                downloadRequest.setFrequency(DownloadRequest.Frequency.valueOf(parts[2]));
                String data = DownloadSeries(downloadRequest);
                ValueHistory valueHistory = ValueHistory.parseText(data);
                downloadedData.put(parts[0],valueHistory);
            }
        }
        LocalDate start = LocalDate.of(1979,1,1);
        LocalDate end = LocalDate.of(2019,1,1);

        String marketText = toText("nasdaq-history.csv");
        ValueHistory market = ValueHistory.parseText(marketText);

        DownloadRequest.Frequency frequency = DownloadRequest.Frequency.Monthly;

        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(buff);
        writer.write("Date");
        for (String key : downloadedData.keySet()) {
            writer.write(",");
            writer.write(key);
        }
        writer.write(",");
        writer.write("NASDAQ");
        writer.write("\n");
        DoWithDate(start,end,DownloadRequest.Frequency.Monthly,d -> {
            try {
                writer.write(d.toString());
                for (String key : downloadedData.keySet()) {
                    writer.write(",");
                    ValueHistory valueHistory = downloadedData.get(key);
                    String value = valueHistory.getValueOn(d, 1);
                    writer.write(value);
                }
                writer.write(",");
                writer.write(market.getValueOn(d, 5));
                writer.write("\n");
            }catch (Exception e) {

            }
        });
        writer.close();
        buff.close();
        String out = new String(buff.toByteArray());
        File outFile = new File(BASE_DIR+"temp-output.csv");
        FileUtils.writeStringToFile(outFile, out);
    }

    public static void DoWithDate(String start, String end, DownloadRequest.Frequency frequency, Consumer<LocalDate> consumer) {
        LocalDate s = LocalDate.parse(start);
        LocalDate e = LocalDate.parse(end);
        DoWithDate(s,e,frequency,consumer);
    }

    public static void DoWithDate(LocalDate start, LocalDate end, DownloadRequest.Frequency frequency, Consumer<LocalDate> consumer) {
        LocalDate d = start;
        while (!d.isAfter(end)) {
            consumer.accept(d);
            if (frequency.equals(DownloadRequest.Frequency.Annual)) {
                d = d.plusYears(1);
            } else if (frequency.equals(DownloadRequest.Frequency.Quarterly)) {
                d = d.plusMonths(3);
            } else if (frequency.equals(DownloadRequest.Frequency.Monthly)) {
                d = d.plusMonths(1);
            } else if (frequency.equals(DownloadRequest.Frequency.Daily)) {
                d = d.plusDays(1);
            }
        }
    }

    public static void DownloadThings() throws IOException {
        DownloadRequest downloadRequest = new DownloadRequest();
        downloadRequest.setSeries("AEXUSEU");
        downloadRequest.setFrequency(DownloadRequest.Frequency.Annual);
        downloadRequest.setEnd(LocalDate.now());
        downloadRequest.setStart(LocalDate.now().minusYears(20));
        String filePath = BASE_DIR+"test-download.csv";
        File file = new File(filePath);

        DownloadToFile(downloadRequest,file);
    }

    public static void mergeData() throws IOException {
        String gdpText = toText("GDP.csv");
        String marketText = toText("nasdaq-history.csv");
        String cpiText = toText("cpi.csv");
        String unemploymentText = toText("unemployment-rate-sa.csv");
        String bankrateText = toText("bank-prime-loan-rate.csv");
        ValueHistory gdp = ValueHistory.parseText(gdpText);
        ValueHistory market = ValueHistory.parseText(marketText);
        ValueHistory cpi = ValueHistory.parseText(cpiText);
        ValueHistory unemployment = ValueHistory.parseText(unemploymentText);
        ValueHistory bankRate = ValueHistory.parseText(bankrateText);

        LocalDate d = LocalDate.of(1950,1,1);
        LocalDate end = LocalDate.of(2019,1,1);

        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(buff);
        writer.write("Date,CPI,GDP,Unemployment Rate,Loan Rate, Market\n");
        while (d.isBefore(end)) {
            writer.write(d.toString());
            writer.write(",");
            writer.write(cpi.getValueOn(d,1));
            writer.write(",");
            writer.write(gdp.getValueOn(d,1));
            writer.write(",");
            writer.write(unemployment.getValueOn(d,1));
            writer.write(",");
            writer.write(bankRate.getValueOn(d,1));
            writer.write(",");
            writer.write(market.getValueOn(d,5));
            writer.write("\n");
            d = d.plusYears(1);
        }
        writer.close();
        buff.close();
        String out = new String(buff.toByteArray());
        File outFile = new File(BASE_DIR+"temp-output.csv");
        FileUtils.writeStringToFile(outFile, out);
    }

    private static String toText(String p) throws IOException {
        return FileUtils.readFileToString(toFile(p));
    }

    private static File toFile(String p) {
        return new File(BASE_DIR+p);
    }
}
