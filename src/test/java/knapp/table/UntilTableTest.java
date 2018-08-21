package knapp.table;

import knapp.predict.MarketSlice;
import knapp.table.values.GetMethod;
import knapp.table.values.LagBasedLastValueGetter;
import knapp.table.values.TableColumnView;
import knapp.util.InputLoader;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

public class UntilTableTest {

    @Test
    public void testUntil() {
        Table t0 = InputLoader.loadTableFromClasspath("/best-inputs/M1SL.csv");
        Table t1 = InputLoader.loadTableFromClasspath("/good-inputs/TASACBW027SBOG.csv");
        Table joined = UnevenTable.from(Arrays.asList(t0,t1));
        LocalDate simPresentDate = LocalDate.of(2018,8,1);
        Map<String,Integer> lags = joined.getLags(simPresentDate);

        Table trimmed = joined.untilExclusive(LocalDate.of(2010,1,1));
        Assert.assertEquals(1,trimmed.getColumn("TASACBW027SBOG"));
        TableColumnView view0 = trimmed.getTableColumnView(0);
        TableColumnView view1 = trimmed.getTableColumnView(1);
        Assert.assertEquals(LocalDate.of(2009,12,1),view0.getLastDate());
        Assert.assertEquals(LocalDate.of(2009,12,30),view1.getLastDate());
        Assert.assertEquals(LocalDate.of(1959,1,1),view0.getFirstDate());
        Assert.assertEquals(LocalDate.of(1973,1,3),view1.getFirstDate());

        Assert.assertNull(view0.getDateAfter(LocalDate.of(2009,12,2)));
        Assert.assertNull(view1.getDateAfter(LocalDate.of(2009,12,30)));

        Assert.assertEquals(LocalDate.of(2009,12,1),view0.getDateOnOrBefore(LocalDate.of(2010,1,1)));

        LagBasedLastValueGetter gtr = new LagBasedLastValueGetter(lags.get("M1SL"));
        double d = trimmed.getValue(LocalDate.of(2010,9,1),0,gtr);
        UnevenTableTest.assertDoubleEquals(1694.1, d, "");
        Map<String,Integer> artificialLags = trimmed.getLags(LocalDate.of(2010,3,1));
        Assert.assertEquals(90, artificialLags.get("M1SL").intValue());
        Assert.assertEquals(61, artificialLags.get("TASACBW027SBOG").intValue());

        MarketSlice marketSlice = trimmed.getMarketSlice(LocalDate.of(2011,1,1),GetMethod.LAST_KNOWN_VALUE);
        UnevenTableTest.assertDoubleEquals(1694.1,marketSlice.getValue("M1SL"),"");
        UnevenTableTest.assertDoubleEquals(1460.0755,marketSlice.getValue("TASACBW027SBOG"),"");
    }

    @Test
    public void testCheatingUntil() {
        Table t0 = InputLoader.loadTableFromClasspath("/best-inputs/M1SL.csv");
        Table t1 = InputLoader.loadTableFromClasspath("/good-inputs/TASACBW027SBOG.csv");
        Table joined = UnevenTable.from(Arrays.asList(t0,t1));
        LocalDate simPresentDate = LocalDate.of(2018,8,1);
        Map<String,Integer> lags = joined.getLags(simPresentDate);

        Table trimmed = joined.untilExclusive(LocalDate.of(2010,1,1));
        TableColumnView view0 = trimmed.getTableColumnView(0);
        TableColumnView view1 = trimmed.getTableColumnView(1);
        try {
            double d = view0.getExactValue(LocalDate.of(2010, 1, 1));
            Assert.fail();
        } catch (Exception e) {}
        try {
            double d = view1.getExactValue(LocalDate.of(2010, 1, 1));
            Assert.fail();
        } catch (Exception e) {}
        try {
            double d = view1.getExactValue(LocalDate.of(2010, 1, 6));
            Assert.fail();
        } catch (Exception e) {}
        try {
            double d = view1.getExactValue(LocalDate.of(2010, 7,7));
            Assert.fail();
        } catch (Exception e) {}
        double d = trimmed.getValue(LocalDate.of(2010,2,1),0,GetMethod.EXTRAPOLATE);
        // the real value is 1702.5, but it should not get that.
        Assert.assertTrue(Math.abs(d - 1702.5) > 10);
    }
}
