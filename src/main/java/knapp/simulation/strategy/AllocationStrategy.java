package knapp.simulation.strategy;

import knapp.simulation.*;
import knapp.table.Table;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AllocationStrategy implements InvestmentStrategy {


    public abstract InvestmentAllocation chooseAllocation(LocalDate presentDay, Account account, Table inputs,
                                                   Table stockMarket, Table bondMarket, CurrentPrices currentPrices);


    @Override
    public final Set<Order> rebalance(LocalDate presentDay, Account account, Table inputs, Table stockMarket, Table bondMarket, CurrentPrices currentPrices) {
        InvestmentAllocation desired = chooseAllocation(presentDay,account,inputs,stockMarket,bondMarket,currentPrices);
        if (desired == null) {
            // this is an easy way that child classes can say to not trade anything.
            return Collections.emptySet();
        }
        InvestmentAllocation current = approximateCurrentAllocation(account,currentPrices);


        if (Math.abs(current.getPercentBond() - desired.getPercentBond()) < getMinimumPercentChange() &&
                Math.abs(current.getPercentStock() - desired.getPercentStock()) < getMinimumPercentChange()) {
            // we must meet a minimum threshold for change.
            return Collections.emptySet();
        }

        double liquidValueDollars = account.netValueCents(currentPrices,presentDay) / 100.0;

        double moreStockPercent = (desired.getPercentStock() - current.getPercentStock())/100.0;
        double moreBondPercent = (desired.getPercentBond() - current.getPercentBond())/100.0;

        double moreStockDollars = liquidValueDollars * moreStockPercent;
        double moreBondDollars = liquidValueDollars * moreBondPercent;

        Account simAccount = account;
        Set<Order> orders = new HashSet<>();
        // focus on sales first so you have more money available to buy things.
        if (moreBondDollars < 0) {
            // you want to sell bonds.
            int bondSharesToSell = (int) Math.floor(- moreBondDollars / currentPrices.getBondPriceDollars());
            if (bondSharesToSell > 0) {
                Map<LocalDate,Integer> toSell = chooseBondsToSell(account,bondSharesToSell, presentDay);
                for (LocalDate d : toSell.keySet()) {
                    int q = toSell.get(d);
                    Order order = Order.SellBonds(q,d);
                    orders.add(order);
                    simAccount = simAccount.executeOrder(order,currentPrices,presentDay);
                }
            }
        }
        if (moreStockDollars < 0) {
            // you want to sell stock.
            int stockSharesToSell = (int) Math.floor(- moreStockDollars / currentPrices.getStockPriceDollars());
            if (stockSharesToSell > 0) {
                Map<LocalDate,Integer> toSell = chooseStockToSell(account,stockSharesToSell, presentDay);
                for (LocalDate d : toSell.keySet()) {
                    int q = toSell.get(d);
                    Order order = Order.SellStock(q,d);
                    orders.add(order);
                    simAccount = simAccount.executeOrder(order,currentPrices,presentDay);
                }
            }
        }
        if (moreStockDollars > 0) {
            // must worry about the maximum amount of money you have.
            double dollarsAvailable = (simAccount.getCurrentCents() - simAccount.getTradeFeeCents())/100.0;
            double availableMoreStockDollars = Math.min(dollarsAvailable,moreStockDollars);
            int stockSharesToBuy = (int) Math.floor(availableMoreStockDollars / currentPrices.getStockPriceDollars());
            if (stockSharesToBuy > 0) {
                Order order = Order.BuyStock(stockSharesToBuy);
                orders.add(order);
                simAccount = simAccount.executeOrder(order, currentPrices,presentDay);
            }
        }
        if (moreBondDollars > 0) {
            // must worry about the maximum amount of money you have.
            double dollarsAvailable = (simAccount.getCurrentCents() - simAccount.getTradeFeeCents())/100.0;
            double availableMoreBondDollars = Math.min(dollarsAvailable,moreBondDollars);
            int bondSharesToBuy = (int) Math.floor(availableMoreBondDollars / currentPrices.getBondPriceDollars());
            if (bondSharesToBuy > 0) {
                Order order = Order.BuyBonds(bondSharesToBuy);
                orders.add(order);
            }
        }
        return orders;
    }

    private Map<LocalDate,Integer> chooseBondsToSell(Account account, int bondSharesToSell, LocalDate presentDay) {
        return chooseThingsToSell(account, bondSharesToSell, presentDay, Asset.BONDS);
    }

    private Map<LocalDate,Integer> chooseThingsToSell(Account account, int sharesToSell, LocalDate presentDay, Asset asset) {
        Map<LocalDate,Integer> m = new HashMap<>();
        int total = 0;
        Map<LocalDate,PurchaseInfo> purchaseMap = (asset == Asset.BONDS) ?
                account.getOwnedBondShares() :
                account.getOwnedStockShares();
        List<LocalDate> dates = new ArrayList<>(purchaseMap.keySet());
        Collections.sort(dates);
        LocalDate oneYearAgo = presentDay.minusYears(1);
        List<LocalDate> oldThings = dates.stream().filter(d -> d.isBefore(oneYearAgo)).collect(Collectors.toList());
        Collections.sort(oldThings);
        List<LocalDate> newThings = dates.stream().filter(d -> !d.isBefore(oneYearAgo)).collect(Collectors.toList());
        Collections.sort(newThings);
        Collections.reverse(newThings);

        for (LocalDate d : oldThings) {
            int needed = sharesToSell - total;
            if (needed < 1) {
                break;
            }
            int trade = Math.min(needed,purchaseMap.get(d).getQuantity());
            m.put(d,trade);
            total += trade;
        }
        for (LocalDate d : newThings) {
            int needed = sharesToSell - total;
            if (needed < 1) {
                break;
            }
            int trade = Math.min(needed,purchaseMap.get(d).getQuantity());
            m.put(d,trade);
            total += trade;
        }
        return m;
    }

    private Map<LocalDate,Integer> chooseStockToSell(Account account, int stockSharesToSell, LocalDate presentDay) {
        return chooseThingsToSell(account, stockSharesToSell, presentDay, Asset.STOCK);
    }

    private InvestmentAllocation approximateCurrentAllocation(Account account, CurrentPrices currentPrices) {
        double s = account.getCurrentSharesStock() * currentPrices.getStockPriceDollars();
        double b = account.getCurrentSharesBonds() * currentPrices.getBondPriceDollars();
        double c = account.getCurrentCents() / 100.0;
        double total = s + b + c;
        int pctStock = (int) Math.round(100 * s / total);
        int pctBond = (int) Math.round(100 * b / total);
        int pctMoney = 100 - pctBond - pctStock;

        return new InvestmentAllocation(pctStock,pctBond,pctMoney);
    }

    public abstract int getMinimumPercentChange();
}
