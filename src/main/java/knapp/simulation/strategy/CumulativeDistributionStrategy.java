package knapp.simulation.strategy;

import knapp.predict.NormalModel;
import knapp.predict.TrendFinder;
import knapp.simulation.Account;
import knapp.simulation.CurrentPrices;
import knapp.simulation.InvestmentAllocation;
import knapp.table.Table;
import knapp.util.NormalDistUtil;

import java.time.LocalDate;
import java.util.Map;

public class CumulativeDistributionStrategy extends SigmaBasedStrategy {

    private final boolean inverse;
    private final int minPercentStock;
    private final int maxPercentStock;

    public CumulativeDistributionStrategy(TrendFinder trendFinder, Map<String, Integer> lags) {
        this(trendFinder,lags, false,25,100);
    }

    public CumulativeDistributionStrategy(TrendFinder trendFinder, Map<String, Integer> lags,
                                          boolean inverse, int minPercentStock, int maxPercentStock) {
        super(trendFinder, lags);
        this.inverse = inverse;
        this.minPercentStock = minPercentStock;
        this.maxPercentStock = maxPercentStock;
    }

    @Override
    public AllocationAndThoughts chooseAllocation(LocalDate presentDay, Account account, Table inputs, Table stockMarket,
                                                  Table bondMarket, CurrentPrices currentPrices,
                                                  InvestmentAllocation current, double sigma, NormalModel model) {
        // positive sigmas mean the stock market is undervalued
        // negative sigmas mean the stock market is overvalued.

        double pctl = NormalDistUtil.calculatePercentile(sigma,0,1);

        MarketThoughts marketThoughts = new MarketThoughts();
        marketThoughts.setPercentile(pctl);

        int p = (int) Math.round(pctl * (maxPercentStock - minPercentStock));
        // positive sigmas yield percentiles > 50, and it means the market is undervalued.
        // so typically as p goes to 100, your percent stock should approach 100.
        String comment = null;
        if (sigma < 0) {
            comment = "The market is overvalued;";
        } else {
            comment = "The market is undervalued;";
        }

        InvestmentAllocation ia = null;
        if (inverse) {
            // this is basically saying you run in the opposite direction of
            // common sense.  It's like you want to lose money.
            ia = new InvestmentAllocation(100-p-minPercentStock,minPercentStock + p,0);
            comment += " inverse trading strategy";
        } else {
            ia = new InvestmentAllocation(minPercentStock + p,100-p-minPercentStock,0);
            comment += " intuitive trading strategy";
        }
        marketThoughts.setDecisionComment(comment);
        return new AllocationAndThoughts(ia,marketThoughts);
    }

    @Override
    public boolean canEvolve() {
        return false;
    }
}
