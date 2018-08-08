package knapp.table;

import java.time.LocalDate;

final class TableRow implements Comparable<TableRow> {
    private final LocalDate date;
    private final double[] values;

    public TableRow(LocalDate date, double[] values) {
        if (date == null) {
            throw new IllegalArgumentException("The date is null");
        }
        if (values == null) {
            throw new IllegalArgumentException("The values are null.");
        }
        if (values.length < 1) {
            throw new IllegalArgumentException("The values are empty.");
        }
        this.date = date;
        this.values = new double[values.length];
        System.arraycopy(values,0,this.values,0,values.length);
    }

    LocalDate getDate() {
        return date;
    }

    double getValue(int col) {
        return values[col];
    }

    double[] getValues() {
        double[] copy = new double[values.length];
        System.arraycopy(values,0,copy,0,values.length);
        return copy;
    }

    void populate(double[] x) {
        if (x.length != values.length) {
            throw new IllegalArgumentException("Wrong length");
        }
        for (int i = 0; i < x.length;i ++) {
            x[i] = values[i];
        }
    }

    @Override
    public int compareTo(TableRow o) {
        return date.compareTo(o.date);
    }

    public int getColumnCount() {
        return values.length;
    }
}
