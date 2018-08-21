package knapp.report;

import knapp.advisor.Advice;
import knapp.advisor.CombinedAdvice;
import knapp.history.Frequency;
import knapp.predict.*;
import knapp.simulation.Simulater;
import knapp.simulation.functions.Line;
import knapp.table.DoubleRange;
import knapp.table.values.GetMethod;
import knapp.table.util.TableUtil;
import knapp.util.CurrentDirectory;
import knapp.util.Util;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Reporter {

    private String filePrefix;

    public Reporter(String filePrefix) {
        this.filePrefix = filePrefix;
    }

    public void produceReportBeneathHome(Advice advice) {
        produceReportBeneathHome("/Documents/investing", advice);
    }

    public void produceReportBeneathHome(String subPath, Advice advice) {
        String baseDir = System.getenv("HOME")+subPath;
        produceReport(baseDir, advice);
    }

    public void produceReport(String baseDir, Advice advice) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String subDir = LocalDateTime.now().format(dtf);
        String cdDir = baseDir+"/"+subDir;
        System.out.println("Your report will be written to: "+cdDir);
        File dir = new File(cdDir);
        dir.mkdirs();
        CurrentDirectory currentDirectory = new CurrentDirectory(cdDir);
        produceReport(currentDirectory, advice);
    }

    public void produceReport(CurrentDirectory currentDirectory, Advice advice) {
        if (filePrefix == null) {
            filePrefix = "";
        }
        if (!filePrefix.isEmpty() && !filePrefix.endsWith("-")) {
            filePrefix += "-";
        }
        // write files to current directory.
        // When this is called, the current directory should be new each time.


        // I want a CSV with:
        // date, inputs, market value, estimate, sigmas, recommended percent stock
        // another csv with the simulation results
        // and one file with a summary.
        writeSummaryText(currentDirectory, advice);
        writeSimulationText(currentDirectory, advice);
        writeDataText(currentDirectory, advice);
    }

    private void writeSummaryText(CurrentDirectory currentDirectory, Advice advice) {
        String text = Util.doWithWriter(writer -> {
            Consumer<String> lineConsumer = line -> {
                try {
                    writer.write(line+"\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            double estimate = advice.getModel().estimateValue(advice.getInputs().getPresentDayMarketSlice(GetMethod.EXTRAPOLATE));
            double sigma = advice.getSigmas();

            double recommendedPercentStock = Math.round(1000.0 * advice.getRecommendedPercentStock()) / 10.0;
            writer.write("Summary:\n\n");
            writer.write(String.format("You should have %.1f%% of your money invested in stock right now.\n\n", recommendedPercentStock));

            writer.write(String.format("The market ticker symbol is: %s\n",advice.getMarket().getName()));
            writer.write(String.format("Current Market Price: $%.2f\n",advice.getCurrentMarketValue()));
            writer.write(String.format("Estimated Market Price: $%.2f\n",estimate));

            writer.write(String.format("Current Sigma: %.2f\n\n",sigma));

            writer.write("Simulation Results:\n");
            writer.write(String.format("The simulation ended with: $%d\n",advice.getBestSimulationResults().getFinalDollars()));
            writer.write(String.format("The simulation had ROI: %.1f%%\n",advice.getBestSimulationResults().getAverageROI()*100.0));
            writer.write(String.format("The simulation made %d transactions.\n",advice.getBestSimulationResults().getTransactions().size()));


            if (advice instanceof CombinedAdvice) {
                writeCombinedSummaryText((CombinedAdvice) advice, lineConsumer);
            } else {
                writeBasicSummaryText(advice, lineConsumer);
            }

            // TODO assess sensitivity to each parameter.

        });

        File file = currentDirectory.toFile(filePrefix+"summary.txt");
        try {
            FileUtils.writeStringToFile(file,text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeBasicSummaryText(Advice advice, Consumer<String> writer) throws IOException {
        writer.accept("");
        writer.accept(String.format("Quick summary: A model had %.1f%% ROI in simulation, " +
                        "and is recommending to have %.1f%% invested in stock today.",
                advice.getBestSimulationResults().getAverageROI() * 100.0,
                advice.getRecommendedPercentStock() * 100.0));
        writer.accept("");
        writer.accept(String.format("Advice type: %s",advice.getClass().getSimpleName()));
        writer.accept(String.format("The simulation's ROI was: %.1f%%",advice.getBestSimulationResults().getAverageROI() * 100.0));
        writer.accept(String.format("The simulation's ended with: $%d",advice.getBestSimulationResults().getFinalDollars()));
        writer.accept(String.format("The simulation made %d transactions.",advice.getBestSimulationResults().getTransactions().size()));
        writer.accept("");

        double recommendedPercentStock = Math.round(1000.0 * advice.getRecommendedPercentStock()) / 10.0;
        double sigma = advice.getSigmas();

        writer.accept(advice.getBestFunction().describe());
        writer.accept(advice.getBestFunction().describe(sigma)+" = "+recommendedPercentStock);
        if (advice.getModel() instanceof NormalModel) {
            writeSimpleModelToSummaryText(advice, (NormalModel) advice.getModel(),writer);
        }
    }

    private void writeSimpleModelToSummaryText(Advice advice, NormalModel model, Consumer<String> writer) throws IOException {

        double estimate = advice.getModel().estimateValue(advice.getInputs().getPresentDayMarketSlice(GetMethod.EXTRAPOLATE));
        double sigma = advice.getSigmas();

        double recommendedPercentStock = Math.round(1000.0 * advice.getRecommendedPercentStock()) / 10.0;

        writer.accept("");
        writer.accept(String.format("The model's estimated value is: %.2f",estimate));
        writer.accept(String.format("The model's sigmas is: %.2f",sigma));
        writer.accept(String.format("The model's Recommended Percent Stock is: %.2f",recommendedPercentStock));
        writer.accept(String.format("The model's standard deviation is: $%.2f", model.getStandardDeviation()));
        writer.accept(String.format("The model's R-Squared is: %.4f", model.getRsquared()));
        writer.accept("");
        writer.accept("SimpleEstimate From Inputs:");
        List<ParameterInfo> parameters = model.getParameters();
        double sum = 0;
        MarketSlice lastKnownMarketSlice = advice.getInputs().getPresentDayMarketSlice(GetMethod.LAST_KNOWN_VALUE);
        MarketSlice extrapolatedMarketSlice = advice.getInputs().getPresentDayMarketSlice(GetMethod.EXTRAPOLATE);
        for (ParameterInfo parameterInfo : parameters) {
            double parmVal = parameterInfo.getValue();
            double presentVal = ("INTERCEPT".equalsIgnoreCase(parameterInfo.getName()) ? 1 :
                    extrapolatedMarketSlice.getValue(parameterInfo.getName()));
            double product = parmVal * presentVal;
            sum += product;
            writer.accept(String.format("%s =>  parameter: '%f' (stderr: %f) x last value: '%f' = '%.2f' --> Cumulative Sum: %f",
                    parameterInfo.getName(),
                    parmVal,
                    parameterInfo.getStandardError(),
                    presentVal,
                    product,
                    sum));
            if (!"INTERCEPT".equalsIgnoreCase(parameterInfo.getName())) {
                LocalDate fd = advice.getInputs().getFirstDateOf(parameterInfo.getName());
                LocalDate ld = advice.getInputs().getLastDateOf(parameterInfo.getName());
                writer.accept(String.format("The parameter's data goes from '%s' to '%s'",
                        fd.toString(), ld.toString()));
                writer.accept(String.format("The last value is %.4f, we extrapolated it to %.4f",
                        lastKnownMarketSlice.getValue(parameterInfo.getName()),
                        presentVal));
            }

        }
        writer.accept("");
        writer.accept(String.format("Yields: $%.2f",sum));
        writer.accept(String.format("Which should equal: $%.2f",estimate));
        writer.accept("");

        // tell me how these parameters influence the price.
        for (ParameterInfo parameterInfo : parameters) {
            if ("INTERCEPT".equalsIgnoreCase(parameterInfo.getName())) {
                continue;
            }
            writer.accept("----");
            double parmVal = parameterInfo.getValue();
            double presentVal = ("INTERCEPT".equalsIgnoreCase(parameterInfo.getName()) ? 1 :
                    extrapolatedMarketSlice.getValue(parameterInfo.getName()));
            double product = parmVal * presentVal;
            double percent = product / estimate;
            double averageChange = TableUtil.getStandardChange(advice.getInputs(),parameterInfo.getName());
            double averagePercentChange = TableUtil.getStandardPercentChange(advice.getInputs(),parameterInfo.getName());
            DoubleRange doubleRange = TableUtil.getRange(advice.getInputs(), parameterInfo.getName());
            writer.accept(String.format("For parameter %s, it contributed %.1f%% to the final estimate.",
                    parameterInfo.getName(), percent * 100.0));
            writer.accept(String.format("For parameter %s, the average change between periods is %.4f.",
                    parameterInfo.getName(), averageChange));
            writer.accept(String.format("For parameter %s, the average percent change between periods is %.1f%%.",
                    parameterInfo.getName(), averagePercentChange * 100));
            writer.accept(String.format("For parameter %s, the values went from %.4f to %.4f",
                    parameterInfo.getName(), doubleRange.getMin(), doubleRange.getMax()));
            writer.accept(String.format("For parameter %s, the input value could easily change by %.1f%%, or %.4f in one period, " +
                            "which would change the estimate by %.1f%%, or $%.2f",
                    parameterInfo.getName(),
                    averagePercentChange * 100,
                    averagePercentChange * presentVal,
                    Math.abs(100.0 * averagePercentChange * presentVal * parmVal / advice.getCurrentMarketValue()),
                    Math.abs(averagePercentChange * presentVal * parmVal)));
            double lastPercent = TableUtil.getLastPercentChange(advice.getInputs(),parameterInfo.getName());
            writer.accept(String.format("For parameter %s, in the most recent period it changed by %.2f%%, if that continues the estimate would change by $%.2f",
                    parameterInfo.getName(),lastPercent * 100.0,
                    lastPercent * presentVal * parmVal));
        }

        writer.accept("");
        writer.accept("Function Output:");
        writer.accept("Sigma, Percent Stock:");
        for (double x = -6; x <= 6; x+=0.5) {
            writer.accept(String.format("%.1f,%.1f%%",x,advice.getBestFunction().apply(x)*100));
        }
    }

    private void writeCombinedSummaryText(CombinedAdvice advice, Consumer<String> writer) throws IOException {
        int i = 0;
        List<Advice> superList = new ArrayList<>();
        superList.add(advice);
        superList.addAll(advice.getAllAdvice().keySet());
        for (Advice x : superList) {
            writer.accept("");
            writer.accept(String.format("------------ Advice %d (weight: %.2f) ------------",i, advice.getAllAdvice().get(x)));
            writer.accept("");
            // add more indentation.
            Consumer<String> subWriter = line -> {
                writer.accept("    "+line);
            };
            writeBasicSummaryText(x,subWriter);
            writer.accept("");
            writer.accept(String.format("------------ End Advice %d ------------",i));
            i += 1;
        }
    }

    private void writeDataText(CurrentDirectory currentDirectory, Advice advice) {
        String dataText = Util.doWithWriter(writer -> {

            Util.ExceptionalConsumer<LocalDate> cons = date->{
                writer.write("\n");
                writer.write(date.toString());
                double[] currentInputs = new double[advice.getInputs().getColumnCount()];
                for (int i = 0;i<advice.getInputs().getColumnCount();i++) {
                    writer.write(",");
                    double v = advice.getInputs().getValue(date,i,GetMethod.INTERPOLATE);
                    currentInputs[i] = v;
                    writer.write(String.format("%.2f",v));
                }
                writer.write(",");
                double presentValue = advice.getMarket().getValue(date,0,GetMethod.INTERPOLATE);
                writer.write(String.valueOf(presentValue));
                List<String> inputNames = advice.getInputs().getColumns();
                MarketSlice marketSlice = advice.getInputs().getMarketSlice(date, inputNames, GetMethod.INTERPOLATE);
                double est = advice.getModel().estimateValue(marketSlice);
                writer.write(",");
                writer.write(String.format("%.2f",est));
                if (advice.getModel() instanceof NormalModel) {
                    NormalModel normalModel = (NormalModel) advice.getModel();
                    double sigmas = (est - presentValue) / normalModel.getStandardDeviation();
                    writer.write(",");
                    writer.write(String.format("%.4f",sigmas));
                    writer.write(",");
                    writer.write(String.format("%.4f",advice.getBestFunction().apply(sigmas)));
                }
            };

            // date, inputs, market value, estimate, sigmas, recommended percent stock.
            writer.write("Date");
            for (int i = 0;i<advice.getInputs().getColumnCount();i++) {
                writer.write(","+advice.getInputs().getColumn(i));
            }
            writer.write(",Market Value,SimpleEstimate,Sigma,Recommended Percent Stock");
            Util.doWithDate(advice.getMarket().getFirstDateOf(0),advice.getMarket().getLastDateOf(0),
                    Frequency.Weekly, cons);
        });
        File dataFile = currentDirectory.toFile(filePrefix+"data.csv");
        try {
            FileUtils.writeStringToFile(dataFile,dataText);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeSimulationText(CurrentDirectory currentDirectory, Advice advice) {
        String simText = Util.doWithWriter(writer -> {
            writer.write("Date,Value,Percent Stock\n");
            // must go in chronological order.
            List<LocalDate> dates = new ArrayList<>(advice.getBestSimulationResults().getWorthOverTime().keySet());
            Collections.sort(dates);
            for (LocalDate date : dates) {
                Simulater.Stance stance = advice.getBestSimulationResults().getWorthOverTime().get(date);
                writer.write(String.format("%s,%d,%d%%\n",date.toString(),
                        (int) Math.round(stance.getNetWorthDollars()),
                        stance.getPercentStock()));
            }
        });
        File simFile = currentDirectory.toFile(filePrefix+"simulation.csv");
        try {
            FileUtils.writeStringToFile(simFile,simText);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String evalWith(SimpleEstimate estimate, Line line, String modelName) {
        double pct = line.apply(estimate.getSigmas());
        if (pct > 1) {
            pct = 1;
        }
        if (pct < 0) {
            pct = 0;
        }
        int percnt = (int) Math.round(pct*100);
        return String.format("According to '%s', you should have %d%% (unbound: %d%%) invested in stock and the " +
                "rest in bonds.",modelName,percnt,Math.round(pct*100));
    }
}
