package knapp.table.values;

import java.time.LocalDate;

public class InterpolatedValuesGetter extends ExtrapolatedValuesGetter {

    @Override
    public double getValue(LocalDate date, TableColumnView view) {
        if (view.containsDate(date)) {
            return view.getExactValue(date);
        }
        if (date.isBefore(view.getFirstDate())) {
            return extrapolateDataBeforeStart(date,view);
        }
        if (date.isAfter(view.getLastDate())) {
            return extrapolateDataAfterEnd(date,view);
        }
        return super.interpolateValue(date, view);
    }
}
