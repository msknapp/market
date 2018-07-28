package knapp.table;

import knapp.history.Frequency;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;

import static knapp.table.TableImpl.GetMethod.INTERPOLATE;

public class TableImplTest {
    private static String testTableText = "Date,Value\n2000-01-01,10\n2000-01-08,20\n2000-01-15,14\n2000-01-22,28";
    private static Table testTable = TableParser.parse(testTableText,true,Frequency.Weekly);

    @Test
    public void testBasics() {
        Assert.assertEquals(LocalDate.of(2000,1,1),testTable.getFirstDate());
        Assert.assertEquals(LocalDate.of(2000,1,22),testTable.getLastDate());
        Assert.assertEquals(1,testTable.getColumnCount());
        Assert.assertEquals(0,testTable.getColumn("Value"));
        Assert.assertEquals("Value",testTable.getColumn(0));
        Assert.assertNull(testTable.getName());
        testTable.setName("Test");
        Assert.assertEquals("Test",testTable.getName());
        LocalDate[] dates = testTable.getAllDates();
        Assert.assertEquals(4,dates.length);
        Assert.assertEquals(LocalDate.of(2000,1,1),dates[0]);
        Assert.assertEquals(LocalDate.of(2000,1,15),dates[2]);
        Assert.assertEquals(LocalDate.of(2000,1,22),dates[3]);
    }

    @Test
    public void testGetLastKnownValue() {
        TableImpl.GetMethod gm = TableImpl.GetMethod.LAST_KNOWN_VALUE;
        double v = testTable.getValue(LocalDate.of(2000,1,1),0,gm);
        assertDoubleEquals(10,v);
        v = testTable.getValue(LocalDate.of(2000,1,2),0,gm);
        assertDoubleEquals(10,v);
        v = testTable.getValue(LocalDate.of(2000,1,7),0,gm);
        assertDoubleEquals(10,v);
        v = testTable.getValue(LocalDate.of(2000,1,8),0,gm);
        assertDoubleEquals(20,v);
        v = testTable.getValue(LocalDate.of(2000,1,14),0,gm);
        assertDoubleEquals(20,v);
        v = testTable.getValue(LocalDate.of(2000,1,15),0,gm);
        assertDoubleEquals(14,v);
        v = testTable.getValue(LocalDate.of(2000,3,1),0,gm);
        assertDoubleEquals(28,v);
        v = testTable.getValue(LocalDate.of(1999,3,1),0,gm);
        assertDoubleEquals(0,v);
    }

    @Test
    public void testGetInterpolatedValue() {
        TableImpl.GetMethod gm = TableImpl.GetMethod.INTERPOLATE;
        double v = testTable.getValue(LocalDate.of(2000,1,1),0,gm);
        assertDoubleEquals(10,v);
        v = testTable.getValue(LocalDate.of(2000,1,2),0,gm);
        assertDoubleEquals(10.0+10.0/7.0,v);
        v = testTable.getValue(LocalDate.of(2000,1,7),0,gm);
        assertDoubleEquals(10.0+(6.0/7.0)*10.0,v);
        v = testTable.getValue(LocalDate.of(2000,1,8),0,gm);
        assertDoubleEquals(20,v);
        v = testTable.getValue(LocalDate.of(2000,1,14),0,gm);
        assertDoubleEquals(20.0-(6.0/7.0)*6.0,v);
        v = testTable.getValue(LocalDate.of(2000,1,15),0,gm);
        assertDoubleEquals(14,v);
        v = testTable.getValue(LocalDate.of(2000,1,22),0,gm);
        assertDoubleEquals(28,v);
        v = testTable.getValue(LocalDate.of(2000,1,23),0,gm);
        assertDoubleEquals(30,v);
        v = testTable.getValue(LocalDate.of(1999,12,31),0,gm);
        assertDoubleEquals(10.0-(1.0/7.0)*10.0,v);
    }

    @Test
    public void testGetExtrapolatedValue() {
        TableImpl.GetMethod gm = TableImpl.GetMethod.EXTRAPOLATE;
        double v = testTable.getValue(LocalDate.of(2000,1,1),0,gm);
        assertDoubleEquals(10,v);

        // between the first two values it will interpolate
        v = testTable.getValue(LocalDate.of(2000,1,2),0,gm);
        assertDoubleEquals(10.0+10.0/7.0,v);
        v = testTable.getValue(LocalDate.of(2000,1,7),0,gm);
        assertDoubleEquals(10.0+(6.0/7.0)*10.0,v);

        v = testTable.getValue(LocalDate.of(2000,1,8),0,gm);
        assertDoubleEquals(20,v);

        // after we have two values, it sticks with extrapolation.
        v = testTable.getValue(LocalDate.of(2000,1,14),0,gm);
        assertDoubleEquals(20.0 + (6.0/7.0) * 10.0,v);
        v = testTable.getValue(LocalDate.of(2000,1,15),0,gm);
        assertDoubleEquals(14,v);
        v = testTable.getValue(LocalDate.of(2000,1,22),0,gm);
        assertDoubleEquals(28,v);
        v = testTable.getValue(LocalDate.of(2000,1,23),0,gm);
        assertDoubleEquals(30,v);

        // it extrapolates values before the beginning.
        v = testTable.getValue(LocalDate.of(1999,12,31),0,gm);
        assertDoubleEquals(10.0-(1.0/7.0)*10.0,v);
    }

    @Test
    public void testGetExactValue() {
        TableImpl.GetMethod gm = TableImpl.GetMethod.EXACT;
        double v = testTable.getValue(LocalDate.of(2000,1,1),0,gm);
        assertDoubleEquals(10,v);
        v = testTable.getValue(LocalDate.of(2000,1,22),0,gm);
        assertDoubleEquals(28,v);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetExactMissingValue() {
        TableImpl.GetMethod gm = TableImpl.GetMethod.EXACT;
        testTable.getValue(LocalDate.of(2000,1,2),0,gm);
    }

    private void assertDoubleEquals(double expected, double actual) {
        String msg = String.format("Expected '%f' but got '%f'",expected, actual);
        Assert.assertTrue(msg,Math.abs(expected - actual) < 1e-4);
    }
}
