package knapp.simulator.function;

import knapp.simulation.functions.Normal;
import org.junit.Assert;
import org.junit.Test;

public class NormalTest {

    @Test
    public void testBuild() {
        Normal normal = (Normal) Normal.initialNormal();
        assertDoubleEquals(0,normal.getIntercept());
        assertDoubleEquals(1,normal.getSlope());

        normal = Normal.slope(0.5).intercept(0.7).toNormal();
        assertDoubleEquals(0.5,normal.getSlope());
        assertDoubleEquals(0.7,normal.getIntercept());
    }

    @Test
    public void testApply() {
        Normal normal = (Normal) Normal.initialNormal();
        double out = normal.apply(0.0);
        assertDoubleEquals(0.5,out);
        out = normal.apply(1.0);
        assertDoubleEquals(0.841344,out);
        out = normal.apply(2.0);
        assertDoubleEquals(0.97725,out);
        out = normal.apply(-1.0);
        assertDoubleEquals(1-0.841344,out);
        out = normal.apply(-2.0);
        assertDoubleEquals(1-0.97725,out);

        normal = Normal.slope(2).intercept(0.5).toNormal();
        out = normal.apply(0.0);
        assertDoubleEquals(2 * 0.5 + 0.5,out);
        out = normal.apply(1.0);
        assertDoubleEquals(2 * 0.841344 + 0.5,out);
    }

    @Test
    public void testRandom() {
        Normal normal = (Normal) Normal.initialNormal();
        normal = (Normal) normal.deviateRandomly(1.0);

        Assert.assertTrue(Math.abs(normal.getSlope() - 1.0) > 1e-7);
        Assert.assertTrue(Math.abs(normal.getIntercept()) > 1e-7);
    }

    public static void assertDoubleEquals(double exp, double act) {
        String msg = String.format("Expected %f, got %f",exp, act);
        assertDoubleEquals(exp,act,msg);
    }

    public static void assertDoubleEquals(double exp, double act, String msg) {
        Assert.assertTrue(msg,Math.abs(exp-act) < 1e-4);
    }
}
