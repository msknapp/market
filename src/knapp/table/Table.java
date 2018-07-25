package knapp.table;

import knapp.history.Frequency;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Set;

public interface Table {

    String getName();
    void setName(String name);
    String getColumn(int i);
    int getColumn(String name);
    double getValue(LocalDate date, String column, TableImpl.GetMethod getMethod);
    double getValue(LocalDate date, int column, TableImpl.GetMethod getMethod);
    int getColumnCount();
    LocalDate[] getAllDates();
    double[] getExactValues(LocalDate date);

    Frequency getFrequency();

    Table withoutColumn(String column);
    Table withDerivedColumn(TableWithDerived.ValueDeriver valueDeriver);
    Table withLogOf(String column);
    Table replaceColumnWithLog(String column);
    Table retainColumns(Set<String> columns);

    LocalDate getLastDate();
    LocalDate getFirstDate();
    Table untilExclusive(LocalDate date);
    Table onOrAfter(LocalDate date);
    Table inTimeFrame(LocalDate startInclusive, LocalDate endExclusive);
    LocalDate getDateBefore(LocalDate date);
    LocalDate getDateOnOrBefore(LocalDate date);
    LocalDate getDateAfter(LocalDate date);
    LocalDate getDateOnOrAfter(LocalDate date);
}
