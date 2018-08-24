package knapp.simulation;

public class CurrentPrices {
    private final USDollars stockPrice;
    private final USDollars bondPrice;

    public CurrentPrices(USDollars stockPrice, USDollars bondPrice) {
        this.stockPrice = stockPrice;
        this.bondPrice = bondPrice;
    }

    public USDollars getStockPrice() {
        return stockPrice;
    }

    public USDollars getBondPrice() {
        return bondPrice;
    }

    public USDollars getPrice(Asset asset) {
        if (asset == Asset.STOCK) {
            return stockPrice;
        } else if (asset == Asset.BONDS) {
            return bondPrice;
        }
        throw new IllegalArgumentException("Can only get the price of bonds or stock.");
    }
}
