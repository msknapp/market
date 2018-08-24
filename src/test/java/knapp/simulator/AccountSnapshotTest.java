package knapp.simulator;

import knapp.simulation.AccountSnapshot;
import knapp.simulation.USDollars;
import knapp.simulator.function.NormalTest;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;

public class AccountSnapshotTest {

    @Test
    public void testIt() {
        AccountSnapshot accountSnapshot = new AccountSnapshot(
                LocalDate.of(2008,3,21),52,98,USDollars.cents(8751));
        Assert.assertEquals(52,accountSnapshot.getSharesOfStockMarket());
        Assert.assertEquals(98,accountSnapshot.getSharesOfBondMarket());
        Assert.assertEquals(8751,accountSnapshot.getValue().getTotalInCents());
        Assert.assertEquals(LocalDate.of(2008,3,21),accountSnapshot.getDate());
    }
}
