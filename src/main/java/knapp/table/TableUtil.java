package knapp.table;

import knapp.history.Frequency;
import knapp.util.Util;

import java.time.LocalDate;
import java.util.Set;

public class TableUtil {

    public static double[][] toDoubleColumns(Table table, int[] xColumns, LocalDate start, LocalDate end,
                                      Frequency frequency, final TableImpl.GetMethod getMethod) {
        return Util.toDoubleColumns(xColumns,start,end,frequency,(date, col) -> {
            double v = table.getValue(date,col, getMethod);
            return String.valueOf(v);
        });
    }

    public static double[][] toDoubleRows(Table table, int[] xColumns, LocalDate start, LocalDate end,
                                   Frequency frequency, final TableImpl.GetMethod getMethod) {
        return Util.toDoubleRows(xColumns,start,end,frequency,(date, col) -> {
            double v =  table.getValue(date,col, getMethod);
            return String.valueOf(v);
        });
    }

    public static double[] getExactValues(Table table, LocalDate date, Set<String> columns) {
        double[] y = new double[columns.size()];
        int i = 0;
        for (String col : columns) {
            int c = table.getColumn(col);
            y[i++] = table.getValue(date,c,TableImpl.GetMethod.EXACT);
        }
        return y;
    }
}
