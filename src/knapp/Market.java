package knapp;

import knapp.download.DownloadRequest;
import knapp.history.Frequency;
import knapp.history.ValueHistory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math.stat.regression.OLSMultipleLinearRegression;

import java.io.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

import static knapp.util.Util.doWithDate;
import static knapp.util.Util.writeToFile;
import static knapp.download.Downloader.DownloadSeries;
import static knapp.download.Downloader.DownloadToFile;

public class Market {

    // https://fred.stlouisfed.org/categories

    // http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math4/stat/regression/OLSMultipleLinearRegression.html
    // http://commons.apache.org/proper/commons-math/userguide/stat.html#a1.4_Simple_regression

    public static void main(String[] args) throws IOException {
        MarketContext marketContext = new MarketContext();
//        ConsolidateData();
        AnalyzeTrend();
    }

}
