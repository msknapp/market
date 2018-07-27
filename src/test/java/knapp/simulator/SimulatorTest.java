package knapp.simulator;

import knapp.simulation.Simulater;
import org.junit.Assert;
import org.junit.Test;

public class SimulatorTest {

    @Test
    public void averageROI() {
        double d = Simulater.calculateROI(11,10,1);
        Assert.assertTrue(Math.abs(d - .1) < 1e-3);

        double roi = 0.158;
        int years = 7;
        int initialFunds = 98523;
        int finalFunds = (int) (Math.pow( (1 + roi), years) * initialFunds);

        double calculatedRoi = Simulater.calculateROI(finalFunds,initialFunds,years);
        Assert.assertTrue(Math.abs(calculatedRoi - roi) < 1e-3);

    }
}
