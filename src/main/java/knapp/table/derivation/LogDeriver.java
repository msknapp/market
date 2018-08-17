package knapp.table.derivation;

import knapp.table.values.GetMethod;
import knapp.table.Table;

import java.time.LocalDate;

public class LogDeriver implements ValueDeriver {

    private String coreColumn;

    public LogDeriver(String coreColumn) {
        this.coreColumn = coreColumn;
    }

    @Override
    public String getColumnName() {
        return "Log "+coreColumn;
    }

    @Override
    public double deriveValue(Table core, LocalDate date, GetMethod getMethod) {
        double d = core.getValue(date,coreColumn,getMethod);
        return Math.log(d);
    }
}
