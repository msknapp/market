package knapp.simulator;

import knapp.simulation.Transaction;
import knapp.simulation.USDollars;
import knapp.simulator.function.NormalTest;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;

public class TransactionTest {

    @Test
    public void testIt() {
        Transaction transaction = new Transaction(
                LocalDate.of(2013,1,5),293,USDollars.dollars(83.5),true);
        Assert.assertEquals(293,transaction.getQuantity());
        NormalTest.assertDoubleEquals(83.5,transaction.getPrice().getDollars());
        Assert.assertTrue(transaction.isPurchase());
        Assert.assertEquals(LocalDate.of(2013,1,5),transaction.getDate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadArg() {
        Transaction transaction = new Transaction(
                LocalDate.of(2013, 1, 5), -293, USDollars.dollars(83.5), true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadArg2() {
        Transaction transaction = new Transaction(
                LocalDate.of(2013, 1, 5), 293, USDollars.dollars(-83.5), true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadArg3() {
        Transaction transaction = new Transaction(null, 293, USDollars.dollars(83.5), true);
    }
}
