package knapp.simulation;

public class InvestmentAllocation {
    private final int percentStock;
    private final int percentBond;
    private final int percentCash;

    public InvestmentAllocation(int percentStock, int percentBond, int percentCash) {
        int sum = percentBond + percentCash + percentStock;
        if (sum != 100) {
            throw new IllegalArgumentException("The sum must be 100%");
        }
        if (percentBond > 100 || percentStock > 100 || percentCash > 100) {
            throw new IllegalArgumentException("All allocations must be less than or equal to 100%");
        }
        if (percentBond < 0 || percentStock < 0 || percentCash < 0) {
            throw new IllegalArgumentException("All allocations must be at least 0%");
        }
        this.percentBond = percentBond;
        this.percentCash = percentCash;
        this.percentStock = percentStock;
    }


    public int getPercentStock() {
        return percentStock;
    }

    public int getPercentBond() {
        return percentBond;
    }

    public int getPercentCash() {
        return percentCash;
    }
}
