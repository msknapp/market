package knapp;

import knapp.simulation.Simulater;
import knapp.simulation.strategy.Line;
import knapp.simulation.strategy.StrategyBank;
import knapp.table.Table;
import knapp.table.TableImpl;
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
import java.util.Random;

public class Reporter {
    private Model model;
    private Table inputs;
    private Table market;
    private LocalDate start;
    private double currentMarketValue;
    private Simulater.SimulationResults bestSimulationResults;
    private Line bestLine;

    public Reporter() {

    }

    public Line getBestLine() {
        return bestLine;
    }

    public void setBestLine(Line bestLine) {
        this.bestLine = bestLine;
    }

    public Simulater.SimulationResults getBestSimulationResults() {
        return bestSimulationResults;
    }

    public void setBestSimulationResults(Simulater.SimulationResults bestSimulationResults) {
        this.bestSimulationResults = bestSimulationResults;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Table getInputs() {
        return inputs;
    }

    public void setInputs(Table inputs) {
        this.inputs = inputs;
    }

    public Table getMarket() {
        return market;
    }

    public void setMarket(Table market) {
        this.market = market;
    }

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public double getCurrentMarketValue() {
        return currentMarketValue;
    }

    public void setCurrentMarketValue(double currentMarketValue) {
        this.currentMarketValue = currentMarketValue;
    }

    public void produceReportBeneathHome() {
        produceReportBeneathHome("/Documents/investing");
    }

    public void produceReportBeneathHome(String subPath) {
        String baseDir = System.getenv("HOME")+subPath;
        produceReport(baseDir);
    }

    public void produceReport(String baseDir) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String subDir = LocalDateTime.now().format(dtf);
        String cdDir = baseDir+"/"+subDir;
        System.out.println("Your report will be written to: "+cdDir);
        File dir = new File(cdDir);
        dir.mkdirs();
        CurrentDirectory currentDirectory = new CurrentDirectory(cdDir);
        produceReport(currentDirectory);
    }

    public void produceReport(CurrentDirectory currentDirectory) {
        // write files to current directory.
        // When this is called, the current directory should be new each time.


        // I want a CSV with:
        // date, inputs, market value, estimate, sigmas, recommended percent stock
        // another csv with the simulation results
        // and one file with a summary.
        String text = Util.doWithWriter(writer -> {
            LocalDate lastInputDate = getInputs().getLastDate();
            double[] lastInputs = getInputs().getExactValues(lastInputDate);
            Model.Estimate estimate = getModel().produceEstimate(lastInputs, getCurrentMarketValue());
            double sigma = estimate.getSigmas();

            // make it have one decimal place:
            double recommendedPercentStock = Math.round(1000*getBestLine().apply(sigma)) / 10.0;
            writer.write("Summary:\n\n");
            writer.write(String.format("You should have %f%% of your money invested in stock right now.\n\n",recommendedPercentStock));
            writer.write(String.format("Best Strategy Equation: percent stock = %f + %f * sigma\n",
                    getBestLine().getIntercept(),getBestLine().getSlope()));
            writer.write(String.format("Solving: percent stock = %f + %f * %f = %f\n",
                    getBestLine().getIntercept(),getBestLine().getSlope(), sigma, recommendedPercentStock));
            writer.write(String.format("Current Market Price: $%f\n",getCurrentMarketValue()));
            writer.write(String.format("Estimated Market Price: $%f\n",estimate.getEstimatedValue()));
            writer.write(String.format("The model's standard deviation is: %f\n",getModel().getStandardDeviation()));
            writer.write(String.format("The model's R-Squared is: %f\n",getModel().getRsquared()));
            writer.write(String.format("Current Sigma: %f\n\n",sigma));
            writer.write("Estimate From Inputs:\n");
            double[] parms = model.getParameters();
            double sum = parms[0];
            writer.write(String.format("Intercept: %f --> Cumulative Sum: %f\n",parms[0],sum));
            for (int col = 0; col < getInputs().getColumnCount(); col ++) {
                String colName = getInputs().getColumn(col);
                double parm = parms[col+1];
                double value = lastInputs[col];
                sum += parm * value;
                writer.write(String.format("%s =>  parameter: '%f' x last value: '%f' = '%f' --> Cumulative Sum: %f'\n",colName,
                        parm, value, parm*value, sum));
            }
            writer.write(String.format("\nYields: %f\n\n",estimate.getEstimatedValue()));
            
            writer.write("Simulation Results:\n");
            writer.write(String.format("The simulation ended with: $%d\n",getBestSimulationResults().getFinalDollars()));
            writer.write(String.format("The simulation had ROI: $%f\n",getBestSimulationResults().getAverageROI()));
            writer.write(String.format("The simulation made %d transactions.\n",getBestSimulationResults().getTransactions().size()));

            writer.write("\nAlternative Strategies:\n\n");
            String s = evalWith(estimate,StrategyBank.trainedUntil2014_Equation(),"trained until 2014");
            writer.write(s+"\n");
            s = evalWith(estimate,StrategyBank.trainedUntil2018_Equation(),"trained until 2018");
            writer.write(s+"\n");
            s = evalWith(estimate,StrategyBank.winner1_Equation(),"winner1");
            writer.write(s+"\n");

        });
        File file = currentDirectory.toFile("summary.txt");
        try {
            FileUtils.writeStringToFile(file,text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String simText = Util.doWithWriter(writer -> {
            writer.write("Date,Value,Percent Stock\n");
            // must go in chronological order.
            List<LocalDate> dates = new ArrayList<>(getBestSimulationResults().getWorthOverTime().keySet());
            Collections.sort(dates);
            for (LocalDate date : dates) {
                Simulater.Stance stance = getBestSimulationResults().getWorthOverTime().get(date);
                writer.write(String.format("%s,%f,%d\n",date.toString(),stance.getNetWorthDollars(),stance.getPercentStock()));
            }
        });
        File simFile = currentDirectory.toFile("simulation.csv");
        try {
            FileUtils.writeStringToFile(simFile,simText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String dataText = Util.doWithWriter(writer -> {
            // date, inputs, market value, estimate, sigmas, recommended percent stock.
            writer.write("Date");
            for (int i = 0;i<inputs.getColumnCount();i++) {
                writer.write(","+inputs.getColumn(i));
            }
            writer.write(",Market Value,Estimate,Sigma,Recommended Percent Stock");
            Util.doWithDate(getMarket().getFirstDate(),getMarket().getLastDate(),getMarket().getFrequency(), date->{
                writer.write("\n");
                writer.write(date.toString());
                double[] currentInputs = new double[inputs.getColumnCount()];
                for (int i = 0;i<inputs.getColumnCount();i++) {
                    writer.write(",");
                    double v = inputs.getValue(date,i,TableImpl.GetMethod.INTERPOLATE);
                    currentInputs[i] = v;
                    writer.write(String.valueOf(v));
                }
                writer.write(",");
                double presentValue = market.getValue(date,0,TableImpl.GetMethod.INTERPOLATE);
                writer.write(String.valueOf(presentValue));
                Model.Estimate est = getModel().produceEstimate(currentInputs,presentValue);
                writer.write(",");
                writer.write(String.valueOf(est.getEstimatedValue()));
                writer.write(",");
                writer.write(String.valueOf(est.getSigmas()));
                writer.write(",");
                writer.write(String.valueOf(getBestLine().apply(est.getSigmas())));
            });
        });
        File dataFile = currentDirectory.toFile("data.csv");
        try {
            FileUtils.writeStringToFile(dataFile,dataText);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String evalWith(Model.Estimate estimate, Line line, String modelName) {
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
