package knapp.util;

import org.junit.Assert;
import org.junit.Test;

public class NormalDistUtilTest {

    @Test
    public void testIt() {
        double x = NormalDistUtil.calculatePercentile(100,100,1);
        assertDoubleEquals(0.5,x);

        x = NormalDistUtil.calculatePercentile(101,100,1);
        assertDoubleEquals(0.84134,x);
    }

    private void assertDoubleEquals(double exp, double actual) {
        Assert.assertTrue(Math.abs(exp - actual) < 1e-4);
    }
}
