package knapp.simulator;

import knapp.simulation.Asset;
import knapp.simulation.Order;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;

public class OrderTest {

    @Test
    public void testIt() {
        Order order = Order.BuyStock(100);
        Assert.assertEquals(100,order.getQuantity());
        Assert.assertTrue(order.isPurchase());
        Assert.assertEquals(Asset.STOCK,order.getAsset());
        Assert.assertNull(order.getDateSharesWerePurchased());

        order = Order.BuyBonds(30);
        Assert.assertEquals(30, order.getQuantity());
        Assert.assertTrue(order.isPurchase());
        Assert.assertEquals(Asset.BONDS,order.getAsset());
        Assert.assertNull(order.getDateSharesWerePurchased());

        order = Order.SellStock(70,LocalDate.of(2000,1,1));
        Assert.assertEquals(70, order.getQuantity());
        Assert.assertFalse(order.isPurchase());
        Assert.assertEquals(Asset.STOCK,order.getAsset());
        Assert.assertEquals(LocalDate.of(2000,1,1),order.getDateSharesWerePurchased());

        order = Order.SellBonds(180,LocalDate.of(2005,6,11));
        Assert.assertEquals(180, order.getQuantity());
        Assert.assertFalse(order.isPurchase());
        Assert.assertEquals(Asset.BONDS,order.getAsset());
        Assert.assertEquals(LocalDate.of(2005,6,11),order.getDateSharesWerePurchased());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadQuantity() {
        Order order = Order.BuyStock(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadQuantity2() {
        Order order = Order.BuyBonds(-1);
    }

}
