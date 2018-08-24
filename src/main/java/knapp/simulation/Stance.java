package knapp.simulation;

public class Stance {
    private final int percentStock;
    private final USDollars netWorthDollars;

    public Stance(int percentStock, USDollars netWorthDollars) {
        this.percentStock = percentStock;
        this.netWorthDollars = netWorthDollars;
    }

    public int getPercentStock() {
        return percentStock;
    }

    public USDollars getNetWorthDollars() {
        return netWorthDollars;
    }
}
