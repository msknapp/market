package knapp.table.wraps;

import knapp.table.values.GetMethod;
import knapp.table.Table;
import knapp.table.derivation.ValueDeriver;

import java.time.LocalDate;

public class TableWithDerived extends AbstractWrappingTable {

    private final ValueDeriver valueDeriver;

    public TableWithDerived(Table core, ValueDeriver valueDeriver) {
        super(core);
        this.valueDeriver = valueDeriver;
    }

    public int getColumnCount() {
        return core.getColumnCount()+1;
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
    public double getValue(LocalDate date, String column, GetMethod getMethod) {
        if (valueDeriver.getColumnName().equals(column)) {
            return valueDeriver.deriveValue(core,date,getMethod);
        } else {
            return core.getValue(date,column,getMethod);
        }
    }

    @Override
    public double getValue(LocalDate date, int column, GetMethod getMethod) {
        if (column == core.getColumnCount()) {
            return valueDeriver.deriveValue(core,date,getMethod);
        } else {
            return core.getValue(date,column,getMethod);
        }
    }

    @Override
    public double[] getExactValues(LocalDate date) {
        double[] x = core.getExactValues(date);
        double[] y = new double[x.length+1];
        System.arraycopy(x,0,y,0,x.length);
        y[y.length-1] = valueDeriver.deriveValue(core,date,GetMethod.LAST_KNOWN_VALUE);
        return y;
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

}
