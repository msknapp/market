package knapp;

import knapp.download.DownloadRequest;

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

    public double[][] toDoubleRows(int[] xColumns, LocalDate start, LocalDate end, DownloadRequest.Frequency frequency) {
        List<double[]> values = new ArrayList<>();
        LocalDate cd = start;
        while (!cd.isAfter(end)) {
            double[] t = new double[xColumns.length];
            int curCol = 0;
            for (int i : xColumns) {
                String v = getValueOn(cd,i);
                if (v.isEmpty()) {
                    t[curCol] = 0.0;
                } else if (!v.matches("[\\d\\.]+")) {
                    t[curCol] = 0.0;
                } else if (".".equals(v)) {
                    t[curCol] = 0.0;
                } else {
                    double d = Double.parseDouble(v);
                    t[curCol] = d;
                }
                curCol++;
            }
            values.add(t);
            if (frequency == DownloadRequest.Frequency.Annual) {
                cd = cd.plusYears(1);
            } else if (frequency == DownloadRequest.Frequency.Quarterly) {
                cd = cd.plusMonths(3);
            } else if (frequency == DownloadRequest.Frequency.Monthly) {
                cd = cd.plusMonths(1);
            } else if (frequency == DownloadRequest.Frequency.Daily) {
                cd = cd.plusDays(1);
            }

        }
        double[][] x = new double[values.size()][];
        for (int i = 0;i<x.length;i++) {
            x[i] = values.get(i);
        }

        return x;
    }

    public double[][] toDoubleColumns(int[] xColumns, LocalDate start, LocalDate end, DownloadRequest.Frequency frequency) {
        Map<Integer,List<Double>> values = new HashMap<>(xColumns.length);
        for (int i : xColumns) {
            values.put(i,new ArrayList<>());
        }
        LocalDate cd = start;
        while (!cd.isAfter(end)) {
            for (int i : xColumns) {
                List<Double> ds = values.get(i);
                String v = getValueOn(cd,i);
                if (v.isEmpty()) {
                    ds.add(0.0);
                } else if (!v.matches("[\\d\\.]+")) {
                    ds.add(0.0);
                } else if (".".equals(v)) {
                    ds.add(0.0);
                } else {
                    double d = Double.parseDouble(v);
                    ds.add(d);
                }
            }
            if (frequency == DownloadRequest.Frequency.Annual) {
                cd = cd.plusYears(1);
            } else if (frequency == DownloadRequest.Frequency.Quarterly) {
                cd = cd.plusMonths(3);
            } else if (frequency == DownloadRequest.Frequency.Monthly) {
                cd = cd.plusMonths(1);
            } else if (frequency == DownloadRequest.Frequency.Daily) {
                cd = cd.plusDays(1);
            }

        }
        double[][] x = new double[xColumns.length][];
        int colNum = 0;
        for (int i : xColumns) {
            double[] colVals = new double[values.get(i).size()];
            List<Double> l = values.get(i);
            int row = 0;
            for (Double d : l) {
                colVals[row++] = d;
            }
            x[colNum] = colVals;
            colNum++;
        }

        return x;
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
