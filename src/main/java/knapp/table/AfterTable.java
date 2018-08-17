package knapp.table;

import knapp.table.values.GetMethod;
import knapp.table.values.TableColumnView;
import knapp.table.wraps.AbstractWrappingTable;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class AfterTable extends AbstractWrappingTable {

    private final LocalDate minInclusive;

    private Map<Integer,TableColumnView> views = new HashMap<>();

    public AfterTable(Table core,LocalDate minInclusive) {
        super(core);
        this.minInclusive = minInclusive;
    }

    @Override
    public double getValue(LocalDate date, String column, GetMethod getMethod) {
        if (date.isBefore(minInclusive)) {
            throw new IllegalArgumentException("DNE");
        }
        return core.getValue(date,column,getMethod);
    }

    @Override
    public double getValue(LocalDate date, int column, GetMethod getMethod) {
        if (date.isBefore(minInclusive)) {
            throw new IllegalArgumentException("DNE");
        }
        return core.getValue(date,column,getMethod);
    }

    @Override
    public int getColumnCount() {
        return core.getColumnCount();
    }

    @Override
    public TableColumnView getTableColumnView(int column) {
        if (!views.containsKey(column)) {
            views.put(column,new DefaultTableColumnView(this, column, minInclusive, null));
        }
        return views.get(column);
    }

//    @Override
//    public LocalDate[] getAllDates() {
//        return new LocalDate[0];
//    }

    @Override
    public double[] getExactValues(LocalDate date) {
        if (date.isBefore(minInclusive)) {
            throw new IllegalArgumentException("DNE");
        }
        return core.getExactValues(date);
    }

//    @Override
//    public LocalDate getFirstDate() {
//        return core.getDateOnOrAfter(minInclusive);
//    }


}
