package knapp.table;

import java.time.LocalDate;

public class TableWithGetMethod extends AbstractWrappingTable {

    private TableImpl.GetMethod getMethod;
    private boolean strict;

    public TableWithGetMethod(Table core, TableImpl.GetMethod getMethod, boolean strict) {
        super(core);
        this.getMethod = getMethod;
        this.strict = strict;
    }

    @Override
    public double getValue(LocalDate date, String column, TableImpl.GetMethod whatever) {
        TableImpl.GetMethod current = (strict || whatever == null ) ? this.getMethod : whatever;
        return core.getValue(date,column,current);
    }

    @Override
    public double getValue(LocalDate date, int column, TableImpl.GetMethod whatever) {
        TableImpl.GetMethod current = (strict || whatever == null ) ? this.getMethod : whatever;
        return core.getValue(date,column,current);
    }
}
