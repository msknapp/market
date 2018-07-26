package knapp.table;

import knapp.history.Frequency;
import knapp.util.Util;

import java.time.LocalDate;
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
}
