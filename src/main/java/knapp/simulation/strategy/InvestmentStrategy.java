package knapp.simulation.strategy;

import knapp.simulation.Account;
import knapp.simulation.CurrentPrices;
import knapp.simulation.Order;
import knapp.table.Table;

import java.time.LocalDate;
import java.util.Set;

public interface InvestmentStrategy {
    StrategyOrders rebalance(LocalDate presentDay, Account account, Table inputs, Table stockMarket, Table bondMarket,
                         CurrentPrices currentPrices);
    boolean canEvolve();
}
