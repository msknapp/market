package knapp.table;

import knapp.history.Frequency;

import java.time.LocalDate;
import java.util.Set;

public class UntilTable implements Table {
    private Table core;
    private LocalDate maxDateExclusive;

    public UntilTable(Table core, LocalDate max) {
        this.core = core;
        this.maxDateExclusive = max;
    }

    @Override
    public String getName() {
        return core.getName();
    }

    @Override
    public void setName(String name) {
        core.setName(name);
    }

    @Override
    public String getColumn(int i) {
        return core.getColumn(i);
    }

    @Override
    public int getColumn(String name) {
        return core.getColumn(name);
    }

    @Override
    public double getValue(LocalDate date, String column, TableImpl.GetMethod getMethod) {
        if (date.equals(maxDateExclusive) || date.isAfter(maxDateExclusive)) {
            throw new IllegalArgumentException("DNE");
        }
        return core.getValue(date,column,getMethod);
    }

    @Override
    public double getValue(LocalDate date, int column, TableImpl.GetMethod getMethod) {
        if (date.equals(maxDateExclusive) || date.isAfter(maxDateExclusive)) {
            throw new IllegalArgumentException("DNE");
        }
        return core.getValue(date,column,getMethod);
    }

    @Override
    public int getColumnCount() {
        return core.getColumnCount();
    }

    @Override
    public LocalDate[] getAllDates() {
        LocalDate[] d = core.getAllDates();
        int len = 0;
        for (int i = 0;i<d.length;i++) {
            if (!d[i].isBefore(maxDateExclusive)) {
                len = i;
                break;
            }
        }
        LocalDate[] x = new LocalDate[len];
        System.arraycopy(d,0,x,0,len);
        return x;
    }

    @Override
    public double[] getExactValues(LocalDate date) {
        if (date.equals(maxDateExclusive) || date.isAfter(maxDateExclusive)) {
            throw new IllegalArgumentException("DNE");
        }
        return core.getExactValues(date);
    }

    @Override
    public Frequency getFrequency() {
        return core.getFrequency();
    }

    @Override
    public Table withoutColumn(String column) {
        return new TableWithoutColumn(this,column);
    }

    @Override
    public Table withDerivedColumn(TableWithDerived.ValueDeriver valueDeriver) {
        return new TableWithDerived(this,valueDeriver);
    }

    @Override
    public Table withLogOf(String column) {
        return withDerivedColumn(new LogDeriver(column));
    }

    @Override
    public Table replaceColumnWithLog(String column) {
        return withDerivedColumn(new LogDeriver(column)).withoutColumn(column);
    }

    @Override
    public Table retainColumns(Set<String> columns) {
        return TableParser.retainColumns(this,columns);
    }

    @Override
    public LocalDate getLastDate() {
        return core.getDateBefore(maxDateExclusive);
    }

    @Override
    public LocalDate getFirstDate() {
        return core.getFirstDate();
    }

    @Override
    public Table untilExclusive(LocalDate date) {
        return new UntilTable(this, date);
    }

    @Override
    public Table onOrAfter(LocalDate date) {
        return null;
    }

    @Override
    public Table inTimeFrame(LocalDate startInclusive, LocalDate endExclusive) {
        return null;
    }

    @Override
    public LocalDate getDateBefore(LocalDate date) {
        if (!date.isBefore(maxDateExclusive)) {
            // the date is on or onOrAfter the max
            return core.getDateBefore(maxDateExclusive);
        } else {
            return core.getDateBefore(date);
        }
    }

    @Override
    public LocalDate getDateOnOrBefore(LocalDate date) {
        if (!date.isBefore(maxDateExclusive)) {
            // the date is on or onOrAfter the max
            return core.getDateOnOrBefore(maxDateExclusive);
        } else {
            return core.getDateOnOrBefore(date);
        }
    }

    @Override
    public LocalDate getDateAfter(LocalDate date) {
        if (!date.isBefore(maxDateExclusive)) {
            // the date is on or onOrAfter the max
            return null;
        } else {
            return core.getDateAfter(date);
        }
    }

    @Override
    public LocalDate getDateOnOrAfter(LocalDate date) {
        if (!date.isBefore(maxDateExclusive)) {
            // the date is on or onOrAfter the max
            return null;
        } else {
            return core.getDateOnOrAfter(date);
        }
    }
}
