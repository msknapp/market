package knapp.table.values;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

public class LagBasedExtrapolatedValuesGetter implements TableValueGetter {

    private final int lagDays;

    public LagBasedExtrapolatedValuesGetter(int lagDays) {
        if (lagDays < 0) {
            throw new IllegalArgumentException("You can't see the future");
        }
        this.lagDays = lagDays;
    }

    @Override
    public double getValue(LocalDate date, TableColumnView view) {
        LocalDate second = view.getDateOnOrBefore(date.minusDays(lagDays));
        LocalDate first = view.getDateBefore(second);
        double firstValue = view.getExactValue(first);
        double secondValue = view.getExactValue(second);
        long delta = DAYS.between(first,second);

        double slope = (secondValue - firstValue) / ((double)delta);
        long moreDays = DAYS.between(second,date);

        return secondValue + slope * moreDays;
    }

}
