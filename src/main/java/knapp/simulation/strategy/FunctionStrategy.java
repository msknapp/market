package knapp.simulation.strategy;

import knapp.predict.*;
import knapp.history.Frequency;
import knapp.simulation.Account;
import knapp.simulation.CurrentPrices;
import knapp.simulation.InvestmentAllocation;
import knapp.simulation.functions.EvolvableFunction;
import knapp.table.Table;

import java.time.LocalDate;

public class FunctionStrategy extends AllocationStrategy {

    private final EvolvableFunction evolvableFunction;
    private final TrendFinder trendFinder;

    public FunctionStrategy(TrendFinder trendFinder, EvolvableFunction evolvableFunction) {
        this.evolvableFunction = evolvableFunction;
        this.trendFinder = trendFinder;
    }

    @Override
    public InvestmentAllocation chooseAllocation(LocalDate presentDay, Account account, Table inputs, Table stockMarket, Table bondMarket, CurrentPrices currentPrices) {
        LocalDate end = presentDay;
        LocalDate start = end.minusYears(10);

        TrendFinder.Analasys analysis = trendFinder.startAnalyzing().start(start).
                end(end).inputs(inputs).market(stockMarket).
                frequency(Frequency.Monthly).build();
        NormalModel model = analysis.deriveModel();

        MarketSlice marketSlice = inputs.getLastMarketSlice();
        double estimate = model.estimateValue(marketSlice);
        double lastMarketValue = stockMarket.getExactValues(stockMarket.getLastDate())[0];
        double sigmas = (estimate - lastMarketValue) / model.getStandardDeviation();

        double pctStock = this.evolvableFunction.apply(sigmas);
        if (pctStock > 1.0) {
            pctStock = 1;
        }
        if (pctStock < 0) {
            pctStock = 0;
        }
        int ps = (int) Math.round(100*pctStock);
        if (ps < 0) {
            ps = 0;
        }
        if (ps > 100) {
            ps = 100;
        }
        return new InvestmentAllocation(ps,100-ps,0);
    }

    @Override
    public int getMinimumPercentChange() {
        return 5;
    }
}