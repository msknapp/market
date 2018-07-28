package knapp.table;

import knapp.history.Frequency;
import knapp.util.Util;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class TableParser {

    public static TableImpl parse(String csv, boolean header, Frequency frequency) {
        TableImpl.TableBuilder tableBuilder = TableImpl.newBuilder();
        tableBuilder.frequency(frequency);
        boolean first = true;
        for (String line : csv.split("\n")) {
            String[] parts = line.split(",",-1);
            if (header && first) {
                first = false;
                for (int i = 1;i<parts.length;i++) {
                    tableBuilder.column(parts[i]);
                }
                continue;
            }
            LocalDate date = LocalDate.parse(parts[0]);
            double[] values = new double[parts.length-1];
            for (int i = 1;i< parts.length;i++) {
                String sValue = parts[i];
                double value = 0;
                if (sValue != null && !sValue.isEmpty() && !".".equals(sValue)) {
                    if (sValue.matches("[E\\d\\.]+")) {
                        value = Double.parseDouble(sValue);
                    }
                }
                values[i-1] = value;
            }
            tableBuilder.addRow(date,values);
            first = false;
        }
        return tableBuilder.build();
    }

    public static Table solidifyTable(Table table) {
        TableImpl.TableBuilder tableBuilder = TableImpl.newBuilder().frequency(table.getFrequency());
        for (int i = 0 ;i<table.getColumnCount();i++) {
            tableBuilder.column(table.getColumn(i));
        }
        for (LocalDate localDate : table.getAllDates()) {
            tableBuilder.addRow(localDate,table.getExactValues(localDate));
        }
        return tableBuilder.build();
    }

    public static Table retainColumns(Table table, Set<String> retainColumns) {
        TableImpl.TableBuilder tableBuilder = TableImpl.newBuilder().frequency(table.getFrequency());
        for (String column : retainColumns) {
            tableBuilder.column(column);
        }
        for (LocalDate localDate : table.getAllDates()) {
            double[] values = TableUtil.getExactValues(table,localDate,retainColumns);
            tableBuilder.addRow(localDate,values);
        }
        return tableBuilder.build();
    }

    public static Table produceConstantTable(final double value, LocalDate start, LocalDate end, Frequency frequency) {
        TableImpl.TableBuilder tableBuilder = TableImpl.newBuilder();
        tableBuilder.frequency(frequency);
        tableBuilder.column("Adj Price");
        Util.doWithDate(start,end,frequency,date -> {
            tableBuilder.addRow(date,new double[]{value});
        });
        return tableBuilder.build();
    }

    public static Table mergeTables(LocalDate start, LocalDate end, List<Table> values, Frequency frequency) {
        TableImpl.TableBuilder tableBuilder = TableImpl.newBuilder();
        tableBuilder.frequency(frequency);
        for (Table t : values) {
            tableBuilder.column(t.getName());
        }

        Util.doWithDate(start,end,frequency,date -> {
            double[] d = new double[values.size()];
            int i = 0;
            for (Table t : values) {
                double value = t.getValue(date,0,TableImpl.GetMethod.LAST_KNOWN_VALUE);
                d[i++] = value;
            }
            tableBuilder.addRow(date,d);
        });
        return tableBuilder.build();
    }

    public static Table mergeTableRows(LocalDate start, LocalDate end, List<Table> values, Frequency frequency) {
        Table t1 = values.get(0);
        for (Table t : values) {
            if (t.getColumnCount() != t1.getColumnCount()) {
                throw new IllegalArgumentException("inconsistent columns");
            }
        }
        TableImpl.TableBuilder tableBuilder = TableImpl.newBuilder();
        tableBuilder.frequency(frequency);
        for (int i = 0;i < t1.getColumnCount(); i++) {
            tableBuilder.column(t1.getColumn(i));
        }

        Util.doWithDate(start,end,frequency,date -> {
            for (int i = 0;i < t1.getColumnCount();i++) {
                for (Table t : values) {
                    if (!t.getFirstDate().isAfter(date) && !t.getLastDate().isBefore(date)) {
                        double value = t.getValue(date,0,TableImpl.GetMethod.LAST_KNOWN_VALUE);
                        tableBuilder.addRow(date,new double[]{value});
                        break;
                    }
                }
            }
        });
        return tableBuilder.build();
    }
}
