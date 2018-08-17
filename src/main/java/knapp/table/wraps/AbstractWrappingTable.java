package knapp.table.wraps;

import knapp.history.Frequency;
import knapp.table.DefaultTableColumnView;
import knapp.table.values.GetMethod;
import knapp.table.Table;
import knapp.table.values.TableColumnView;
import knapp.table.values.TableValueGetter;

import java.time.LocalDate;
import java.util.List;

public class AbstractWrappingTable implements Table {
    protected Table core;

    public AbstractWrappingTable(Table core) {
        this.core = core;
    }

    protected Table getCore() {
        return core;
    }

    @Override
    public double getValue(LocalDate date) {
        if (core.getColumnCount() < 1) {
            return 0;
        }
        if (core.getColumnCount() < 2) {
            return core.getValue(date,0);
        }
        return core.getValue(date);
    }

    public double getValue(LocalDate date, int column) {
        return core.getValue(date, column);
    }

    @Override
    public String getName() {
        return core.getName();
    }

    @Override
    public void setName(String name) {
        core.setName(name);
    }

    @Override
    public String getColumn(int i) {
        return core.getColumn(i);
    }

    @Override
    public int getColumn(String name) {
        return core.getColumn(name);
    }

    @Override
    public double getValue(LocalDate date, int column, GetMethod getMethod) {
        return core.getValue(date, column, getMethod);
    }

    @Override
    public int getColumnCount() {
        return core.getColumnCount();
    }

    @Override
    public List<LocalDate> getAllDates(int column) {
        return core.getAllDates(column);
    }

//    @Override
//    public LocalDate[] getAllDates() {
//        return core.getAllDates();
//    }

    @Override
    public double[] getExactValues(LocalDate date) {
        return core.getExactValues(date);
    }

//    @Override
//    public LocalDate getLastDate() {
//        return core.getLastDate();
//    }
//
//    @Override
//    public LocalDate getFirstDate() {
//        return core.getFirstDate();
//    }

    @Override
    public Frequency getFrequency() {
        return core.getFrequency();
    }

    @Override
    public Table untilExclusive(LocalDate date) {
        return core.untilExclusive(date);
    }

    @Override
    public Table onOrAfter(LocalDate date) {
        return core.onOrAfter(date);
    }

    @Override
    public Table inTimeFrame(LocalDate startInclusive, LocalDate endExclusive) {
        return core.inTimeFrame(startInclusive,endExclusive);
    }

//    @Override
//    public LocalDate getDateBefore(LocalDate date) {
//        return core.getDateBefore(date);
//    }
//
//    @Override
//    public LocalDate getDateOnOrBefore(LocalDate date) {
//        return core.getDateOnOrBefore(date);
//    }
//
//    @Override
//    public LocalDate getDateAfter(LocalDate date) {
//        return core.getDateAfter(date);
//    }
//
//    @Override
//    public LocalDate getDateOnOrAfter(LocalDate date) {
//        return core.getDateOnOrAfter(date);
//    }

    @Override
    public boolean isExact() {
        return core.isExact();
    }
    public boolean hasAllValuesForAllDates() {
        return core.hasAllValuesForAllDates();
    }

    public double getValue(LocalDate date, int column, TableValueGetter getter) {
        return core.getValue(date, column, getter);
    }

    @Override
    public TableColumnView getTableColumnView(int column) {
        return new DefaultTableColumnView(this, column);
    }

    @Override
    public double getExactValue(LocalDate date, int column) {
        return core.getExactValue(date, column);
    }


    public LocalDate getLastDateOf(String column) {
        return core.getLastDateOf(column);
    }

    public LocalDate getFirstDateOf(String column) {
        return core.getFirstDateOf(column);
    }

}
