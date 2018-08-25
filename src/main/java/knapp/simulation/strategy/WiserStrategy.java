package knapp.simulation.strategy;

import knapp.predict.TrendFinder;
import knapp.simulation.Account;
import knapp.simulation.CurrentPrices;
import knapp.simulation.InvestmentAllocation;
import knapp.simulation.functions.EvolvableFunction;
import knapp.table.Table;

import java.time.LocalDate;
import java.util.Map;

public class WiserStrategy extends SigmaBasedStrategy {

    private double upperLine = 0.7;
    private double lowerLine = -0.7;
    boolean first = true;

    public WiserStrategy(TrendFinder trendFinder,
                         Map<String, Integer> lags, double upperLine, double lowerLine) {
        super(trendFinder, lags);
        this.upperLine = upperLine;
        this.lowerLine = lowerLine;
    }

    @Override
    public InvestmentAllocation chooseAllocation(LocalDate presentDay, Account account, Table inputs, Table stockMarket, Table bondMarket, CurrentPrices currentPrices, InvestmentAllocation current, double sigma) {
        if (first) {
            first = false;
            return new InvestmentAllocation(70,30,0);
        }

//        double sigmas = (estimate - lastMarketValue) / model.getStandardDeviation();
        // a positive sigma means it is undervalued.
        if (sigma < lowerLine) {
            // that means it is very overvalued.
            // you should sell.
            return new InvestmentAllocation(0,100,0);
        } else if (sigma > upperLine) {
            // it is undervalued.
            // you should buy.
            return new InvestmentAllocation(100,0,0);
        }
        return current;
    }

    @Override
    public boolean canEvolve() {
        return false;
    }
}
