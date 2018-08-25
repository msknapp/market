package knapp.simulation.strategy;

import knapp.predict.TrendFinder;
import knapp.simulation.Account;
import knapp.simulation.CurrentPrices;
import knapp.simulation.InvestmentAllocation;
import knapp.simulation.functions.EvolvableFunction;
import knapp.table.Table;

import java.time.LocalDate;
import java.util.Map;

public class DirectFunctionStrategy extends FunctionStrategy {

    public DirectFunctionStrategy(TrendFinder trendFinder, EvolvableFunction evolvableFunction, Map<String, Integer> lags) {
        super(trendFinder, evolvableFunction, lags);
    }

    @Override
    public InvestmentAllocation reassessAllocation(LocalDate presentDay, Account account, Table inputs, Table stockMarket,
                                                   Table bondMarket, CurrentPrices currentPrices, InvestmentAllocation current,
                                                   double sigma, InvestmentAllocation ideal) {
        return ideal;
    }

    @Override
    public int getMinimumPercentChange() {
        return 20;
    }

}
