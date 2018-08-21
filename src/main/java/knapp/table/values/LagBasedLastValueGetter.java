package knapp.table.values;

import java.time.LocalDate;

public class LagBasedLastValueGetter implements TableValueGetter {

    private final int lagDays;

    public LagBasedLastValueGetter(int lagDays) {
        if (lagDays < 0) {
            throw new IllegalArgumentException("You can't see the future");
        }
        this.lagDays = lagDays;
    }

    @Override
    public double getValue(LocalDate date, TableColumnView view) {
        LocalDate lagged = date.minusDays(lagDays);
        LocalDate x = view.getDateOnOrBefore(lagged);
        return view.getExactValue(x);
    }

}
