package knapp.simulation.strategy;

import knapp.simulation.Account;
import knapp.simulation.CurrentPrices;
import knapp.simulation.Order;
import knapp.table.Table;

import java.time.LocalDate;
import java.util.Set;

public class AllStockStrategy implements InvestmentStrategy {

    @Override
    public Set<Order> rebalance(LocalDate presentDay, Account account, Table inputs, Table stockMarket,
                                Table bondMarket, CurrentPrices currentPrices) {
        return StrategyUtil.buyAsMuchStockAsPossible(presentDay,account,inputs,stockMarket,
                bondMarket,currentPrices);
    }
}
