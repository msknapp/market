package knapp.simulator;

import knapp.simulation.CurrentPrices;
import knapp.simulation.USDollars;
import knapp.simulator.function.NormalTest;
import org.junit.Test;

public class CurrentPricesTest {

    @Test
    public void testIt() {
        CurrentPrices currentPrices = new CurrentPrices(USDollars.dollars(87.5),USDollars.dollars(38.9));
        NormalTest.assertDoubleEquals(87.5,currentPrices.getStockPrice().getDollars());
        NormalTest.assertDoubleEquals(38.9,currentPrices.getBondPrice().getDollars());
    }
}
