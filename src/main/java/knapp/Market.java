package knapp;

import knapp.advisor.Advice;
import knapp.advisor.AdvisorImpl;
import knapp.advisor.MixedAdvisor;
import knapp.download.DataRetriever;
import knapp.simulation.USDollars;
import knapp.simulation.functions.EvolvableFunction;
import knapp.simulation.strategy.*;
import knapp.table.Frequency;
import knapp.predict.TrendFinder;
import knapp.report.Reporter;
import knapp.simulation.Account;
import knapp.simulation.Simulater;
import knapp.table.*;
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

        StrategySupplier strategySupplier = new StrategySupplier() {
            @Override
            public InvestmentStrategy getStrategy(TrendFinder trendFinder, EvolvableFunction evolvableFunction, Map<String, Integer> lags) {
//                return new DirectFunctionStrategy(trendFinder,evolvableFunction, lags);
//                return new OneDirectionFunctionStrategy(trendFinder,evolvableFunction, lags);
//                return new MomentumFunctionStrategy(trendFinder,evolvableFunction, lags);
//                return new AllInStrategy();
//                return new CumulativeDistributionStrategy(trendFinder, lags,true,25,100);
//                return new NaiveMomentumStrategy(false);
                return new WiserStrategy(trendFinder,lags,1.5,-1);
            }
        };

        // most defaults are correct, I don't override them.
        AdvisorImpl advisorImpl = AdvisorImpl.define().strategySupplier(strategySupplier).requiredAccuracy(.01)
                .addInputs("M1SL", "UNRATE", "M1V", "UMCSENT", "IPMAN", "TTLCONS", "REVOLSL")
                .addInputs("CE16OV", "RSAFS", "IPMAN", "CSUSHPISA", "REVOLSL")
                .addInputs("EXUSEU", "CPIAUCSL", "INDPRO", "RSAFS", "M2V")
                .addInputs("MZMV", "TOTALSL", "UNEMPLOY", "TWEXBMTH","TCU","M2MSL","PPIACO")
                .build();

        MixedAdvisor mixedAdvisor = MixedAdvisor.define().core(advisorImpl)
                .addInputs("M1SL","INDPRO","MZMV","TOTALSL","UNEMPLOY")
//                .addInputs("M1SL","M1V","PPIACO","TOTALSL","UNEMPLOY")
//                .addInputs("M1SL","M1V","IPMAN","CE16OV","TWEXBMTH","UNEMPLOY")
//                .addInputs("M1SL","UNRATE","M1V","IPMAN","CE16OV","TWEXBMTH")
//                .addInputs("M1SL","UNRATE","IPMAN","UNEMPLOY","M2MSL")
//                .addInputs("CPIAUCSL","IPMAN","CE16OV","M2V","M2MSL")
//                .addInputs("INDPRO","TCU","TWEXBMTH","UNEMPLOY","M2MSL")
//                .addInputs("M1SL","CPIAUCSL","INDPRO","TCU","TWEXBMTH")





//                .addInputs("M1SL", "UNRATE", "M1V", "UMCSENT", "IPMAN", "TTLCONS", "REVOLSL")
//                .addInputs("M1SL", "UNRATE", "M1V", "UMCSENT", "IPMAN", "CE16OV", "RSAFS")
//                .addInputs("M1SL", "UNRATE", "M1V", "IPMAN", "CSUSHPISA", "REVOLSL")
//                .addInputs("M1SL", "UNRATE", "M1V", "EXUSEU", "CE16OV")
//                .addInputs("M1SL", "UNRATE", "M1V", "CPIAUCSL", "INDPRO")
//                .addInputs("M1SL", "UNRATE", "M1V", "REVOLSL")
//                .addInputs("M1SL", "UNRATE", "UMCSENT", "M1V")
//                .addInputs("UNRATE", "CSUSHPISA", "RSAFS")
//                .addInputs("UNRATE", "UMCSENT", "CPIAUCSL")
                .build();

        mixedAdvisor.initialize();
        Advice advice = mixedAdvisor.getAdvice(Collections.emptyList());

        Reporter reporter = new Reporter("");
        reporter.produceReport(currentDirectory,advice);
    }

}