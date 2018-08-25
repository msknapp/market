package knapp.simulator.strategy;

import knapp.simulation.*;
import knapp.simulation.strategy.AllocationStrategy;
import knapp.table.Table;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.*;

public class AllocationStrategyTest {

    @Test
    public void approximateCurrentAllocation() {
        Account account = BasicAccount.createAccount(10000,0.2);
        CurrentPrices currentPrices = new CurrentPrices(USDollars.dollars(90),USDollars.dollars(80));
        InvestmentAllocation investmentAllocation = AllocationStrategy.approximateCurrentAllocation(account,currentPrices);
        Assert.assertEquals(100,investmentAllocation.getPercentCash());
        Assert.assertEquals(0,investmentAllocation.getPercentStock());
        Assert.assertEquals(0,investmentAllocation.getPercentBond());

        Order order = Order.BuyStock(10);
        account = account.executeOrder(order,currentPrices,LocalDate.of(2000,1,1));
        order = Order.BuyBonds(30);
        account = account.executeOrder(order,currentPrices,LocalDate.of(2000,1,1));
        investmentAllocation = AllocationStrategy.approximateCurrentAllocation(account,currentPrices);
        double newTotalValue = 10000 - 2 * 7;

        Assert.assertEquals(67,investmentAllocation.getPercentCash());
        Assert.assertEquals(9,investmentAllocation.getPercentStock());
        Assert.assertEquals(24,investmentAllocation.getPercentBond());
    }

    @Test
    public void derivePurchaseOrders() {
        Account account = BasicAccount.createAccount(10000,0.2);
        CurrentPrices currentPrices = new CurrentPrices(USDollars.dollars(90),USDollars.dollars(80));
        LocalDate present = LocalDate.of(2000,1,1);
        Set<Order> orders = new HashSet<>();
        USDollars dollars = USDollars.dollars(4000);
        Account simAccount = AllocationStrategy.derivePurchaseOrders(present, currentPrices, dollars, Asset.STOCK,
                account, orders);
        Assert.assertFalse(orders.isEmpty());
        Assert.assertEquals(1,orders.size());
        Order o1 = orders.iterator().next();
        Assert.assertEquals(Asset.STOCK,o1.getAsset());
        Assert.assertTrue(o1.isPurchase());
        Assert.assertNull(o1.getDateSharesWerePurchased());
        Assert.assertEquals(44, o1.getQuantity());

        orders.clear();
        USDollars bondDollars = USDollars.dollars(5000);
        simAccount = AllocationStrategy.derivePurchaseOrders(present, currentPrices, bondDollars, Asset.BONDS,
                simAccount, orders);
        Assert.assertFalse(orders.isEmpty());
        Assert.assertEquals(1,orders.size());
        o1 = orders.iterator().next();
        Assert.assertEquals(Asset.BONDS,o1.getAsset());
        Assert.assertTrue(o1.isPurchase());
        Assert.assertNull(o1.getDateSharesWerePurchased());
        Assert.assertEquals(62, o1.getQuantity());

        // if we go on to try buying more stock, we are limited.
        orders.clear();
        AllocationStrategy.derivePurchaseOrders(present.plusDays(1), currentPrices, USDollars.dollars(3000), Asset.STOCK,
                simAccount, orders);
        // I don't keep the sim account here.
        Assert.assertFalse(orders.isEmpty());
        Assert.assertEquals(1,orders.size());
        o1 = orders.iterator().next();
        Assert.assertEquals(Asset.STOCK,o1.getAsset());
        Assert.assertTrue(o1.isPurchase());
        Assert.assertNull(o1.getDateSharesWerePurchased());
        // the quantity purchased was limited by how much cash they had remaining,
        // it is far short of the $3000 they requested.
        Assert.assertEquals(11, o1.getQuantity());

        orders.clear();
        AllocationStrategy.derivePurchaseOrders(present.plusDays(1), currentPrices, USDollars.dollars(3000), Asset.BONDS,
                simAccount, orders);
        // I don't keep the sim account here.
        Assert.assertFalse(orders.isEmpty());
        Assert.assertEquals(1,orders.size());
        o1 = orders.iterator().next();
        Assert.assertEquals(Asset.BONDS,o1.getAsset());
        Assert.assertTrue(o1.isPurchase());
        Assert.assertNull(o1.getDateSharesWerePurchased());
        // the quantity purchased was limited by how much cash they had remaining,
        // it is far short of the $3000 they requested.
        Assert.assertEquals(13, o1.getQuantity());
    }

    @Test
    public void deriveSellOrders() {
        Account account = BasicAccount.createAccount(10000,0.2);
        CurrentPrices firstPrices = new CurrentPrices(USDollars.dollars(90),USDollars.dollars(80));
        CurrentPrices laterPrices = new CurrentPrices(USDollars.dollars(110),USDollars.dollars(75));
        LocalDate present = LocalDate.of(2000,1,1);
        Set<Order> orders = new HashSet<>();
        Account simAccount = AllocationStrategy.derivePurchaseOrders(present, firstPrices, USDollars.dollars(4000), Asset.STOCK,
                account, orders);
        simAccount = AllocationStrategy.derivePurchaseOrders(present, firstPrices, USDollars.dollars(3000), Asset.BONDS,
                simAccount, orders);

        orders.clear();
        simAccount = AllocationStrategy.deriveSellOrders(present.plusMonths(11), laterPrices,
                USDollars.dollars(-1000),Asset.STOCK, simAccount, orders);
        Assert.assertEquals(1,orders.size());
        Order o1 = orders.iterator().next();
        Assert.assertEquals(Asset.STOCK,o1.getAsset());
        Assert.assertEquals(9,o1.getQuantity());
        Assert.assertEquals(present,o1.getDateSharesWerePurchased());
        Assert.assertFalse(o1.isPurchase());
        int remainingStock = simAccount.getCurrentSharesStock();
        Assert.assertEquals(35, remainingStock);

        orders.clear();
        simAccount = AllocationStrategy.deriveSellOrders(present.plusMonths(11), laterPrices,
                USDollars.dollars(-2000),Asset.BONDS, simAccount, orders);
        Assert.assertEquals(1,orders.size());
        o1 = orders.iterator().next();
        Assert.assertEquals(Asset.BONDS,o1.getAsset());
        Assert.assertEquals(26,o1.getQuantity());
        Assert.assertEquals(present,o1.getDateSharesWerePurchased());
        Assert.assertFalse(o1.isPurchase());

        orders.clear();
        simAccount = AllocationStrategy.deriveSellOrders(present.plusMonths(11), laterPrices,
                USDollars.dollars(-6000),Asset.STOCK, simAccount, orders);
        Assert.assertEquals(1,orders.size());
        o1 = orders.iterator().next();
        Assert.assertEquals(Asset.STOCK,o1.getAsset());
        Assert.assertEquals(remainingStock,o1.getQuantity());
        Assert.assertEquals(present,o1.getDateSharesWerePurchased());
        Assert.assertFalse(o1.isPurchase());
    }

    @Test
    public void rebalance() {
        MyTestStrat strat = new MyTestStrat();
        LocalDate present = LocalDate.of(2000,1,1);
        CurrentPrices firstPrices = new CurrentPrices(USDollars.dollars(90),USDollars.dollars(80));
        Account account = BasicAccount.createAccount(10000,0.2);

        Order order = Order.BuyStock(10);
        account = account.executeOrder(order,firstPrices,present.minusMonths(1));
        order = Order.BuyBonds(110);
        account = account.executeOrder(order,firstPrices,present.minusMonths(1));

        Set<Order> orders = strat.rebalance(present, account, null, null, null, firstPrices);
        Assert.assertEquals(2,orders.size());
        Map<AssetAndDate, Order> indexedOrders = indexOrders(orders);
        Order o1 = indexedOrders.get(new AssetAndDate(Asset.STOCK,null));
        Order o2 = indexedOrders.get(new AssetAndDate(Asset.BONDS,present.minusMonths(1)));
        Assert.assertEquals(56,o1.getQuantity());
        Assert.assertEquals(62,o2.getQuantity());

        account = Simulater.executeAllOrders(firstPrices,present,account, new ArrayList<>(),orders);
        InvestmentAllocation reached = AllocationStrategy.approximateCurrentAllocation(account, firstPrices);
        Assert.assertTrue(Math.abs(reached.getPercentStock()-60) <= 1);
        Assert.assertTrue(Math.abs(reached.getPercentBond()-38) <= 1);
        Assert.assertTrue(Math.abs(reached.getPercentCash()-2) <= 1);

        strat.response = new InvestmentAllocation(15,75, 10);

        CurrentPrices laterPrices = new CurrentPrices(USDollars.dollars(110),USDollars.dollars(75));
        orders = strat.rebalance(present.plusMonths(6), account, null, null, null, laterPrices);

        account = Simulater.executeAllOrders(laterPrices, present.plusMonths(6), account, new ArrayList<>(), orders);
        reached = AllocationStrategy.approximateCurrentAllocation(account, laterPrices);
        Assert.assertTrue(Math.abs(reached.getPercentStock()-15) <= 1);
        Assert.assertTrue(Math.abs(reached.getPercentBond()-75) <= 1);
        Assert.assertTrue(Math.abs(reached.getPercentCash()-10) <= 1);
    }

    public static Map<AssetAndDate,Order> indexOrders(Set<Order> orders) {
        Map<AssetAndDate, Order> out = new HashMap<>();
        for (Order o : orders) {
            AssetAndDate x = new AssetAndDate(o.getAsset(), o.getDateSharesWerePurchased());
            out.put(x, o);
        }
        return out;
    }

    public static class MyTestStrat extends AllocationStrategy {

        public InvestmentAllocation response = new InvestmentAllocation(60,38,2);

        @Override
        public InvestmentAllocation chooseAllocation(LocalDate presentDay, Account account, Table inputs, Table stockMarket, Table bondMarket, CurrentPrices currentPrices, InvestmentAllocation current) {
            return response;
        }

        @Override
        public int getMinimumPercentChange() {
            return 5;
        }

        @Override
        public boolean canEvolve() {
            return false;
        }
    }

    private static class AssetAndDate {
        private final Asset asset;
        private final LocalDate date;

        public AssetAndDate(Asset asset, LocalDate date) {
            this.asset = asset;
            this.date = date;
        }

        public Asset getAsset() {
            return asset;
        }

        public LocalDate getDate() {
            return date;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AssetAndDate that = (AssetAndDate) o;
            return asset == that.asset &&
                    Objects.equals(date, that.date);
        }

        @Override
        public int hashCode() {
            return Objects.hash(asset, date);
        }
    }
}
