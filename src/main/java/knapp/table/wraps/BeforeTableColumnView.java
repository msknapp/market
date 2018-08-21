package knapp.table.wraps;

import knapp.table.values.TableColumnView;

import java.time.LocalDate;
import java.util.List;

public class BeforeTableColumnView implements TableColumnView {

    private final TableColumnView core;
    private final LocalDate maxExclusive;
    private List<LocalDate> dates;

    public BeforeTableColumnView(TableColumnView core, LocalDate maxExclusive) {
        if (core == null) {
            throw new IllegalArgumentException("Can't have an null core table.");
        }
        if (maxExclusive == null) {
            throw new IllegalArgumentException("max exclusive date is null");
        }
        this.core = core;
        this.maxExclusive = maxExclusive;
    }


    @Override
    public List<LocalDate> getAllDates() {
        if (dates == null) {
            List<LocalDate> x = core.getAllDates();
            int i = 0;
            while (i < x.size() && x.get(i).isBefore(maxExclusive)) {
                i++;
            }
            dates = core.getAllDates().subList(0, i);
        }
        return dates;
    }

    @Override
    public double getExactValue(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Given a null date.");
        }
        if (date.isBefore(maxExclusive)) {
            return core.getExactValue(date);
        }
        throw new IllegalArgumentException("DNE");
    }

    @Override
    public LocalDate getLastDate() {
        return core.getDateBefore(maxExclusive);
    }

    @Override
    public LocalDate getFirstDate() {
        return core.getFirstDate();
    }

    @Override
    public LocalDate getDateBefore(LocalDate date) {
        if (date.isBefore(maxExclusive)) {
            return core.getDateBefore(date);
        }
        return core.getDateBefore(maxExclusive);
    }

    @Override
    public LocalDate getDateOnOrBefore(LocalDate date) {
        if (date.isBefore(maxExclusive)) {
            return core.getDateOnOrBefore(date);
        }
        return core.getDateBefore(maxExclusive);
    }

    @Override
    public LocalDate getDateAfter(LocalDate date) {
        LocalDate t = core.getDateAfter(date);
        return !t.isBefore(maxExclusive) ? null : t;
    }

    @Override
    public LocalDate getDateOnOrAfter(LocalDate date) {
        LocalDate t = core.getDateOnOrAfter(date);
        return !t.isBefore(maxExclusive) ? null : t;
    }

    @Override
    public boolean containsDate(LocalDate date) {
        return core.containsDate(date) && date.isBefore(maxExclusive);
    }
}
