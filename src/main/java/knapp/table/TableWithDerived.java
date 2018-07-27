package knapp.table;

import knapp.history.Frequency;

import java.time.LocalDate;
import java.util.Set;

public class TableWithDerived implements Table {

    private final Table core;
    private final ValueDeriver valueDeriver;

    public interface ValueDeriver {
        String getColumnName();
        double deriveValue(Table core, LocalDate date, TableImpl.GetMethod getMethod);
    }

    public TableWithDerived(Table core, ValueDeriver valueDeriver) {
        if (core == null) {
            throw new IllegalArgumentException("null core");
        }
        this.core = core;
        this.valueDeriver = valueDeriver;
    }

    public int getColumnCount() {
        return core.getColumnCount()+1;
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
        if (i == core.getColumnCount()) {
            return valueDeriver.getColumnName();
        }
        return core.getColumn(i);
    }

    @Override
    public int getColumn(String name) {
        if (valueDeriver.getColumnName().equals(name)) {
            return core.getColumnCount();
        } else {
            return core.getColumn(name);
        }
    }

    @Override
    public double getValue(LocalDate date, String column, TableImpl.GetMethod getMethod) {
        if (valueDeriver.getColumnName().equals(column)) {
            return valueDeriver.deriveValue(core,date,getMethod);
        } else {
            return core.getValue(date,column,getMethod);
        }
    }

    @Override
    public double getValue(LocalDate date, int column, TableImpl.GetMethod getMethod) {
        if (column == core.getColumnCount()) {
            return valueDeriver.deriveValue(core,date,getMethod);
        } else {
            return core.getValue(date,column,getMethod);
        }
    }

    @Override
    public LocalDate[] getAllDates() {
        return core.getAllDates();
    }

    @Override
    public double[] getExactValues(LocalDate date) {
        double[] x = core.getExactValues(date);
        double[] y = new double[x.length+1];
        System.arraycopy(x,0,y,0,x.length);
        y[y.length-1] = valueDeriver.deriveValue(core,date,TableImpl.GetMethod.LAST_KNOWN_VALUE);
        return y;
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
    public Table retainColumns(Set<String> columns) {
        return TableParser.retainColumns(this,columns);
    }

    @Override
    public LocalDate getLastDate() {
        return core.getLastDate();
    }

    @Override
    public LocalDate getFirstDate() {
        return core.getFirstDate();
    }

    @Override
    public Table untilExclusive(LocalDate date) {
        return new TableWithDerived(core.untilExclusive(date),valueDeriver);
    }

    @Override
    public Table onOrAfter(LocalDate date) {
        return new TableWithDerived(core.onOrAfter(date),valueDeriver);
    }

    @Override
    public Table inTimeFrame(LocalDate startInclusive, LocalDate endExclusive) {
        return new TableWithDerived(core.inTimeFrame(startInclusive,endExclusive),valueDeriver);
    }

    @Override
    public LocalDate getDateBefore(LocalDate date) {
        return core.getDateBefore(date);
    }

    @Override
    public LocalDate getDateOnOrBefore(LocalDate date) {
        return core.getDateOnOrBefore(date);
    }

    @Override
    public LocalDate getDateAfter(LocalDate date) {
        return core.getDateAfter(date);
    }

    @Override
    public LocalDate getDateOnOrAfter(LocalDate date) {
        return core.getDateOnOrAfter(date);
    }

    @Override
    public Table withLogOf(String column) {
        return withDerivedColumn(new LogDeriver(column));
    }

    @Override
    public Table replaceColumnWithLog(String column) {
        return withDerivedColumn(new LogDeriver(column)).withoutColumn(column);
    }
}
