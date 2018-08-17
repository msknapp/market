package knapp.table.values;

import java.time.LocalDate;

public interface TableValueGetter {
    double getValue(LocalDate date, TableColumnView view);
}
