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

    public static Table withoutColumn(Table table, String column) {
        return new TableWithoutColumn(table,column);
    }

    public static DoubleRange getRange(Table table, String column) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (LocalDate localDate : table.getAllDates()) {
            double value = table.getValue(localDate,column,TableImpl.GetMethod.EXACT);
            if (max < value) {
                max = value;
            }
            if (min > value) {
                min = value;
            }
        }
        return new DoubleRange(min,max);
    }

    public static double getStandardChange(Table table, String column) {
        double totalChange = 0;
        int periods = 0;
        double lastValue = 0;
        boolean first = true;
        for (LocalDate localDate : table.getAllDates()) {
            double value = table.getValue(localDate,column,TableImpl.GetMethod.EXACT);
            if (!first) {
                totalChange += Math.abs(value - lastValue);
                periods+=1;
            }
            lastValue = value;
            first = false;
        }
        return totalChange / ((double)periods);
    }

    public static double getStandardPercentChange(Table table, String column) {
        double totalChange = 0;
        int periods = 0;
        double lastValue = 0;
        boolean first = true;
        for (LocalDate localDate : table.getAllDates()) {
            double value = table.getValue(localDate,column,TableImpl.GetMethod.EXACT);
            if (!first) {
                totalChange += Math.abs(1 - value / lastValue);
                periods+=1;
            }
            lastValue = value;
            first = false;
        }
        return totalChange / ((double)periods);
    }

    public static double getLastPercentChange(Table table, String column) {
        LocalDate last = table.getLastDate();
        LocalDate secondLast = table.getDateBefore(last);
        double olderValue = table.getValue(secondLast,column,TableImpl.GetMethod.EXACT);
        double recentValue = table.getValue(last,column,TableImpl.GetMethod.EXACT);
        return 1.0 - (recentValue / olderValue );
    }
}
