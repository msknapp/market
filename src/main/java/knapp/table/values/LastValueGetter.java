package knapp.table.values;

import java.time.LocalDate;

public class LastValueGetter implements TableValueGetter {

    @Override
    public double getValue(LocalDate date, TableColumnView view) {
        LocalDate x = view.getDateOnOrBefore(date);
        return view.getExactValue(x);
    }
}
