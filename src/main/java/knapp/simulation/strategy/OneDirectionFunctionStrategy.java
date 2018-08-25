package knapp.simulation.strategy;

import knapp.predict.TrendFinder;
import knapp.simulation.Account;
import knapp.simulation.CurrentPrices;
import knapp.simulation.InvestmentAllocation;
import knapp.simulation.functions.EvolvableFunction;
import knapp.table.Table;

import java.time.LocalDate;
import java.util.Map;

public class OneDirectionFunctionStrategy extends FunctionStrategy {

    public OneDirectionFunctionStrategy(TrendFinder trendFinder, EvolvableFunction evolvableFunction, Map<String,Integer> lags) {
        super(trendFinder, evolvableFunction, lags);
    }

    @Override
    public AllocationAndThoughts reassessAllocation(LocalDate presentDay, Account account, Table inputs,
                                                   Table stockMarket, Table bondMarket, CurrentPrices currentPrices,
                                                   InvestmentAllocation current, double sigma,
                                                   InvestmentAllocation ideal) {
        MarketThoughts marketThoughts = new MarketThoughts();
        marketThoughts.setOvervalued(sigma < 0);
        InvestmentAllocation investmentAllocation = null;
        if (sigma < 0) {
            // the market is overvalued,
            // the only option is to sell right now.
            if (ideal.getPercentStock() > current.getPercentStock()) {
                // this is suggesting we buy more, don't do that until sigma is positive.
                marketThoughts.setDecisionComment("The market is overvalued but ideally I would have more stock so let's do nothing.");
                return new AllocationAndThoughts(null,marketThoughts);
            }
            marketThoughts.setDecisionComment("The market is overvalued so let's sell stock");
            investmentAllocation = ideal;
        } else {
            // the market is undervalued, we should be buying more.
            if (ideal.getPercentStock() < current.getPercentStock()) {
                // this is saying we should sell some stock.
                // let's wait until it passes into negative sigma territory.
                marketThoughts.setDecisionComment("The market is undervalued but ideally I would have less stock so let's do nothing.");
                return new AllocationAndThoughts(null,marketThoughts);
            }
            marketThoughts.setDecisionComment("The market is undervalued so let's buy stock");
            investmentAllocation = ideal;
        }
        return new AllocationAndThoughts(investmentAllocation, marketThoughts);
    }

    @Override
    public int getMinimumPercentChange() {
        return 5;
    }
}
