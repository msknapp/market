package knapp.history;

import knapp.util.Util;

import java.time.LocalDate;
import java.util.*;

public class ValueHistory {

    private List<String> columns;
    private SortedMap<LocalDate,String[]> history;

    public List<String> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public static ValueHistory parseText(String text) {
        TreeMap<LocalDate,String[]> h = new TreeMap<LocalDate, String[]>();
        boolean first = true;
        ValueHistory vh = new ValueHistory();
        for (String line : text.split("\n")) {
            if (first) {
                vh.columns = Arrays.asList(line.split(",", -1));
                first = false;
            } else {
                String[] parts = line.split(",", -1);
                LocalDate d = LocalDate.parse(parts[0]);
                h.put(d,parts);
            }
        }
        vh.history = h;
        return vh;
    }

    public SortedMap<LocalDate,String[]> getHistory() {
        return history;
    }

    public String getColumn(int i) {
        return columns.get(i);
    }

    public String getValueOn(LocalDate d, String column) {
        int i = columns.indexOf(column);
        return getValueOn(d,i);
    }

    public String getValueOn(LocalDate d, int column) {
        LocalDate max = d.plusYears(1);
        for (LocalDate cd : history.keySet()) {
            if (cd.equals(max) || cd.isAfter(max)) {
                return "";
            }
            if (d.equals(cd) || cd.isAfter(d)) {
                return history.get(cd)[column];
            }
        }
        return "";
    }

    public double[][] toDoubleRows(int[] xColumns, LocalDate start, LocalDate end, Frequency frequency) {
        return Util.toDoubleRows(xColumns,start,end,frequency,(date,col) -> {
            return getValueOn(date,col);
        });
    }

    public double[][] toDoubleColumns(int[] xColumns, LocalDate start, LocalDate end, Frequency frequency) {
        return Util.toDoubleColumns(xColumns,start,end,frequency,(date,col) -> {
            return getValueOn(date,col);
        });
    }

    public static class RegressionInput {
        private double[] y;
        private double[][] x;

        public RegressionInput(double[][] x,double[] y) {
            this.y = y;
            this.x = x;
        }

        public double[] getY() {
            return y;
        }

        public double[][] getX() {
            return x;
        }
    }
}
