package knapp.simulation.strategy;

import org.junit.Assert;
import org.junit.Test;

public class LineTest {

    @Test
    public void testLine() {
        Line line = Line.slope(1).intercept(1).toLine();
        assertDoublesEqual(-1,line.apply(-2.0));
        assertDoublesEqual(0,line.apply(-1.0));
        assertDoublesEqual(1,line.apply(0.0));
        assertDoublesEqual(2,line.apply(1.0));
        assertDoublesEqual(3,line.apply(2.0));

        Line line2 = Line.intercept(6).slope(-2).toLine();
        assertDoublesEqual(4,line2.apply(1.0));
        assertDoublesEqual(2,line2.apply(2.0));
        assertDoublesEqual(0,line2.apply(3.0));

    }

    public void assertDoublesEqual(double expected, double actual) {
        Assert.assertTrue(Math.abs(expected - actual) < 1e-4);
    }
}
