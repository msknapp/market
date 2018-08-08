package knapp.table;

import knapp.history.Frequency;

import java.time.LocalDate;
import java.util.Set;

public class AfterTable extends AbstractWrappingTable {

    private final LocalDate minInclusive;

    public AfterTable(Table core,LocalDate minInclusive) {
        super(core);
        this.minInclusive = minInclusive;
    }

    @Override
    public double getValue(LocalDate date, String column, TableImpl.GetMethod getMethod) {
        if (date.isBefore(minInclusive)) {
            throw new IllegalArgumentException("DNE");
        }
        return core.getValue(date,column,getMethod);
    }

    @Override
    public double getValue(LocalDate date, int column, TableImpl.GetMethod getMethod) {
        if (date.isBefore(minInclusive)) {
            throw new IllegalArgumentException("DNE");
        }
        return core.getValue(date,column,getMethod);
    }

    @Override
    public int getColumnCount() {
        return core.getColumnCount();
    }

    @Override
    public LocalDate[] getAllDates() {
        return new LocalDate[0];
    }

    @Override
    public double[] getExactValues(LocalDate date) {
        if (date.isBefore(minInclusive)) {
            throw new IllegalArgumentException("DNE");
        }
        return core.getExactValues(date);
    }

    @Override
    public LocalDate getFirstDate() {
        return core.getDateOnOrAfter(minInclusive);
    }


}
