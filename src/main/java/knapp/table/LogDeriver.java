package knapp.table;

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
    public double deriveValue(Table core, LocalDate date, TableImpl.GetMethod getMethod) {
        double d = core.getValue(date,coreColumn,getMethod);
        return Math.log(d);
    }
}
