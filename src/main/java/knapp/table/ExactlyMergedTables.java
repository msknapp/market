//package knapp.table;
//
//import knapp.history.Frequency;
//import knapp.table.values.GetMethod;
//import knapp.table.values.TableValueGetter;
//import knapp.table.wraps.UntilTable;
//
//import java.time.LocalDate;
//import java.util.*;
//
//public class ExactlyMergedTables implements Table {
//    private String name;
//    private final Table[] tables;
//    private Map<String,List<TableAndColumn>> columnNameIndex;
//    private Map<Integer,List<TableAndColumn>> columnNumberIndex;
//    private final DatePolicy datePolicy;
//    private LocalDate[] cachedDates;
//
//    public ExactlyMergedTables(Table[] tables, DatePolicy datePolicy) {
//        if (tables == null || tables.length < 1) {
//            throw new IllegalArgumentException("Tables are not set.");
//        }
//        if (datePolicy == null) {
//            throw new IllegalArgumentException("The date policy must be set");
//        }
//
//        this.tables = new Table[tables.length];
//        System.arraycopy(tables,0,this.tables,0,tables.length);
//        columnNameIndex = new HashMap<>();
//        columnNumberIndex = new HashMap<>();
//        int totalColumnNumber = 0;
//        for (Table table : this.tables) {
//            if (!table.isExact()) {
//                throw new IllegalArgumentException("All input tables must be exact");
//            }
//            for (int i = 0; i < table.getColumnCount(); i++) {
//                TableAndColumn tableAndColumn = new TableAndColumn();
//                tableAndColumn.table = table;
//                tableAndColumn.columnName = table.getColumn(i);
//                tableAndColumn.internalColumnNumber = i;
//
//                if (columnNameIndex.containsKey(tableAndColumn.columnName)) {
//                    // this is not a new column, it's a continuation of one that exists.
//                    int exNo = columnNameIndex.get(tableAndColumn.columnName).get(0).externalColumnNumber;
//                    tableAndColumn.externalColumnNumber = exNo;
//                    columnNameIndex.get(tableAndColumn.columnName).add(tableAndColumn);
//                    columnNumberIndex.get(exNo).add(tableAndColumn);
//                    // don't increment total column number.
//                } else {
//                    tableAndColumn.externalColumnNumber = totalColumnNumber;
//                    columnNameIndex.put(tableAndColumn.columnName,new ArrayList<>(Arrays.asList(tableAndColumn)));
//                    columnNumberIndex.put(tableAndColumn.externalColumnNumber,new ArrayList<>(Arrays.asList(tableAndColumn)));
//                    totalColumnNumber++;
//                }
//            }
//        }
//        this.datePolicy = datePolicy;
//    }
//
//    @Override
//    public String getName() {
//        return name;
//    }
//
//    @Override
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    @Override
//    public String getColumn(int i) {
//        return columnNumberIndex.get(i).get(0).columnName;
//    }
//
//    @Override
//    public int getColumn(String name) {
//        return columnNameIndex.get(name).get(0).externalColumnNumber;
//    }
//
//    @Override
//    public double getValue(LocalDate date, int column, GetMethod getMethod) {
//        List<TableAndColumn> tableAndColumns = columnNumberIndex.get(column);
//        if (tableAndColumns.size() == 1 ) {
//            TableAndColumn tableAndColumn = tableAndColumns.get(0);
//            return tableAndColumn.table.getValue(date,tableAndColumn.internalColumnNumber, getMethod);
//        } else {
//            // multiple consecutive things.
//            for (TableAndColumn tableAndColumn : tableAndColumns) {
//                if (!tableAndColumn.table.getFirstDateOf(tableAndColumn.internalColumnNumber).isAfter(date) &&
//                        !tableAndColumn.table.getLastDateOf(tableAndColumn.internalColumnNumber).isBefore(date)) {
//                    return tableAndColumn.table.getValue(date, tableAndColumn.internalColumnNumber, getMethod);
//                }
//            }
//        }
//        throw new IllegalStateException("Can't find the value");
//    }
//
//    @Override
//    public double getValue(LocalDate date, int column, TableValueGetter getter) {
//        DefaultTableColumnView view = new DefaultTableColumnView(this,column);
//        return getter.getValue(date, view);
//    }
//
//    @Override
//    public double getExactValue(LocalDate date, int column) {
//        return 0;
//    }
//
//    @Override
//    public int getColumnCount() {
//        return columnNumberIndex.size();
//    }
//
//    @Override
//    public List<LocalDate> getAllDates(int column) {
//        List<TableAndColumn> x = columnNumberIndex.get(column);
//        Set<LocalDate> dates = new HashSet<>();
//        for (TableAndColumn tableAndColumn : x) {
//            dates.addAll(tableAndColumn.table.getAllDates(tableAndColumn.internalColumnNumber));
//        }
//        List<LocalDate> y = new ArrayList<>(dates);
//        Collections.sort(y);
//        return y;
//    }
//
//    @Override
//    public boolean hasAllValuesForAllDates() {
//        return false;
//    }
//
////    @Override
////    public LocalDate[] getAllDates() {
////        if (this.cachedDates == null) {
////            establishCachedDates();
////        }
////        LocalDate[] copy = new LocalDate[this.cachedDates.length];
////        System.arraycopy(this.cachedDates,0,copy,0,this.cachedDates.length);
////        return copy;
////    }
//
//    private void establishCachedDates() {
//        if (this.cachedDates != null) {
//            // its been done, let's move on.
//            return;
//        }
//        Set<LocalDate> dates = new HashSet<>();
//        boolean first = true;
//        for (Table table : tables) {
//            if (first || datePolicy == DatePolicy.ANYMUSTHAVE) {
//                dates.addAll(table.getAllDates(datePolicy));
//                first = false;
//            } else {
//                dates.retainAll(table.getAllDates(datePolicy));
//            }
//        }
//        List<LocalDate> ds = new ArrayList<>(dates);
//        Collections.sort(ds);
//        this.cachedDates = new LocalDate[ds.size()];
//        ds.toArray(this.cachedDates);
//    }
//
//    @Override
//    public double[] getExactValues(LocalDate date) {
//        return new double[0];
//    }
//
////    @Override
////    public LocalDate getLastDate() {
////        establishCachedDates();
////        return cachedDates[cachedDates.length-1];
////    }
//
//    public LocalDate getLastDateOf(String column) {
//        List<TableAndColumn> x = columnNameIndex.get(column);
//        LocalDate last = null;
//        for (TableAndColumn tableAndColumn : x) {
//            if (last == null || last.isBefore(tableAndColumn.table.getLastDateOf(column))) {
//                last = tableAndColumn.table.getLastDateOf(column);
//            }
//        }
//        return last;
//    }
//
//    public LocalDate getFirstDateOf(String column) {
//        List<TableAndColumn> x = columnNameIndex.get(column);
//        LocalDate first = null;
//        for (TableAndColumn tableAndColumn : x) {
//            if (first == null || first.isAfter(tableAndColumn.table.getFirstDateOf(column))) {
//                first = tableAndColumn.table.getFirstDateOf(column);
//            }
//        }
//        return first;
//    }
//
////    @Override
////    public LocalDate getFirstDate() {
////        establishCachedDates();
////        return cachedDates[0];
////    }
//
//    @Override
//    public Frequency getFrequency() {
//        throw new UnsupportedOperationException("Why do you really want to know the frequency?");
//    }
//
//    @Override
//    public Table untilExclusive(LocalDate date) {
//        return new UntilTable(this,date);
//    }
//
//    @Override
//    public Table onOrAfter(LocalDate date) {
//        return new AfterTable(this, date);
//    }
//
//    @Override
//    public Table inTimeFrame(LocalDate startInclusive, LocalDate endExclusive) {
//        // it's hacky I know.
//        return new UntilTable(new AfterTable(this, startInclusive),endExclusive);
//    }
//
////    @Override
////    public LocalDate getDateBefore(LocalDate date) {
////        establishCachedDates();
////        LocalDate x = null;
////        for (LocalDate d : this.cachedDates) {
////            if (d.isBefore(date)) {
////                // assume it's sorted chronologically
////                x = d;
////            } else {
////                break;
////            }
////        }
////        return x;
////    }
////
////    @Override
////    public LocalDate getDateOnOrBefore(LocalDate date) {
////        establishCachedDates();
////        LocalDate x = null;
////        for (LocalDate d : this.cachedDates) {
////            if (!d.isAfter(date)) {
////                // assume it's sorted chronologically
////                x = d;
////            } else {
////                break;
////            }
////        }
////        return x;
////    }
////
////    @Override
////    public LocalDate getDateAfter(LocalDate date) {
////        establishCachedDates();
////        // assume it's sorted chronologically
////        for (LocalDate d : this.cachedDates) {
////            if (d.isAfter(date)) {
////                return d;
////            }
////        }
////        return null;
////    }
////
////    @Override
////    public LocalDate getDateOnOrAfter(LocalDate date) {
////        establishCachedDates();
////        // assume it's sorted chronologically
////        for (LocalDate d : this.cachedDates) {
////            if (d.equals(date) || d.isAfter(date)) {
////                return d;
////            }
////        }
////        return null;
////    }
//
//    @Override
//    public boolean isExact() {
//        return true;
//    }
//
//    private static class TableAndColumn {
//        private Table table;
//        private String columnName;
//        private int internalColumnNumber;
//        private int externalColumnNumber;
//    }
//}
