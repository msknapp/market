package knapp.simulation.strategy;

import knapp.predict.MarketSlice;
import knapp.predict.NormalModel;
import knapp.predict.TrendFinder;
import knapp.simulation.Account;
import knapp.simulation.CurrentPrices;
import knapp.simulation.InvestmentAllocation;
import knapp.simulation.functions.EvolvableFunction;
import knapp.table.Frequency;
import knapp.table.Table;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class SigmaBasedStrategy extends AllocationStrategy {

    private final TrendFinder trendFinder;
    private final Map<String,Integer> lags;

    public SigmaBasedStrategy(TrendFinder trendFinder, Map<String,Integer> lags) {
        this.trendFinder = trendFinder;
        this.lags = Collections.unmodifiableMap(new HashMap<>(lags));
    }

    @Override
    public final InvestmentAllocation chooseAllocation(LocalDate presentDay, Account account, Table inputs, Table stockMarket,
                                                 Table bondMarket, CurrentPrices currentPrices,
                                                 InvestmentAllocation current) {
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
        return chooseAllocation(presentDay,account,inputs,stockMarket,
                bondMarket,currentPrices,current, sigmas);
    }

    public abstract InvestmentAllocation chooseAllocation(LocalDate presentDay, Account account, Table inputs, Table stockMarket,
                                                          Table bondMarket, CurrentPrices currentPrices,
                                                          InvestmentAllocation current,double sigma);

    @Override
    public int getMinimumPercentChange() {
        return 5;
    }
}
