package knapp.simulation.strategy;

import knapp.simulation.*;
import knapp.table.Table;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AllocationStrategy implements InvestmentStrategy {


    public abstract InvestmentAllocation chooseAllocation(LocalDate presentDay, Account account, Table inputs,
                                                   Table stockMarket, Table bondMarket, CurrentPrices currentPrices,
                                                  InvestmentAllocation current);

    private boolean verbose = false;

    public void setVerbose(boolean x) {
        this.verbose = x;
    }

    @Override
    public final Set<Order> rebalance(LocalDate presentDay, Account account, Table inputs, Table stockMarket, Table bondMarket, CurrentPrices currentPrices) {
        InvestmentAllocation current = approximateCurrentAllocation(account,currentPrices);
        InvestmentAllocation desired = chooseAllocation(presentDay,account,inputs,stockMarket,bondMarket,currentPrices, current);
        if (verbose) {
            System.out.println(String.format("Desired levels on %s is %d stock, %d bonds, %d cash", presentDay.toString(), desired.getPercentStock(),
                    desired.getPercentBond(), desired.getPercentCash()));
        }
        if (desired == null) {
            // this is an easy way that child classes can say to not trade anything.
            return Collections.emptySet();
        }


        if (Math.abs(current.getPercentBond() - desired.getPercentBond()) < getMinimumPercentChange() &&
                Math.abs(current.getPercentStock() - desired.getPercentStock()) < getMinimumPercentChange()) {
            // we must meet a minimum threshold for change.
            return Collections.emptySet();
        }

        USDollars liquidValue = account.netValue(currentPrices,presentDay);

        double moreStockPercent = (desired.getPercentStock() - current.getPercentStock())/100.0;
        double moreBondPercent = (desired.getPercentBond() - current.getPercentBond())/100.0;

        USDollars moreStockDollars = liquidValue.times(moreStockPercent);
        USDollars moreBondDollars = liquidValue.times(moreBondPercent);

        Account simAccount = account;
        Set<Order> orders = new HashSet<>();
        // focus on sales first so you have more money available to buy things.
        updateOrders(presentDay, account, currentPrices, moreStockDollars, moreBondDollars, simAccount, orders);
        return orders;
    }

    public static void updateOrders(LocalDate presentDay, Account account, CurrentPrices currentPrices,
                                    USDollars moreStockDollars, USDollars moreBondDollars,
                                    Account simAccount, Set<Order> orders) {
        simAccount = deriveSellOrders(presentDay,currentPrices,moreBondDollars,Asset.BONDS,simAccount,orders);
        simAccount = deriveSellOrders(presentDay,currentPrices,moreStockDollars,Asset.STOCK,simAccount,orders);
        simAccount = derivePurchaseOrders(presentDay,currentPrices,moreStockDollars,Asset.STOCK,simAccount,orders);
        derivePurchaseOrders(presentDay,currentPrices,moreBondDollars,Asset.BONDS,simAccount,orders);
    }

    public static Account deriveSellOrders(LocalDate presentDay, CurrentPrices currentPrices,
                                          USDollars dollars, Asset asset, Account simAccount, Set<Order> orders) {
        if (dollars.isDebt()) {
            // you want to sell an asset.
            int sharesToSell = (int) Math.floor(- dollars.dividedBy(currentPrices.getPrice(asset)));
            if (sharesToSell > 0) {
                Map<LocalDate,Integer> toSell = chooseThingsToSell(simAccount,sharesToSell, presentDay, asset);
                for (LocalDate d : toSell.keySet()) {
                    int q = toSell.get(d);
                    Order order = Order.SellAsset(q,d, asset);
                    orders.add(order);
                    simAccount = simAccount.executeOrder(order,currentPrices,presentDay);
                }
            }
        }
        return simAccount;
    }

    public static Account derivePurchaseOrders(LocalDate presentDay, CurrentPrices currentPrices,
                                              USDollars dollars, Asset asset, Account simAccount, Set<Order> orders) {
        if (!dollars.isDebt()) {
            // must worry about the maximum amount of money you have.
            USDollars dollarsAvailable = simAccount.getCurrentCash().minus(simAccount.getTradeFee());
            USDollars availableMoreDollars = dollarsAvailable.isLessThan(dollars) ? dollarsAvailable :
                    dollars;
            int sharesToBuy = (int) Math.floor(availableMoreDollars.dividedBy(currentPrices.getPrice(asset)));
            if (sharesToBuy > 0) {
                Order order = Order.BuyAsset(sharesToBuy, asset);
                orders.add(order);
                simAccount = simAccount.executeOrder(order, currentPrices,presentDay);
            }
        }
        return simAccount;
    }

    public static Map<LocalDate,Integer> chooseBondsToSell(Account account, int bondSharesToSell, LocalDate presentDay) {
        return chooseThingsToSell(account, bondSharesToSell, presentDay, Asset.BONDS);
    }

    public static Map<LocalDate,Integer> chooseThingsToSell(Account account, int sharesToSell, LocalDate presentDay, Asset asset) {
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
            int trade = Math.min(needed,purchaseMap.get(d).getCurrentQuantity());
            m.put(d,trade);
            total += trade;
        }
        for (LocalDate d : newThings) {
            int needed = sharesToSell - total;
            if (needed < 1) {
                break;
            }
            int trade = Math.min(needed,purchaseMap.get(d).getCurrentQuantity());
            m.put(d,trade);
            total += trade;
        }
        return m;
    }

    public static Map<LocalDate,Integer> chooseStockToSell(Account account, int stockSharesToSell, LocalDate presentDay) {
        return chooseThingsToSell(account, stockSharesToSell, presentDay, Asset.STOCK);
    }

    public static InvestmentAllocation approximateCurrentAllocation(Account account, CurrentPrices currentPrices) {
        USDollars s = currentPrices.getStockPrice().times(account.getCurrentSharesStock());
        USDollars b = currentPrices.getBondPrice().times(account.getCurrentSharesBonds());
        USDollars c = account.getCurrentCash();
        USDollars total = s.plus(b).plus(c);
        int pctStock = (int) Math.round(100.0 * s.dividedBy(total));
        int pctBond = (int) Math.round(100.0 * b.dividedBy(total));
        int pctMoney = 100 - pctBond - pctStock;

        return new InvestmentAllocation(pctStock,pctBond,pctMoney);
    }

    public abstract int getMinimumPercentChange();
}
