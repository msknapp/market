package knapp.simulator;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sun.org.apache.xpath.internal.operations.Or;
import knapp.simulation.*;
import knapp.simulator.function.NormalTest;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;

public class BasicAccountTest {

    @Test
    public void testBasics() {
        Account account = BasicAccount.createAccount(10000,0.2);
        Assert.assertEquals(USDollars.dollars(10000),account.getCurrentCash());
        Assert.assertEquals(0,account.getCurrentSharesStock());
        Assert.assertEquals(0,account.getCurrentSharesBonds());
        NormalTest.assertDoubleEquals(0.15,account.getLongTermTaxRate());
        NormalTest.assertDoubleEquals(0.20,account.getShortTermTaxRate());
        Assert.assertEquals(USDollars.dollars(7),account.getTradeFee());
        Assert.assertTrue(account.getOwnedBondShares().isEmpty());
        Assert.assertTrue(account.getOwnedStockShares().isEmpty());
        CurrentPrices currentPrices = new CurrentPrices(USDollars.dollars(100), USDollars.dollars(100));
        USDollars nv = account.netValue(currentPrices, LocalDate.of(2010,1,1));
        Assert.assertEquals(nv,USDollars.dollars(10000));
    }

    @Test
    public void testAddCash() {
        Account account = BasicAccount.createAccount(10000, 0.2);
        account = account.addCash(USDollars.dollars(8));
        CurrentPrices currentPrices = new CurrentPrices(USDollars.dollars(100), USDollars.dollars(100));
        LocalDate firstDay = LocalDate.of(2000,1,1);
        USDollars nv = account.netValue(currentPrices,firstDay);
        Assert.assertEquals(USDollars.dollars(10008),nv);
    }

    @Test
    public void testExecuting() {
        Account account = BasicAccount.createAccount(10000,0.2);
        LocalDate firstDay = LocalDate.of(2000,1,1);
        CurrentPrices currentPrices = new CurrentPrices(USDollars.dollars(100), USDollars.dollars(100));
        Order order = Order.BuyStock(50);
        account = account.executeOrder(order,currentPrices,firstDay);
        Assert.assertTrue(account.getOwnedBondShares().isEmpty());
        Assert.assertEquals(1,account.getOwnedStockShares().size());

        PurchaseInfo firstPurchaseInfo = account.getOwnedStockShares().get(firstDay);
        Assert.assertEquals(50,firstPurchaseInfo.getCurrentQuantity());

        Assert.assertEquals(USDollars.dollars(10000-7-50*100),account.getCurrentCash());
        Assert.assertEquals(50,account.getCurrentSharesStock());
        Assert.assertEquals(0,account.getCurrentSharesBonds());

        USDollars nv = account.netValue(currentPrices,firstDay);
        Assert.assertEquals(USDollars.dollars(9988.80),nv);

        order = Order.BuyBonds(40);
        account = account.executeOrder(order,currentPrices,firstDay);

        Assert.assertEquals(USDollars.dollars(1000-7*2),account.getCurrentCash());
        Assert.assertEquals(50,account.getCurrentSharesStock());
        Assert.assertEquals(40,account.getCurrentSharesBonds());

        nv = account.netValue(currentPrices,firstDay);
        Assert.assertEquals(USDollars.dollars(9977.60),nv);

        BasicAccount basicAccount = (BasicAccount) account;
        LocalDate oneMonthLater = firstDay.plusMonths(1);
        USDollars netGain = basicAccount.calculateNetGainToClosePosition(oneMonthLater,firstPurchaseInfo,USDollars.dollars(160));

        Assert.assertEquals(USDollars.dollars(7395.80),netGain);

        CurrentPrices oneMonthLaterPrices = new CurrentPrices(USDollars.dollars(120), USDollars.dollars(100));
        order = Order.SellStock(15,firstDay);
        account = account.executeOrder(order,oneMonthLaterPrices,oneMonthLater);
        Assert.assertEquals(35,account.getCurrentSharesStock());
        Assert.assertEquals(40,account.getCurrentSharesBonds());
        Assert.assertEquals(USDollars.dollars(2720.82),account.getCurrentCash());

        LocalDate twoYearsLater = firstDay.plusYears(2);
        CurrentPrices twoYearLaterPrices = new CurrentPrices(USDollars.dollars(200), USDollars.dollars(115));
        order = Order.SellStock(35,firstDay);
        account = account.executeOrder(order,twoYearLaterPrices,twoYearsLater);
        order = Order.SellBonds(40,firstDay);
        account = account.executeOrder(order,twoYearLaterPrices,twoYearsLater);

        Assert.assertEquals(0,account.getCurrentSharesStock());
        Assert.assertEquals(0,account.getCurrentSharesBonds());
        Assert.assertEquals(USDollars.dollars(13695.70),account.getCurrentCash());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTrickingIt() {
        Account account = BasicAccount.createAccount(10000, 0.2);
        LocalDate date = LocalDate.of(2000,1,1);
        account.getOwnedBondShares().put(date,new PurchaseInfo(100,100,USDollars.dollars(100),date,Asset.BONDS));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTrickingIt2() {
        Account account = BasicAccount.createAccount(10000, 0.2);
        LocalDate date = LocalDate.of(2000,1,1);
        account.getOwnedStockShares().put(date,new PurchaseInfo(100,100,USDollars.dollars(100),date,Asset.STOCK));
    }

    @Test
    public void testOperations() {
        Account account = BasicAccount.createAccount(10000, 0.2);
        account = account.addCash(USDollars.dollars(30));
        Assert.assertEquals(USDollars.dollars(10030),account.getCurrentCash());
        Order order = Order.BuyStock(100);
        CurrentPrices currentPrices = new CurrentPrices(USDollars.dollars(95),USDollars.dollars(30));
        LocalDate present = LocalDate.of(2010,1,1);
        account = account.executeOrder(order,currentPrices,present);
        // pay the trade fee, and the cost of the stock.
        USDollars expected = USDollars.dollars(10030).minus(USDollars.dollars(7)).minus(USDollars.dollars(95).times(100));
        Assert.assertEquals(expected, account.getCurrentCash());
        Assert.assertTrue(account.getOwnedBondShares().isEmpty());
        Assert.assertEquals(1,account.getOwnedStockShares().size());
        PurchaseInfo purchaseInfo = account.getOwnedStockShares().get(present);
        Assert.assertEquals(100,purchaseInfo.getCurrentQuantity());
        NormalTest.assertDoubleEquals(95.0,purchaseInfo.getPriceDollars().getDollars());
        Assert.assertEquals(Asset.STOCK,purchaseInfo.getAsset());
    }
}
