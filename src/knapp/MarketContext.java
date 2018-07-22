package knapp;

import knapp.util.CurrentDirectory;

public class MarketContext {

    private String BASE_DIR="/home/michael/Documents/investing";
    private CurrentDirectory currentDirectory = new CurrentDirectory(BASE_DIR);

    public MarketContext() {

    }

    public String getBASE_DIR() {
        return BASE_DIR;
    }

    public CurrentDirectory getCurrentDirectory() {
        return currentDirectory;
    }
}
