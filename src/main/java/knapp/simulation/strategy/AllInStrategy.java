package knapp.simulation.strategy;

import knapp.simulation.Account;
import knapp.simulation.CurrentPrices;
import knapp.simulation.InvestmentAllocation;
import knapp.table.Table;

import java.time.LocalDate;

public class AllInStrategy extends AllocationStrategy {
    @Override
    public InvestmentAllocation chooseAllocation(LocalDate presentDay, Account account, Table inputs, Table stockMarket, Table bondMarket, CurrentPrices currentPrices, InvestmentAllocation current) {
        return new InvestmentAllocation(100,0,0);
    }

    @Override
    public int getMinimumPercentChange() {
        return 2;
    }

    @Override
    public boolean canEvolve() {
        return false;
    }
}
