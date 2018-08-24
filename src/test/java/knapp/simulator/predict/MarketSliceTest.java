package knapp.simulator.predict;

import knapp.predict.MarketSlice;
import knapp.simulator.function.NormalTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MarketSliceTest {

    @Test
    public void testIt() {
        Map<String,Double> values = new HashMap<>();
        values.put("A",1.5);
        values.put("B",7.3);
        MarketSlice marketSlice = new MarketSlice(values);
        values.put("C",3.1);
        values.remove("B");
        values.put("A",9.0);
        NormalTest.assertDoubleEquals(1.5,marketSlice.getValue("A"));
        NormalTest.assertDoubleEquals(7.3,marketSlice.getValue("B"));
        Assert.assertTrue(marketSlice.contains("A"));
        Assert.assertTrue(marketSlice.contains("B"));
        Assert.assertFalse(marketSlice.contains("C"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDNE() {
        Map<String,Double> values = new HashMap<>();
        values.put("A",1.5);
        values.put("B",7.3);
        MarketSlice marketSlice = new MarketSlice(values);
        values.put("C",3.1);
        marketSlice.getValue("C");
    }

    @Test
    public void testGetValues() {
        Map<String,Double> values = new HashMap<>();
        values.put("A",1.5);
        values.put("B",7.3);
        values.put("C",89.2);
        values.put("D",35.9);
        MarketSlice marketSlice = new MarketSlice(values);
        double[] out = marketSlice.getValues(Arrays.asList("C","A","D"));
        NormalTest.assertDoubleEquals(89.2,out[0]);
        NormalTest.assertDoubleEquals(1.5, out[1]);
        NormalTest.assertDoubleEquals(35.9, out[2]);
        Assert.assertEquals(3,out.length);
    }
}
