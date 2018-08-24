package knapp.table.values;

import knapp.simulator.function.NormalTest;
import knapp.table.Table;
import knapp.table.util.TableParser;
import knapp.table.util.TableUtil;
import knapp.util.InputLoader;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;

public class LastValueGetterTest {

    @Test
    public void testIt() {
        Table table = InputLoader.loadTableFromClasspath("/market/IVE.csv");
        table = table.retainColumns(Collections.singleton("Adj Close"));
        TableColumnView view = table.getTableColumnView(0);
        Assert.assertTrue(view.containsDate(LocalDate.of(2018,7,16)));
        Assert.assertFalse(view.containsDate(LocalDate.of(2018,7,17)));
        Assert.assertFalse(view.containsDate(LocalDate.of(2018,7,22)));
        Assert.assertTrue(view.containsDate(LocalDate.of(2018,7,23)));
        Assert.assertEquals(LocalDate.of(2018,7,16),
                view.getDateBefore(LocalDate.of(2018,7,23)));
        LastValueGetter lastValueGetter = new LastValueGetter();
        NormalTest.assertDoubleEquals(114.019997,lastValueGetter.getValue(LocalDate.of(2018,7,23),view));
        NormalTest.assertDoubleEquals(112.379997,lastValueGetter.getValue(LocalDate.of(2018,7,22),view));
        NormalTest.assertDoubleEquals(112.379997,lastValueGetter.getValue(LocalDate.of(2018,7,17),view));
        NormalTest.assertDoubleEquals(112.379997,lastValueGetter.getValue(LocalDate.of(2018,7,16),view));

    }
}
