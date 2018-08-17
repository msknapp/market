package knapp.table.wraps;

import knapp.table.values.GetMethod;
import knapp.table.Table;

import java.time.LocalDate;

public class TableWithGetMethod extends AbstractWrappingTable {

    private GetMethod getMethod;
    private boolean strict;

    public TableWithGetMethod(Table core, GetMethod getMethod, boolean strict) {
        super(core);
        this.getMethod = getMethod;
        this.strict = strict;
    }

    @Override
    public double getValue(LocalDate date, String column, GetMethod whatever) {
        GetMethod current = (strict || whatever == null ) ? this.getMethod : whatever;
        return core.getValue(date,column,current);
    }

    @Override
    public double getValue(LocalDate date, int column, GetMethod whatever) {
        GetMethod current = (strict || whatever == null ) ? this.getMethod : whatever;
        return core.getValue(date,column,current);
    }
}
