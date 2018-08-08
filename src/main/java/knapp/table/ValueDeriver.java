package knapp.table;

import java.time.LocalDate;

public interface ValueDeriver {
    String getColumnName();
    double deriveValue(Table core, LocalDate date, TableImpl.GetMethod getMethod);
}
