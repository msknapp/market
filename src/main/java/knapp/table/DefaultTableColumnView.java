package knapp.table;

import knapp.table.values.TableColumnView;

import java.time.LocalDate;
import java.util.*;

public class DefaultTableColumnView implements TableColumnView {

    private final Table table;
    private final int column;
    private SortedSet<LocalDate> allDates;
    private List<LocalDate> allDatesList;

    public DefaultTableColumnView(Table core, int column) {
        this(core,column,null,null);
    }

    public DefaultTableColumnView(Table core, int column, LocalDate minInclusive, LocalDate maxExclusive) {
        this.table = core;
        this.column = column;
        List<LocalDate> tmp = new ArrayList<>();
        if (minInclusive == null && maxExclusive == null) {
            tmp.addAll(core.getAllDates(column));
        } else {
            for (LocalDate localDate : core.getAllDates(column)) {
                if (minInclusive != null && minInclusive.isAfter(localDate)) {
                    continue;
                }
                if (maxExclusive != null && !maxExclusive.isAfter(localDate)) {
                    continue;
                }
                tmp.add(localDate);
            }
        }
        Collections.sort(tmp);
        this.allDatesList = Collections.unmodifiableList(tmp);
        this.allDates = Collections.unmodifiableSortedSet(new TreeSet<>(tmp));
    }

    @Override
    public List<LocalDate> getAllDates() {
        return allDatesList;
    }

    @Override
    public double getExactValue(LocalDate date) {
        return table.getExactValue(date, column);
    }

    @Override
    public LocalDate getLastDate() {
        return allDates.last();
    }

    @Override
    public LocalDate getFirstDate() {
        return allDates.first();
    }

    @Override
    public LocalDate getDateBefore(LocalDate date) {
        SortedSet<LocalDate> t = allDates.headSet(date);
        if (t.isEmpty()) {
            return null;
        }
        return t.last();
    }

    @Override
    public LocalDate getDateOnOrBefore(LocalDate date) {
        if (allDates.contains(date)) {
            return date;
        }
        return getDateBefore(date);
    }

    @Override
    public LocalDate getDateAfter(LocalDate date) {
        return allDates.tailSet(date.plusDays(1)).first();
    }

    @Override
    public LocalDate getDateOnOrAfter(LocalDate date) {
        return allDates.tailSet(date).first();
    }

    @Override
    public boolean containsDate(LocalDate date) {
        return getAllDates().contains(date);
    }
}
