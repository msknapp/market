package knapp.simulator;

import knapp.simulation.Asset;
import knapp.simulation.Order;
import knapp.simulation.PurchaseInfo;
import knapp.simulation.USDollars;
import knapp.simulator.function.NormalTest;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;

public class PurchaseInfoTest {

    @Test
    public void testIt() {
        PurchaseInfo purchaseInfo = new PurchaseInfo(183,183,USDollars.dollars(32.4),LocalDate.of(2006,10,3),Asset.BONDS);
        Assert.assertEquals(183,purchaseInfo.getCurrentQuantity());
        Assert.assertEquals(Asset.BONDS,purchaseInfo.getAsset());
        NormalTest.assertDoubleEquals(32.4,purchaseInfo.getPriceDollars().getDollars());
        Assert.assertEquals(LocalDate.of(2006,10,3),purchaseInfo.getDateExchanged());

        purchaseInfo = purchaseInfo.lessQuantity(97);
        Assert.assertEquals(183-97,purchaseInfo.getCurrentQuantity());
        Assert.assertEquals(Asset.BONDS,purchaseInfo.getAsset());
        NormalTest.assertDoubleEquals(32.4,purchaseInfo.getPriceDollars().getDollars());
        Assert.assertEquals(LocalDate.of(2006,10,3),purchaseInfo.getDateExchanged());

        Order order = purchaseInfo.toSellAllOrder();
        Assert.assertFalse(order.isPurchase());
        Assert.assertEquals(183-97,order.getQuantity());
        Assert.assertEquals(Asset.BONDS,order.getAsset());
        Assert.assertEquals(LocalDate.of(2006,10,3),order.getDateSharesWerePurchased());
    }

    @Test
    public void testCostBasis() {
        LocalDate date = LocalDate.of(2010,1,1);
        PurchaseInfo purchaseInfo = new PurchaseInfo(100,100,USDollars.dollars(100),
                date,Asset.STOCK);
        USDollars tradeFee = USDollars.dollars(10);

        USDollars cb = purchaseInfo.getCostBasis(tradeFee,100);
        Assert.assertEquals(USDollars.dollars(10+100*100),cb);

        cb = purchaseInfo.getCostBasis(tradeFee,50);
        Assert.assertEquals(USDollars.dollars(5+50*100),cb);

        purchaseInfo = purchaseInfo.lessQuantity(50);
        cb = purchaseInfo.getCostBasis(tradeFee,30);
        Assert.assertEquals(USDollars.dollars(30*100 + 3),cb);
        cb = purchaseInfo.getCostBasis(tradeFee,50);
        Assert.assertEquals(USDollars.dollars(50*100 + 5),cb);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadArg() {
        new PurchaseInfo(-1,-1,USDollars.dollars(1000),LocalDate.now(),Asset.STOCK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadArg2() {
        new PurchaseInfo(1,1,USDollars.dollars(-90.0),LocalDate.now(),Asset.STOCK);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadArg3() {
        new PurchaseInfo(1,1,USDollars.dollars(90.0),LocalDate.now(),Asset.CASH);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadArg4() {
        new PurchaseInfo(1,1,USDollars.dollars(90.0),null,Asset.STOCK);
    }
}
