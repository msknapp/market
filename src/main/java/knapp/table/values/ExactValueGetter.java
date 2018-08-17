package knapp.table.values;

import java.time.LocalDate;

public class ExactValueGetter implements TableValueGetter {
    @Override
    public double getValue(LocalDate date, TableColumnView view) {
        if (!view.containsDate(date)) {
            throw new IllegalArgumentException("That date DNE in the table");
        }
        return view.getExactValue(date);
    }
}
