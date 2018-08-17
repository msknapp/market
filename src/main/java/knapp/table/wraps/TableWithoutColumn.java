package knapp.table.wraps;

import knapp.history.Frequency;
import knapp.table.values.GetMethod;
import knapp.table.Table;
import knapp.util.Util;

import java.time.LocalDate;

public class TableWithoutColumn extends AbstractWrappingTable {

    private final String removeColumn;

    public TableWithoutColumn(Table core, String removeColumn) {
        super(core);
        if (core.getColumn(removeColumn) < 0 ) {
            throw new IllegalArgumentException("Can't remove a column that doesn't exist.");
        }
        this.removeColumn = removeColumn;
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
    public double getValue(LocalDate date, String column, GetMethod getMethod) {
        if (column.equals(removeColumn)) {
            throw new IllegalArgumentException("The column does not exist.");
        }
        return getValue(date,getColumn(column),getMethod);
    }

    @Override
    public double getValue(LocalDate date, int column, GetMethod getMethod) {
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
                                   Frequency frequency, final GetMethod getMethod) {
        return Util.toDoubleRows(xColumns,start,end,frequency,(date, col) -> {
            double v =  getValue(date,col, getMethod);
            return String.valueOf(v);
        });
    }

    @Override
    public double[] getExactValues(LocalDate date) {
        double[] x = core.getExactValues(date);
        double[] y = new double[x.length-1];
        int i = core.getColumn(removeColumn);
        System.arraycopy(x,0,y,0,i);
        System.arraycopy(x,i+1,y,0,y.length-i);
        return y;
    }

    public double[][] toDoubleColumns(int[] xColumns, LocalDate start, LocalDate end,
                                      Frequency frequency, final GetMethod getMethod) {
        return Util.toDoubleColumns(xColumns,start,end,frequency,(date,col) -> {
            double v = getValue(date,col, getMethod);
            return String.valueOf(v);
        });
    }

    @Override
    public Table untilExclusive(LocalDate date) {
        return new TableWithoutColumn(core.untilExclusive(date),this.removeColumn);
    }

    @Override
    public Table onOrAfter(LocalDate date) {
        return new TableWithoutColumn(core.onOrAfter(date),this.removeColumn);
    }

    @Override
    public Table inTimeFrame(LocalDate startInclusive, LocalDate endExclusive) {
        return new TableWithoutColumn(core.inTimeFrame(startInclusive,endExclusive),this.removeColumn);
    }
}
