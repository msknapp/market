package knapp.simulation.strategy;

import knapp.predict.MarketSlice;
import knapp.predict.NormalModel;
import knapp.predict.TrendFinder;
import knapp.simulation.Account;
import knapp.simulation.CurrentPrices;
import knapp.simulation.InvestmentAllocation;
import knapp.simulation.USDollars;
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

    public TrendFinder getTrendFinder() {
        return trendFinder;
    }

    public Map<String, Integer> getLags() {
        return lags;
    }

    @Override
    public final AllocationAndThoughts chooseAllocation(LocalDate presentDay, Account account, Table inputs, Table stockMarket,
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
        AllocationAndThoughts allocationAndThoughts = chooseAllocation(presentDay,account,inputs,stockMarket,
                bondMarket,currentPrices,current, sigmas, model);

        allocationAndThoughts.getMarketThoughts().setExpectedValue(USDollars.dollars(estimate));
        allocationAndThoughts.getMarketThoughts().setActualValue(USDollars.dollars(lastMarketValue));
        allocationAndThoughts.getMarketThoughts().setSigma(sigmas);
        allocationAndThoughts.getMarketThoughts().setStandardDeviation(USDollars.dollars(model.getStandardDeviation()));
        allocationAndThoughts.getMarketThoughts().setRsquared(model.getRsquared());

        // TODO maybe record the market parameters here?

        return allocationAndThoughts;
    }

    public abstract AllocationAndThoughts chooseAllocation(LocalDate presentDay, Account account, Table inputs, Table stockMarket,
                                                          Table bondMarket, CurrentPrices currentPrices,
                                                          InvestmentAllocation current,double sigma, NormalModel model);

    @Override
    public int getMinimumPercentChange() {
        return 5;
    }
}
