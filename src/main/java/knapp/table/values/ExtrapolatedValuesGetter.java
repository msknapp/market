package knapp.table.values;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

public class ExtrapolatedValuesGetter implements TableValueGetter {

    @Override
    public double getValue(LocalDate date, TableColumnView view) {
        if (view.containsDate(date)){
            return view.getExactValue(date);
        }
        if (date.isBefore(view.getFirstDate())) {
            return extrapolateDataBeforeStart(date,view);
        }
        if (date.isAfter(view.getLastDate())) {
            return extrapolateDataAfterEnd(date,view);
        }
        return extrapolateValue(date, view);
    }

    double extrapolateDataBeforeStart(LocalDate date, TableColumnView view) {
        LocalDate first = view.getFirstDate();
        LocalDate second= view.getDateAfter(first);
        double firstValue = view.getExactValue(first);
        double secondValue = view.getExactValue(second);
        long days = DAYS.between(first,second);
        double slope = (secondValue - firstValue) / days;
        long x = - DAYS.between(date,first);
        return firstValue + (x * slope);
    }

    double extrapolateDataAfterEnd(LocalDate date, TableColumnView view) {
        LocalDate last = view.getLastDate();
        LocalDate secondLast = view.getDateBefore(last);
        double firstValue = view.getExactValue(secondLast);
        double secondValue = view.getExactValue(last);
        long days = DAYS.between(secondLast,last);
        double slope = (secondValue - firstValue) / days;
        long x = DAYS.between(last,date);
        return secondValue + (x * slope);
    }

    double extrapolateValue(LocalDate date, TableColumnView view) {
        LocalDate second = view.getDateBefore(date);
        if (second == null) {
            return extrapolateDataBeforeStart(date, view);
        }
        LocalDate first = view.getDateBefore(second);
        double secondVal = view.getExactValue(second);
        if (first == null) {
            // it's between the first and second value,
            // we interpolate it figuring that this is due to the table
            // just not going far enough in the past.
            return interpolateValue(date, view);
        }
        double firstVal = view.getExactValue(first);
        long days = DAYS.between(first,second);
        double slope = (secondVal - firstVal) / days;
        long elapsed = DAYS.between(second,date);
        return slope * elapsed + secondVal;
    }

    double interpolateValue(LocalDate date, TableColumnView view) {
        LocalDate first = view.getDateBefore(date);
        if (first == null) {
            view.getDateBefore(date);
            throw new IllegalStateException("The first date was null");
        }
        LocalDate second = view.getDateAfter(date);
        if (second == null) {
            view.getDateAfter(date);
            throw new IllegalStateException("The second date was null");
        }
        double firstVal = view.getExactValue(first);
        double secondVal = view.getExactValue(second);
        long days = DAYS.between(first,second);
        double slope = (secondVal - firstVal) / days;
        long elapsed = DAYS.between(first,date);
        return slope * elapsed + firstVal;
    }
}
