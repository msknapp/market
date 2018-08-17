package knapp.table.derivation;

import knapp.table.values.GetMethod;
import knapp.table.Table;

import java.time.LocalDate;

public interface ValueDeriver {
    String getColumnName();
    double deriveValue(Table core, LocalDate date, GetMethod getMethod);
}
