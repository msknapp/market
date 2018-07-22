package knapp.table;

import knapp.history.Frequency;
import knapp.util.Util;

import java.time.LocalDate;

public class TableWithDerived implements Table {

    private Table core;
    private final ValueDeriver valueDeriver;

    public interface ValueDeriver {
        String getColumnName();
        double deriveValue(Table core, LocalDate date, TableImpl.GetMethod getMethod);
    }

    public TableWithDerived(Table core, ValueDeriver valueDeriver) {
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

    public double[][] toDoubleColumns(int[] xColumns, LocalDate start, LocalDate end,
                                      Frequency frequency, final TableImpl.GetMethod getMethod) {
        return Util.toDoubleColumns(xColumns,start,end,frequency,(date,col) -> {
            double v = getValue(date,col, getMethod);
            return String.valueOf(v);
        });
    }
}
