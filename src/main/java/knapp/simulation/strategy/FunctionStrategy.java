package knapp.simulation.strategy;

import knapp.predict.*;
import knapp.history.Frequency;
import knapp.simulation.Account;
import knapp.simulation.CurrentPrices;
import knapp.simulation.InvestmentAllocation;
import knapp.simulation.functions.EvolvableFunction;
import knapp.table.values.GetMethod;
import knapp.table.Table;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FunctionStrategy extends AllocationStrategy {

    private final EvolvableFunction evolvableFunction;
    private final TrendFinder trendFinder;
    private final Map<String,Integer> lags;

    public FunctionStrategy(TrendFinder trendFinder, EvolvableFunction evolvableFunction, Map<String,Integer> lags) {
        this.evolvableFunction = evolvableFunction;
        this.trendFinder = trendFinder;
        this.lags = Collections.unmodifiableMap(new HashMap<>(lags));
    }

    @Override
    public InvestmentAllocation chooseAllocation(LocalDate presentDay, Account account, Table inputs, Table stockMarket,
                                                 Table bondMarket, CurrentPrices currentPrices) {
        LocalDate end = presentDay;
        LocalDate start = end.minusYears(10);

        TrendFinder.Analasys analysis = trendFinder.startAnalyzing().start(start).
                end(end).inputs(inputs).market(stockMarket).
                frequency(Frequency.Monthly).
                lags(lags).build();
        NormalModel model = analysis.deriveModel();

        MarketSlice marketSlice = inputs.getMarketSlice(presentDay,lags,true);
        double estimate = model.estimateValue(marketSlice);
        LocalDate ld = stockMarket.getLastDateOf(0);
        if (ld.isAfter(presentDay)) {
            throw new IllegalStateException("The future data is being leaked.");
        }
        double lastMarketValue = stockMarket.getExactValues(ld)[0];
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
