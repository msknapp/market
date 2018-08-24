package knapp.simulator;

import knapp.predict.ParameterInfo;
import knapp.simulator.function.NormalTest;
import org.junit.Assert;
import org.junit.Test;

public class ParameterInfoTest {

    @Test
    public void testIt() {
        ParameterInfo parameterInfo = new ParameterInfo("A",0.8,10);

        Assert.assertEquals("A",parameterInfo.getName());
        NormalTest.assertDoubleEquals(0.8,parameterInfo.getStandardError());
        NormalTest.assertDoubleEquals(10.0,parameterInfo.getValue());
    }
}
