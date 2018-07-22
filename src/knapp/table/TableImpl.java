package knapp.table;

import knapp.history.Frequency;
import knapp.util.Util;

import java.time.LocalDate;
import java.util.*;

public class TableImpl implements Table {

    private final List<String> columns;
    private final SortedMap<LocalDate,TableRow> rows;
    private final Frequency frequency;
    private String name;

    public static TableBuilder newBuilder() {
        return new TableBuilder();
    }

    private TableImpl(List<String> columns, List<TableRow> rows, Frequency frequency) {
        if (frequency == null) {
            throw new IllegalArgumentException("Frequency can't be null");
        }
        if (columns == null) {
            throw new IllegalArgumentException("Columns must be defined.");
        }
        this.frequency = frequency;
        this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
        TreeMap<LocalDate,TableRow> tmp = new TreeMap<LocalDate,TableRow>();
        for (TableRow tr : rows) {
            if (tr.values.length != this.columns.size()) {
                throw new IllegalArgumentException("The rows must all have the same number of columns");
            }
            tmp.put(tr.date,tr);
        }
        this.rows = Collections.unmodifiableSortedMap(tmp);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColumn(int i) {
        return columns.get(i);
    }

    public int getColumn(String name) {
        return columns.indexOf(name);
    }

    public enum GetMethod {
        LAST_KNOWN_VALUE,
        EXTRAPOLATE,
        INTERPOLATE;
    }

    public double getValue(LocalDate date,String column,GetMethod getMethod) {
        return getValue(date,getColumn(column), getMethod);
    }

    private static class FourDates {
        LocalDate secondLastBefore = null;
        LocalDate lastBefore = null;
        LocalDate firstAfter = null;
        LocalDate secondAfter = null;
    }

    public double getValue(LocalDate date,int column,GetMethod getMethod) {
        if (column < 0 || column >= columns.size()) {
            throw new IllegalArgumentException("illegal column number "+column);
        }
        if (date == null) {
            throw new IllegalArgumentException("date can't be null");
        }
        if (rows.containsKey(date)) {
            return rows.get(date).values[column];
        }
        if (getMethod == GetMethod.LAST_KNOWN_VALUE) {
            return getLastKnownValue(date, column);
        }
        FourDates fourDates = new FourDates();
        for (LocalDate d : this.rows.keySet()) {
            if (d.isBefore(date)) {
                fourDates.secondLastBefore = fourDates.lastBefore;
                fourDates.lastBefore = d;
            }
            if (d.isAfter(date)) {
                if (fourDates.firstAfter == null) {
                    fourDates.firstAfter = d;
                } else {
                    fourDates.secondAfter = d;
                    break;
                }
            }
        }
        if (getMethod == GetMethod.EXTRAPOLATE) {
            return extrapolateValue(date, fourDates);
        } else if (getMethod == GetMethod.INTERPOLATE) {
            return interpolateValue(date, fourDates);
        }
        return 0;
    }

    public double getLastKnownValue(LocalDate date,int column) {
        LocalDate lastBefore = null;
        for (LocalDate d : this.rows.keySet()) {
            if (d.isBefore(date)) {
                lastBefore = d;
            } else {
                break;
            }
        }
        if (lastBefore == null) {
            return 0;
        }
        return rows.get(lastBefore).values[column];
    }

    private double extrapolateValue(LocalDate date,FourDates fourDates) {
        if (fourDates.lastBefore != null && fourDates.firstAfter != null) {

        } else if (fourDates.lastBefore != null) {

        } else if (fourDates.firstAfter != null) {

        }
        throw new UnsupportedOperationException("extrapolation is not supported yet.");
    }

    private double interpolateValue(LocalDate date,FourDates fourDates) {
        if (fourDates.lastBefore != null && fourDates.firstAfter != null) {

        } else if (fourDates.lastBefore != null) {

        } else if (fourDates.firstAfter != null) {

        }
        throw new UnsupportedOperationException("interpolation is not supported yet.");
    }

    public int getColumnCount() {
        return columns.size();
    }

    public double[][] toDoubleRows(int[] xColumns, LocalDate start, LocalDate end,
                                   Frequency frequency, final GetMethod getMethod) {
        return Util.toDoubleRows(xColumns,start,end,frequency,(date,col) -> {
            double v =  getValue(date,col, getMethod);
            return String.valueOf(v);
        });
    }

    @Override
    public LocalDate[] getAllDates() {
        LocalDate[] dates = new LocalDate[rows.size()];
        rows.keySet().toArray(dates);
        return dates;
    }

    @Override
    public double[] getExactValues(LocalDate date) {
        TableRow r = rows.get(date);
        double[] d = new double[r.values.length];
        System.arraycopy(r.values,0,d,0,r.values.length);
        return d;
    }

    @Override
    public Frequency getFrequency() {
        return frequency;
    }

    public double[][] toDoubleColumns(int[] xColumns, LocalDate start, LocalDate end,
                                      Frequency frequency, final GetMethod getMethod) {
        return Util.toDoubleColumns(xColumns,start,end,frequency,(date,col) -> {
            double v = getValue(date,col, getMethod);
            return String.valueOf(v);
        });
    }


    public static final class TableBuilder {
        private List<String> columns;
        private List<TableRow> rows;
        private Frequency frequency;

        public TableBuilder() {
            this.columns = new ArrayList<>();
            this.rows = new ArrayList<>();
        }

        public TableBuilder column(String c) {
            this.columns.add(c);
            return this;
        }
        public TableBuilder frequency(String c) {
            this.frequency = Frequency.valueOf(c);
            return this;
        }
        public TableBuilder frequency(Frequency frequency) {
            this.frequency = frequency;
            return this;
        }

        public TableBuilder addRow(LocalDate date,double[] values) {
            if (values.length != columns.size()) {
                throw new IllegalArgumentException("Wrong number of columns: "+values.length);
            }
            TableRow tableRow = new TableRow(date, values);
            this.rows.add(tableRow);
            return this;
        }

        public TableImpl build() {
            return new TableImpl(columns, rows,frequency);
        }
    }

    private static final class TableRow {
        private final LocalDate date;
        private final double[] values;

        public TableRow(LocalDate date,double[] values) {
            if (date == null) {
                throw new IllegalArgumentException("The date is null");
            }
            if (values == null) {
                throw new IllegalArgumentException("The values are null.");
            }
            this.date = date;
            this.values = new double[values.length];
            System.arraycopy(values,0,this.values,0,values.length);
        }

    }
}
