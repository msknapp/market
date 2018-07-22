package knapp;

import knapp.util.CurrentDirectory;

public class MarketContext {

    private String BASE_DIR="/home/michael/Documents/investing";
    private CurrentDirectory currentDirectory = new CurrentDirectory(BASE_DIR);
    private TrendFinder trendFinder;
    private DataRetriever dataRetriever;

    public MarketContext() {

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
