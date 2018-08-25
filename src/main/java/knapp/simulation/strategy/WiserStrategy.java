package knapp.simulation.strategy;

import knapp.predict.NormalModel;
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
    public AllocationAndThoughts chooseAllocation(LocalDate presentDay, Account account, Table inputs, Table stockMarket,
                                                  Table bondMarket, CurrentPrices currentPrices,
                                                  InvestmentAllocation current, double sigma, NormalModel model) {
        MarketThoughts marketThoughts = new MarketThoughts();
        if (first) {
            first = false;
            InvestmentAllocation investmentAllocation = new InvestmentAllocation(70,30,0);
            marketThoughts.setDecisionComment("Just entering the market");
            return new AllocationAndThoughts(investmentAllocation, marketThoughts);
        }

//        double sigmas = (estimate - lastMarketValue) / model.getStandardDeviation();
        // a positive sigma means it is undervalued.
        InvestmentAllocation investmentAllocation = current;
        if (sigma < lowerLine) {
            // that means it is very overvalued.
            // you should sell.
            marketThoughts.setDecisionComment(String.format("The market is overvalued; Sigma is below %.1f;let's sell all stocks",lowerLine));
            investmentAllocation = new InvestmentAllocation(0,100,0);
        } else if (sigma > upperLine) {
            // it is undervalued.
            // you should buy.
            marketThoughts.setDecisionComment(String.format("The market is undervalued; Sigma is above %.1f;let's buy all stocks",upperLine));
            investmentAllocation = new InvestmentAllocation(100,0,0);
        } else {
            marketThoughts.setDecisionComment(String.format("The market is fairly priced; sigma is between %.1f and %.1f; let's just wait and see.",lowerLine, upperLine));
        }
        return new AllocationAndThoughts(investmentAllocation, marketThoughts);
    }

    @Override
    public boolean canEvolve() {
        return false;
    }
}
