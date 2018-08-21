package knapp.table;

import knapp.predict.MarketSlice;
import knapp.table.values.GetMethod;
import knapp.table.values.LagBasedExtrapolatedValuesGetter;
import knapp.table.values.LagBasedLastValueGetter;
import knapp.table.values.TableColumnView;
import knapp.util.InputLoader;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UnevenTableTest {

    @Test
    public void testIt() {
        Table t = InputLoader.loadTableFromClasspath("/best-inputs/M1SL.csv");
        Table table = UnevenTable.from(Collections.singleton(t));
        Assert.assertEquals(LocalDate.of(2018,6,1),table.getLastDateOf(0));
        Assert.assertEquals(LocalDate.of(1959,1,1),table.getFirstDateOf(0));
        Assert.assertEquals(LocalDate.of(2018,6,1),table.getLastDateOf("M1SL"));
        Assert.assertEquals(LocalDate.of(1959,1,1),table.getFirstDateOf("M1SL"));
        MarketSlice ms = table.getPresentDayMarketSlice(GetMethod.LAST_KNOWN_VALUE);
        assertDoubleEquals(3657.9,ms.getValue("M1SL"),"market slice is not right.");
        Assert.assertEquals(1,table.getColumnCount());
        Assert.assertEquals(Arrays.asList("M1SL"),table.getColumns());
        Assert.assertTrue(table.isExact());
        Assert.assertTrue(table.hasAllValuesForAllDates());
        Map<String,Integer> lags = table.getLags(LocalDate.of(2018,7,1));
        Assert.assertEquals(1,lags.size());
        Assert.assertEquals(30,lags.get("M1SL").intValue());
        List<LocalDate> dts = table.getAllDates(0);
        Assert.assertEquals(714,dts.size());
        for (int i = 1;i<7;i++) {
            Assert.assertTrue(dts.get(i).isAfter(dts.get(i-1)));
        }
        // 1991-03-01,838.7
        assertDoubleEquals(838.7,table.getValue(LocalDate.of(1991,3,1),0,GetMethod.EXACT),"Getting wrong values");

        LagBasedExtrapolatedValuesGetter gtr = new LagBasedExtrapolatedValuesGetter(40);
        double expected = 1702.5 + 59 * (1702.5 - 1679.0)/31;
        double laggedValue = table.getValue(LocalDate.parse("2010-04-01"),0,gtr);
        // the actual value is 1699.6
        assertDoubleEquals(expected,laggedValue,"extrapolated lag values are not right.");

        LagBasedLastValueGetter g = new LagBasedLastValueGetter(40);
        assertDoubleEquals(1702.5,table.getValue(LocalDate.parse("2010-04-01"),0,g),
                "Getting a lagged last known value is not right.");

        assertDoubleEquals(1699.6,table.getExactValue(LocalDate.parse("2010-04-01"),0),
                "Doesn't get exact values right.");

        TableColumnView view = table.getTableColumnView(0);
        Assert.assertTrue(view.containsDate(LocalDate.of(1992,7,1)));
        Assert.assertFalse(view.containsDate(LocalDate.of(1992,7,9)));

        Assert.assertEquals(LocalDate.of(1992,1,1),view.getDateBefore(LocalDate.of(1992,1,7)));
        Assert.assertEquals(LocalDate.of(1992,2,1),view.getDateAfter(LocalDate.of(1992,1,7)));
        Assert.assertEquals(LocalDate.of(1992,8,1),view.getDateAfter(LocalDate.of(1992,7,1)));
        Assert.assertEquals(LocalDate.of(1992,6,1),view.getDateBefore(LocalDate.of(1992,7,1)));
        Assert.assertEquals(LocalDate.of(1992,7,1),view.getDateOnOrBefore(LocalDate.of(1992,7,1)));
        Assert.assertEquals(LocalDate.of(1992,7,1),view.getDateOnOrAfter(LocalDate.of(1992,7,1)));
        assertDoubleEquals(607.8,view.getExactValue(LocalDate.of(1985,10,1)),"The view doesn't get exact dates right.");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testNoMod() {
        Table t = InputLoader.loadTableFromClasspath("/best-inputs/M1SL.csv");
        Table table = UnevenTable.from(Collections.singleton(t));
        TableColumnView view = table.getTableColumnView(0);
        view.getAllDates().remove(8);
    }

    @Test
    public void testDifferentTables() {
        Table t0 = InputLoader.loadTableFromClasspath("/best-inputs/M1SL.csv");
        Table t1 = InputLoader.loadTableFromClasspath("/good-inputs/TASACBW027SBOG.csv");
        Table joined = UnevenTable.from(Arrays.asList(t0,t1));
        Assert.assertEquals(2,joined.getColumnCount());
        Assert.assertEquals(LocalDate.of(2018,7,25),joined.getLastDateOf("TASACBW027SBOG"));

        LocalDate simPresentDate = LocalDate.of(2018,8,1);
        Map<String,Integer> lags = joined.getLags(simPresentDate);

        Assert.assertEquals(61,lags.get("M1SL").intValue());
        Assert.assertEquals(7,lags.get("TASACBW027SBOG").intValue());
        List<LocalDate> dts1 = joined.getAllDates(DatePolicy.ANYMUSTHAVE);
        List<LocalDate> dts2 = joined.getAllDates(DatePolicy.ALLMUSTHAVE);
        Assert.assertTrue(dts2.size() < dts1.size());
        MarketSlice ms1 = joined.getPresentDayMarketSlice(GetMethod.LAST_KNOWN_VALUE);
        assertDoubleEquals(3657.9,ms1.getValue("M1SL"),"");
        assertDoubleEquals(2566.413,ms1.getValue("TASACBW027SBOG"),"");

        MarketSlice ms2 = joined.getMarketSlice(simPresentDate,lags,false);
        assertDoubleEquals(3657.9,ms2.getValue("M1SL"),"");
        assertDoubleEquals(2566.413,ms2.getValue("TASACBW027SBOG"),"");

        MarketSlice ms3 = joined.getMarketSlice(simPresentDate.minusDays(1),lags,false);
        assertDoubleEquals(3653.9,ms3.getValue("M1SL"),"");
        assertDoubleEquals(2561.2771,ms3.getValue("TASACBW027SBOG"),"");

        MarketSlice ms4 = joined.getMarketSlice(simPresentDate,lags);
        assertDoubleEquals(3665.77096774,ms4.getValue("M1SL"),"");
        assertDoubleEquals(2571.5489,ms4.getValue("TASACBW027SBOG"),"");
    }

    @Test
    public void testGetExactValues() {
        Table t0 = InputLoader.loadTableFromClasspath("/best-inputs/M1SL.csv");
        Table t1 = InputLoader.loadTableFromClasspath("/good-inputs/TASACBW027SBOG.csv");
        Table joined = UnevenTable.from(Arrays.asList(t0, t1));

        double[] ds = joined.getExactValues(LocalDate.of(2017,11,1));
        assertDoubleEquals(3606.5,ds[0],"");
        assertDoubleEquals(2489.8807,ds[1],"");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetExactValuesOfBadDate() {
        Table t0 = InputLoader.loadTableFromClasspath("/best-inputs/M1SL.csv");
        Table t1 = InputLoader.loadTableFromClasspath("/good-inputs/TASACBW027SBOG.csv");
        Table joined = UnevenTable.from(Arrays.asList(t0, t1));

        double[] ds = joined.getExactValues(LocalDate.of(2017,12,1));
    }

    public static void assertDoubleEquals(double exp, double act, String msg) {
        Assert.assertTrue(msg,Math.abs(exp-act) < 1e-4);
    }
}
