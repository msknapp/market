package knapp.table.util;

import knapp.history.Frequency;
import knapp.table.Table;
import knapp.table.TableImpl;
import knapp.table.UnevenTable;
import knapp.table.values.TableColumnView;
import knapp.util.Util;

import java.time.LocalDate;
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
        // this delivers a table with exact values.
        return tableBuilder.exact(true).build();
    }

    public static Table solidifyTable(Table table) {
        UnevenTable.UnevenTableBuilder builder = UnevenTable.defineTable();
        for (int i = 0 ;i<table.getColumnCount();i++) {
            String name = table.getColumn(i);
            for (LocalDate date : table.getAllDates(i)) {
                builder.add(name,date,table.getExactValue(date,i));
            }
        }
        Table out = builder.build();
        out.setName(table.getName());
        return out;
    }

    public static Table retainColumns(Table table, Set<String> retainColumns) {
        UnevenTable.UnevenTableBuilder builder = UnevenTable.defineTable();
        for (int i = 0 ;i<table.getColumnCount();i++) {
            String name = table.getColumn(i);
            if (retainColumns.contains(name)) {
                for (LocalDate date : table.getAllDates(i)) {
                    builder.add(name, date, table.getExactValue(date, i));
                }
            }
        }
        Table out = builder.exact(table.isExact()).build();
        out.setName(table.getName());
        return out;
    }

    public static Table produceConstantTable(final double value, LocalDate start, LocalDate end, Frequency frequency) {
        TableImpl.TableBuilder tableBuilder = TableImpl.newBuilder();
        tableBuilder.frequency(frequency);
        tableBuilder.column("Adj Price");
        Util.doWithDate(start,end,frequency,date -> {
            tableBuilder.addRow(date,new double[]{value});
        });
        return tableBuilder.exact(true).build();
    }

    public static Table mergeTableRowsExacly(List<Table> values) {
        return UnevenTable.from(values);
    }
}
