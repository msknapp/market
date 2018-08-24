package knapp.table;

import knapp.predict.MarketSlice;
import knapp.table.derivation.LogDeriver;
import knapp.table.derivation.ValueDeriver;
import knapp.table.util.TableParser;
import knapp.table.values.*;
import knapp.table.wraps.TableWithDerived;
import knapp.table.wraps.TableWithGetMethod;
import knapp.table.wraps.TableWithoutColumn;

import java.time.LocalDate;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

public interface Table {

    String getName();
    void setName(String name);
    String getColumn(int i);
    int getColumn(String name);

    default double getValue(LocalDate date) {
        throw new UnsupportedOperationException("There is no default get method on this table.");
    }

    default double getValue(LocalDate date, int column) {
        throw new UnsupportedOperationException("There is no default get method on this table.");
    }

    default double getValue(LocalDate date, String column, GetMethod getMethod) {
        return getValue(date,getColumn(column), getMethod);
    }

    default double getValue(LocalDate date, int column, GetMethod getMethod) {
        TableValueGetter tableValueGetter = GetterFor.getterFor(getMethod);
        return getValue(date, column, tableValueGetter);
    }

    default double getValue(LocalDate date, int column, TableValueGetter getter) {
        TableColumnView view = getTableColumnView(column);
        return getter.getValue(date,view);
    }

    default TableColumnView getTableColumnView(int column) {
        return new DefaultTableColumnView(this, column);
    }

    default TableColumnView getTableColumnView(String column) {
        return getTableColumnView(getColumn(column));
    }

    double getExactValue(LocalDate date, int column);

    int getColumnCount();

    default List<LocalDate> getAllDates(DatePolicy datePolicy) {
        Set<LocalDate> dates = new HashSet<>();
        for (int i = 0;i < getColumnCount();i++) {
            if (dates.isEmpty() || datePolicy == DatePolicy.ANYMUSTHAVE) {
                dates.addAll(getAllDates(i));
            } else {
                dates.retainAll(getAllDates(i));
            }
        }
        List<LocalDate> x = new ArrayList<>(dates);
        Collections.sort(x);
        return x;
    }

    List<LocalDate> getAllDates(int column);

    default List<LocalDate> getAllDates(String column) {
        return getAllDates(getColumn(column));
    }

    double[] getExactValues(LocalDate date);

//    LocalDate getLastDate();
//    LocalDate getFirstDate();

    Frequency getFrequency();

    default Table withoutColumn(String column) {
        return new TableWithoutColumn(this,column);
    }

    default Table withDerivedColumn(ValueDeriver valueDeriver) {
        return new TableWithDerived(this, valueDeriver);
    }

    default Table withLogOf(String column) {
        return new TableWithDerived(this, new LogDeriver(column));
    }

    default Table replaceColumnWithLog(String column) {
        return new TableWithDerived(this, new LogDeriver(column)).withoutColumn(column);
    }

    default Table retainColumns(Set<String> columns) {
        return TableParser.retainColumns(this, columns);
    }

    Table untilExclusive(LocalDate date);
    Table onOrAfter(LocalDate date);
    Table inTimeFrame(LocalDate startInclusive, LocalDate endExclusive);
//    LocalDate getDateBefore(LocalDate date);
//    LocalDate getDateOnOrBefore(LocalDate date);
//    LocalDate getDateAfter(LocalDate date);
//    LocalDate getDateOnOrAfter(LocalDate date);

    default Table withGetMethod(GetMethod getMethod, boolean strict) {
        return new TableWithGetMethod(this, getMethod, strict);
    }

    default List<String> getColumns() {
        List<String> cols = new ArrayList<>(getColumnCount());
        for (int i = 0;i < getColumnCount();i++) {
            cols.add(getColumn(i));
        }
        return cols;
    }

//    @Deprecated
//    default MarketSlice getLastMarketSlice() {
//        if (hasAllValuesForAllDates()) {
//            return getLastMarketSlice(GetMethod.EXACT);
//        } else {
//            return getLastMarketSlice(GetMethod.EXTRAPOLATE);
//        }
////        double[]
////        if (hasAllValuesForAllDates()) {
////            return getMarketSlice(getLastDate(), getColumns());
////        } else {
////            return getMarketSlice(getLastDate(), getColumns(), GetMethod.EXTRAPOLATE);
////        }
//    }

//    default MarketSlice getLastMarketSlice(GetMethod getMethod) {
//        return getMarketSlice(getLastDate(),getColumns(), getMethod);
//    }
//
//    default MarketSlice getLastMarketSlice(List<String> columns) {
//        return getMarketSlice(getLastDate(),columns);
//    }

    default MarketSlice getMarketSlice(LocalDate date) {
        return getMarketSlice(date,getColumns());
    }

    default MarketSlice getMarketSlice(LocalDate date, List<String> inputs) {
        return getMarketSlice(date, inputs,GetMethod.EXACT);
    }

    default MarketSlice getPresentDayMarketSlice(Map<String,Integer> lags) {
        return getPresentDayMarketSlice(lags,true);
    }

    default MarketSlice getPresentDayMarketSlice(Map<String,Integer> lags, boolean extrapolate) {
        return getMarketSlice(LocalDate.now(),lags,extrapolate);
    }

    default MarketSlice getMarketSlice(LocalDate date, Map<String,Integer> lags) {
        return getMarketSlice(date,lags,true);
    }

    default MarketSlice getMarketSlice(LocalDate date, Map<String,Integer> lags, boolean extrapolate) {
        return getMarketSlice(date,getColumns(),lags, extrapolate);
    }

    default MarketSlice getMarketSlice(LocalDate date, List<String> inputs, Map<String,Integer> lags, boolean extrapolate) {
        Map<String,Double> values = new HashMap<>(inputs.size());
        for (String input : inputs) {
            TableValueGetter x = extrapolate ? new LagBasedExtrapolatedValuesGetter(lags.get(input)) :
                    new LagBasedLastValueGetter(lags.get(input));
            values.put(input,getValue(date,getColumn(input), x));
        }
        return new MarketSlice(values);
    }

    default MarketSlice getPresentDayMarketSlice(GetMethod getMethod) {
        return getMarketSlice(LocalDate.now(), getMethod);
    }

    default MarketSlice getMarketSlice(LocalDate date, GetMethod getMethod) {
        return getMarketSlice(date,getColumns(), getMethod);
    }

    default MarketSlice getMarketSlice(LocalDate date, List<String> inputs, GetMethod getMethod) {
        Map<String,Double> values = new HashMap<>(inputs.size());
        for (String input : inputs) {
            values.put(input,getValue(date,input, getMethod));
        }
        return new MarketSlice(values);
    }

    boolean isExact();

    default boolean hasAllValuesForAllDates() {
        return true;
    }

    default boolean containsColumn(String column) {
        for (int i = 0; i < getColumnCount(); i++) {
            if (getColumn(i).equals(column)) {
                return true;
            }
        }
        return false;
    }

    default LocalDate getLastDateOf(int column) {
        return getLastDateOf(getColumn(column));
    }

    default LocalDate getLastDateOf(String column) {
        return getTableColumnView(getColumn(column)).getLastDate();
    }

    default LocalDate getFirstDateOf(String column) {
        return getTableColumnView(getColumn(column)).getFirstDate();
    }

    default LocalDate getFirstDateOf(int column) {
        return getFirstDateOf(getColumn(column));
    }

    default Map<String,Integer> getLags(LocalDate presentDay) {
        Map<String,Integer> out = new HashMap<>();
        for (int i = 0;i < getColumnCount(); i++) {
            String nm = getColumn(i);
            LocalDate ld = getTableColumnView(i).getLastDate();
            int lag = (int) DAYS.between(ld,presentDay);
            if (lag < 0) {
                lag = 0;
            }
            out.put(nm,lag);
        }
        return out;
    }
}
