package knapp.table;

import knapp.history.Frequency;

import java.time.LocalDate;
import java.util.Set;

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
    default double getValue(LocalDate date, String column, TableImpl.GetMethod getMethod) {
        return getValue(date,getColumn(column), getMethod);
    }
    double getValue(LocalDate date, int column, TableImpl.GetMethod getMethod);
    int getColumnCount();
    LocalDate[] getAllDates();
    double[] getExactValues(LocalDate date);

    LocalDate getLastDate();
    LocalDate getFirstDate();

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
    LocalDate getDateBefore(LocalDate date);
    LocalDate getDateOnOrBefore(LocalDate date);
    LocalDate getDateAfter(LocalDate date);
    LocalDate getDateOnOrAfter(LocalDate date);

    default Table withGetMethod(TableImpl.GetMethod getMethod, boolean strict) {
        return new TableWithGetMethod(this, getMethod, strict);
    }
}
