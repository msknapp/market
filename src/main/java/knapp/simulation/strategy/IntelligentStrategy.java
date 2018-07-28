package knapp.simulation.strategy;

import knapp.Model;
import knapp.TrendFinder;
import knapp.history.Frequency;
import knapp.simulation.Account;
import knapp.simulation.CurrentPrices;
import knapp.simulation.InvestmentAllocation;
import knapp.table.Table;

import java.time.LocalDate;

public class IntelligentStrategy extends AllocationStrategy implements InvestmentStrategy {

    private final TrendFinder trendFinder;

    public IntelligentStrategy(TrendFinder trendFinder) {
        this.trendFinder = trendFinder;
    }

    @Override
    public InvestmentAllocation chooseAllocation(LocalDate presentDay, Account account, Table inputs,
                                Table stockMarket, Table bondMarket, CurrentPrices currentPrices) {
        LocalDate end = presentDay;
        LocalDate start = end.minusYears(10);

        TrendFinder.Analasys analysis = trendFinder.startAnalyzing().start(start).
                end(end).inputs(inputs).market(stockMarket).
                frequency(Frequency.Monthly).build();
        Model model = analysis.deriveModel();

        double[] lastInputs = inputs.getExactValues(inputs.getLastDate());
        double lastMarketValue = stockMarket.getExactValues(stockMarket.getLastDate())[0];
        Model.Estimate estimate = model.produceEstimate(lastInputs,lastMarketValue);

        // a positive sigmas means the estimate is higher than the actual value,
        // so the market is undervalued.
        if (estimate.getSigmas() > 2) {
            return new InvestmentAllocation(100,0,0);
        } else if (estimate.getSigmas() > 1) {
            return new InvestmentAllocation(85,15,0);
        } else if (estimate.getSigmas() > 0) {
            return new InvestmentAllocation(75,25,0);
        } else if (estimate.getSigmas() > -1) {
            return new InvestmentAllocation(50,50,0);
        } else if (estimate.getSigmas() > -2) {
            return new InvestmentAllocation(35,65,0);
        } else {
            // the stock market appears to be very overvalued.
            return new InvestmentAllocation(25,75,0);
        }

    }

    @Override
    public int getMinimumPercentChange() {
        return 10;
    }

}
