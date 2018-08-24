package knapp.simulator.predict;

import knapp.predict.MarketSlice;
import knapp.predict.ParameterInfo;
import knapp.predict.SimpleModel;
import knapp.simulator.function.NormalTest;
import org.apache.commons.math.stat.regression.OLSMultipleLinearRegression;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SimpleModelTest {

    @Test
    public void testIt() {
        double[] y = new double[] {1,2,3,4};
        double[][] x = new double[][] {
                {4},{3},{2},{1}
        };
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(y,x);

        SimpleModel simpleModel = new SimpleModel(regression,Arrays.asList("A"));

        Map<String,Double> input = new HashMap<>();
        input.put("A",0.0);
        MarketSlice marketSlice = new MarketSlice(input);
        double est = simpleModel.estimateValue(marketSlice);
        NormalTest.assertDoubleEquals(5,est);

        input.put("A",7.0);
        marketSlice = new MarketSlice(input);
        est = simpleModel.estimateValue(marketSlice);
        NormalTest.assertDoubleEquals(-2,est);

        Assert.assertTrue(Math.abs(simpleModel.getStandardDeviation()) < 1e-6);
        Assert.assertTrue(Math.abs(simpleModel.getRsquared()-1) < 1e-6);

        Assert.assertEquals(2,simpleModel.getParameters().size());
        ParameterInfo parameterInfo0 = simpleModel.getParameters().get(0);
        ParameterInfo parameterInfo1 = simpleModel.getParameters().get(1);

        Assert.assertEquals("INTERCEPT",parameterInfo0.getName());
        NormalTest.assertDoubleEquals(5,parameterInfo0.getValue());
        Assert.assertEquals("A",parameterInfo1.getName());
        NormalTest.assertDoubleEquals(-1,parameterInfo1.getValue());
        Assert.assertTrue(Math.abs(parameterInfo1.getStandardError()) < 1e-5);
        Assert.assertTrue(Math.abs(parameterInfo0.getStandardError()) < 1e-5);
    }
}
