package knapp;

import knapp.history.Frequency;
import knapp.simulation.Simulater;
import knapp.table.Table;
import knapp.table.TableParser;
import knapp.util.CurrentDirectory;

import java.io.IOException;

public class MarketContext {

    private String BASE_DIR="/home/michael/Documents/investing";
    private CurrentDirectory currentDirectory = new CurrentDirectory(BASE_DIR);
    private TrendFinder trendFinder;
    private DataRetriever dataRetriever;
    private Table market;
    private String consolidatedDataFile;
    private String marketFile;
    private String predictionFile;
    private Simulater simulater;

    public String getIndicatorsFile() {
        return indicatorsFile;
    }

    public void setIndicatorsFile(String indicatorsFile) {
        this.indicatorsFile = indicatorsFile;
    }

    private String indicatorsFile;

    public String getPredictionFile() {
        return predictionFile;
    }

    public void setPredictionFile(String predictionFile) {
        this.predictionFile = predictionFile;
    }

    public String getMarketFile() {
        return marketFile;
    }

    public void setMarketFile(String marketFile) {
        this.marketFile = marketFile;
    }

    public String getConsolidatedDataFile() {
        return consolidatedDataFile;
    }

    public void loadMarketData() throws IOException {
        String marketText = getCurrentDirectory().toText(getMarketFile());
        Table market = TableParser.parse(marketText,true,Frequency.Monthly);

        setMarket(market);
    }

    public void setConsolidatedDataFile(String consolidatedDataFile) {
        this.consolidatedDataFile = consolidatedDataFile;
    }

    public Table getMarket() {
        return market;
    }

    public void setMarket(Table market) {
        this.market = market;
    }

    public MarketContext() {

    }

    public Simulater getSimulater() {
        return simulater;
    }

    public void setSimulater(Simulater simulater) {
        this.simulater = simulater;
    }

    public CurrentDirectory getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(CurrentDirectory currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public TrendFinder getTrendFinder() {
        return trendFinder;
    }

    public void setTrendFinder(TrendFinder trendFinder) {
        this.trendFinder = trendFinder;
    }

    public DataRetriever getDataRetriever() {
        return dataRetriever;
    }

    public void setDataRetriever(DataRetriever dataRetriever) {
        this.dataRetriever = dataRetriever;
    }
}
