package knapp.table;

import knapp.history.Frequency;
import knapp.util.Util;

import java.time.LocalDate;

public class TableWithoutColumn implements Table {

    private Table core;

    private String removeColumn;

    public TableWithoutColumn(Table core, String removeColumn) {
        this.core = core;
        if (core.getColumn(removeColumn) < 0 ) {
            throw new IllegalArgumentException("Can't remove a column that doesn't exist.");
        }
        this.removeColumn = removeColumn;
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
        int x = core.getColumn(removeColumn);
        if (i < x) {
            return core.getColumn(i);
        }
        return core.getColumn(i+1);
    }

    @Override
    public int getColumn(String name) {
        if (name.equals(removeColumn)) {
            throw new IllegalArgumentException("The column does not exist.");
        }
        int x = core.getColumn(name);
        int y = core.getColumn(removeColumn);
        if (x > y) {
            return x - 1;
        }
        return x;
    }

    @Override
    public double getValue(LocalDate date, String column, TableImpl.GetMethod getMethod) {
        if (column.equals(removeColumn)) {
            throw new IllegalArgumentException("The column does not exist.");
        }
        return getValue(date,getColumn(column),getMethod);
    }

    @Override
    public double getValue(LocalDate date, int column, TableImpl.GetMethod getMethod) {
        int x = core.getColumn(removeColumn);
        if (column < x) {
            return core.getValue(date,column,getMethod);
        }
        return core.getValue(date, column+1, getMethod);
    }

    @Override
    public int getColumnCount() {
        return core.getColumnCount()-1;
    }

    public double[][] toDoubleRows(int[] xColumns, LocalDate start, LocalDate end,
                                   Frequency frequency, final TableImpl.GetMethod getMethod) {
        return Util.toDoubleRows(xColumns,start,end,frequency,(date, col) -> {
            double v =  getValue(date,col, getMethod);
            return String.valueOf(v);
        });
    }

    @Override
    public LocalDate[] getAllDates() {
        return core.getAllDates();
    }

    @Override
    public double[] getExactValues(LocalDate date) {
        return core.getExactValues(date);
    }

    public double[][] toDoubleColumns(int[] xColumns, LocalDate start, LocalDate end,
                                      Frequency frequency, final TableImpl.GetMethod getMethod) {
        return Util.toDoubleColumns(xColumns,start,end,frequency,(date,col) -> {
            double v = getValue(date,col, getMethod);
            return String.valueOf(v);
        });
    }
}
