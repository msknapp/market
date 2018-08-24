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
import knapp.table.values.TableColumnView;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FunctionStrategy3 extends AllocationStrategy {

    private final EvolvableFunction evolvableFunction;
    private final TrendFinder trendFinder;
    private final Map<String,Integer> lags;

    public FunctionStrategy3(TrendFinder trendFinder, EvolvableFunction evolvableFunction, Map<String,Integer> lags) {
        this.evolvableFunction = evolvableFunction;
        this.trendFinder = trendFinder;
        this.lags = Collections.unmodifiableMap(new HashMap<>(lags));
    }

    @Override
    public InvestmentAllocation chooseAllocation(LocalDate presentDay, Account account, Table inputs, Table stockMarket,
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
        InvestmentAllocation ideal = new InvestmentAllocation(ps,100-ps,0);

        boolean rising = isRising(stockMarket);
        boolean buyMoreStock = ideal.getPercentStock() > current.getPercentStock();
        boolean undervalued = sigmas > 0;

        // if the prices are rising, and the market is undervalued, proceed
        // if the prices are falling, and the market is overvalued, proceed.
        // if the prices are rising but the market is overvalued, do nothing.
        // if the prices are falling but the market is undervalued, do nothing.
        if (rising == undervalued) {
            return ideal;
        }
        return null;
    }

    private boolean isRising(Table stockMarket) {
        TableColumnView view = stockMarket.getTableColumnView(0);
        LocalDate last = view.getLastDate();
        LocalDate secondLast = view.getDateBefore(last);
        return view.getExactValue(last) > view.getExactValue(secondLast);
    }

    @Override
    public int getMinimumPercentChange() {
        return 5;
    }
}
