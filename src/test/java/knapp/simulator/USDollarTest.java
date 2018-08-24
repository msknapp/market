package knapp.simulator;

import knapp.simulation.USDollars;
import knapp.simulator.function.NormalTest;
import org.junit.Assert;
import org.junit.Test;

public class USDollarTest {

    @Test
    public void testIt() {
        USDollars x = USDollars.cents(149);
        Assert.assertEquals("$1.49",x.toString());
        NormalTest.assertDoubleEquals(1.49,x.getDollars());
        Assert.assertEquals(1,x.getDollarsInt());
        Assert.assertEquals(149,x.getTotalInCents());
        Assert.assertEquals(49,x.getRemainingCents());

        USDollars y = USDollars.dollars(32);
        Assert.assertEquals("$32.00",y.toString());
        NormalTest.assertDoubleEquals(32,y.getDollars());
        Assert.assertEquals(32,y.getDollarsInt());
        Assert.assertEquals(0,y.getRemainingCents());
        Assert.assertEquals(3200,y.getTotalInCents());

        NormalTest.assertDoubleEquals(32.0-1.49,y.minus(x).getDollars());
        NormalTest.assertDoubleEquals(32.0+1.49,y.plus(x).getDollars());
        NormalTest.assertDoubleEquals(32.0/1.49,y.dividedBy(x));

        Assert.assertEquals("$16.00",y.dividedBy(2).toString());
        Assert.assertEquals("$32.39",y.plusCents(39).toString());
        Assert.assertEquals("$31.61",y.minusCents(39).toString());
        Assert.assertEquals("$49.00",y.plusDollars(17).toString());
        Assert.assertEquals("$15.00",y.minusDollars(17).toString());
        Assert.assertEquals("$96.00",y.times(3).toString());
        NormalTest.assertDoubleEquals(32.0 * 0.84, y.times(0.84).getDollars());

        // in this case it must round to the nearest cent.
        NormalTest.assertDoubleEquals(38.10, y.dividedBy(0.84).getDollars());

        Assert.assertFalse(x.isDebt());
        Assert.assertFalse(y.isDebt());
        Assert.assertFalse(USDollars.dollars(0).isDebt());
        Assert.assertTrue(USDollars.cents(-1).isDebt());
        Assert.assertTrue(USDollars.dollars(-3).isDebt());

        Assert.assertTrue(y.isGreaterThan(x));
        Assert.assertTrue(y.isGreaterThanOrEqualTo(x));
        Assert.assertTrue(x.isLessThan(y));
        Assert.assertTrue(x.isLessThanOrEqualTo(y));
        Assert.assertTrue(y.isLessThanOrEqualTo(y));
        Assert.assertFalse(y.isLessThan(y));
        Assert.assertFalse(y.isGreaterThan(y));

    }
}
