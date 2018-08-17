//package knapp.table;
//
//import knapp.history.Frequency;
//import knapp.table.values.GetMethod;
//import knapp.table.values.TableColumnView;
//import knapp.table.values.TableValueGetter;
//import knapp.table.wraps.UntilTable;
//
//import java.time.LocalDate;
//
//public class TableOfTables implements Table {
//
//    private String name;
//    private final Table[] tables;
//
//    public TableOfTables(Table[] tables) {
//        for (Table table : tables) {
//            if (table == null) {
//                throw new IllegalArgumentException("Can't use a null table");
//            }
//            if (table.getName() == null || table.getName().isEmpty()) {
//                throw new IllegalArgumentException("Can't use a table with a null/empty name.");
//            }
//            if (table.getColumnCount() > 1) {
//                throw new IllegalArgumentException("Can't use a table with more than one column.");
//            }
//        }
//        this.tables = new Table[tables.length];
//        System.arraycopy(tables,0,this.tables,0,tables.length);
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
//        return tables[i].getName();
//    }
//
//    @Override
//    public int getColumn(String name) {
//        for (int i = 0; i < tables.length;i++) {
//            if (name.equals(tables[i].getName())) {
//                return i;
//            }
//        }
//        return -1;
//    }
//
//    @Override
//    public double getValue(LocalDate date, int column, GetMethod getMethod) {
//        return tables[column].getValue(date,0,getMethod);
//    }
//
//    @Override
//    public double getValue(LocalDate date, int column, TableValueGetter getter) {
//        DefaultTableColumnView view = new DefaultTableColumnView(this, column);
//        return getter.getValue(date, view);
//    }
//
//    @Override
//    public TableColumnView getTableColumnView(int column) {
//        return new DefaultTableColumnView(this, column);
//    }
//
//    @Override
//    public double getExactValue(LocalDate date, int column) {
//        return tables[column].getExactValue(date,0);
//    }
//
//    @Override
//    public int getColumnCount() {
//        return tables.length;
//    }
//
////    @Override
////    public LocalDate[] getAllDates() {
////        return tables[0].getAllDates();
////    }
//
//    @Override
//    public double[] getExactValues(LocalDate date) {
//        return new double[0];
//    }
//
////    @Override
////    public LocalDate getLastDate() {
////        return tables[0].getLastDate();
////    }
////
////    @Override
////    public LocalDate getFirstDate() {
////        return tables[0].getFirstDate();
////    }
//
//    @Override
//    public Frequency getFrequency() {
//        return tables[0].getFrequency();
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
//        return new AfterTable(new UntilTable(this,endExclusive), startInclusive);
//    }
//
////    @Override
////    public LocalDate getDateBefore(LocalDate date) {
////        return tables[0].getDateBefore(date);
////    }
////
////    @Override
////    public LocalDate getDateOnOrBefore(LocalDate date) {
////        return tables[0].getDateOnOrBefore(date);
////    }
////
////    @Override
////    public LocalDate getDateAfter(LocalDate date) {
////        return tables[0].getDateAfter(date);
////    }
////
////    @Override
////    public LocalDate getDateOnOrAfter(LocalDate date) {
////        return tables[0].getDateOnOrAfter(date);
////    }
//
//    @Override
//    public boolean isExact() {
//        return false;
//    }
//}
