package knapp.simulation.strategy;

import knapp.predict.*;
import knapp.table.Frequency;
import knapp.simulation.Account;
import knapp.simulation.CurrentPrices;
import knapp.simulation.InvestmentAllocation;
import knapp.simulation.functions.EvolvableFunction;
import knapp.table.Table;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class FunctionStrategy extends SigmaBasedStrategy {

    private final EvolvableFunction evolvableFunction;

    public FunctionStrategy(TrendFinder trendFinder, EvolvableFunction evolvableFunction, Map<String,Integer> lags) {
        super(trendFinder,lags);
        this.evolvableFunction = evolvableFunction;
    }

    @Override
    public final InvestmentAllocation chooseAllocation(LocalDate presentDay, Account account, Table inputs,
                                                       Table stockMarket, Table bondMarket, CurrentPrices currentPrices,
                                                       InvestmentAllocation current, double sigma) {
        double pctStock = this.evolvableFunction.apply(sigma);
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
        return reassessAllocation(presentDay,account,inputs,stockMarket, bondMarket, currentPrices,
                current, sigma, ideal);
    }

    public abstract InvestmentAllocation reassessAllocation(LocalDate presentDay, Account account, Table inputs,
                                                   Table stockMarket, Table bondMarket, CurrentPrices currentPrices,
                                                   InvestmentAllocation current, double sigma,
                                                   InvestmentAllocation ideal);

    @Override
    public int getMinimumPercentChange() {
        return 12;
    }

    @Override
    public boolean canEvolve() {
        return true;
    }
}
