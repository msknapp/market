package knapp.table;

import knapp.history.Frequency;

import java.time.LocalDate;

public interface Table {

    String getName();
    void setName(String name);
    String getColumn(int i);
    int getColumn(String name);
    double getValue(LocalDate date, String column, TableImpl.GetMethod getMethod);
    double getValue(LocalDate date, int column, TableImpl.GetMethod getMethod);
    int getColumnCount();
    double[][] toDoubleColumns(int[] xColumns, LocalDate start, LocalDate end,
                                      Frequency frequency, final TableImpl.GetMethod getMethod);
    double[][] toDoubleRows(int[] xColumns, LocalDate start, LocalDate end,
                                   Frequency frequency, final TableImpl.GetMethod getMethod);
    LocalDate[] getAllDates();
    double[] getExactValues(LocalDate date);
}
