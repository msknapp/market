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

public class MomentumFunctionStrategy extends FunctionStrategy {

    public MomentumFunctionStrategy(TrendFinder trendFinder, EvolvableFunction evolvableFunction,
                                    Map<String,Integer> lags) {
        super(trendFinder,evolvableFunction, lags);
    }

    @Override
    public InvestmentAllocation reassessAllocation(LocalDate presentDay, Account account, Table inputs,
                                                   Table stockMarket, Table bondMarket, CurrentPrices currentPrices,
                                                   InvestmentAllocation current, double sigma,
                                                   InvestmentAllocation ideal) {

        boolean rising = isRising(stockMarket);
        boolean buyMoreStock = ideal.getPercentStock() > current.getPercentStock();
        boolean undervalued = sigma > 0;

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
}
