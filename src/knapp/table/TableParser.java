package knapp.table;

import java.time.LocalDate;

public class TableParser {

    public static TableImpl parse(String csv, boolean header) {
        TableImpl.TableBuilder tableBuilder = TableImpl.newBuilder();
        boolean first = true;
        for (String line : csv.split("\n")) {
            String[] parts = line.split("\n");
            if (header && first) {
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
                    if (sValue.matches("[\\d\\.]+")) {
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

    public static TableImpl solidifyTable(Table table) {
        TableImpl.TableBuilder tableBuilder = TableImpl.newBuilder();
        for (int i = 0 ;i<table.getColumnCount();i++) {
            tableBuilder.column(table.getColumn(i));
        }
        for (LocalDate localDate : table.getAllDates()) {
            tableBuilder.addRow(localDate,table.getExactValues(localDate));
        }
        return tableBuilder.build();
    }
}
