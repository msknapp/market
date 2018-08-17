package knapp.table.wraps;

import knapp.predict.MarketSlice;
import knapp.table.Table;
import knapp.table.values.GetMethod;
import knapp.table.values.GetterFor;
import knapp.table.values.TableColumnView;
import knapp.table.values.TableValueGetter;

import java.time.LocalDate;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

public class ConsistentLagTable extends AbstractWrappingTable {

    private final LocalDate presentDay;

    private Map<Integer, ConsistentLagView> cache = new HashMap<>();

    private LocalDate[] allDates;

    public ConsistentLagTable(Table core, LocalDate presentDay) {
        super(core);
        this.presentDay = presentDay;
    }

    public TableColumnView getTableColumnView(int column) {
        if (!cache.containsKey(column)) {
            // cache this because its creation is expensive and it will be called often.
            int lagDays = (int) DAYS.between(core.getLastDateOf(core.getColumn(column)), LocalDate.now());
            cache.put(column,new ConsistentLagView(core.getTableColumnView(column),presentDay, lagDays));
        }
        return cache.get(column);
    }

    public static class ConsistentLagView implements TableColumnView {
        private final TableColumnView core;
        private final LocalDate presentDay;
        private final int lagDays;

        private SortedSet<LocalDate> dates;

        public ConsistentLagView(TableColumnView core, LocalDate presentDay, int lagDays) {
            this.core = core;
            this.presentDay = presentDay;
            this.dates = new TreeSet<>();
            this.lagDays = lagDays;
            LocalDate maxDateInclusive = presentDay.minusDays(lagDays);
            for (LocalDate date : core.getAllDates()) {
                if (!maxDateInclusive.isAfter(date)) {
                    this.dates.add(date);
                }
            }
        }

        @Override
        public List<LocalDate> getAllDates() {
            return new ArrayList<>(dates);
        }

        @Override
        public double getExactValue(LocalDate date) {
            if (containsDate(date)) {
                return core.getExactValue(date);
            }
            throw new IllegalArgumentException("That date DNE in this data");
        }

        @Override
        public LocalDate getLastDate() {
            return dates.last();
        }

        @Override
        public LocalDate getFirstDate() {
            return dates.first();
        }

        @Override
        public LocalDate getDateBefore(LocalDate date) {
            return dates.headSet(date).last();
        }

        @Override
        public LocalDate getDateOnOrBefore(LocalDate date) {
            if (dates.contains(date)) {
                return date;
            }
            return getDateBefore(date);
        }

        @Override
        public LocalDate getDateAfter(LocalDate date) {
            return dates.tailSet(date).first();
        }

        @Override
        public LocalDate getDateOnOrAfter(LocalDate date) {
            if (dates.contains(date)) {
                return date;
            }
            return getDateAfter(date);
        }

        @Override
        public boolean containsDate(LocalDate date) {
            return dates.contains(date);
        }
    }

    public double getValue(LocalDate date) {
        throw new UnsupportedOperationException("There is no default get method on this table.");
    }

    public double getValue(LocalDate date, int column) {
        throw new UnsupportedOperationException("There is no default get method on this table.");
    }

    public double getValue(LocalDate date, String column, GetMethod getMethod) {
        return getValue(date,getColumn(column), getMethod);
    }

    public double getValue(LocalDate date, int column, GetMethod getMethod) {
        TableValueGetter tableValueGetter = GetterFor.getterFor(getMethod);
        return getValue(date, column, tableValueGetter);
    }

    public double getValue(LocalDate date, int column, TableValueGetter getter) {
        TableColumnView view = getTableColumnView(column);
        return getter.getValue(date,view);
    }

    public double getExactValue(LocalDate date, int column) {
        return getTableColumnView(column).getExactValue(date);
    }

//    public LocalDate[] getAllDates() {
//
//    }
//    public double[] getExactValues(LocalDate date) {
//
//    }

//    public LocalDate getLastDate() {
//        return getAllDates()[getAllDates().length - 1];
//    }



//    LocalDate getDateBefore(LocalDate date);
//    LocalDate getDateOnOrBefore(LocalDate date);
//    LocalDate getDateAfter(LocalDate date);
//    LocalDate getDateOnOrAfter(LocalDate date);

    public Table withGetMethod(GetMethod getMethod, boolean strict) {
        return new TableWithGetMethod(this, getMethod, strict);
    }

    public List<String> getColumns() {
        List<String> cols = new ArrayList<>(getColumnCount());
        for (int i = 0;i < getColumnCount();i++) {
            cols.add(getColumn(i));
        }
        return cols;
    }

//    public MarketSlice getLastMarketSlice() {
//        if (hasAllValuesForAllDates()) {
//            return getMarketSlice(getLastDate(), getColumns());
//        } else {
//            return getMarketSlice(getLastDate(), getColumns(), GetMethod.EXTRAPOLATE);
//        }
//    }
//
//    public MarketSlice getLastMarketSlice(GetMethod getMethod) {
//        return getMarketSlice(getLastDate(),getColumns(), getMethod);
//    }
//
//    public MarketSlice getLastMarketSlice(List<String> columns) {
//        return getMarketSlice(getLastDate(),columns);
//    }

    public MarketSlice getMarketSlice(LocalDate date) {
        return getMarketSlice(date,getColumns());
    }

    public MarketSlice getMarketSlice(LocalDate date, List<String> inputs) {
        return getMarketSlice(date, inputs,GetMethod.EXACT);
    }

    public MarketSlice getMarketSlice(LocalDate date, List<String> inputs, GetMethod getMethod) {
        Map<String,Double> values = new HashMap<>(inputs.size());
        for (String input : inputs) {
            values.put(input,getValue(date,input, getMethod));
        }
        return new MarketSlice(values);
    }

    public LocalDate getLastDateOf(String column) {
        return getTableColumnView(getColumn(column)).getLastDate();
    }

    public LocalDate getFirstDateOf(String column) {
        return getTableColumnView(getColumn(column)).getFirstDate();
    }
}
