package knapp.table;

import knapp.history.Frequency;

import java.time.LocalDate;
import java.util.Set;

public class AfterTable implements Table {

    private final Table core;
    private final LocalDate minInclusive;

    public AfterTable(Table core,LocalDate minInclusive) {
        this.core = core;
        this.minInclusive = minInclusive;
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
        if (date.isBefore(minInclusive)) {
            throw new IllegalArgumentException("DNE");
        }
        return core.getValue(date,column,getMethod);
    }

    @Override
    public double getValue(LocalDate date, int column, TableImpl.GetMethod getMethod) {
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
    public LocalDate[] getAllDates() {
        return new LocalDate[0];
    }

    @Override
    public double[] getExactValues(LocalDate date) {
        if (date.isBefore(minInclusive)) {
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
        return core.getLastDate();
    }

    @Override
    public LocalDate getFirstDate() {
        return core.getDateOnOrAfter(minInclusive);
    }

    @Override
    public Table untilExclusive(LocalDate date) {
        return new UntilTable(this,date);
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
        return null;
    }

    @Override
    public LocalDate getDateOnOrBefore(LocalDate date) {
        return null;
    }

    @Override
    public LocalDate getDateAfter(LocalDate date) {
        return null;
    }

    @Override
    public LocalDate getDateOnOrAfter(LocalDate date) {
        return null;
    }
}
