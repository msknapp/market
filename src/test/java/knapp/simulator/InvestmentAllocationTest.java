package knapp.simulator;

import knapp.simulation.InvestmentAllocation;
import org.junit.Assert;
import org.junit.Test;

public class InvestmentAllocationTest {

    @Test
    public void testIt() {
        InvestmentAllocation investmentAllocation = new InvestmentAllocation(9,11,80);
        Assert.assertEquals(9,investmentAllocation.getPercentStock());
        Assert.assertEquals(11,investmentAllocation.getPercentBond());
        Assert.assertEquals(80,investmentAllocation.getPercentCash());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadArg() {
        new InvestmentAllocation(9,11,81);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadArg2() {
        new InvestmentAllocation(-1,2,99);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadArg3() {
        new InvestmentAllocation(105,0,-5);
    }
}
