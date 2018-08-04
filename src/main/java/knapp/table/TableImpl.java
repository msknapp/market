package knapp.table;

import knapp.history.Frequency;
import knapp.util.Util;

import java.time.LocalDate;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

public class TableImpl implements Table {

    private final List<String> columns;
    private final SortedMap<LocalDate,TableRow> rows;
    private final Frequency frequency;
    private String name;

    // getting first and last key is a bit slow, cache them.
    private LocalDate firstDate;
    private LocalDate lastDate;

    public static TableBuilder newBuilder() {
        return new TableBuilder();
    }

    private TableImpl(List<String> columns, SortedMap<LocalDate,TableRow> rows, Frequency frequency) {
        this.columns = columns;
        this.rows = rows;
        this.frequency = frequency;
    }

    private TableImpl(List<String> columns, List<TableRow> rows, Frequency frequency) {
        if (frequency == null) {
            throw new IllegalArgumentException("Frequency can't be null");
        }
        if (columns == null) {
            throw new IllegalArgumentException("Columns must be defined.");
        }
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("Rows must be defined.");
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
        EXACT,
        LAST_KNOWN_VALUE,
        EXTRAPOLATE,
        INTERPOLATE;
    }

    public double getValue(LocalDate date,String column,GetMethod getMethod) {
        return getValue(date,getColumn(column), getMethod);
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
        } else if (getMethod == GetMethod.EXACT) {
            throw new IllegalArgumentException("There is no record on that date.");
        }
        if (getMethod == GetMethod.LAST_KNOWN_VALUE) {
            if (date.isBefore(rows.firstKey())) {
                return 0;
            }
            return getLastKnownValue(date, column);
        }
        if (date.isBefore(rows.firstKey())) {
            return extrapolateDataBeforeStart(date,column);
        }
        if (date.isAfter(rows.lastKey())) {
            return extrapolateDataAfterEnd(date,column);
        }
        if (getMethod == GetMethod.EXTRAPOLATE) {
            return extrapolateValue(date, column);
        } else if (getMethod == GetMethod.INTERPOLATE) {
            return interpolateValue(date, column);
        }
        return 0;
    }

    public double getLastKnownValue(LocalDate date,int column) {
        LocalDate lb = getDateBefore(date);
        if (lb == null) {
            return 0;
        }
        return rows.get(lb).values[column];
    }

    private double extrapolateDataBeforeStart(LocalDate date,int column) {
        Iterator<LocalDate> iter = rows.keySet().iterator();
        LocalDate first = iter.next();
        LocalDate second= iter.next();
        double firstValue = rows.get(first).values[column];
        double secondValue = rows.get(second).values[column];
        long days = DAYS.between(first,second);
        double slope = (secondValue - firstValue) / days;
        long x = - DAYS.between(date,first);
        return firstValue + (x * slope);
    }

    private double extrapolateDataAfterEnd(LocalDate date,int column) {
        LocalDate secondLast = null;
        LocalDate last = null;
        for (LocalDate d : rows.keySet()) {
            secondLast = last;
            last = d;
        }
        double firstValue = rows.get(secondLast).values[column];
        double secondValue = rows.get(last).values[column];
        long days = DAYS.between(secondLast,last);
        double slope = (secondValue - firstValue) / days;
        long x = DAYS.between(last,date);
        return secondValue + (x * slope);
    }

    @Override
    public LocalDate getDateBefore(LocalDate date) {
        if (date.isBefore(rows.firstKey())) {
            return null;
        }
        SortedMap<LocalDate, TableRow> t = rows.headMap(date);
        if (!t.isEmpty()) {
            return t.lastKey();
        } else {
            return null;
        }
    }

    @Override
    public LocalDate getDateOnOrBefore(LocalDate date) {
        if (rows.containsKey(date)) {
            return date;
        }
        return rows.headMap(date).lastKey();
    }

    @Override
    public LocalDate getDateAfter(LocalDate date) {
        LocalDate d = date.plusDays(1);
        return rows.tailMap(d).firstKey();
    }

    @Override
    public LocalDate getDateOnOrAfter(LocalDate date) {
        if (rows.containsKey(date)) {
            return date;
        }
        return rows.tailMap(date).firstKey();
    }

    private double extrapolateValue(LocalDate date, int column) {
        LocalDate second = getDateBefore(date);
        if (second == null) {
            return extrapolateDataBeforeStart(date, column);
        }
        LocalDate first = getDateBefore(second);
        double secondVal = rows.get(second).values[column];
        if (first == null) {
            // it's between the first and second value,
            // we interpolate it figuring that this is due to the table
            // just not going far enough in the past.
            return interpolateValue(date, column);
        }
        double firstVal = rows.get(first).values[column];
        long days = DAYS.between(first,second);
        double slope = (secondVal - firstVal) / days;
        long elapsed = DAYS.between(second,date);
        return slope * elapsed + secondVal;
    }

    private double interpolateValue(LocalDate date, int column) {
        LocalDate first = getDateBefore(date);
        LocalDate second = getDateAfter(date);
        double firstVal = rows.get(first).values[column];
        double secondVal = rows.get(second).values[column];
        long days = DAYS.between(first,second);
        double slope = (secondVal - firstVal) / days;
        long elapsed = DAYS.between(first,date);
        return slope * elapsed + firstVal;
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

    @Override
    public Table withoutColumn(String column) {
        return new TableWithoutColumn(this,column);
    }

    @Override
    public Table withDerivedColumn(TableWithDerived.ValueDeriver valueDeriver) {
        return new TableWithDerived(this,valueDeriver);
    }

    @Override
    public Table withLogOf(String column) {
        return withDerivedColumn(new LogDeriver(column));
    }

    @Override
    public Table replaceColumnWithLog(String column) {
        return withDerivedColumn(new LogDeriver(column)).withoutColumn(column);
    }

    @Override
    public Table retainColumns(Set<String> columns) {
        return TableParser.retainColumns(this,columns);
    }

    @Override
    public LocalDate getLastDate() {
        if (lastDate == null) {
            lastDate = rows.lastKey();
        }
        return lastDate;
    }

    @Override
    public LocalDate getFirstDate() {
        if (firstDate == null) {
            firstDate = rows.firstKey();
        }
        return firstDate;
    }

    @Override
    public Table untilExclusive(LocalDate date) {
        return new TableImpl(columns,rows.headMap(date), frequency);
    }

    @Override
    public Table onOrAfter(LocalDate date) {
        return new TableImpl(columns,rows.tailMap(date), frequency);
    }

    @Override
    public Table inTimeFrame(LocalDate start, LocalDate end) {
        return new TableImpl(columns,rows.subMap(start,end), frequency);
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
