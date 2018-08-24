package knapp.simulation.strategy;

import knapp.simulation.*;
import knapp.table.Table;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StrategyUtil {

    public static Set<Order> buyAsMuchStockAsPossible(LocalDate presentDay, Account account, Table inputs, Table stockMarket,
                                               Table bondMarket, CurrentPrices currentPrices) {
        USDollars cash = account.getCurrentCash();
        USDollars cashToInvestInStock = cash.minus(account.getTradeFee());
        if (cashToInvestInStock.isDebt()) {
            return Collections.emptySet();
        }
        double shares = cashToInvestInStock.dividedBy(currentPrices.getStockPrice());
        int quantity = (int)Math.floor(shares);
        if (quantity < 1) {
            return Collections.emptySet();
        }

        Order order = Order.BuyStock(quantity);
        return Collections.singleton(order);
    }

    public static Set<Order> buyAsMuchBondsAsPossible(LocalDate presentDay, Account account, Table inputs, Table stockMarket,
                                                      Table bondMarket, CurrentPrices currentPrices) {
        USDollars cash = account.getCurrentCash();
        USDollars cashToInvestInStock = cash.minus(account.getTradeFee());
        if (cashToInvestInStock.isDebt()) {
            return Collections.emptySet();
        }
        double shares = cashToInvestInStock.dividedBy(currentPrices.getBondPrice());
        int quantity = (int)Math.floor(shares);
        if (quantity < 1) {
            return Collections.emptySet();
        }

        Order order = Order.BuyBonds(quantity);
        return Collections.singleton(order);
    }

    public static Set<Order> sellAllYourStock(LocalDate presentDay, Account account, Table inputs, Table stockMarket,
                                                      Table bondMarket, CurrentPrices currentPrices) {
        Set<Order> orders = new HashSet<>();
        for (PurchaseInfo purchaseInfo : account.getOwnedStockShares().values()) {
            orders.add(sellAll(purchaseInfo));
        }
        return orders;
    }

    public static Set<Order> sellAllYourBonds(LocalDate presentDay, Account account, Table inputs, Table stockMarket,
                                              Table bondMarket, CurrentPrices currentPrices) {
        Set<Order> orders = new HashSet<>();
        for (PurchaseInfo purchaseInfo : account.getOwnedBondShares().values()) {
            orders.add(sellAll(purchaseInfo));
        }
        return orders;
    }

    public static Order sellAll(PurchaseInfo purchaseInfo) {
        return purchaseInfo.toSellAllOrder();
    }
}
