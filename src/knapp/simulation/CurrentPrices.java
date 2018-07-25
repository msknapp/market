package knapp.simulation;

public class CurrentPrices {
    private final double stockPrice;
    private final double bondPrice;

    public CurrentPrices(double stockPrice, double bondPrice) {
        this.stockPrice = stockPrice;
        this.bondPrice = bondPrice;
    }

    public double getStockPriceDollars() {
        return stockPrice;
    }

    public double getBondPriceDollars() {
        return bondPrice;
    }
}
