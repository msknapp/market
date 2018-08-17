package knapp.table.util;

import knapp.history.Frequency;
import knapp.table.DoubleRange;
import knapp.table.values.GetMethod;
import knapp.table.Table;
import knapp.table.values.LagBasedExtrapolatedValuesGetter;
import knapp.table.values.TableValueGetter;
import knapp.table.wraps.TableWithoutColumn;
import knapp.util.Util;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public class TableUtil {

    public static double[][] toDoubleColumns(Table table, int[] xColumns, LocalDate start, LocalDate end,
                                             Frequency frequency,
                                             TableValueGetter tableValueGetter) {
        return Util.toDoubleColumns(xColumns,start,end,frequency,(date, col) -> {
            double v = table.getValue(date,col, tableValueGetter);
            return String.valueOf(v);
        });
    }

    public static double[][] toDoubleRows(Table table, int[] xColumns, LocalDate start, LocalDate end,
                                          Frequency frequency,
                                          Map<String,Integer> lags) {
        return Util.toDoubleRows(xColumns,start,end,frequency,(date, col) -> {
            LagBasedExtrapolatedValuesGetter tableValueGetter = new LagBasedExtrapolatedValuesGetter(lags.get(table.getColumn(col)));
            double v =  table.getValue(date,col, tableValueGetter);
            return String.valueOf(v);
        });
    }

    public static double[] getExactValues(Table table, LocalDate date, Set<String> columns) {
        double[] y = new double[columns.size()];
        int i = 0;
        for (String col : columns) {
            int c = table.getColumn(col);
            y[i++] = table.hasAllValuesForAllDates() ?
                    table.getValue(date,c,GetMethod.EXACT) :
                    table.getValue(date,c,GetMethod.LAST_KNOWN_VALUE);
        }
        return y;
    }

    public static Table withoutColumn(Table table, String column) {
        return new TableWithoutColumn(table,column);
    }

    public static DoubleRange getRange(Table table, String column) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (LocalDate localDate : table.getAllDates(column)) {
            double value = table.hasAllValuesForAllDates() ?
                    table.getValue(localDate,column,GetMethod.EXACT) :
                    table.getValue(localDate,column,GetMethod.LAST_KNOWN_VALUE);
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
        for (LocalDate localDate : table.getAllDates(column)) {
            double value = table.hasAllValuesForAllDates() ?
                    table.getValue(localDate,column,GetMethod.EXACT) :
                    table.getValue(localDate,column,GetMethod.LAST_KNOWN_VALUE);
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
        for (LocalDate localDate : table.getAllDates(column)) {
            double value = table.hasAllValuesForAllDates() ?
                    table.getValue(localDate,column,GetMethod.EXACT) :
                    table.getValue(localDate,column,GetMethod.LAST_KNOWN_VALUE);
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
        LocalDate last = table.getLastDateOf(column);
        LocalDate secondLast = table.getTableColumnView(column).getDateBefore(last);
        double olderValue = table.hasAllValuesForAllDates() ?
                table.getValue(secondLast,column,GetMethod.EXACT) :
                table.getValue(secondLast,column,GetMethod.LAST_KNOWN_VALUE);
        double recentValue = table.hasAllValuesForAllDates() ?
                table.getValue(last,column,GetMethod.EXACT) :
                table.getValue(last,column,GetMethod.LAST_KNOWN_VALUE);
        return 1.0 - (recentValue / olderValue );
    }

    public static LocalDate getEarliestStartDate(Table core) {
        LocalDate earliest = null;
        for (int i = 0;i < core.getColumnCount(); i++) {
            LocalDate x = core.getTableColumnView(i).getFirstDate();
            if (earliest == null || earliest.isAfter(x)) {
                earliest = x;
            }
        }
        return earliest;
    }

    public static LocalDate getLatestStartDate(Table core) {
        LocalDate latest = null;
        for (int i = 0;i < core.getColumnCount(); i++) {
            LocalDate x = core.getTableColumnView(i).getFirstDate();
            if (latest == null || latest.isBefore(x)) {
                latest = x;
            }
        }
        return latest;
    }
}
