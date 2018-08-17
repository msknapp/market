package knapp.table.wraps;

import knapp.table.DefaultTableColumnView;
import knapp.table.UnevenTable;
import knapp.table.values.GetMethod;
import knapp.table.Table;
import knapp.table.values.TableColumnView;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class UntilTable extends AbstractWrappingTable {
    private LocalDate maxDateExclusive;

    private Map<Integer,TableColumnView> views = new HashMap<>();

    public UntilTable(Table core, LocalDate max) {
        super(core);
        this.maxDateExclusive = max;
    }

    @Override
    public double getValue(LocalDate date, String column, GetMethod getMethod) {
        if (date.equals(maxDateExclusive) || date.isAfter(maxDateExclusive)) {
            throw new IllegalArgumentException("DNE");
        }
        return core.getValue(date,column,getMethod);
    }

    @Override
    public double getValue(LocalDate date, int column, GetMethod getMethod) {
        if (date.equals(maxDateExclusive) || date.isAfter(maxDateExclusive)) {
            throw new IllegalArgumentException("DNE");
        }
        return core.getValue(date,column,getMethod);
    }

//    @Override
//    public LocalDate[] getAllDates() {
//        LocalDate[] d = core.getAllDates();
//        int len = 0;
//        for (int i = 0;i<d.length;i++) {
//            if (!d[i].isBefore(maxDateExclusive)) {
//                len = i;
//                break;
//            }
//        }
//        LocalDate[] x = new LocalDate[len];
//        System.arraycopy(d,0,x,0,len);
//        return x;
//    }

    @Override
    public double[] getExactValues(LocalDate date) {
        if (date.equals(maxDateExclusive) || date.isAfter(maxDateExclusive)) {
            throw new IllegalArgumentException("DNE");
        }
        return core.getExactValues(date);
    }

    @Override
    public TableColumnView getTableColumnView(int column) {
        if (!views.containsKey(column)) {
            views.put(column,new BeforeTableColumnView(core.getTableColumnView(column), maxDateExclusive));
        }
        return views.get(column);
    }

    public LocalDate getLastDateOf(int column) {
        if (core instanceof UnevenTable) {
            // this is a faster optimization
            return core.getTableColumnView(column).getDateBefore(maxDateExclusive);
        }
        return getLastDateOf(getColumn(column));
    }

    public LocalDate getFirstDateOf(int column) {
        return core.getTableColumnView(column).getFirstDate();
    }

//    @Override
//    public LocalDate getLastDate() {
//        LocalDate x = core.getDateBefore(maxDateExclusive);
//        if (x.equals(maxDateExclusive)) {
//            throw new IllegalStateException("The core table is flawed.");
//        }
//        return x;
//    }
//
//    @Override
//    public LocalDate getDateBefore(LocalDate date) {
//        if (!date.isBefore(maxDateExclusive)) {
//            // the date is on or onOrAfter the max
//            return core.getDateBefore(maxDateExclusive);
//        } else {
//            return core.getDateBefore(date);
//        }
//    }
//
//    @Override
//    public LocalDate getDateOnOrBefore(LocalDate date) {
//        if (!date.isBefore(maxDateExclusive)) {
//            // the date is on or onOrAfter the max
//            return core.getDateOnOrBefore(maxDateExclusive);
//        } else {
//            return core.getDateOnOrBefore(date);
//        }
//    }
//
//    @Override
//    public LocalDate getDateAfter(LocalDate date) {
//        if (!date.isBefore(maxDateExclusive)) {
//            // the date is on or onOrAfter the max
//            return null;
//        } else {
//            return core.getDateAfter(date);
//        }
//    }
//
//    @Override
//    public LocalDate getDateOnOrAfter(LocalDate date) {
//        if (!date.isBefore(maxDateExclusive)) {
//            // the date is on or onOrAfter the max
//            return null;
//        } else {
//            return core.getDateOnOrAfter(date);
//        }
//    }
}
