package knapp.table;

import knapp.history.Frequency;
import knapp.table.values.TableColumnView;
import knapp.table.wraps.UntilTable;

import java.time.LocalDate;
import java.util.*;

public class UnevenTable implements Table {

    private String name;
    private final boolean exact;

    private final Map<String,TableColumn> columnsByName;
    private final Map<Integer,TableColumn> columnsByNumber;

    public static UnevenTable from(Collection<Table> tables) {
        UnevenTable.UnevenTableBuilder unevenTableBuilder = UnevenTable.defineTable();
        boolean exact = true;
        for (Table table : tables) {
            exact &= table.isExact();
            for (String column : table.getColumns()) {
                TableColumnView view = table.getTableColumnView(column);
                for (LocalDate date : view.getAllDates()) {
                    unevenTableBuilder.add(column,date,view.getExactValue(date));
                }
            }
        }
        return unevenTableBuilder.exact(exact).build();
    }

    UnevenTable(boolean exact, Collection<Cell> cells) {
        this.exact = exact;
        Map<String, Map<LocalDate,Double>> tmp = new TreeMap<>();
        for (Cell cell : cells) {
            if (!tmp.containsKey(cell.getName())) {
                tmp.put(cell.getName(),new HashMap<>());
            }
            Map<LocalDate, Double> x = tmp.get(cell.getName());
            x.put(cell.getDate(),cell.getValue());
        }
        Map<String, TableColumn> tmpByName = new HashMap<>();
        Map<Integer, TableColumn> tmpByNum = new HashMap<>();
        int i = 0;
        for (String colName : tmp.keySet()) {
            TableColumn tableColumn = new TableColumn(colName, i, tmp.get(colName));
            tmpByName.put(colName, tableColumn);
            tmpByNum.put(i, tableColumn);
            i += 1;
        }
        this.columnsByName = Collections.unmodifiableMap(tmpByName);
        this.columnsByNumber = Collections.unmodifiableMap(tmpByNum);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getColumn(int i) {
        return columnsByNumber.get(i).getName();
    }

    @Override
    public int getColumn(String name) {
        return columnsByName.get(name).getColumnNumber();
    }

    @Override
    public double getExactValue(LocalDate date, int column) {
        return columnsByNumber.get(column).values.get(date);
    }

    @Override
    public int getColumnCount() {
        return columnsByNumber.size();
    }

//    @Override
//    public LocalDate[] getAllDates() {
//        List<LocalDate> dates = getAllDates(DatePolicy.ANYMUSTHAVE);
//        LocalDate[] x = new LocalDate[dates.size()];
//        dates.toArray(x);
//        return x;
//    }

    public List<LocalDate> getAllDates(DatePolicy datePolicy) {
        Set<LocalDate> dates = new HashSet<>();
        for (TableColumn tableColumn : columnsByName.values()) {
            if (dates.isEmpty() || datePolicy == DatePolicy.ANYMUSTHAVE) {
                dates.addAll(tableColumn.values.keySet());
            } else {
                dates.retainAll(tableColumn.values.keySet());
            }
        }
        List<LocalDate> out = new ArrayList<>(dates);
        Collections.sort(out);
        return out;
    }

    @Override
    public List<LocalDate> getAllDates(int column) {
        List<LocalDate> x = new ArrayList<>(columnsByNumber.get(column).values.keySet());
        Collections.sort(x);
        return x;
    }

    @Override
    public double[] getExactValues(LocalDate date) {
        double[] out = new double[getColumnCount()];
        for (int i = 0;i<getColumnCount();i++) {
            out[i] = getExactValue(date, i);
        }
        return out;
    }

    @Override
    public TableColumnView getTableColumnView(int column) {
        return columnsByNumber.get(column);
    }

    @Override
    public Frequency getFrequency() {
        throw new UnsupportedOperationException("You can't get the frequency");
    }

    @Override
    public Table untilExclusive(LocalDate date) {
        return new UntilTable(this,date);
    }

    @Override
    public Table onOrAfter(LocalDate date) {
        return new AfterTable(this,date);
    }

    @Override
    public Table inTimeFrame(LocalDate startInclusive, LocalDate endExclusive) {
        return new AfterTable(new UntilTable(this,endExclusive),startInclusive);
    }

    @Override
    public boolean isExact() {
        return exact;
    }

    public static class TableColumn implements TableColumnView {
        private final int columnNumber;
        private final String name;

        private SortedMap<LocalDate,Double> values;

        TableColumn(String name, int columnNumber, Map<LocalDate, Double> x) {
            this.name = name;
            this.columnNumber = columnNumber;
            this.values = Collections.unmodifiableSortedMap(new TreeMap<>(x));
        }

        public int getColumnNumber() {
            return columnNumber;
        }

        public String getName() {
            return name;
        }

        @Override
        public List<LocalDate> getAllDates() {
            List<LocalDate> dts = new ArrayList<>(values.keySet());
            Collections.sort(dts);
            return dts;
        }

        @Override
        public double getExactValue(LocalDate date) {
            return values.get(date);
        }

        @Override
        public LocalDate getLastDate() {
            return values.lastKey();
        }

        @Override
        public LocalDate getFirstDate() {
            return values.firstKey();
        }

        @Override
        public LocalDate getDateBefore(LocalDate date) {
            return values.headMap(date).lastKey();
        }

        @Override
        public LocalDate getDateOnOrBefore(LocalDate date) {
            if (values.containsKey(date)) {
                return date;
            }
            return getDateBefore(date);
        }

        @Override
        public LocalDate getDateAfter(LocalDate date) {
            return values.tailMap(date.plusDays(1)).firstKey();
        }

        @Override
        public LocalDate getDateOnOrAfter(LocalDate date) {
            return values.tailMap(date).firstKey();
        }

        @Override
        public boolean containsDate(LocalDate date) {
            return values.containsKey(date);
        }
    }

    private static class Cell {
        private final String name;
        private final LocalDate date;
        private final double value;

        public Cell(String name, LocalDate date, double value) {
            this.name = name;
            this.date = date;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public LocalDate getDate() {
            return date;
        }

        public double getValue() {
            return value;
        }
    }

    public static UnevenTableBuilder defineTable() {
        return new UnevenTableBuilder();
    }

    public static final class UnevenTableBuilder {
        private List<Cell> cells = new ArrayList<>();
        private boolean exact = false;

        public UnevenTableBuilder exact(boolean x) {
            this.exact = x;
            return this;
        }

        public UnevenTableBuilder add(String column, LocalDate date, double value) {
            cells.add(new Cell(column,date, value));
            return this;
        }

        public UnevenTable build() {
            return new UnevenTable(exact, cells);
        }
    }
}
