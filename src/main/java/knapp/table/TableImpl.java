package knapp.table;

import knapp.history.Frequency;
import knapp.table.derivation.LogDeriver;
import knapp.table.derivation.ValueDeriver;
import knapp.table.util.TableParser;
import knapp.table.values.GetMethod;
import knapp.table.values.TableColumnView;
import knapp.table.values.TableValueGetter;
import knapp.table.wraps.TableWithDerived;
import knapp.table.wraps.TableWithoutColumn;
import knapp.util.Util;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static java.time.temporal.ChronoUnit.*;

public class TableImpl implements Table {

    private final List<String> columns;
    private final SortedMap<LocalDate,TableRow> rows;
    private final Frequency frequency;
    private final boolean exact;
    private String name;

    // getting first and last key is a bit slow, cache them.
    private LocalDate firstDate;
    private LocalDate lastDate;

    private Map<Integer,TableImplColumnView> views = new HashMap<>();
    private List<LocalDate> allDates;

    public static TableBuilder newBuilder() {
        return new TableBuilder();
    }

    private TableImpl(List<String> columns, SortedMap<LocalDate,TableRow> rows, Frequency frequency, boolean exact) {
        this.columns = columns;
        this.rows = rows;
        this.frequency = frequency;
        this.exact = exact;
    }

    private TableImpl(List<String> columns, List<TableRow> rows, Frequency frequency, boolean exact) {
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
            if (tr.getColumnCount() != this.columns.size()) {
                throw new IllegalArgumentException("The rows must all have the same number of columns");
            }
            tmp.put(tr.getDate(),tr);
        }
        this.rows = Collections.unmodifiableSortedMap(tmp);
        this.exact = exact;
    }

    @Override
    public boolean isExact() {
        return exact;
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

    public double getValue(LocalDate date,String column,GetMethod getMethod) {
        return getValue(date,getColumn(column), getMethod);
    }

    @Override
    public double getValue(LocalDate date, int column, TableValueGetter getter) {
        TableColumnView view = getTableColumnView(column);
        return getter.getValue(date, view);
    }

    @Override
    public TableColumnView getTableColumnView(int column) {
        if (!views.containsKey(column)) {
            views.put(column,new TableImplColumnView(column));
        }
        return views.get(column);
    }

    public class TableImplColumnView implements TableColumnView {
        private final int column;
        private List<LocalDate> dates;

        public TableImplColumnView(int column){
            this.column = column;
        }

        @Override
        public List<LocalDate> getAllDates() {
            if (allDates == null) {
                allDates = new ArrayList<>(rows.keySet());
                Collections.sort(allDates);
                allDates = Collections.unmodifiableList(allDates);
            }
            return allDates;
        }

        @Override
        public double getExactValue(LocalDate date) {
            return rows.get(date).getValue(column);
        }

        @Override
        public LocalDate getLastDate() {
            return rows.lastKey();
        }

        @Override
        public LocalDate getFirstDate() {
            return rows.firstKey();
        }

        @Override
        public LocalDate getDateBefore(LocalDate date) {
            return rows.headMap(date).lastKey();
        }

        @Override
        public LocalDate getDateOnOrBefore(LocalDate date) {
            if (containsDate(date)) {
                return date;
            }
            return getDateBefore(date);
        }

        @Override
        public LocalDate getDateAfter(LocalDate date) {
            return rows.tailMap(date.plusDays(1)).firstKey();
        }

        @Override
        public LocalDate getDateOnOrAfter(LocalDate date) {
            if (containsDate(date)) {
                return date;
            }
            return null;
        }

        @Override
        public boolean containsDate(LocalDate date) {
            return rows.containsKey(date);
        }
    }

    @Override
    public double getExactValue(LocalDate date, int column) {
        if (column < 0 || column >= columns.size()) {
            throw new IllegalArgumentException("illegal column number " + column);
        }
        if (date == null) {
            throw new IllegalArgumentException("date can't be null");
        }
        if (!rows.containsKey(date)) {
            throw new IllegalArgumentException("There is no record on that date.");
        }
        return rows.get(date).getValue(column);
    }


    public double getLastKnownValue(LocalDate date,int column) {
        LocalDate lb = getDateBefore(date);
        if (lb == null) {
            return 0;
        }
        return rows.get(lb).getValue(column);
    }

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

    public LocalDate getDateOnOrBefore(LocalDate date) {
        if (rows.containsKey(date)) {
            return date;
        }
        return rows.headMap(date).lastKey();
    }

    public LocalDate getDateAfter(LocalDate date) {
        LocalDate d = date.plusDays(1);
        return rows.tailMap(d).firstKey();
    }

    public LocalDate getDateOnOrAfter(LocalDate date) {
        if (rows.containsKey(date)) {
            return date;
        }
        return rows.tailMap(date).firstKey();
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
    public List<LocalDate> getAllDates(int column) {
        List<LocalDate> dates = new ArrayList<>(rows.keySet());
        Collections.sort(dates);
        return dates;
    }

    @Override
    public double[] getExactValues(LocalDate date) {
        TableRow r = rows.get(date);
        return r.getValues();
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
    public Table withDerivedColumn(ValueDeriver valueDeriver) {
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

    public LocalDate getLastDate() {
        if (lastDate == null) {
            lastDate = rows.lastKey();
        }
        return lastDate;
    }

    public LocalDate getFirstDate() {
        if (firstDate == null) {
            firstDate = rows.firstKey();
        }
        return firstDate;
    }

    @Override
    public Table untilExclusive(LocalDate date) {
        return new TableImpl(columns,rows.headMap(date), frequency, isExact());
    }

    @Override
    public Table onOrAfter(LocalDate date) {
        return new TableImpl(columns,rows.tailMap(date), frequency, isExact());
    }

    @Override
    public Table inTimeFrame(LocalDate start, LocalDate end) {
        return new TableImpl(columns,rows.subMap(start,end), frequency, isExact());
    }

    public double[][] toDoubleColumns(int[] xColumns, LocalDate start, LocalDate end,
                                      Frequency frequency, final GetMethod getMethod) {
        return Util.toDoubleColumns(xColumns,start,end,frequency,(date,col) -> {
            double v = getValue(date,col, getMethod);
            return String.valueOf(v);
        });
    }


    @Override
    public double getValue(LocalDate date) {
        if (getColumnCount() < 1) {
            return 0;
        }
        if (getColumnCount() < 2) {
            return getValue(date,0);
        }
        throw new IllegalArgumentException("There are multiple columns");
    }

    public static final class TableBuilder {
        private List<String> columns;
        private List<TableRow> rows;
        private Frequency frequency;
        private Boolean exact = null;

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

        public TableBuilder exact(boolean ex) {
            this.exact = ex;
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

        public TableBuilder guessFrequency() {
            Collections.sort(rows);
            LocalDate start = rows.get(0).getDate();
            LocalDate second = rows.get(1).getDate();
            if (DAYS.between(start,second) <= 3) {
                frequency = Frequency.Daily;
            } else {
                LocalDate end = rows.get(rows.size() - 1).getDate();
                Duration duration = Duration.between(start, end);
                duration = duration.dividedBy(rows.size() - 1);
                long days = duration.get(DAYS);
                if (days < 12) {
                    frequency = Frequency.Weekly;
                } else if (days < 60) {
                    frequency = Frequency.Monthly;
                } else if (days < 180) {
                    frequency = Frequency.Quarterly;
                } else {
                    frequency = Frequency.Annual;
                }
            }
            return this;
        }

        public TableImpl build() {
            if (exact == null) {
                throw new IllegalArgumentException("You have not specified if this table has exact values or not.");
            }
            if (frequency == null) {
                guessFrequency();
            }
            return new TableImpl(columns, rows,frequency, exact);
        }
    }

}
